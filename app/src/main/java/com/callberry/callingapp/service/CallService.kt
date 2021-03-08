package com.callberry.callingapp.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.callberry.callingapp.R
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.model.Recent
import com.callberry.callingapp.plivo.PlivoConfig
import com.callberry.callingapp.plivo.PlivoConfig.getOutgoingCall
import com.callberry.callingapp.plivo.PlivoEventListener
import com.callberry.callingapp.plivo.PlivoHelper
import com.callberry.callingapp.plivo.STATE
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.NotificationUtil
import com.callberry.callingapp.util.UIUtil
import com.callberry.callingapp.util.Utils
import com.plivo.endpoint.Outgoing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class CallService : Service(), PlivoEventListener {

    inner class CallBinder : Binder() {
        val service: CallService
            get() = this@CallService
    }

    override fun onBind(intent: Intent?): IBinder = CallBinder()

    private lateinit var plivoHelper: PlivoHelper
    private var callData: Outgoing? = null
    private var listener: CallStateChangeListener? = null
    private var estimatedCallDuration: Long = 10
    private var callTimer = Timer()
    private var tick: Long = 0

    private var sensorEventListener: SensorEventListener? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var sensorManager: SensorManager? = null

    override fun onCreate() {
        plivoHelper = PlivoHelper()
        plivoHelper.setListener(this)
        initWakeSensor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.STOPFOREGROUND_ACTION) {
            terminate()
        } else {
            startForeground(1, NotificationUtil.createNotification(this))
        }
        return START_NOT_STICKY
    }

    fun setListener(callStateChangeListener: CallStateChangeListener) {
        this.listener = callStateChangeListener
    }

    fun initCall() {
        plivoHelper.initCall()
    }

    override fun onInitialization(success: Boolean) {
        if (!success) {
            PlivoConfig.setCallSate(Constants.STATE_FAILED)
            listener?.onStateChange(LocalCallState.LOGIN_FAILED)
            terminate()
            return
        }
        callData = plivoHelper.getOutgoing()
        callData?.call(getOutgoingCall().phoneNo)
        PlivoConfig.setCallInitiated()
    }

    override fun onCallStateChange(data: Outgoing?, callState: STATE) {
        try {
            callData = data
            when (callState) {
                STATE.RINGING -> {
                    PlivoConfig.setCallSate(Constants.STATE_RINGING)
                    listener?.onStateChange(LocalCallState.CALL_RINGING)
                }
                STATE.ANSWERED -> {
                    PlivoConfig.setCallSate(Constants.STATE_ANSWERED)
                    PlivoConfig.setCallStarted()
                    estimatedCallDuration =
                        Utils.getEstimatedDurationInSeconds(getOutgoingCall().callRate)
                    listener?.onStateChange(LocalCallState.CALL_STARTED)
                    startTimer()
                }
                STATE.HANGUP, STATE.REJECTED -> {
                    PlivoConfig.setCallSate(Constants.STATE_ENDED)
                    onCallEnded()
                }
                else -> {
                    Log.d("onOutgoingCall", "State in IDLE")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("callLogsError: ", "Message: ${e.message}")
        }
    }

    private fun startTimer() {
        callTimer.cancel()
        tick = 0
        callTimer = Timer(false)
        callTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                tick++
                if (tick == estimatedCallDuration) {
                    onCallEnded()
                }
                PlivoConfig.setCurrentTime(tick)
            }
        }, 100, TimeUnit.SECONDS.toMillis(1))
    }

    fun onCallEnded() {
        callTimer.cancel()
        listener?.onStateChange(LocalCallState.CALL_ENDED)
        terminate()
    }

    private fun terminate() {
        callData?.hangup()
        plivoHelper.logout()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        saveLogs()
        PlivoConfig.endOutgoingCall()
        wakeLock?.let {
            if (it.isHeld) {
                wakeLock?.release()
            }
        }
        sensorManager?.unregisterListener(sensorEventListener)
        (getSystemService(Context.AUDIO_SERVICE) as AudioManager).mode = AudioManager.MODE_NORMAL
        super.onDestroy()
    }

    private fun saveLogs() {
        val recentDao = AppDatabase(this).recentDao()
        val creditDao = AppDatabase(this).creditDao()
        var creditsUsed: Float = 0.0F
        var callDuration = "00:00"
        if (getOutgoingCall().callInitiated) {
            creditsUsed = UIUtil.calculateCredits(tick.toInt(), getOutgoingCall().callRate)
            callDuration = UIUtil.formatCallDuration(tick.toInt())
        }

        val recent = Recent()
        recent.contactId = getOutgoingCall().contactId
        recent.phoneNo = getOutgoingCall().phoneNo
        recent.duration = callDuration
        recent.credit = creditsUsed
        recent.dialCode = getOutgoingCall().dialCode
        CoroutineScope(Dispatchers.IO).launch {
            recentDao.insert(recent)
        }

        val credit = Credit()
        credit.name = "${getString(R.string.called)}: ${getOutgoingCall().phoneNo}"
        credit.credit = creditsUsed
        credit.type = Constants.DEBIT
        CoroutineScope(Dispatchers.IO).launch {
            creditDao.insert(credit)
            Utils.updateCredit(credit.credit, credit.type)
        }
    }

    private fun initWakeSensor() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "totalk:wake-lock"
        )
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!wakeLock!!.isHeld) {
                    wakeLock?.acquire(10 * 60 * 1000L)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

            }
        }

        val sensor: Sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        sensorManager!!.registerListener(
            sensorEventListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

    }

    fun updateSpeakerMode() {
        val speaker = !getOutgoingCall().isSpakerON
        Constants.callLogs("CallService:updateHoldMode -> $speaker")
        val speakerAudioManager: AudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        speakerAudioManager.mode = AudioManager.MODE_IN_CALL
        speakerAudioManager.isSpeakerphoneOn = speaker
        PlivoConfig.setSpeakerMode(speaker)
        listener?.onStateChange(LocalCallState.ON_MODE_CHANGE)
    }

    fun updateMuteMode() {
        val mute = !getOutgoingCall().isMuteON
        Constants.callLogs("CallService:updateHoldMode -> $mute")
        if (mute)
            callData?.mute()
        else
            callData?.unmute()
        PlivoConfig.setMuteMode(mute)
    }

    fun updateHoldMode() {
        val hold = !getOutgoingCall().isHoldON
        Constants.callLogs("CallService:updateHoldMode -> $hold")
        if (hold)
            callData?.hold()
        else
            callData?.unhold()

        PlivoConfig.setHoldMode(hold)
    }


}