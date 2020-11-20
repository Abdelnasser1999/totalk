package com.callberry.callingapp.model.remote.account

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RemoteAccounts {
    @SerializedName("remote_user")
    @Expose
    var remoteUser: RemoteAccount? = null

}