package com.callberry.callingapp.plivo

import com.plivo.endpoint.Outgoing

interface PlivoEventListener {
    fun onInitialization(success: Boolean)
    fun onCallStateChange(data: Outgoing?, callState: STATE)
}