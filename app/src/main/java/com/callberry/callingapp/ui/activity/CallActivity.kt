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
import android.os.PowerManager.WakeLock
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.materialdialog.MaterialAlertDialog
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.model.Recent
import com.callberry.callingapp.service.CallChangeListener
import com.callberry.callingapp.service.OutgoingCallService
import com.callberry.callingapp.util.*
import com.callberry.callingapp.viewmodel.CreditViewModel
import com.callberry.callingapp.viewmodel.RecentViewModel
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.android.synthetic.main.activity_call.textViewPhoneNo

class CallActivity : AppCompatActivity(), CallChangeListener {

    private var isInProgress = true
    private var isCallStarted = false
    private var muteAudioManager: AudioManager? = null
    private var speakerAudioManager: AudioManager? = null
    private var sensorEventListener: SensorEventListener? = null
    private var wakeLock: WakeLock? = null
    private var sensorManager: SensorManager? = null
    private var callService: OutgoingCallService? = null
    private var isServiceBound: Boolean = false
    private val tag = "ServiceConnection"
    private var isCallInProgress = false
    private lateinit var outgoingCall: OutgoingCall
    private var timeInSeconds: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

    }

    override fun onResume() {
        super.onResume()
        initViews()
        isCallInProgress = SharedPrefUtil.getBoolean(Constants.IS_CALL_IN_PROGRESS, false)
        if (!isCallInProgress)
            startCall()
        else
            resumeCall()

        bindService()

    }

    private fun startCall() {
        chronometer.stop()
        chronometer.text = getString(R.string.connecting)
        chronometer.onChronometerTickListener = getChronometerTickListener()
        timeInSeconds = UIUtil.getEstimatedDurationInSeconds(outgoingCall.callRate)

        val serviceIntent = Intent(this, OutgoingCallService::class.java)
        serviceIntent.action = Constants.STARTFOREGROUND_ACTION
        serviceIntent.putExtra(Constants.PHONE_NO, outgoingCall.phoneNo)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun resumeCall() {
        if (!outgoingCall.isCallStarted) {
            chronometer.stop()
            chronometer.text = getString(R.string.connecting)
            chronometer.onChronometerTickListener = getChronometerTickListener()
        } else {
            val diff = TimeUtil.getTimeDiff(outgoingCall.callStartedTime, TimeUtil.getTimestamp())
            val seconds = TimeUtil.getSeconds(diff)
            val timeInMilSeconds = seconds * 1000
//            chronometer.base = SystemClock.elapsedRealtime() - timeInMilSeconds
//            chronometer.start()
            timeInSeconds =
                UIUtil.getEstimatedDurationInSeconds(outgoingCall.callRate) - seconds
        }

    }

    private fun onServiceConnectionFailed() {

    }

    private fun onServiceConnected() {
        log("Service Connected")
        callService?.let {
//            it.setCallStateChangeListener(this)
            if (!isCallInProgress) {
                it.initializeClient()
            }
        }

    }

    override fun onCallEstablished() {
        log("Call Established")
        isCallStarted = true
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        outgoingCall.callStartedTime = TimeUtil.getTimestamp()
        outgoingCall.isCallStarted = true
        SharedPrefUtil.set(Constants.OUTGOING_CALL, outgoingCall)
    }

    override fun onCallHangup() {
        log("Call Ended")
        chronometer.stop()
        chronometer.text = getString(R.string.call_ended)
        if (!isCallStarted) {
            textViewCallDuration.text = "00:00"
            textViewCreditUsed.text = "$0.0"
            chronometer.text = getString(R.string.call_ended)
            exchangeView(layoutCallDetail, layoutCalling)
            saveCallLogs()
            return
        }

        val creditUsed =
            UIUtil.getCreditUsedOnCallEnded(outgoingCall.callStartedTime, outgoingCall.callRate)
        textViewCreditUsed.text = "$${creditUsed}"
        textViewCallDuration.text =
            UIUtil.getCallEndedDurationInSecond(outgoingCall.callStartedTime)
        exchangeView(layoutCallDetail, layoutCalling)
        saveCallLogs()
        saveCreditLogs()
    }

    override fun onClientFailed() {
        log("Call Failed")
        chronometer.text = getString(R.string.call_failed)
        val materialAlertDialog = MaterialAlertDialog(this)
        materialAlertDialog.setTitle(getString(R.string.call_failed))
        materialAlertDialog.setMessage(getString(R.string.call_failed_message))
        materialAlertDialog.setPositiveClick(getString(R.string.close)) { dialog ->
            dialog.dismiss()
            finish()
        }
        materialAlertDialog.show()
    }

    private fun saveCallLogs() {
        val recentViewModel = RecentViewModel.getInstance(this)
        val recent = Recent()
        recent.contactId = outgoingCall.contactId
        recent.phoneNo = outgoingCall.phoneNo
        recent.duration = textViewCallDuration.text.toString()
        recent.credit = textViewCreditUsed.text.toString().replace("$", "").toFloat()
        recent.dialCode = outgoingCall.dialCode
        recentViewModel.addRecent(recent) {
            isInProgress = false
        }
    }

    private fun saveCreditLogs() {
        val creditViewModel = CreditViewModel.getInstance(this)
        val credit = Credit()
        credit.name = "${getString(R.string.called)}: ${outgoingCall.phoneNo}"
        credit.credit = textViewCreditUsed.text.toString().replace("$", "").toFloat()
        credit.type = Constants.DEBIT
        creditViewModel.updateCredits(credit.name, credit.credit, credit.type)
    }

    private fun getChronometerTickListener(): Chronometer.OnChronometerTickListener? {
        return Chronometer.OnChronometerTickListener { chronometer ->
            val elapsedMillis =
                (SystemClock.elapsedRealtime() - chronometer!!.base).toInt()
            val seconds = elapsedMillis / 1000
            if (seconds == timeInSeconds) {
                hangupCall()
            }
        }
    }

    private fun initViews() {
        outgoingCall =
            SharedPrefUtil.get( Constants.OUTGOING_CALL, OutgoingCall::class) as OutgoingCall
        textViewName.text = outgoingCall.name
        textViewPhoneNo.text = outgoingCall.phoneNo
        UIUtil.setIcon(this, textViewIcon, outgoingCall.name, outgoingCall.theme)
        initSpeakerMuteListeners()
        btnEndCall.setOnClickListener { hangupCall() }
        materialBtnClose.setOnClickListener { finish() }
//        textViewEstimatedCallDuration.text =
//            "${getString(R.string.estimation)} ${UIUtil.formatCallDuration(timeInSeconds)}"
//        textViewEstimatedCallDuration.visibility = View.VISIBLE

    }

    private fun hangupCall() {
        if (callService != null) {
//            callService!!.hangupCall()
            log("Call Hangup by service")
        } else {
            val stopIntent = Intent(this@CallActivity, OutgoingCallService::class.java)
            stopIntent.action = Constants.STOPFOREGROUND_ACTION
            startService(stopIntent)
            onCallHangup()
            log("Call Hangup by stop")
        }
    }

    private fun initSpeakerMuteListeners() {
        setActionView(imgViewSpeaker, outgoingCall.isSpeakerOn)
        setActionView(imgViewMute, outgoingCall.isMuteOn)

        speakerAudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        speakerAudioManager!!.mode = AudioManager.MODE_IN_CALL

        muteAudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        muteAudioManager!!.mode = AudioManager.MODE_IN_CALL

        imgViewSpeaker.setOnClickListener {
            if (!speakerAudioManager!!.isSpeakerphoneOn) {
                speakerAudioManager!!.isSpeakerphoneOn = true
                setActionView(imgViewSpeaker, true)
                outgoingCall.isSpeakerOn = true
                return@setOnClickListener
            }

            outgoingCall.isSpeakerOn = false
            speakerAudioManager!!.mode = AudioManager.MODE_IN_CALL
            speakerAudioManager!!.isSpeakerphoneOn = false
            setActionView(imgViewSpeaker, false)

        }

        imgViewMute.setOnClickListener {
            if (!muteAudioManager!!.isMicrophoneMute) {
                muteAudioManager!!.isMicrophoneMute = true
                setActionView(imgViewMute, true)
                outgoingCall.isMuteOn = true
                return@setOnClickListener
            }

            outgoingCall.isMuteOn = false
            muteAudioManager!!.isMicrophoneMute = false
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
        Intent(this, OutgoingCallService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            Log.d(tag, "ServiceConnection: connected from service.")
            val binder = iBinder as OutgoingCallService.CallBinder
            callService = binder.service
            isServiceBound = true
            onServiceConnected()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(tag, "ServiceConnection: disconnected from service.")
            isServiceBound = false
            onServiceConnectionFailed()
        }
    }

    override fun onStop() {
        super.onStop()
        Intent(this, OutgoingCallService::class.java).also { intent ->
            unbindService(serviceConnection)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        wakeLock?.let {
//            if (it.isHeld) {
//                wakeLock!!.release()
//            }
//        }
//        sensorManager!!.unregisterListener(sensorEventListener)
        speakerAudioManager!!.mode = AudioManager.MODE_NORMAL
    }

    override fun onBackPressed() {
        if (!isInProgress) {
            super.onBackPressed()
        }
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

    companion object {
        const val tag = "CallLogs"

        fun log(message: String) {
            Log.d(tag, message)
        }
    }

}
