package com.callberry.callingapp.service

interface CallStateChangeListener {
    fun onStateChange(state: LocalCallState);
}