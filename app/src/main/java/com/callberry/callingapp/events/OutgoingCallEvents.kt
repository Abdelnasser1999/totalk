package com.callberry.callingapp.events

class OutgoingCallEvents(val callStatus: String) {

    companion object {
        const val CALL_STARTED = "CALL_STARTED"
        const val CALL_ENDED = "CALL_ENDED"
        const val CLINT_FAILED = "CLIENT_FAILED"
        const val CLIENT_STARTED = "CLIENT_STARTED"
    }
}