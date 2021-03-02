package com.callberry.callingapp.util

import android.util.Log
import com.callberry.callingapp.model.remote.credits.RemoteCredit


class Constants {
    companion object {
        val IS_HOLD_ON: String = "is_hold_on"
        const val ANSWER_ACTION = "ANSWER_ACTION"
        const val REJECT_ACTION = "REJECT_ACTION"
        const val LAUNCH_ACTION = "LAUNCH_ACTION"
        const val NOTIFICATION_ID = 0
        const val RINGING_LABEL = "Ringing..."
        const val NOTIFICATION_CHANNEL = "PlivoVoiceQuickStart"
        const val NOTIFICATION_DESCRIPTION = "Incoming call"
        const val OUTGOING_CALL_DIAL_HINT = "Enter sip uri or phone number"
        const val LOGGED_IN_LABEL = "Logged in as:"
        const val LOGIN_SUCCESS = "onLogin success"
        const val LOGOUT_SUCCESS = "onLogout success"
        const val LOGIN_FAILED = "onLoginFailed"
        const val INCOMING_CALL_RINGING = "onIncomingCall Ringing"
        const val INCOMING_CALL_HANGUP = "onIncomingCallHangup"
        const val INCOMING_CALL_REJECTED = "onIncomingCallRejected"
        const val OUTGOING_CALL_RINGING = "onOutgoingCall Ringing"
        const val OUTGOING_CALL_ANSWERED = "onOutgoingCall Answered"
        const val OUTGOING_CALL_REJECTED = "onOutgoingCall Rejected"
        const val OUTGOING_CALL_HANGUP = "onOutgoingCall Hangup"
        const val OUTGOING_CALL_INVALID = "onOutgoingCall Invalid"
        const val MEDIAMETRICS = "mediaMetrics called"
        const val DEMO_CALL_PHONE_N0 = "+923085386652"
        const val TOKEN = "TOKEN"
        const val ACCOUNT = "account"
        const val LAST_BONUS_TIME = "LAST_BONUS_TIME"
        const val CURRENT_BALANCE = "CURRENT_BALANCE"
        const val IS_ACCOUNT_SETUP_SUCCESSFUL = "IS_ACCOUNT_SETUP_SUCCESSFUL"
        const val STATE_CONNECTING = "Connecting..."
        const val STATE_RINGING = "Ringing..."
        const val STATE_ANSWERED = "ANSWERED"


        const val BONUS_CREDIT = "BONUS_CREDIT"
        const val CHECK_IN_CREDIT = "CHECK_IN_CREDIT"
        const val WATCH_VIDEOS_CREDIT = "WATCH_VIDEOS_CREDIT"
        const val REFERRAL_CREDIT = "REFERRAL_CREDIT"
        const val CREDIT = "CREDIT"
        const val DEBIT = "DEBIT"

        const val IS_AUTO_SYNC_ENABLED = "IS_AUTO_SYNC_ENABLED"
        const val NOTIFICATION_INTENT = "NOTIFICATION_INTENT"

        const val PHONE_NO = "phone_no"
        const val CONTACT = "contact"
        const val TIME_IN_SECONDS = "time_in_seconds"
        const val CALL_STARTED_TIME = "call_started_time"
        const val CALL_RATE = "call_rate"
        const val RECENT = "RECENT"
        const val HAS_PERMISSION = "HAS_PERMISSION"
        const val IS_BONUS_NOTIFICATION_ENABLED = "IS_BONUS_NOTIFICATION_ENABLED"
        const val IS_CALL_IN_PROGRESS = "IS_CALL_IN_PROGRESS"
        const val IS_SPREAKER_ON = "IS_SPEAKER_ON"
        const val IS_MUTE_ON = "IS_MUTE_ON"
        const val OUTGOING_CALL = "OUTGOING_CALL"
        const val STOPFOREGROUND_ACTION = "com.action.stop.service"
        const val STARTFOREGROUND_ACTION = "com.action.start.service"
        const val IS_SETUP_COMPLETE = "IS_SETUP_COMPLETE"
        const val LAST_SYNC = "LAST_SYNC"

        val defaultCreditsLimits = arrayListOf<RemoteCredit>(
            RemoteCredit(BONUS_CREDIT, "0.1, 0.001"),
            RemoteCredit(CHECK_IN_CREDIT, "1, 0.01"),
            RemoteCredit(WATCH_VIDEOS_CREDIT, "2, 0.1")
        )

        const val CALL_RATES_LOGS = "callRatesLogs"
        const val ERROR_LOGS = "errorLogs"
        const val LOGS_CALLS = "callLogs"

        @JvmStatic
        fun callLogs(msg: String) {
            Log.d(LOGS_CALLS, msg)
        }


    }
}