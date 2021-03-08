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
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.plivo.PlivoConfig
import com.callberry.callingapp.service.CallStateChangeListener
import com.callberry.callingapp.service.LocalCallState
import com.callberry.callingapp.service.OutgoingCallService
import com.callberry.callingapp.util.*
import com.plivo.endpoint.Outgoing
import kotlinx.android.synthetic.main.activity_call_screen.*
import java.util.*
import java.util.concurrent.TimeUnit


class CallScreenActivity : AppCompatActivity(), CallStateChangeListener {

    private lateinit var outgoingCall: OutgoingCall
    private var outgoingCallService: OutgoingCallService? = null
    private var callTimer = Timer()
    private var callDurationTask = UpdateCallDurationTask()
    private var durationInSec: Long = 0
    private var sensorEventListener: SensorEventListener? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var sensorManager: SensorManager? = null
    private var muteAudioManager: AudioManager? = null
    private var speakerAudioManager: AudioManager? = null
    private var tick: Long = 0
    private var isSpeakerOn = false
    private var isHoldOn = false
    private var isMuteOn = false
    private var callData: Outgoing? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_screen)

        outgoingCall = PrefUtils.get(Constants.OUTGOING_CALL, OutgoingCall::class) as OutgoingCall
        initViews()
        initWakeSensor()
        bindService()

    }

    override fun onResume() {
        super.onResume()
        if (PrefUtils.getBoolean(Constants.IS_CALL_IN_PROGRESS, false)) {
            if (outgoingCallService != null) {
                Log.d("outgoingCallService","Out going call service is not null")
                if (!outgoingCallService!!.isCallStarted()) {
                    txtViewCallDuration.text = outgoingCallService!!.getCurrentCallState()
                } else {
                    updateCallTimer()
                }
            }
            else{
                Log.d("outgoingCallService","Service is null")
            }
        }

        updateSpeakerMuteAction()

    }

    override fun onStateChange(state: LocalCallState) {
        runOnUiThread {
            if (state == LocalCallState.LOGIN_FAILED) {
                txtViewCallDuration.text = getString(R.string.call_failed)
                txtViewCallDuration.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
                txtViewMessage.text = getString(R.string.call_failed_message)
                exchangeView(layoutDetail, layoutCalling)
            } else if (state == LocalCallState.CALL_RINGING) {
                runOnUiThread { txtViewCallDuration.text = getString(R.string.text_ringing) }
            } else if (state == LocalCallState.CALL_STARTED) {
//                startTimer()
                updateCallTimer()
            } else if (state == LocalCallState.CALL_ENDED) {
                toast("Call Ended")
                callFinished()
            } else if (state == LocalCallState.CALL_REJECTED) {
                toast("Call Rejected")
                callFinished()
            } else {
                Log.d("state", state.name)
            }
        }
    }

    private fun startTimer() {
        callTimer = Timer(false)
        callTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val hours =
                    TimeUnit.SECONDS.toHours(tick.toLong()).toInt()
                val minutes = TimeUnit.SECONDS.toMinutes(
                    TimeUnit.HOURS.toSeconds(hours.toLong()).let { tick -= it; tick })
                    .toInt()
                val seconds =
                    (tick - TimeUnit.MINUTES.toSeconds(minutes.toLong())).toInt()
                val text = if (hours > 0) String.format(
                    PlivoConfig.HH_MM_SS,
                    hours,
                    minutes,
                    seconds
                ) else String.format(PlivoConfig.MM_SS, minutes, seconds)
                txtViewCallDuration.text = text
                tick++

            }
        }, 100, TimeUnit.SECONDS.toMillis(1))
    }

    private fun callFinished() {
        if (outgoingCallService?.isCallStarted() == true) {
            callDurationTask.cancel()
            callTimer.cancel()
            var totalTime = txtViewCallDuration.text.toString()
            if (totalTime == getString(R.string.connecting)) {
                totalTime = "00:00"
            }
            txtViewCallDuration.text = "${getString(R.string.call_ended)}, $totalTime"
            val creditUsed = UIUtil.calculateCredits(tick.toInt(), outgoingCall.callRate)
            txtViewMessage.text = "${getString(R.string.total_credit_used)} $$creditUsed"
            exchangeView(layoutDetail, layoutCalling)
            return@callFinished
        } else {
            txtViewCallDuration.text = "${getString(R.string.call_ended)}, 00:00"
            val creditUsed = UIUtil.calculateCredits(0, outgoingCall.callRate)
            txtViewMessage.text = "${getString(R.string.total_credit_used)} $$creditUsed"
            exchangeView(layoutDetail, layoutCalling)
        }

    }

    private fun updateCallTimer() {
        callTimer = Timer()
        callDurationTask = UpdateCallDurationTask()
        callTimer.schedule(callDurationTask, 0, 500)
    }

    private inner class UpdateCallDurationTask : TimerTask() {
        override fun run() {
            this@CallScreenActivity.runOnUiThread { updateCallDuration() }
        }

        private fun updateCallDuration() {
            if (PrefUtils.getBoolean(Constants.IS_CALL_IN_PROGRESS, false)) {
                durationInSec = outgoingCallService!!.getTimeInSeconds()
                txtViewCallDuration.text = UIUtil.formatTimestamp(durationInSec)
            }
        }
    }

    private fun initViews() {
        textViewName.text = outgoingCall.name
        textViewPhoneNo.text = outgoingCall.phoneNo
        UIUtil.setIcon(this, textViewIcon, outgoingCall.name, outgoingCall.theme)
        btnEndCall.setOnClickListener { terminateCall() }
        materialBtnClose.setOnClickListener { finish() }
        imgViewHold.isEnabled = false
        imgViewHold.setOnClickListener { setHoldCall() }
    }

    private fun setHoldCall() {
        if (isHoldOn) {
            isHoldOn = false
            unHoldCall()
            PrefUtils.setBoolean(Constants.IS_HOLD_ON, false)
            setActionView(imgViewHold, false)
        } else {
            isHoldOn = true
            holdCall()
            PrefUtils.setBoolean(Constants.IS_HOLD_ON, true)
            setActionView(imgViewHold, true)
        }
    }

    private fun holdCall() {
        if (callData != null && callData is Outgoing) {
            (callData as Outgoing).hold()
        }
    }

    private fun unHoldCall() {
        if (callData != null && callData is Outgoing) {
            (callData as Outgoing).unhold()
        }
    }

    private fun updateSpeakerMuteAction() {
        setActionView(imgViewSpeaker, PrefUtils.getBoolean(Constants.IS_SPREAKER_ON, false))
        setActionView(imgViewMute, PrefUtils.getBoolean(Constants.IS_MUTE_ON, false))

        speakerAudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        speakerAudioManager!!.mode = AudioManager.MODE_IN_CALL

        muteAudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        muteAudioManager!!.mode = AudioManager.MODE_IN_CALL

        imgViewSpeaker.setOnClickListener {
            if (!speakerAudioManager!!.isSpeakerphoneOn) {
                speakerAudioManager!!.isSpeakerphoneOn = true
                PrefUtils.setBoolean(Constants.IS_SPREAKER_ON, true)
                setActionView(imgViewSpeaker, true)
                return@setOnClickListener
            }

            speakerAudioManager!!.mode = AudioManager.MODE_IN_CALL
            speakerAudioManager!!.isSpeakerphoneOn = false
            PrefUtils.setBoolean(Constants.IS_SPREAKER_ON, false)
            setActionView(imgViewSpeaker, false)

        }

        imgViewMute.setOnClickListener {
            val btn = findViewById<View>(R.id.imgViewMute) as ImageView
            if (isMuteOn) {
                PrefUtils.setBoolean(Constants.IS_MUTE_ON, false)
                setActionView(imgViewMute, false)
                isMuteOn = false
                unMuteCall()
            } else {
                PrefUtils.setBoolean(Constants.IS_MUTE_ON, true)
                setActionView(imgViewMute, true)
                isMuteOn = true

                muteCall()
            }
        }
        /*if (!muteAudioManager!!.isMicrophoneMute) {
            muteAudioManager!!.isMicrophoneMute = true
            SharedPrefUtil.setBoolean(Constants.IS_MUTE_ON, true)
            setActionView(imgViewMute, true)
            return@setOnClickListener
        }

        muteAudioManager!!.isMicrophoneMute = false
        SharedPrefUtil.setBoolean(Constants.IS_MUTE_ON, false)
        setActionView(imgViewMute, false)
    }*/
    }

    private fun muteCall() {
        outgoingCallService?.getCallData()?.mute()
    }

    private fun unMuteCall() {
        outgoingCallService?.getCallData()?.unmute()
    }

    private fun initWakeSensor() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            localClassName
        )
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!wakeLock!!.isHeld) {
                    wakeLock?.acquire(10*60*1000L)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        sensorManager!!.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
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
            outgoingCallService?.setCallStateChangeListener(this@CallScreenActivity)
            outgoingCallService?.initCall()

        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            terminateCall()
        }
    }

    private fun terminateCall() {
        if (outgoingCallService == null) {
            Log.d("TESTTAG", "outgoingCallService=null")
            forceHungUp()
        }
        outgoingCallService!!.onCallEnded()
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        isSpeakerOn = false
        isHoldOn = false
        isMuteOn = false
        audioManager.isSpeakerphoneOn = isSpeakerOn
    }

    override fun onBackPressed() {
        if (!PrefUtils.getBoolean(Constants.IS_CALL_IN_PROGRESS, false))
            super.onBackPressed()
    }

    override fun onDestroy() {
        wakeLock?.let {
            if (it.isHeld) {
                wakeLock?.release()
            }
        }
        sensorManager?.unregisterListener(sensorEventListener)
        PrefUtils.setBoolean(Constants.IS_SPREAKER_ON, false)
        PrefUtils.setBoolean(Constants.IS_MUTE_ON, false)
        super.onDestroy()

    }

    fun forceHungUp() {
        val serviceIntent = Intent(this, OutgoingCallService::class.java)
        serviceIntent.action = Constants.STOPFOREGROUND_ACTION
        ContextCompat.startForegroundService(this, serviceIntent)
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
