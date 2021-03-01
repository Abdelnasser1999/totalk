package com.callberry.callingapp.plivo

import android.content.Context
import com.google.firebase.iid.FirebaseInstanceId
import com.plivo.endpoint.Endpoint
import com.plivo.endpoint.EventListener
import com.plivo.endpoint.Incoming
import com.plivo.endpoint.Outgoing
import java.util.*

public class PlivoHelper() : EventListener {
    private var endpoint: Endpoint = Endpoint.newInstance(true, this)
    private lateinit var listener: PlivoEventListener

    public fun setListener(listener: PlivoEventListener) {
        this.listener = listener;
    }

    fun initCall() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            endpoint.login(PlivoConfig.USERNAME, PlivoConfig.PASSWORD, instanceIdResult.token)
        }
    }

    fun getOutgoing(): Outgoing {
        return endpoint.createOutgoingCall()
    }

    override fun onIncomingCallRejected(p0: Incoming?) {

    }

    override fun onOutgoingCall(p0: Outgoing?) {
        listener.onCallStateChange(p0, STATE.PROGRESS)
    }

    override fun onIncomingCallInvalid(p0: Incoming?) {

    }

    override fun onOutgoingCallAnswered(p0: Outgoing?) {
        listener.onCallStateChange(p0, STATE.ANSWERED)
    }

    override fun onLogout() {

    }

    override fun onLoginFailed() {
        listener.onInitialization(false)
    }

    override fun onIncomingCall(p0: Incoming?) {

    }

    override fun onOutgoingCallRejected(p0: Outgoing?) {
        listener.onCallStateChange(p0, STATE.REJECTED)
    }

    override fun onIncomingDigitNotification(p0: String?) {

    }

    override fun onOutgoingCallRinging(p0: Outgoing?) {
        listener.onCallStateChange(p0, STATE.RINGING)
    }

    override fun onOutgoingCallInvalid(p0: Outgoing?) {
        listener.onCallStateChange(p0, STATE.INVALID)
    }

    override fun onOutgoingCallHangup(p0: Outgoing?) {
        listener.onCallStateChange(p0, STATE.HANGUP)
    }

    override fun onLogin() {
        listener.onInitialization(true)
    }

    override fun mediaMetrics(p0: HashMap<*, *>?) {

    }

    override fun onIncomingCallHangup(p0: Incoming?) {

    }

    fun logout() {
        endpoint.logout()
    }
}