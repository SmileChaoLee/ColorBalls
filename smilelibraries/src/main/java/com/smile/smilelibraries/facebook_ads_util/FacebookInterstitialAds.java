package com.smile.smilelibraries.facebook_ads_util;

import android.content.Context;
import android.util.Log;
import com.facebook.ads.*;

public class FacebookInterstitialAds {

    private final String TAG = "FacebookInterstitialAds";
    private final InterstitialAd interstitialAd;
    // private boolean isDisplayed;
    // private boolean isDismissed;
    private boolean isError;

    public FacebookInterstitialAds(final Context context, String placementID) {
        // isDisplayed = false;
        // isDismissed = false;
        isError = false;

        InterstitialAdListener adListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.");
                // isDisplayed = true;
                // isDismissed = false;
                isError = false;
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.");
                // isDisplayed = true;
                // isDismissed = true;
                isError = false;
                interstitialAd.loadAd();    // load next ad
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e(TAG, "Interstitial ad failed to load: " + interstitialAd.isAdLoaded() + " - " + adError.getErrorMessage());
                // isDisplayed = false;
                // isDismissed = false;
                isError = true;
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // isDisplayed = false;
                // isDismissed = false;
                isError = false;
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                // after displayed
                Log.d(TAG, "Interstitial ad impression logged!");
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
        if (!interstitialAd.isAdLoaded()) {
            // isDismissed = false;
            // isDisplayed = false;
            isError = false;
            interstitialAd.loadAd();
        }
    }
    public boolean showAd() {
        boolean succeeded = false;

        // isDisplayed = false;
        // isDismissed = false;
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

    public boolean isError() {
        return isError;
    }

    /*
    public boolean adsShowDisplayedOrStopped() {
        return (isDisplayed || (isError));
    }
    public boolean adsShowDismissedOrStopped() {
        return (isDismissed || (isError));
    }
    */

    public void close() {
        // destroy the instance of facebook ads
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
    }
}
