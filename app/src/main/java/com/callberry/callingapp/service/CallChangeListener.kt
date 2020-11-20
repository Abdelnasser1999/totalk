package com.callberry.callingapp.service

interface CallChangeListener {
    fun onCallEstablished()

    fun onCallHangup()

    fun onClientFailed()
}