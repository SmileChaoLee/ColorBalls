package com.smile.facebookadsutil;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.*;

public class FacebookAds {

    private static final int MAX_NUMBER_OF_RETRIES = 3;
    private static boolean shouldLoadAd = true;

    private final Context context;
    private final String TAG = new String("FacebookAdsUtil");

    private InterstitialAd interstitialAd;
    private int retryCount = 0;

    public FacebookAds(final Context context, String placementID) {

        this.context = context;

        // for facebook ads
        // Instantiate an InterstitialAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        // interstitialAd = new InterstitialAd(context, "200699663911258_200701030577788");
        interstitialAd = new InterstitialAd(context, placementID);
        // 241884113266033_241884616599316 for 五色球 color balls colorballs

        // Set listeners for the Interstitial Ad
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                shouldLoadAd = false;
                retryCount = 0;
                Log.e(TAG, "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                shouldLoadAd = true;
                retryCount = 0;
                Log.e(TAG, "Interstitial ad dismissed.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                // Ad error callback
                // Stop retrying when it reaches to MAX_NUMBER_OF_RETRIES
                if(retryCount < FacebookAds.MAX_NUMBER_OF_RETRIES) {
                    shouldLoadAd = false;
                    retryCount += 1;
                    interstitialAd.loadAd();
                } else {
                    // no more trying so can do next ad showing
                    shouldLoadAd = true;
                }
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                shouldLoadAd = false;
                interstitialAd.show();
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
                shouldLoadAd = false;
                Log.d(TAG, "Interstitial ad impression logged!");
            }
        });
    }

    public void showAd(String callingObject) {
        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        Log.e(TAG, callingObject + " calling showAd() method.");
        if (shouldLoadAd) {
            Log.e(TAG, callingObject + " loading Ad now.");
            interstitialAd.loadAd();
        }
    }

    public void close() {
        // destroy the instance of facebook ads
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
    }
}
