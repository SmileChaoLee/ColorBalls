package com.smile.smilelibraries.google_admob_ads_util;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class GoogleAdMobInterstitial {

    private final String TAG = new String("google_admob_ads_util.GoogleAdMobInterstitial");

    private Context context;
    private InterstitialAd mInterstitialAd;

    private boolean isLoaded;
    private boolean isDisplayed;
    private boolean isDismissed;
    private boolean isError;


    public GoogleAdMobInterstitial(Context context, String interstitialID) {

        this.context = context;
        mInterstitialAd = new InterstitialAd(this.context);
        mInterstitialAd.setAdUnitId(interstitialID);

        isLoaded = false;
        isDisplayed = false;
        isDismissed = false;
        isError = false;

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.e(TAG, "Interstitial ad loaded.");
                isLoaded = true;
                isDisplayed = false;
                isDismissed = false;
                isError = false;
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e(TAG, "Interstitial ad failed to load.");
                isLoaded = false;
                isDisplayed = false;
                isDismissed = false;
                isError = true;
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.e(TAG, "Interstitial ad displayed.");
                isLoaded = true;
                isDisplayed = true;
                isDismissed = false;
                isError = false;
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.e(TAG, "User left application.");
                isLoaded = false;
                isDisplayed = false;
                isDismissed = false;
                isError = false;
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                Log.e(TAG, "Interstitial ad dismissed.");
                isLoaded = true;
                isDisplayed = true;
                isDismissed = true;
                isError = false;
                AdRequest adRequest = new AdRequest.Builder().build();
                mInterstitialAd.loadAd(adRequest);
            }
        });
    }
    public void loadAd() {
        if ( !mInterstitialAd.isLoaded() && !mInterstitialAd.isLoading() ) {
            isLoaded = false;
            isDismissed = false;
            isDisplayed = false;
            isError = false;
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }
    public boolean showAd() {
        boolean succeeded = false;

        isDismissed = false;
        isDisplayed = false;
        if (mInterstitialAd.isLoaded()) {
            isLoaded = true;
            isError = false;
            mInterstitialAd.show();
            succeeded = true;
        } else {
            isLoaded = false;
            isError = true;
        }

        return succeeded;
    }

    public boolean isLoaded() {
        return mInterstitialAd.isLoaded();
    }
    public boolean isLoading() {
        return mInterstitialAd.isLoading();
    }
    public boolean adsShowDisplayedOrStopped() {
        return (isDisplayed || (isError));
    }
    public boolean adsShowDismissedOrStopped() {
        return (isDismissed || (isError));
    }
}
