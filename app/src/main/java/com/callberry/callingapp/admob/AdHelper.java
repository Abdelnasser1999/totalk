
package com.callberry.callingapp.admob;

import android.content.Context;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdSize;

public class AdHelper {

    private static AdMobUtil admobUtil;
    private static AdUnitHelper adUnitHelper;

    public static AdUnitHelper initAdMob(Context context, String appId) {
        admobUtil = new AdMobUtil(context, appId);
        return new AdUnitHelper();
    }

    public void loadAds(AdUnitHelper adUnits) {
        adUnitHelper = adUnits;
        if (adUnitHelper.getInterstitialId() != null)
            admobUtil.initInterstitialAd(adUnits.getInterstitialId());
        if (adUnitHelper.getRewardedId() != null)
            admobUtil.initRewardedAd(adUnits.getRewardedId());

    }

    public static void loadBannerAd(ViewGroup container, AdSize adSize) {
        if (adUnitHelper.getBannerId() != null) {
            admobUtil.loadBannerAd(container, adSize, adUnitHelper.getBannerId());
        }
    }

    public static void loadNativeAd(OnNativeAdListener listener) {
        if (adUnitHelper.getNativeId() != null) {
            admobUtil.loadNativeAd(adUnitHelper.getNativeId(), listener);
        }
    }

    public static void showInterstitialAd(OnInterstitialAdListener listener) {
        admobUtil.showInterstitialAd(listener);
    }

    public static void showRewardedAd(OnRewardedAdListener listener) {
        admobUtil.showRewardedAd(listener);
    }

    private static int getMinutes(long milliseconds1, long milliseconds2) {
        long diff = milliseconds2 - milliseconds1;
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);
        return (int) diffMinutes;
    }
}
