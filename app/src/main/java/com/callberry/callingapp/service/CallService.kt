package com.callberry.callingapp.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.callberry.callingapp.R
import com.callberry.callingapp.database.dao.CreditDao
import com.callberry.callingapp.database.dao.RecentDao
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.model.Recent
import com.callberry.callingapp.util.*
import com.sinch.android.rtc.*
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallClient
import com.sinch.android.rtc.calling.CallListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*

class CallService : Service() {

    private val tag = "CallService"
    private val mBinder: IBinder = CallBinder()
    private lateinit var outgoingCall: OutgoingCall
    private lateinit var callingTone: MediaPlayer
    private var sinchClient: SinchClient? = null
    private var call: Call? = null
    private var callStateChangeListener: CallStateChangeListener? = null
    private var estimatedCallDuration: Int = 0
    //    private var checkCallTimer: Timer? = null
    private var checkCallDurationTask: CheckCallDurationTask? = null
    private lateinit var creditDao: CreditDao
    private lateinit var recentDao: RecentDao
    private var isCallInitiated = false

    override fun onCreate() {
        super.onCreate()
        outgoingCall = SharedPrefUtil.get(Constants.OUTGOING_CALL, OutgoingCall::class) as OutgoingCall
//        checkCallTimer = Timer()
//        checkCallDurationTask = CheckCallDurationTask()
//        creditDao = AppDatabase(this).creditDao()
//        recentDao = AppDatabase(this).recentDao()
        initializeCallerTune()
        updateCallInProgress(true)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.STOPFOREGROUND_ACTION) {
            terminate()
        } else {
            startForeground(1, App.createNotification(this))
        }
        return START_NOT_STICKY
    }

    fun observerListener(callStateChangeListener: CallStateChangeListener) {
        this.callStateChangeListener = callStateChangeListener
    }

    fun initializeClient() {
        callingTone.start()
        val sinchCred = PreferenceUtil.getSinchCred()
        sinchClient = Sinch.getSinchClientBuilder().context(this)
            .applicationKey(sinchCred.appKey)
            .applicationSecret(sinchCred.appSecret)
            .environmentHost(sinchCred.environment)
            .userId(sinchCred.userId)
            .build()

        sinchClient!!.setSupportCalling(true)
        sinchClient!!.start()
        sinchClient!!.addSinchClientListener(sinchClientListener)

    }

    private val sinchClientListener = object : SinchClientListener {
        override fun onRegistrationCredentialsRequired(p0: SinchClient?, p1: ClientRegistration?) {

        }

        override fun onLogMessage(p0: Int, p1: String?, p2: String?) {

        }

        override fun onClientStopped(p0: SinchClient?) {
            callStateChangeListener?.apply {
                onStateChange(CallState.CLIENT_STOPPED)
            }
        }

        override fun onClientFailed(p0: SinchClient?, p1: SinchError?) {
            callStateChangeListener?.apply {
                onStateChange(CallState.CLIENT_FAILED)
                terminate()
            }
        }

        override fun onClientStarted(p0: SinchClient?) {
            val callClient: CallClient = p0!!.callClient
//            call = callClient.callPhoneNumber(outgoingCall.phoneNo)
            call!!.addCallListener(callListener)
            isCallInitiated = true
        }
    }

    private val callListener = object : CallListener {
        override fun onCallProgressing(p0: Call?) {

        }

        override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {

        }

        override fun onCallEstablished(p0: Call?) {
            callingTone.stop()
            callStateChangeListener?.apply {
                onStateChange(CallState.CALL_ESTABLISHED)
//                estimatedCallDuration = Utils.getEstimatedDurationInSeconds(outgoingCall.callRate)
//                checkCallTimer!!.schedule(checkCallDurationTask, 0, 500)
            }
        }

        override fun onCallEnded(p0: Call?) {
            callStateChangeListener?.apply {
                onStateChange(CallState.CALL_HANGUP)
                terminate()
            }
        }
    }

    private fun terminate() {
        updateCallInProgress(false)
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        if (callingTone.isPlaying) {
            callingTone.stop()
        }
        if (sinchClient != null && sinchClient!!.isStarted) {
            sinchClient!!.stopListeningOnActiveConnection()
            sinchClient!!.terminateGracefully()
            sinchClient = null
            if (call != null) {
                call!!.hangup()
                call = null
            }

        }

        super.onDestroy()
    }

    private fun updateCallInProgress(callStatus: Boolean) {
        SharedPrefUtil.setBoolean(Constants.IS_CALL_IN_PROGRESS, callStatus)
        if (!callStatus) {
            SharedPrefUtil.remove(Constants.OUTGOING_CALL)
        }
    }

    private fun initializeCallerTune() {
        callingTone = MediaPlayer.create(this, R.raw.calling)
        val mAudioManager =
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
    }

    fun getCall(): Call? {
        return call
    }

    fun isCallInitiated(): Boolean {
        return isCallInitiated
    }

    private inner class CheckCallDurationTask : TimerTask() {
        override fun run() {
            Log.d(
                tag,
                "Duration: ${UIUtil.formatTimestamp(call!!.details.duration)} Estimated Duration: ${UIUtil.formatTimestamp(
                    estimatedCallDuration
                )}"
            )
            call?.apply {
                if (details.duration == estimatedCallDuration) {
                    hangup()
                }
            }
        }
    }

    private fun saveCallLogs() {
        val recent = Recent()
        recent.contactId = outgoingCall.contactId
        recent.phoneNo = outgoingCall.phoneNo
        recent.duration = UIUtil.formatCallDuration(call!!.details.duration)
        recent.credit =
            UIUtil.calculateCreditsUsed(call!!.details.duration, outgoingCall.callRate).toFloat()
        recent.dialCode = outgoingCall.dialCode
        CoroutineScope(IO).launch {
            recentDao.insert(recent)
        }
    }

    private fun saveCreditLogs() {
        val credit = Credit()
        credit.name = "${getString(R.string.called)}: ${outgoingCall.phoneNo}"
        credit.credit =
            UIUtil.calculateCreditsUsed(call!!.details.duration, outgoingCall.callRate).toFloat()
        credit.type = Constants.DEBIT
        CoroutineScope(IO).launch {
            creditDao.insert(credit)
            Utils.updateCredit(credit.credit, credit.type)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = mBinder

    inner class CallBinder : Binder() {
        val service: CallService
            get() = this@CallService
    }

    interface CallStateChangeListener {
        fun onStateChange(state: CallState);
    }

    enum class CallState {
        CLIENT_FAILED,
        CLIENT_STOPPED,
        CALL_ESTABLISHED,
        CALL_HANGUP
    }

}