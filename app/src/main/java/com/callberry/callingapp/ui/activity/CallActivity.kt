package com.callberry.callingapp.ui.activity

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.plivo.PlivoConfig
import com.callberry.callingapp.service.CallService
import com.callberry.callingapp.service.CallStateChangeListener
import com.callberry.callingapp.service.LocalCallState
import com.callberry.callingapp.util.*
import com.ornach.nobobutton.NoboButton
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.android.synthetic.main.activity_call.textViewIcon
import kotlinx.android.synthetic.main.activity_call.textViewName
import kotlinx.android.synthetic.main.activity_call.textViewPhoneNo
import kotlinx.android.synthetic.main.activity_call.txtViewCallDuration
import java.util.*

class CallActivity : AppCompatActivity(), CallStateChangeListener, View.OnClickListener {

    private var outgoingCallService: CallService? = null
    private var callTimer = Timer()
    private var serviceConnection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
    }

    override fun onResume() {
        super.onResume()

        if (!PlivoConfig.isCallInProgress()) {
            onBackPressed()
            return
        }

        textViewName.text = outgoingCall().name
        textViewPhoneNo.text = outgoingCall().phoneNo
        UIUtil.setIcon(this, textViewIcon, outgoingCall().name, outgoingCall().theme)
        noboBtnEndCall.setOnClickListener(this)
        imgViewHold.setOnClickListener(this)
        imgViewMute.setOnClickListener(this)
        imgViewSpeaker.setOnClickListener(this)

        serviceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                endCall()
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as CallService.CallBinder
                outgoingCallService = binder.service
                outgoingCallService?.setListener(this@CallActivity)
                if (!outgoingCall().callInitiated) {
                    outgoingCallService?.initCall()
                    return
                }

                arrayListOf<ImageView>(imgViewHold, imgViewMute, imgViewSpeaker).enable(true)
                toast("Service Connected")
            }
        }

        val serviceIntent = Intent(this, CallService::class.java)
        serviceIntent.action = Constants.STARTFOREGROUND_ACTION
        ContextCompat.startForegroundService(this, serviceIntent)
        Intent(this, CallService::class.java).also { intent ->
            bindService(intent, serviceConnection as ServiceConnection, Context.BIND_AUTO_CREATE)
        }

        callTimer.schedule(UpdateCallDurationTask(), 0, 500)

        updateModeView()

    }


    private fun endCall() {
        outgoingCallService?.onCallEnded() ?: kotlin.run {
            val serviceIntent = Intent(this, CallService::class.java)
            serviceIntent.action = Constants.STOPFOREGROUND_ACTION
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    override fun onStateChange(state: LocalCallState) {
        runOnUiThread {
            when (state) {
                LocalCallState.LOGIN_FAILED -> {
                    setCurrentStatus()
                    onCallDismiss()
                }
                LocalCallState.CALL_RINGING -> {
                    setCurrentStatus()
                }
                LocalCallState.CALL_STARTED -> {
                    callTimer.schedule(UpdateCallDurationTask(), 0, 500)
                }
                LocalCallState.CALL_ENDED -> {
                    callTimer.cancel()
                    onCallDismiss()
                }
                LocalCallState.ON_MODE_CHANGE -> {
                    updateModeView()
                }
                else -> {
                    Log.d("outgoingCall", "Call In IDLE STATE")
                }
            }
        }
    }

    private fun updateModeView() {
        imgViewSpeaker?.updateView(this, outgoingCall().isSpakerON)
        imgViewMute?.updateView(this, outgoingCall().isMuteON)
        imgViewHold?.updateView(this, outgoingCall().isHoldON)
    }

    private inner class UpdateCallDurationTask : TimerTask() {
        override fun run() {
            this@CallActivity.runOnUiThread {
                if (PlivoConfig.isCallInProgress()) {
                    if (outgoingCall().callStarted) {
                        txtViewCallDuration.text =
                            UIUtil.formatTimestamp(outgoingCall().currentTimeInSec)
                    } else {
                        setCurrentStatus()
                    }
                }
            }
        }
    }

    private fun onCallDismiss() {
        setCurrentStatus()
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 1000)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.noboBtnEndCall -> {
                endCall()
            }
            R.id.imgViewSpeaker -> {
                outgoingCallService?.updateSpeakerMode() ?: kotlin.run {
                    toast("Unable to change speaker")
                }
            }
            R.id.imgViewMute -> {
                outgoingCallService?.updateMuteMode() ?: kotlin.run {
                    toast("Unable to change mute")
                }
            }
            R.id.imgViewHold -> {
                outgoingCallService?.updateHoldMode() ?: kotlin.run {
                    toast("Unable to change hold")
                }
            }
        }
    }

    fun setCurrentStatus() {
        txtViewCallDuration.text = outgoingCall().callState
        val color =
            if (outgoingCall().callState == Constants.STATE_FAILED) R.color.colorRed else R.color.colorPrimary
        txtViewCallDuration.setTextColor(ContextCompat.getColor(this, color))
    }

    override fun onBackPressed() {
        if (!PlivoConfig.isCallInProgress()){
            super.onBackPressed()
        }
    }

    fun outgoingCall(): OutgoingCall {
        return PlivoConfig.getOutgoingCall()
    }

    override fun onStop() {
        serviceConnection?.let { unbindService(it) }
        super.onStop()
    }

}


