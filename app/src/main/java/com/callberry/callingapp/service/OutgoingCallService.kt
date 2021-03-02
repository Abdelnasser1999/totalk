package com.callberry.callingapp.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.model.Recent
import com.callberry.callingapp.plivo.*
import com.callberry.callingapp.ui.activity.CallScreenActivity
import com.callberry.callingapp.util.*
import com.plivo.endpoint.Outgoing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class OutgoingCallService : Service(), PlivoEventListener {

    private var callStateChangeListener: CallStateChangeListener? = null
    private var isCallStarted = false
    private lateinit var outgoingCall: OutgoingCall
    private var estimatedCallDuration: Long = 10
    private lateinit var callingTone: MediaPlayer
    private lateinit var mAudioManager: AudioManager
    private var callTimer = Timer()
    private var tick: Long = 0
    private var callData: Outgoing? = null
    private var plivoHelper: PlivoHelper? = null
    private var currentCallSate: String = Constants.STATE_CONNECTING

    override fun onCreate() {
        super.onCreate()
        outgoingCall =
            SharedPrefUtil.get(Constants.OUTGOING_CALL, OutgoingCall::class) as OutgoingCall
        initCallerTune()
        updateCallInProgress(true)
        plivoHelper = PlivoHelper()
        plivoHelper?.setListener(this)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.STOPFOREGROUND_ACTION) {
            terminate()
        } else {
            startForeground(1, createNotification())
        }
        return START_NOT_STICKY
    }

    fun setCallStateChangeListener(callStateChangeListener: CallStateChangeListener) {
        this.callStateChangeListener = callStateChangeListener
    }

    fun initCall() {
        plivoHelper?.initCall()
    }

    override fun onInitialization(success: Boolean) {
        if (!success) {
            Constants.callLogs("Login Failed")
            callStateChangeListener?.onStateChange(LocalCallState.LOGIN_FAILED)
            terminate()
            return
        }
        Constants.callLogs("Login Successful")
        plivoHelper?.getOutgoing()?.call(outgoingCall.phoneNo) ?: kotlin.run {
            Constants.callLogs("Outgoing Call is NULL")
            callStateChangeListener?.onStateChange(LocalCallState.LOGIN_FAILED)
            terminate()
        }
    }

    override fun onCallStateChange(data: Outgoing?, callState: STATE) {
        callData = data
        when (callState) {
            STATE.RINGING -> {
                Log.d("onOutgoingCall", "State in RINGING")
                currentCallSate = Constants.STATE_RINGING
                callStateChangeListener?.onStateChange(LocalCallState.CALL_RINGING)
                callingTone.stop()
            }
            STATE.ANSWERED -> {
                Log.d("onOutgoingCall", "State in ANSWERED")
                currentCallSate = Constants.STATE_ANSWERED
                isCallStarted = true
                estimatedCallDuration = Utils.getEstimatedDurationInSeconds(outgoingCall.callRate)
                callStateChangeListener?.onStateChange(LocalCallState.CALL_STARTED)
                startTimer()
            }
            STATE.HANGUP -> {
                Log.d("onOutgoingCall", "State in HANGUP")
                onCallEnded()
            }

            STATE.REJECTED -> {
                Log.d("onOutgoingCall", "State in REJECTED")
                callStateChangeListener?.onStateChange(LocalCallState.CALL_REJECTED)
                callingTone.stop()
                terminate()
            }
            else -> {
                Log.d("onOutgoingCall", "State in IDLE")
            }
        }
    }

    fun onCallEnded() {
        callingTone.stop()
        callStateChangeListener?.onStateChange(LocalCallState.CALL_ENDED)
        terminate()
    }

    fun isCallStarted(): Boolean {
        return isCallStarted
    }

    fun getCallData(): Outgoing? {
        return callData
    }

    fun getCurrentCallState(): String {
        return currentCallSate
    }

    private fun startTimer() {
        cancelTimer()
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
                tick++
                if (tick == estimatedCallDuration) {
                    onCallEnded()
                }
            }
        }, 100, TimeUnit.SECONDS.toMillis(1))
    }

    private fun cancelTimer() {
        callTimer.cancel()
        tick = 0
    }

    override fun onDestroy() {
        callData?.hangup()
        callingTone.stop()
        mAudioManager.mode = AudioManager.MODE_NORMAL
        saveLogs()
        super.onDestroy()
    }

    fun getTimeInSeconds(): Long {
        return tick
    }

    private fun saveLogs() {
        val recentDao = AppDatabase(this).recentDao()
        val creditDao = AppDatabase(this).creditDao()
        var creditsUsed: Float = 0.0F
        var callDuration = "00:00"
        if (isCallStarted) {
            creditsUsed = UIUtil.calculateCredits(tick.toInt(), outgoingCall.callRate)
            callDuration = UIUtil.formatCallDuration(tick.toInt())
        }

        val recent = Recent()
        recent.contactId = outgoingCall.contactId
        recent.phoneNo = outgoingCall.phoneNo
        recent.duration = callDuration
        recent.credit = creditsUsed
        recent.dialCode = outgoingCall.dialCode
        CoroutineScope(Dispatchers.IO).launch {
            recentDao.insert(recent)
        }

        if (!isCallStarted) {
            return
        }

        val credit = Credit()
        credit.name = "${getString(R.string.called)}: ${outgoingCall.phoneNo}"
        credit.credit = creditsUsed
        credit.type = Constants.DEBIT
        CoroutineScope(Dispatchers.IO).launch {
            creditDao.insert(credit)
            Utils.updateCredit(credit.credit, credit.type)
        }
    }

    private fun terminate() {
        callData?.hangup()
        plivoHelper?.logout()
        updateCallInProgress(false)
        stopForeground(true)
        stopSelf()
    }

    private fun updateCallInProgress(callStatus: Boolean) {
        SharedPrefUtil.setBoolean(Constants.IS_CALL_IN_PROGRESS, callStatus)
        if (!callStatus) {
            SharedPrefUtil.remove(Constants.OUTGOING_CALL)
        }
    }

    private fun initCallerTune() {
        callingTone = MediaPlayer.create(this, R.raw.calling)
        mAudioManager =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolumeMusic = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        mAudioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            maxVolumeMusic,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )
        mAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        mAudioManager.isSpeakerphoneOn = false
        callingTone.isLooping = true
        callingTone.start()
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, CallScreenActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val builder = NotificationCompat.Builder(this, App.CHANNEL_ID)
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.drawable.ic_call)
        builder.setContentTitle(getString(R.string.app_name))
        builder.setContentText(getString(R.string.call_in_progress))
        builder.setContentIntent(pendingIntent)
        return builder.build();
    }

    inner class CallBinder : Binder() {
        val service: OutgoingCallService
            get() = this@OutgoingCallService
    }

    override fun onBind(intent: Intent?): IBinder = CallBinder()

}