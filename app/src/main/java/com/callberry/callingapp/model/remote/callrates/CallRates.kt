package com.callberry.callingapp.model.remote.callrates

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CallRates {
    @SerializedName("callRates")
    @Expose
    var callRates: List<CallRate>? = null

}