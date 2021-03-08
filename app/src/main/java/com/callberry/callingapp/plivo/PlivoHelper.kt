package com.callberry.callingapp.plivo

import android.util.Log
import com.callberry.callingapp.util.Constants
import com.google.firebase.iid.FirebaseInstanceId
import com.plivo.endpoint.Endpoint
import com.plivo.endpoint.EventListener
import com.plivo.endpoint.Incoming
import com.plivo.endpoint.Outgoing
import java.util.*

class PlivoHelper() : EventListener {
    private var endpoint: Endpoint = Endpoint.newInstance(true, this)
    private lateinit var listener: PlivoEventListener

    fun setListener(listener: PlivoEventListener) {
        this.listener = listener
    }

    fun initCall() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            endpoint.login(PlivoConfig.USERNAME, PlivoConfig.PASSWORD, instanceIdResult.token)
        }
    }

    fun getOutgoing(): Outgoing {
        return try {
            endpoint.createOutgoingCall()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "getOutgoing: ${e.message}")
            Outgoing(endpoint)
        }
    }

    override fun onIncomingCallRejected(p0: Incoming?) {

    }

    override fun onOutgoingCall(p0: Outgoing?) {
        try {
            listener.onCallStateChange(p0, STATE.PROGRESS)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "onOutgoingCall: ${e.message}")
        }
    }

    override fun onIncomingCallInvalid(p0: Incoming?) {

    }

    override fun onOutgoingCallAnswered(p0: Outgoing?) {
        try {
            listener.onCallStateChange(p0, STATE.ANSWERED)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "onOutgoingCallAnswered: ${e.message}")
        }

    }

    override fun onLogout() {

    }

    override fun onLoginFailed() {
        try {
            listener.onInitialization(false)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "onLoginFailed: ${e.message}")
        }

    }

    override fun onIncomingCall(p0: Incoming?) {

    }

    override fun onOutgoingCallRejected(p0: Outgoing?) {
        try {
            listener.onCallStateChange(p0, STATE.REJECTED)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "onOutgoingCallRejected: ${e.message}")
        }

    }

    override fun onIncomingDigitNotification(p0: String?) {

    }

    override fun onOutgoingCallRinging(p0: Outgoing?) {
        try {
            listener.onCallStateChange(p0, STATE.RINGING)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "onOutgoingCallRinging: ${e.message}")
        }

    }

    override fun onOutgoingCallInvalid(p0: Outgoing?) {
        try {
            listener.onCallStateChange(p0, STATE.INVALID)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "onOutgoingCallInvalid: ${e.message}")
        }

    }

    override fun onOutgoingCallHangup(p0: Outgoing?) {
        try {
            listener.onCallStateChange(p0, STATE.HANGUP)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "onOutgoingCallHangup: ${e.message}")
        }

    }

    override fun onLogin() {
        try {
            listener.onInitialization(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "onLogin: ${e.message}")
        }

    }

    override fun mediaMetrics(p0: HashMap<*, *>?) {
        try {
            Log.e(Constants.LOGS_CALLS, "Call Log Messages: ${p0.toString()}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constants.CRASH_LOG_CALL, "mediaMetrics: ${e.message}")
        }

    }

    override fun onIncomingCallHangup(p0: Incoming?) {

    }

    fun logout() {
        endpoint.logout()
    }
}