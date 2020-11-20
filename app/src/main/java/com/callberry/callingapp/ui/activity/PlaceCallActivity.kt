package com.callberry.callingapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.callberry.callingapp.R
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.util.*
import com.sinch.android.rtc.*
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallClient
import com.sinch.android.rtc.calling.CallListener
import kotlinx.android.synthetic.main.activity_place_call.*

class PlaceCallActivity : AppCompatActivity() {

    private lateinit var outgoingCall: OutgoingCall
    private lateinit var sinchClient: SinchClient
    private lateinit var call: Call

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_call)

        outgoingCall = SharedPrefUtil.get(Constants.OUTGOING_CALL, OutgoingCall::class) as OutgoingCall
        txtViewPhoneNo.text = "Calling@${outgoingCall.phoneNo}"

        btnEndCall.setOnClickListener {
            call.hangup()
        }

        initSinch()

    }

    private fun initSinch() {
        val sinchCred = DefaultConfig.sinch
        sinchClient = Sinch.getSinchClientBuilder().context(this)
            .applicationKey(sinchCred.appKey)
            .applicationSecret(sinchCred.appSecret)
            .environmentHost(sinchCred.environment)
            .userId(sinchCred.userId)
            .build()

        sinchClient.setSupportCalling(true)
        sinchClient.start()

        sinchClient.addSinchClientListener(sinchClientListener)

    }

    private val sinchClientListener = object : SinchClientListener {
        override fun onClientStarted(p0: SinchClient?) {
            toast("Client Started")
            val callClient: CallClient = p0!!.callClient
            call = callClient.callPhoneNumber(outgoingCall.phoneNo)
            call.addCallListener(callListener)
        }

        override fun onClientStopped(p0: SinchClient?) {

        }

        override fun onRegistrationCredentialsRequired(p0: SinchClient?, p1: ClientRegistration?) {

        }

        override fun onLogMessage(p0: Int, p1: String?, p2: String?) {

        }

        override fun onClientFailed(p0: SinchClient?, p1: SinchError?) {
            toast("Client Failed")

        }

    }

    private val callListener = object : CallListener {
        override fun onCallProgressing(p0: Call?) {

        }

        override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {

        }

        override fun onCallEstablished(p0: Call?) {
            toast("Call Established")
            txtViewPhoneNo.text = "Established@${outgoingCall.phoneNo}"
        }

        override fun onCallEnded(p0: Call?) {

        }
    }

}
