package com.callberry.callingapp.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TimeModel {
    @SerializedName("date_time")
    @Expose
    var dateTime: String? = null

    @SerializedName("timestamp")
    @Expose
    var timestamp: Int? = null
}