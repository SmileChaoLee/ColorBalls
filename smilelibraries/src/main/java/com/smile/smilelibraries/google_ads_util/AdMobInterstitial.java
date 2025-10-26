package com.smile.smilelibraries.google_ads_util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.smile.smilelibraries.interfaces.DismissFunction;

public class AdMobInterstitial {

    private final String TAG = "AdMobInterstitial";
    private final long TWO_MINUTES = 120000L;    // 120 seconds
    private final long TEN_MINUTES = 600000L;    // 600 seconds

    private final Context mContext;
    private final String mInterstitialID;
    private InterstitialAd mInterstitialAd;
    private boolean isDismissed;
    private DismissFunction mDismissFunction = null;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            timerHandler.removeCallbacksAndMessages(null);
            loadAd();
            timerHandler.postDelayed(this, TEN_MINUTES);
        }
    };

    private final FullScreenContentCallback mFullScreenContentCallback = new FullScreenContentCallback() {
        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            super.onAdFailedToShowFullScreenContent(adError);
            // Code to be executed when the ad failed to display.
            Log.d(TAG, "Interstitial ad failed to display.");
            timerHandler.removeCallbacksAndMessages(null);
            timerHandler.postDelayed(timerRunnable, TWO_MINUTES);
        }

        @Override
        public void onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent();
            // Code to be executed when the ad is displayed.
            Log.d(TAG, "Interstitial ad displayed.");
        }

        @Override
        public void onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent();
            // Code to be executed when when the interstitial ad is closed.
            Log.d(TAG, "Interstitial ad dismissed.");
            isDismissed = true;
            timerHandler.removeCallbacksAndMessages(null);
            timerHandler.postDelayed(timerRunnable, TEN_MINUTES);
            if (mDismissFunction != null) {
                mDismissFunction.executeDismiss();
            }
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();
            Log.d(TAG, "Interstitial ad impression.");
        }
    };

    private final InterstitialAdLoadCallback mInterstitialAdLoadCallback = new InterstitialAdLoadCallback() {
        @Override
        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
            // The mInterstitialAd reference will be null until
            // an ad is loaded.
            Log.d(TAG, "Interstitial onAdLoaded");
            mInterstitialAd = interstitialAd;
            mInterstitialAd.setFullScreenContentCallback(mFullScreenContentCallback);
            timerHandler.removeCallbacksAndMessages(null);
            timerHandler.postDelayed(timerRunnable, TEN_MINUTES);
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            // Handle the error
            Log.d(TAG, "Interstitial onAdFailedToLoad, " + loadAdError.getMessage());
            timerHandler.removeCallbacksAndMessages(null);
            timerHandler.postDelayed(timerRunnable, TWO_MINUTES);
        }
    };

    public AdMobInterstitial(Context context, String interstitialID) {
        mContext = context;
        mInterstitialID = interstitialID;
    }
    public void loadAd() {
        mInterstitialAd = null; // set to null to begin to load next ad
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(mContext, mInterstitialID, adRequest, mInterstitialAdLoadCallback);
    }

    public boolean showAd(Activity activity) {
        boolean succeeded = false;
        isDismissed = false;
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
            succeeded = true;
        }
        return succeeded;
    }

    public boolean isLoaded() {
        return (mInterstitialAd != null);
    }

    public boolean isDismissed() {
        return isDismissed;
    }

    public void setDismissFunc(DismissFunction dismissFunc) {
        mDismissFunction = dismissFunc;
    }
}
