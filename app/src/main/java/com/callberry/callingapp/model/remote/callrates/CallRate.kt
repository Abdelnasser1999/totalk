package com.callberry.callingapp.model.remote.callrates

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CallRate {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("iso")
    @Expose
    var iso: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("dialcode")
    @Expose
    var dialcode: String? = null

    @SerializedName("cost")
    @Expose
    var cost: String? = null

    @SerializedName("call_rate")
    @Expose
    var callRate: String? = null

    @SerializedName("flag_image")
    @Expose
    var flag: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: Any? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: Any? = null

}