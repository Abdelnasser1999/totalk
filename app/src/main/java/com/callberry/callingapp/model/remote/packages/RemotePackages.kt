package com.callberry.callingapp.model.remote.packages

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RemotePackages {
    @SerializedName("packages")
    @Expose
    var packages: List<RemotePackage>? = null

}