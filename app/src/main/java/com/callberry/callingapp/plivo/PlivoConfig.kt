package com.callberry.callingapp.plivo

import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.PrefUtils

object PlivoConfig {
    const val USERNAME = "phonecall6793321251323498155321"
    const val PASSWORD = "appexes"
    const val HH_MM_SS = "%02d:%02d:%02d"
    const val MM_SS = "%02d:%02d"

    @JvmStatic
    fun getOutgoingCall(): OutgoingCall {
        return PrefUtils.get(Constants.OUTGOING_CALL, OutgoingCall::class) as OutgoingCall
    }

    @JvmStatic
    fun setCallInitiated() {
        val call = getOutgoingCall()
        call.callInitiated = true
        setOutgoingCall(call)
    }

    @JvmStatic
    fun setCallSate(callSate: String) {
        val call = getOutgoingCall()
        call.callState = callSate
        setOutgoingCall(call)
    }

    @JvmStatic
    fun setCallStarted() {
        val call = getOutgoingCall()
        call.callStarted = true
        call.startedTime = System.currentTimeMillis()
        setOutgoingCall(call)
    }

    @JvmStatic
    fun setCurrentTime(timeInSec: Long) {
        val call = getOutgoingCall()
        call.currentTimeInSec = timeInSec
        setOutgoingCall(call)
    }

    @JvmStatic
    fun setSpeakerMode(speaker: Boolean) {
        val call = getOutgoingCall()
        call.isSpakerON = speaker
        setOutgoingCall(call)
    }

    @JvmStatic
    fun setHoldMode(hold: Boolean) {
        val call = getOutgoingCall()
        call.isHoldON = hold
        setOutgoingCall(call)
    }

    @JvmStatic
    fun setMuteMode(mute: Boolean) {
        val call = getOutgoingCall()
        call.isMuteON = mute
        setOutgoingCall(call)
    }

    @JvmStatic
    fun setOutgoingCall(outgoingCall: OutgoingCall) {
        PrefUtils.set(Constants.OUTGOING_CALL, outgoingCall)
    }

    @JvmStatic
    fun isCallInProgress(): Boolean {
        return PrefUtils.contains(Constants.OUTGOING_CALL)
    }

    @JvmStatic
    fun endOutgoingCall() {
        if (PrefUtils.contains(Constants.OUTGOING_CALL))
            PrefUtils.remove(Constants.OUTGOING_CALL)
    }
}