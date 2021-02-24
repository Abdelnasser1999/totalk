package com.callberry.callingapp.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.model.Recent
import com.callberry.callingapp.ui.activity.CallScreenActivity
import com.callberry.callingapp.util.*
import com.sinch.android.rtc.*
import com.sinch.android.rtc.calling.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class OutgoingCallService : Service() {

    private var callStateChangeListener: CallStateChangeListener? = null
    private var isCallStarted = false
    private var sinchClient: SinchClient? = null
    private var call: Call? = null
    private lateinit var outgoingCall: OutgoingCall
    private var checkCallTimer = Timer()
    private var checkCallDurationTask = CheckCallDurationTask()
    private var estimatedCallDuration: Int = 10
    private lateinit var callingTone: MediaPlayer
    private lateinit var mAudioManager: AudioManager

    override fun onCreate() {
        super.onCreate()

        outgoingCall =
            SharedPrefUtil.get(Constants.OUTGOING_CALL, OutgoingCall::class) as OutgoingCall
        initCallerTune()
        updateCallInProgress(true)

        val sinch = PreferenceUtil.getSinchCred()
        Log.d("callLogs", "Sinch: ${sinch.appKey}, ${sinch.appSecret}, ${sinch.environment}, ${sinch.userId}")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.STOPFOREGROUND_ACTION) {
            terminate()
        } else {
            startForeground(1, createNotification())
        }
        return START_NOT_STICKY
    }

    fun initializeClient() {
        val sinchCred = PreferenceUtil.getSinchCred()
        sinchClient = Sinch.getSinchClientBuilder().context(this)
            .applicationKey(sinchCred.appKey)
            .applicationSecret(sinchCred.appSecret)
            .environmentHost(sinchCred.environment)
            .userId(sinchCred.userId)
            .build()

        sinchClient!!.setSupportCalling(true)
        sinchClient!!.setSupportPushNotifications(true)
        sinchClient!!.start()

        sinchClient!!.addSinchClientListener(clientListener)

    }

    private val clientListener = object : SinchClientListener {
        override fun onRegistrationCredentialsRequired(p0: SinchClient?, p1: ClientRegistration?) {

        }

        override fun onLogMessage(p0: Int, p1: String?, p2: String?) {

        }

        override fun onClientStopped(p0: SinchClient?) {
            toast("Client Stopped")
            callingTone.stop()
            if (callingTone.isPlaying) callingTone.stop()
            if (callStateChangeListener != null) {
                callStateChangeListener!!.onStateChange(LocalCallState.CLIENT_STOPPED)
                terminate()
            }
        }

        override fun onClientFailed(p0: SinchClient?, p1: SinchError?) {
            toast("Client Failed")
            Log.d("callLogs", "ERROR: ${p1?.message}")
            callingTone.stop()
            if (callStateChangeListener != null) {
                callStateChangeListener!!.onStateChange(LocalCallState.CLIENT_FAILED)
                terminate()
            }
        }

        override fun onClientStarted(p0: SinchClient?) {
            toast("Client Started")
            val callClient: CallClient = p0!!.callClient
            call = callClient.callPhoneNumber(outgoingCall.phoneNo)
            call!!.addCallListener(callListener)
//            listenForCallProgressing()
        }
    }

    private fun listenForCallProgressing() {
        var isListening = true
        Log.d("callLogs", "Listener Started")
        Thread {
            while (isListening) {
                val case = call?.details?.endCause
                Log.d("callLogs", "call detail: $case")
                if (case == CallEndCause.CANCELED) {
                    Log.d("callLogs", "Call Canceled")
//                    call?.hangup()
                    isListening = false
                }
            }
        }.start()

        Log.d("callLogs", "Listener End Now")
    }

    private val callListener = object : CallListener {
        override fun onCallProgressing(p0: Call?) {
            Log.d("callLogsFor", "End Case: " + p0!!.details.endCause.name)
            if (p0.details.endCause.value == CallEndCause.DENIED.value) {
                Toast.makeText(this@OutgoingCallService, "Call Busy", Toast.LENGTH_LONG).show()
                callStateChangeListener!!.onStateChange(LocalCallState.CALL_ENDED)
                terminate()
            }
        }

        override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {
            toast("CallState: ${p0!!.state}")
        }

        override fun onCallEnded(p0: Call?) {
            callingTone.stop()
            if (callStateChangeListener != null) {
                callStateChangeListener!!.onStateChange(LocalCallState.CALL_ENDED)
                terminate()
            }
        }

        override fun onCallEstablished(p0: Call?) {
            callingTone.stop()
            isCallStarted = true
            estimatedCallDuration = Utils.getEstimatedDurationInSeconds(outgoingCall.callRate)
            checkCallTimer.schedule(checkCallDurationTask, 0, 500)
            if (callStateChangeListener != null) {
                callStateChangeListener!!.onStateChange(LocalCallState.CALL_ESTABLISHED)
            }
        }


    }

    fun isCallStarted(): Boolean {
        return isCallStarted
    }

    fun getCall(): Call? {
        return call
    }

    fun setCallStateChangeListener(callStateChangeListener: CallStateChangeListener) {
        this.callStateChangeListener = callStateChangeListener
    }

    private inner class CheckCallDurationTask : TimerTask() {
        override fun run() {
            call?.apply {
                if (details.duration == estimatedCallDuration) {
                    toast("Call Hang up by auto")
                    hangup()
                }
            }
        }
    }

    override fun onDestroy() {
        callingTone.stop()
        if (call != null) {
            call!!.hangup()
        }
        if (sinchClient != null && sinchClient!!.isStarted) {
            sinchClient!!.stopListeningOnActiveConnection()
            sinchClient!!.terminateGracefully()
            sinchClient = null
        }
        saveLogs()
        mAudioManager.mode = AudioManager.MODE_NORMAL
        super.onDestroy()
    }

    private fun saveLogs() {
        val recentDao = AppDatabase(this).recentDao()
        val creditDao = AppDatabase(this).creditDao()
        var creditsUsed: Float = 0.0F
        var callDuration = "00:00"
        if (isCallStarted) {
            creditsUsed = UIUtil.calculateCredits(call!!.details.duration, outgoingCall.callRate)
            callDuration = UIUtil.formatCallDuration(call!!.details.duration)
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
        return NotificationCompat.Builder(this, App.CHANNEL_ID)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.outgoing_call))
            .setContentIntent(pendingIntent)
            .build()
    }

    inner class CallBinder : Binder() {
        val service: OutgoingCallService
            get() = this@OutgoingCallService
    }

    override fun onBind(intent: Intent?): IBinder = CallBinder()


}