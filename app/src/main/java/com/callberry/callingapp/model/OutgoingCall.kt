package com.callberry.callingapp.model

import com.callberry.callingapp.util.Constants

class OutgoingCall {
    var contactId: Int = 0
    var name: String = ""
    var phoneNo: String = ""
    var dialCode: String = ""
    var theme: String = ""
    var callRate: Float = 0.0f
    var callState: String = Constants.STATE_CONNECTING
    var callStarted = false
    var startedTime: Long = 0
    var currentTimeInSec: Long = 0
    var callInitiated: Boolean = false
    var isSpakerON: Boolean = false
    var isMuteON: Boolean = false
    var isHoldON: Boolean = false

}