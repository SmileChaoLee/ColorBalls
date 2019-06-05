package com.smile.smilelibraries.facebook_ads_util;

import android.content.Context;
import android.util.Log;
import com.facebook.ads.*;

public class FacebookInterstitialAds {

    private final String TAG = new String("facebook_ads_util.FacebookInterstitialAds");

    private final Context context;
    private InterstitialAd interstitialAd;

    private boolean isLoaded;
    private boolean isDisplayed;
    private boolean isDismissed;
    private boolean isClicked;
    private boolean isLoggingImpression;
    private boolean isError;

    public FacebookInterstitialAds(final Context context, String placementID) {

        this.context = context;

        isLoaded = false;
        isDisplayed = false;
        isDismissed = false;
        isClicked = false;
        isError = false;

        interstitialAd = new InterstitialAd(this.context, placementID);

        // Set listeners for the Interstitial Ad
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.");
                isLoaded = true;
                isDisplayed = true;
                isDismissed = false;
                isError = false;
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.");
                isLoaded = true;
                isDisplayed = true;
                isDismissed = true;
                isError = false;
                interstitialAd.loadAd();    // load next ad
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e(TAG, "Interstitial ad failed to load: " + interstitialAd.isAdLoaded() + " - " + adError.getErrorMessage());
                isLoaded = false;
                isDisplayed = false;
                isDismissed = false;
                isError = true;
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                isLoaded = true;
                isDisplayed = false;
                isDismissed = false;
                isClicked = false;
                isError = false;
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                isClicked = true;
                Log.d(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                // after displayed
                isLoggingImpression = true;
                Log.d(TAG, "Interstitial ad impression logged!");
            }
        });
    }
    public void loadAd() {
        if (!interstitialAd.isAdLoaded()) {
            isLoaded = false;
            isDismissed = false;
            isDisplayed = false;
            isError = false;
            interstitialAd.loadAd();
        }
    }
    public boolean showAd() {
        boolean succeeded = false;

        isDisplayed = false;
        isDismissed = false;
        if (interstitialAd.isAdLoaded()) {
            isLoaded = true;
            isClicked = false;
            isLoggingImpression = false;
            isError = false;
            interstitialAd.show();
            succeeded = true;
        } else {
            isLoaded = false;
            isClicked = false;
            isLoggingImpression = false;
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

    public boolean adsShowDisplayedOrStopped() {
        return (isDisplayed || (isError));
    }
    public boolean adsShowDismissedOrStopped() {
        return (isDismissed || (isError));
    }

    public void close() {
        // destroy the instance of facebook ads
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
    }
}
