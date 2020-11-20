package com.callberry.callingapp.model.prefmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Sinch {

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

    @SerializedName("user_id")
    @Expose
    var userId: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    constructor(appKey: String, appSecret: String, environment: String, userId: String) {
        this.appKey = appKey
        this.appSecret = appSecret
        this.environment = environment
        this.userId = userId
    }

}