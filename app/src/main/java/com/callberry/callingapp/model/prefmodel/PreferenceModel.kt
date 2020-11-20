package com.callberry.callingapp.model.prefmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PreferenceModel {
    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("pref_settings")
    @Expose
    var prefSettings: PrefSettings? = null

}