package com.callberry.callingapp.model.remote.sinch

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class SinchCredentials {
    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("sinch")
    @Expose
    var sinch: Sinch? = null

}