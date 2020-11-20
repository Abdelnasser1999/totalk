package com.callberry.callingapp.model.prefmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FacebookAd {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("fb_BannerAd")
    @Expose
    var fbBannerAd: String? = null

    @SerializedName("fb_RewardedAd")
    @Expose
    var fbRewardedAd: String? = null

    @SerializedName("fb_InterstitialAd")
    @Expose
    var fbInterstitialAd: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    constructor(fbBannerAd: String, fbRewardedAd: String, fbInterstialAd: String) {
        this.fbBannerAd = fbBannerAd
        this.fbRewardedAd = fbRewardedAd
        this.fbInterstitialAd = fbInterstialAd
    }

}