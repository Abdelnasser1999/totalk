package com.callberry.callingapp.admob;

import android.content.Context;
import android.view.ViewGroup;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class AdMobUtil {

    private Context context;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;
    private OnInterstitialAdListener mInterstitialAdListener;
    private OnRewardedAdListener mRewardedAdListener;
    private boolean isInterstitialAdLoaded = true;
    private boolean isRewardedAdLoaded = true;

    public AdMobUtil(Context context, String appId) {
        this.context = context;
        MobileAds.initialize(context, appId);
    }

    public void loadBannerAd(ViewGroup container, AdSize adSize, String bannerId) {
        AdView mAdView = new AdView(context);
        mAdView.setAdSize(adSize);
        mAdView.setAdUnitId(bannerId);
        container.addView(mAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void loadNativeAd(String nativeId, OnNativeAdListener listener) {
        AdLoader.Builder builder = new AdLoader.Builder(context, nativeId);
        AdLoader loader = builder.forUnifiedNativeAd(unifiedNativeAd -> {
            TemplateView templateView = listener.onTemplateView();
            templateView.setNativeAd(unifiedNativeAd);
        }).build();
        loader.loadAd(new AdRequest.Builder().build());
    }

    public void showInterstitialAd(OnInterstitialAdListener listener) {
        mInterstitialAdListener = listener;
        if (mInterstitialAd.isLoaded()) {
            isInterstitialAdLoaded = true;
            mInterstitialAd.show();
        } else {
            isInterstitialAdLoaded = false;
        }
    }

    public void initInterstitialAd(String interstitialId) {
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(interstitialId);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (mInterstitialAdListener != null)
                    mInterstitialAdListener.onAdListener(isInterstitialAdLoaded, Interstitial.ON_AD_LOADED);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (mInterstitialAdListener != null)
                    mInterstitialAdListener.onAdListener(isInterstitialAdLoaded, Interstitial.ON_AD_CLICKED);
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (mInterstitialAdListener != null)
                    mInterstitialAdListener.onAdListener(isInterstitialAdLoaded, Interstitial.ON_AD_FAILED_TO_LOAD);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (mInterstitialAdListener != null)
                    mInterstitialAdListener.onAdListener(isInterstitialAdLoaded, Interstitial.ON_AD_LEFT_APP);
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (mInterstitialAdListener != null)
                    mInterstitialAdListener.onAdListener(isInterstitialAdLoaded, Interstitial.ON_AD_OPENED);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                if (mInterstitialAdListener != null)
                    mInterstitialAdListener.onAdListener(isInterstitialAdLoaded, Interstitial.ON_AD_CLOSED);
            }
        });

    }

    public void showRewardedAd(OnRewardedAdListener listener) {
        this.mRewardedAdListener = listener;
        if (mRewardedVideoAd.isLoaded()) {
            isRewardedAdLoaded = true;
            mRewardedVideoAd.show();
        } else {
            isRewardedAdLoaded = false;
        }
        mRewardedAdListener.onAdListener(isRewardedAdLoaded, null);
    }

    public void initRewardedAd(String rewardedId) {
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        mRewardedVideoAd.loadAd(rewardedId, new AdRequest.Builder().build());
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                if (mRewardedAdListener != null)
                    mRewardedAdListener.onAdListener(isRewardedAdLoaded, Rewarded.ON_REWARDED_VIDEO_AD_LOADED);
            }

            @Override
            public void onRewardedVideoAdOpened() {
                if (mRewardedAdListener != null)
                    mRewardedAdListener.onAdListener(isRewardedAdLoaded, Rewarded.ON_REWARDED_VIDEO_AD_OPENED);
            }

            @Override
            public void onRewardedVideoStarted() {
                if (mRewardedAdListener != null)
                    mRewardedAdListener.onAdListener(isRewardedAdLoaded, Rewarded.ON_REWARDED_VIDEO_STARTED);
            }

            @Override
            public void onRewardedVideoAdClosed() {
                mRewardedVideoAd.loadAd(rewardedId, new AdRequest.Builder().build());
                if (mRewardedAdListener != null)
                    mRewardedAdListener.onAdListener(isRewardedAdLoaded, Rewarded.ON_REWARDED_VIDEO_AD_CLOSED);
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                if (mRewardedAdListener != null)
                    mRewardedAdListener.onAdListener(isRewardedAdLoaded, Rewarded.ON_REWARDED);
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                if (mRewardedAdListener != null)
                    mRewardedAdListener.onAdListener(isRewardedAdLoaded, Rewarded.ON_REWARDED_VIDEO_AD_LEFT_APP);
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                if (mRewardedAdListener != null)
                    mRewardedAdListener.onAdListener(isRewardedAdLoaded, Rewarded.ON_REWARDED_VIDEO_AD_FAILED_TO_LOAD);
            }

            @Override
            public void onRewardedVideoCompleted() {
                mRewardedVideoAd.loadAd(rewardedId, new AdRequest.Builder().build());
                if (mRewardedAdListener != null)
                    mRewardedAdListener.onAdListener(isRewardedAdLoaded, Rewarded.ON_REWARDED_AD_COMPLETED);

            }
        });
    }

    public void destroy() {
        mRewardedVideoAd.destroy(context);
        mRewardedVideoAd.destroy(context);
    }

}
