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
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.plivo.PlivoBackEnd
import com.callberry.callingapp.plivo.UtilsPlivo
import com.callberry.callingapp.plivo.UtilsPlivo.HH_MM_SS
import com.callberry.callingapp.plivo.UtilsPlivo.MM_SS
import com.callberry.callingapp.service.CallStateChangeListener
import com.callberry.callingapp.service.LocalCallState
import com.callberry.callingapp.service.OutgoingCallService
import com.callberry.callingapp.util.*
import com.google.firebase.iid.FirebaseInstanceId
import com.plivo.endpoint.Incoming
import com.plivo.endpoint.Outgoing
import kotlinx.android.synthetic.main.activity_call_screen.*
import java.util.*
import java.util.concurrent.TimeUnit


class CallScreenActivity : AppCompatActivity(), CallStateChangeListener,
    PlivoBackEnd.BackendListener {

    private lateinit var outgoingCall: OutgoingCall
    private var outgoingCallService: OutgoingCallService? = null

    // private var call: Call? = null
    private var callTimer = Timer()

    private val callDurationTask = UpdateCallDurationTask()
    private var durationInSec: Int = 0
    private var sensorEventListener: SensorEventListener? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var sensorManager: SensorManager? = null
    private var muteAudioManager: AudioManager? = null
    private var speakerAudioManager: AudioManager? = null

    private var tick: Long = 0

    private val actionBar: ActionBar? = null

    private var isSpeakerOn = false
    private var isHoldOn = false
    private var isMuteOn = false

    private var callData: Any? = null

    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_screen)

        outgoingCall =
            SharedPrefUtil.get(Constants.OUTGOING_CALL, OutgoingCall::class) as OutgoingCall
        vibrator = this.getSystemService(VIBRATOR_SERVICE) as Vibrator
        init()
        initViews()
        initWakeSensor()
        bindService()

    }

    private fun init() {
        registerBackendListener()
        loginWithToken()
    }

    private fun registerBackendListener() {
        (application as App).backend()!!.setListener(this)
        UtilsPlivo.backendListener = this
    }

    private fun loginWithToken() {
        if (UtilsPlivo.loggedinStatus) {
            //updateUI(PlivoBackEnd.STATE.IDLE, null)
            callData = UtilsPlivo.incoming

        } else {

            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
                this
            ) { instanceIdResult ->
                (application as App).backend()!!.login(
                    instanceIdResult.token
                )
            }

        }
    }

    private fun showOutCallUI(state: PlivoBackEnd.STATE, outgoing: Outgoing) {
        Log.d("TESTTAG", "showOutCallUI")
        val title: String = state.name
        val callerState: TextView
        when (state) {
            PlivoBackEnd.STATE.IDLE -> {
                callerState = findViewById<View>(R.id.txtViewCallDuration) as TextView
                callerState.text = title
            }
            PlivoBackEnd.STATE.RINGING -> {
                callerState = findViewById<View>(R.id.txtViewCallDuration) as TextView
                callerState.text = Constants.RINGING_LABEL
            }
            PlivoBackEnd.STATE.ANSWERED -> {
                outgoingCallService!!.onCallStarted()
                imgViewHold.isEnabled = true
                startTimer()
            }
            PlivoBackEnd.STATE.HANGUP, PlivoBackEnd.STATE.REJECTED -> {
                outgoingCallService!!.onCallEnded()
                cancelTimer()
                updateUI(PlivoBackEnd.STATE.IDLE, null)
            }
        }
    }

    private fun makeCall(phoneNum: String) {
        Log.d("TESTTAG", "makeCall")
        val outgoing: Outgoing = (application as App).backend()!!.outgoing
        if (outgoing != null) {
            Log.d("TESTTAG", "makeCall is not null")
            outgoing.call(phoneNum)
        } else {
            Log.d("TESTTAG", "makeCall is null")
        }
    }

    private fun updateUI(state: PlivoBackEnd.STATE, data: Any?) {
        callData = data
        if (state.equals(PlivoBackEnd.STATE.REJECTED) || state.equals(PlivoBackEnd.STATE.HANGUP) || state.equals(
                PlivoBackEnd.STATE.INVALID
            )
        ) {
            if (data is Outgoing) {
                Log.d("TESTTAG", "data is Outgoing")
                showOutCallUI(state, data)
            }
        } else {
            if (data != null) {
                if (data is Outgoing) {
                    // handle outgoing
                    showOutCallUI(state, (data as Outgoing?)!!)
                }
            }
        }
        // showOutCallUI(state, data as Outgoing)

    }

    private fun startTimer() {
        cancelTimer()
        callTimer = Timer(false)
        callTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val hours =
                        TimeUnit.SECONDS.toHours(tick.toLong()).toInt()
                    val minutes = TimeUnit.SECONDS.toMinutes(
                        TimeUnit.HOURS.toSeconds(hours.toLong()).let { tick -= it; tick })
                        .toInt()
                    val seconds =
                        (tick - TimeUnit.MINUTES.toSeconds(minutes.toLong())).toInt()
                    val text = if (hours > 0) java.lang.String.format(
                        HH_MM_SS,
                        hours,
                        minutes,
                        seconds
                    ) else java.lang.String.format(MM_SS, minutes, seconds)
                    val timerTextView =
                        findViewById<View>(R.id.txtViewCallDuration) as TextView
                    if (timerTextView != null) {
                        timerTextView.visibility = View.VISIBLE
                        timerTextView.text = text
                        tick++
                    }
                }
            }
        }, 100, TimeUnit.SECONDS.toMillis(1))
    }

    private fun cancelTimer() {
        if (callTimer != null) callTimer.cancel()
        tick = 0
    }

    override fun onResume() {
        super.onResume()
        if (SharedPrefUtil.getBoolean(Constants.IS_CALL_IN_PROGRESS, false)) {
            //updateCallTimer()
        }

        updateSpeakerMuteAction()

    }

    private fun onServiceConnectionSuccessful() {
        outgoingCallService!!.loginAccount()
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

        /* if (state == LocalCallState.CALL_ESTABLISHED) {
             toast("Call Started")
             outgoingCallService?.apply {
                 getCall()?.let {
                     call = it
                 }
             }
             updateCallTimer()
             return
         }*/

        if (state == LocalCallState.CALL_ENDED) {
            toast("Call Ended")
            callFinished()
            return
        }


    }

    private fun callFinished() {
        (callData as Outgoing).hangup().let {
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
            if (SharedPrefUtil.getBoolean(Constants.IS_CALL_IN_PROGRESS, false)) {
                durationInSec = tick.toInt()
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
            SharedPrefUtil.setBoolean(Constants.IS_HOLD_ON, false)
            setActionView(imgViewHold, false)
        } else {
            isHoldOn = true
            holdCall()
            SharedPrefUtil.setBoolean(Constants.IS_HOLD_ON, true)
            setActionView(imgViewHold, true)
        }
    }

    fun holdCall() {
        if (callData != null) {
            if (callData is Outgoing) {
                (callData as Outgoing).hold()
            }
        }
    }

    fun unHoldCall() {
        if (callData != null) {
            if (callData is Outgoing) {
                (callData as Outgoing).unhold()
            }
        }
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
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val btn = findViewById<View>(R.id.imgViewSpeaker) as ImageView
            if (isSpeakerOn) {
                isSpeakerOn = false
                audioManager.mode = AudioManager.MODE_IN_CALL
                audioManager.mode = AudioManager.MODE_NORMAL
                SharedPrefUtil.setBoolean(Constants.IS_SPREAKER_ON, false)
                setActionView(imgViewSpeaker, false)
            } else {
                isSpeakerOn = true
                audioManager.mode = AudioManager.MODE_NORMAL
                audioManager.mode = AudioManager.MODE_IN_CALL
                SharedPrefUtil.setBoolean(Constants.IS_SPREAKER_ON, true)
                setActionView(imgViewSpeaker, true)
            }
            audioManager.isSpeakerphoneOn = isSpeakerOn
            /*if (!speakerAudioManager!!.isSpeakerphoneOn) {
                speakerAudioManager!!.isSpeakerphoneOn = true
                SharedPrefUtil.setBoolean(Constants.IS_SPREAKER_ON, true)
                setActionView(imgViewSpeaker, true)
                return@setOnClickListener
            }

            speakerAudioManager!!.mode = AudioManager.MODE_IN_CALL
            speakerAudioManager!!.isSpeakerphoneOn = false
            SharedPrefUtil.setBoolean(Constants.IS_SPREAKER_ON, false)
            setActionView(imgViewSpeaker, false)*/

        }

        imgViewMute.setOnClickListener {
            val btn = findViewById<View>(R.id.imgViewMute) as ImageView
            if (isMuteOn) {
                SharedPrefUtil.setBoolean(Constants.IS_MUTE_ON, false)
                setActionView(imgViewMute, false)
                isMuteOn = false
                unMuteCall()
            } else {
                SharedPrefUtil.setBoolean(Constants.IS_MUTE_ON, true)
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

    fun muteCall() {
        if (callData != null) {
            if (callData is Outgoing) {
                (callData as Outgoing).mute()
            }
        }
    }

    fun unMuteCall() {
        if (callData != null) {
            if (callData is Outgoing) {
                (callData as Outgoing).unmute()
            }
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
        outgoingCallService!!.onCallEnded()
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        cancelTimer()
        endCall()
        isSpeakerOn = false
        isHoldOn = false
        isMuteOn = false
        audioManager.isSpeakerphoneOn = isSpeakerOn
        updateUI(PlivoBackEnd.STATE.IDLE, null)
    }

    override fun onBackPressed() {
        if (!SharedPrefUtil.getBoolean(Constants.IS_CALL_IN_PROGRESS, false))
            super.onBackPressed()
    }

    fun endCall() {
        if (callData != null) {
            if (callData is Outgoing) {
                (callData as Outgoing).hangup()
            }
        }
        onBackPressed()
    }

    override fun onDestroy() {
        wakeLock?.let {
            if (it.isHeld) {
                wakeLock!!.release()
            }
        }
//        sensorManager!!.unregisterListener(sensorEventListener)
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

    override fun onLogin(success: Boolean) {
        runOnUiThread {
            if (success) {
                updateUI(PlivoBackEnd.STATE.RINGING, null)
                makeCall(textViewPhoneNo.text.toString())
            } else {
                Toast.makeText(
                    this,
                    R.string.login_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onLogout() {
    }

    override fun onIncomingCall(data: Incoming?, callState: PlivoBackEnd.STATE?) {
    }

    override fun onOutgoingCall(data: Outgoing?, callState: PlivoBackEnd.STATE?) {
        Log.d("TESTTAG", "onOutgoingCall")
        runOnUiThread { updateUI(callState!!, data) }
    }

    override fun onIncomingDigit(digit: String?) {

    }

    override fun mediaMetrics(messageTemplate: HashMap<*, *>?) {

    }

}
