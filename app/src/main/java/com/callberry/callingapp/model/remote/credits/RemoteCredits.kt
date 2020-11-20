package com.callberry.callingapp.model.remote.credits

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RemoteCredits {
    @SerializedName("remote_credits")
    @Expose
    var remoteCredits: List<RemoteCredit>? = null

}