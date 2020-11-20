package com.callberry.callingapp.model.prefmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PrefSettings {
    @SerializedName("sinch")
    @Expose
    var sinch: Sinch? = null

    @SerializedName("google_billing")
    @Expose
    var googleBilling: GoogleBilling? = null

    @SerializedName("google_admob")
    @Expose
    var googleAdmob: GoogleAdmob? = null

    @SerializedName("facebook_ad")
    @Expose
    var facebookAd: FacebookAd? = null

    @SerializedName("remote_credits")
    @Expose
    var remoteCredits: List<RemoteCredit>? =
        null

    @SerializedName("packages")
    @Expose
    var remotePackages: List<Package>? = null

}