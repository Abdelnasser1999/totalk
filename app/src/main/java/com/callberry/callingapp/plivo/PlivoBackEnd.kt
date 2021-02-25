package com.callberry.callingapp.plivo

import android.util.Log
import com.callberry.callingapp.util.Constants
import com.plivo.endpoint.Endpoint
import com.plivo.endpoint.EventListener
import com.plivo.endpoint.Incoming
import com.plivo.endpoint.Outgoing
import java.util.*

class PlivoBackEnd : EventListener {
    enum class STATE {
        IDLE, PROGRESS, RINGING, ANSWERED, HANGUP, REJECTED, INVALID
    }

    private var endpoint: Endpoint? = null
    private var listener: BackendListener? = null
    fun init(log: Boolean) {
        endpoint = Endpoint.newInstance(log, this)

        //Iniatiate SDK with Options, "enableTracking" and "context"(To get network related information)

//        endpoint = Endpoint.newInstance(log, this,UtilsPlivo.options);
    }

    fun setListener(listener: BackendListener?) {
        this.listener = listener
    }

    fun login(newToken: String?) {
        endpoint!!.login(UtilsPlivo.USERNAME, UtilsPlivo.PASSWORD, newToken)
        UtilsPlivo.deviceToken = newToken
    }

    fun loginForIncoming(newToken: String?): Boolean {
        return endpoint!!.login(UtilsPlivo.USERNAME, UtilsPlivo.PASSWORD, newToken)
    }

    fun logout() {
        endpoint!!.logout()
    }

    fun relayIncomingPushData(incomingData: HashMap<String?, String?>?) {
        if (incomingData != null && !incomingData.isEmpty()) {
            endpoint!!.relayVoipPushNotification(incomingData)
        }
    }

    val outgoing: Outgoing
        get() = endpoint!!.createOutgoingCall()

    // Plivo SDK callbacks
    override fun onLogin() {
        Log.d(TAG, Constants.LOGIN_SUCCESS)
        UtilsPlivo.loggedinStatus = true
        if (listener != null) listener!!.onLogin(true)
    }

    override fun onLogout() {
        Log.d(TAG, Constants.LOGOUT_SUCCESS)
        if (listener != null) listener!!.onLogout()
    }

    override fun onLoginFailed() {
        Log.e(TAG, Constants.LOGIN_FAILED)
        if (listener != null) listener!!.onLogin(false)
    }

    override fun onIncomingDigitNotification(s: String) {
        if (listener != null) listener!!.onIncomingDigit(s)
    }

    override fun onIncomingCall(incoming: Incoming) {
        Log.d(TAG, Constants.INCOMING_CALL_RINGING)
        UtilsPlivo.incoming = incoming
        if (listener != null) listener!!.onIncomingCall(incoming, STATE.RINGING)
    }

    override fun onIncomingCallHangup(incoming: Incoming) {
        Log.d(TAG, Constants.INCOMING_CALL_HANGUP)
        if (listener != null) listener!!.onIncomingCall(incoming, STATE.HANGUP)
    }

    override fun onIncomingCallInvalid(incoming: Incoming) {}
    override fun onIncomingCallRejected(incoming: Incoming) {
        Log.d(TAG, Constants.INCOMING_CALL_REJECTED)
        if (listener != null) listener!!.onIncomingCall(incoming, STATE.REJECTED)
    }

    override fun onOutgoingCall(outgoing: Outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL)
        if (listener != null) listener!!.onOutgoingCall(outgoing, STATE.PROGRESS)
    }

    override fun onOutgoingCallRinging(outgoing: Outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_RINGING)
        if (listener != null) listener!!.onOutgoingCall(outgoing, STATE.RINGING)
    }

    override fun onOutgoingCallAnswered(outgoing: Outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_ANSWERED)
        if (listener != null) listener!!.onOutgoingCall(outgoing, STATE.ANSWERED)
    }

    override fun onOutgoingCallRejected(outgoing: Outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_REJECTED)
        if (listener != null) listener!!.onOutgoingCall(outgoing, STATE.REJECTED)
    }

    override fun onOutgoingCallHangup(outgoing: Outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_HANGUP)
        if (listener != null) listener!!.onOutgoingCall(outgoing, STATE.HANGUP)
    }

    override fun onOutgoingCallInvalid(outgoing: Outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_INVALID)
        if (listener != null) listener!!.onOutgoingCall(outgoing, STATE.INVALID)
    }

    override fun mediaMetrics(messageTemplate: HashMap<*, *>) {
        Log.d(TAG, Constants.MEDIAMETRICS)
        Log.i(TAG, messageTemplate.toString())
        if (listener != null) listener!!.mediaMetrics(messageTemplate)
    }

    interface BackendListener {
        fun onLogin(success: Boolean)
        fun onLogout()
        fun onIncomingCall(data: Incoming?, callState: STATE?)
        fun onOutgoingCall(data: Outgoing?, callState: STATE?)
        fun onIncomingDigit(digit: String?)
        fun mediaMetrics(messageTemplate: HashMap<*, *>?)
    }

    companion object {
        private val TAG = PlivoBackEnd::class.java.simpleName
        fun newInstance(): PlivoBackEnd {
            return PlivoBackEnd()
        }
    }
}