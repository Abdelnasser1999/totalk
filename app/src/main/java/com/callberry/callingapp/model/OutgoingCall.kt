package com.callberry.callingapp.model

class OutgoingCall() {
    var contactId: Int = 0
    var name: String = ""
    var phoneNo: String = ""
    var dialCode: String = ""
    var theme: String = ""
    var isCallStarted = false
    var callStartedTime: Long = 0
    var callRate: Float = 0.0f
    var isSpeakerOn: Boolean = false
    var isMuteOn: Boolean = false

}