package com.callberry.callingapp.admob;

public class AdUnitHelper {
    private String bannerId = null;
    private String interstitialId = null;
    private String nativeId = null;
    private String rewardedId = null;

    public AdUnitHelper() {
    }

    public AdUnitHelper(String bannerId, String interstitialId, String nativeId, String rewardedId) {
        this.bannerId = bannerId;
        this.interstitialId = interstitialId;
        this.nativeId = nativeId;
        this.rewardedId = rewardedId;
    }

    public AdUnitHelper setBannerId(String bannerId) {
        this.bannerId = bannerId;
        return this;
    }

    public AdUnitHelper setInterstitialId(String interstitialId) {
        this.interstitialId = interstitialId;
        return this;
    }

    public AdUnitHelper setNativeId(String nativeId) {
        this.nativeId = nativeId;
        return this;
    }

    public AdUnitHelper setRewardedId(String rewardedId) {
        this.rewardedId = rewardedId;
        return this;
    }

    public String getBannerId() {
        return bannerId;
    }

    public String getInterstitialId() {
        return interstitialId;
    }

    public String getNativeId() {
        return nativeId;
    }

    public String getRewardedId() {
        return rewardedId;
    }

    public AdHelper load() {
        AdHelper adHelper = new AdHelper();
        adHelper.loadAds(new AdUnitHelper(bannerId, interstitialId, nativeId, rewardedId));
        return adHelper;
    }
}
