package com.callberry.callingapp.model.remote

import androidx.room.Ignore
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BillingPackage() {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("app_key")
    @Expose
    var appKey: String? = null

    @SerializedName("app_secret")
    @Expose
    var appSecret: String? = null

    @SerializedName("environment")
    @Expose
    var environment: String? = null


    @Ignore
    constructor(appKey: String, appSecret: String, environment: String) : this() {
        this.appKey = appKey
        this.appSecret = appSecret
        this.environment = environment
    }
}