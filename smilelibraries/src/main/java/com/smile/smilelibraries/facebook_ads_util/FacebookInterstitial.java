package com.smile.smilelibraries.facebook_ads_util;

import android.content.Context;
import android.util.Log;
import com.facebook.ads.*;
import com.smile.smilelibraries.interfaces.DismissFunction;

public class FacebookInterstitial {

    private final String TAG = "FacebookInterstitial";
    private final InterstitialAd interstitialAd;
    private boolean isDismissed;
    private DismissFunction mDismissFunction = null;

    private boolean isError;

    public FacebookInterstitial(final Context context, String placementID) {
        isError = false;
        InterstitialAdListener adListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.i(TAG, "Interstitial ad displayed.");
                isError = false;
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.i(TAG, "Interstitial ad dismissed.");
                isError = false;
                isDismissed = true;
                interstitialAd.loadAd();    // load next ad
                if (mDismissFunction != null) {
                    mDismissFunction.executeDismiss();
                    mDismissFunction = null;    // one time only
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e(TAG, "Interstitial ad failed to load: " + interstitialAd.isAdLoaded() + " - " + adError.getErrorMessage());
                isError = true;
                interstitialAd.loadAd();    // load next ad
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.i(TAG, "Interstitial ad is loaded and ready to be displayed!");
                isError = false;
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.i(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                // after displayed
                Log.i(TAG, "Interstitial ad impression logged!");
            }
        };

        interstitialAd = new InterstitialAd(context, placementID);
        interstitialAd.loadAd(interstitialAd.buildLoadAdConfig() // LoadConfigBuilder
                .withBid(placementID)
                .withAdListener(adListener)
                .withCacheFlags(CacheFlag.ALL)
                .build()); // builds LoadConfig
    }
    public void loadAd() {
        interstitialAd.loadAd();
        isError = false;
    }
    public boolean showAd() {
        boolean succeeded = false;
        isDismissed = false;
        if (interstitialAd.isAdLoaded()) {
            isError = false;
            interstitialAd.show();
            succeeded = true;
        } else {
            isError = true;
        }
        return succeeded;
    }
    public boolean isLoaded() {
        return interstitialAd.isAdLoaded();
    }

    public boolean isDismissed() {
        return isDismissed;
    }

    public void close() {
        // destroy the instance of facebook ads
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
    }

    public void setDismissFunc(DismissFunction dismissFunc) {
        mDismissFunction = dismissFunc;
    }
}
