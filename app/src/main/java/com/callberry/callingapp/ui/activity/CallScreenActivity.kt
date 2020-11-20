package com.callberry.callingapp.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.service.CallStateChangeListener
import com.callberry.callingapp.service.LocalCallState
import com.callberry.callingapp.service.OutgoingCallService
import com.callberry.callingapp.util.*
import com.sinch.android.rtc.calling.Call
import kotlinx.android.synthetic.main.activity_call_screen.*
import kotlinx.android.synthetic.main.activity_call_screen.btnEndCall
import kotlinx.android.synthetic.main.activity_call_screen.imgViewMute
import kotlinx.android.synthetic.main.activity_call_screen.imgViewSpeaker
import kotlinx.android.synthetic.main.activity_call_screen.layoutCalling
import kotlinx.android.synthetic.main.activity_call_screen.materialBtnClose
import kotlinx.android.synthetic.main.activity_call_screen.textViewIcon
import kotlinx.android.synthetic.main.activity_call_screen.textViewName
import kotlinx.android.synthetic.main.activity_call_screen.textViewPhoneNo
import kotlinx.android.synthetic.main.activity_call_screen.txtViewMessage
import java.util.*


class CallScreenActivity : AppCompatActivity(), CallStateChangeListener {

    private lateinit var outgoingCall: OutgoingCall
    private var outgoingCallService: OutgoingCallService? = null
    private var call: Call? = null
    private val callTimer = Timer()
    private val callDurationTask = UpdateCallDurationTask()
    private var durationInSec: Int = 0
    private var sensorEventListener: SensorEventListener? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var sensorManager: SensorManager? = null
    private var muteAudioManager: AudioManager? = null
    private var speakerAudioManager: AudioManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_screen)

        outgoingCall = SharedPrefUtil.get(Constants.OUTGOING_CALL, OutgoingCall::class) as OutgoingCall

        initViews()
        initWakeSensor()
        bindService()

    }

    override fun onResume() {
        super.onResume()
        if (SharedPrefUtil.getBoolean(Constants.IS_CALL_IN_PROGRESS, false)) {
            //updateCallTimer()
        }

        updateSpeakerMuteAction()

    }

    private fun onServiceConnectionSuccessful() {
        outgoingCallService!!.initializeClient()
        outgoingCallService!!.setCallStateChangeListener(this)
    }

    override fun onStateChange(state: LocalCallState) {
        if (state == LocalCallState.CLIENT_FAILED) {
            txtViewCallDuration.text = getString(R.string.call_failed)
            txtViewCallDuration.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
            txtViewMessage.text = getString(R.string.call_failed_message)
            exchangeView(layoutDetail, layoutCalling)
            return
        }

        if (state == LocalCallState.CALL_ESTABLISHED) {
            toast("Call Started")
            outgoingCallService?.apply {
                getCall()?.let {
                    call = it
                }
            }
            updateCallTimer()
            return
        }

        if (state == LocalCallState.CALL_ENDED) {
            toast("Call Ended")
            callFinished()
            return
        }


    }

    private fun callFinished() {
        call?.let {
            callDurationTask.cancel()
            callTimer.cancel()
            var totalTime = txtViewCallDuration.text.toString()
            if (totalTime == getString(R.string.connecting)) {
                totalTime = "00:00"
            }
            txtViewCallDuration.text = "${getString(R.string.call_ended)}, $totalTime"
            val creditUsed = UIUtil.calculateCredits(call!!.details.duration, outgoingCall.callRate)
            txtViewMessage.text = "${getString(R.string.total_credit_used)} $$creditUsed"
            exchangeView(layoutDetail, layoutCalling)
            return@callFinished
        }

        txtViewCallDuration.text = "${getString(R.string.call_ended)}, 00:00"
        val creditUsed = UIUtil.calculateCredits(0, outgoingCall.callRate)
        txtViewMessage.text = "${getString(R.string.total_credit_used)} $$creditUsed"
        exchangeView(layoutDetail, layoutCalling)

    }

    private fun updateCallTimer() {
        if (outgoingCallService != null && outgoingCallService!!.isCallStarted()) {
            callTimer.schedule(callDurationTask, 0, 500)
            return
        }

        txtViewCallDuration.text = getString(R.string.connecting)

    }

    private inner class UpdateCallDurationTask : TimerTask() {
        override fun run() {
            this@CallScreenActivity.runOnUiThread { updateCallDuration() }
        }

        private fun updateCallDuration() {
            call?.let {
                if (SharedPrefUtil.getBoolean(Constants.IS_CALL_IN_PROGRESS, false)) {
                    durationInSec = it.details.duration
                    txtViewCallDuration.text = UIUtil.formatTimestamp(durationInSec)
                }
            }
        }
    }

    private fun initViews() {
        textViewName.text = outgoingCall.name
        textViewPhoneNo.text = outgoingCall.phoneNo
        UIUtil.setIcon(this, textViewIcon, outgoingCall.name, outgoingCall.theme)
        btnEndCall.setOnClickListener { terminateCall() }
        materialBtnClose.setOnClickListener { finish() }
    }

    private fun updateSpeakerMuteAction() {
        setActionView(imgViewSpeaker, SharedPrefUtil.getBoolean(Constants.IS_SPREAKER_ON, false))
        setActionView(imgViewMute, SharedPrefUtil.getBoolean(Constants.IS_MUTE_ON, false))

        speakerAudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        speakerAudioManager!!.mode = AudioManager.MODE_IN_CALL

        muteAudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        muteAudioManager!!.mode = AudioManager.MODE_IN_CALL

        imgViewSpeaker.setOnClickListener {
            if (!speakerAudioManager!!.isSpeakerphoneOn) {
                speakerAudioManager!!.isSpeakerphoneOn = true
                SharedPrefUtil.setBoolean(Constants.IS_SPREAKER_ON, true)
                setActionView(imgViewSpeaker, true)
                return@setOnClickListener
            }

            speakerAudioManager!!.mode = AudioManager.MODE_IN_CALL
            speakerAudioManager!!.isSpeakerphoneOn = false
            SharedPrefUtil.setBoolean(Constants.IS_SPREAKER_ON, false)
            setActionView(imgViewSpeaker, false)

        }

        imgViewMute.setOnClickListener {
            if (!muteAudioManager!!.isMicrophoneMute) {
                muteAudioManager!!.isMicrophoneMute = true
                SharedPrefUtil.setBoolean(Constants.IS_MUTE_ON, true)
                setActionView(imgViewMute, true)
                return@setOnClickListener
            }

            muteAudioManager!!.isMicrophoneMute = false
            SharedPrefUtil.setBoolean(Constants.IS_MUTE_ON, false)
            setActionView(imgViewMute, false)
        }
    }

    private fun initWakeSensor() {
        val powerManager =
            getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            localClassName
        )

        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!wakeLock!!.isHeld()) {
                    wakeLock!!.acquire()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        sensorManager!!.registerListener(
            sensorEventListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }


    private fun bindService() {
        val serviceIntent = Intent(this, OutgoingCallService::class.java)
        serviceIntent.action = Constants.STARTFOREGROUND_ACTION
        ContextCompat.startForegroundService(this, serviceIntent)

        Intent(this, OutgoingCallService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            val binder = iBinder as OutgoingCallService.CallBinder
            outgoingCallService = binder.service
            onServiceConnectionSuccessful()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            onServiceConnectionFailed()
        }
    }

    private fun onServiceConnectionFailed() {
        toast("Service Failed")
        terminateCall()
    }

    private fun terminateCall() {
        call?.apply {
            hangup()
            return@terminateCall
        }

        val stopIntent = Intent(this@CallScreenActivity, OutgoingCallService::class.java)
        stopIntent.action = Constants.STOPFOREGROUND_ACTION
        startService(stopIntent)
        callFinished()
    }

    override fun onBackPressed() {
        if (!SharedPrefUtil.getBoolean(Constants.IS_CALL_IN_PROGRESS, false))
            super.onBackPressed()
    }

    override fun onDestroy() {
        wakeLock?.let {
            if (it.isHeld) {
                wakeLock!!.release()
            }
        }
        sensorManager!!.unregisterListener(sensorEventListener)
        SharedPrefUtil.setBoolean(Constants.IS_SPREAKER_ON, false)
        SharedPrefUtil.setBoolean(Constants.IS_MUTE_ON, false)
        super.onDestroy()

    }

    private fun setActionView(imgView: ImageView, active: Boolean) {
        if (active) {
            imgView.background = ContextCompat.getDrawable(this, R.drawable.bg_active)
            imgView.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite))
        } else {
            imgView.background = ContextCompat.getDrawable(this, R.drawable.bg_non_active)
            imgView.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
        }
    }

}
