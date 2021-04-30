package com.smile.smilelibraries.google_admob_ads_util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class GoogleAdMobInterstitial {

    private final String TAG = new String("google_admob_ads_util.GoogleAdMobInterstitial");
    private final Context mContext;
    private final String mInterstitialID;
    private boolean isDisplayed;
    private boolean isDismissed;
    private boolean isError;

    private InterstitialAd mInterstitialAd;

    private final FullScreenContentCallback mFullScreenContentCallback = new FullScreenContentCallback() {
        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            super.onAdFailedToShowFullScreenContent(adError);
            // Code to be executed when the ad failed to display.
            Log.e(TAG, "Interstitial ad failed to display.");
            isDisplayed = false;
            isDismissed = false;
            isError = true;
            loadAd();   // load next ad
        }

        @Override
        public void onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent();
            // Code to be executed when the ad is displayed.
            Log.e(TAG, "Interstitial ad displayed.");
            isDisplayed = true;
            isDismissed = false;
            isError = false;
        }

        @Override
        public void onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent();
            // Code to be executed when when the interstitial ad is closed.
            Log.e(TAG, "Interstitial ad dismissed.");
            isDisplayed = true;
            isDismissed = true;
            isError = false;
            loadAd();   // load next ad
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();
            Log.e(TAG, "Interstitial ad impression.");
        }
    };

    private final InterstitialAdLoadCallback mInterstitialAdLoadCallback = new InterstitialAdLoadCallback() {
        @Override
        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
            // The mInterstitialAd reference will be null until
            // an ad is loaded.
            Log.e(TAG, "Interstitial onAdLoaded");
            isDisplayed = false;
            isDismissed = false;
            isError = false;
            mInterstitialAd = interstitialAd;
            mInterstitialAd.setFullScreenContentCallback(mFullScreenContentCallback);
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            // Handle the error
            Log.e(TAG, "Interstitial onAdFailedToLoad");
            Log.i(TAG, loadAdError.getMessage());
            isDisplayed = false;
            isDismissed = false;
            isError = true;
            mInterstitialAd = null;
        }
    };

    public GoogleAdMobInterstitial(Context context, String interstitialID) {
        mContext = context;
        mInterstitialID = interstitialID;
        isDisplayed = false;
        isDismissed = false;
        isError = false;
    }

    public void loadAd() {
        isDismissed = false;
        isDisplayed = false;
        isError = false;
        mInterstitialAd = null; // set to null to begin to load next ad
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(mContext, mInterstitialID, adRequest, mInterstitialAdLoadCallback);
    }

    public boolean showAd(Activity activity) {
        boolean succeeded = false;
        isDismissed = false;
        isDisplayed = false;
        if (mInterstitialAd != null) {
            isError = false;
            mInterstitialAd.show(activity);
            succeeded = true;
        } else {
            isError = true;
        }

        return succeeded;
    }

    public boolean isLoaded() {
        return (mInterstitialAd != null);
    }
    public boolean isLoading() {
        return (mInterstitialAd == null);
    }
    public boolean adsShowDisplayedOrStopped() {
        return (isDisplayed || (isError));
    }
    public boolean adsShowDismissedOrStopped() {
        return (isDismissed || (isError));
    }
}
