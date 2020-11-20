package com.callberry.callingapp.model.prefmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Package {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("value")
    @Expose
    var value: Int? = null

    constructor(name: String, value: Int) {
        this.name = name
        this.value = value
    }

}