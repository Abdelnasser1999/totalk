package com.callberry.callingapp.model.remote.account

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.callberry.callingapp.model.Account

class RemoteAccount {
    @SerializedName("token")
    @Expose
    var token: String? = null

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("account")
    @Expose
    var account: Account? = null

    @SerializedName("flag")
    @Expose
    var flag: Int? = null

}