package com.callberry.callingapp.model.prefmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GoogleAdmob {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("company")
    @Expose
    var company: String? = null

    @SerializedName("app_id")
    @Expose
    var appId: String? = null

    @SerializedName("banner_id")
    @Expose
    var bannerId: String? = null

    @SerializedName("native_id")
    @Expose
    var nativeId: String? = null

    @SerializedName("rewarded_id")
    @Expose
    var rewardedId: String? = null

    @SerializedName("inersitial_id")
    @Expose
    var inersitialId: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    constructor(
        appId: String, bannerId: String, nativeId: String,
        rewardedId: String, inersitialId: String
    ) {
        this.appId = appId
        this.bannerId = bannerId
        this.nativeId = nativeId
        this.rewardedId = rewardedId
        this.inersitialId = inersitialId
    }

}