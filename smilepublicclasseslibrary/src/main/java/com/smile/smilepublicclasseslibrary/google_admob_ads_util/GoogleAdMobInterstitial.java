package com.smile.smilepublicclasseslibrary.google_admob_ads_util;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

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
    public void showAd() {
        isDismissed = false;
        isDisplayed = false;
        if (mInterstitialAd.isLoaded()) {
            isLoaded = true;
            isError = false;
            mInterstitialAd.show();
        } else {
            isLoaded = false;
            isError = true;
            if (!mInterstitialAd.isLoading()) {
                // if not loading ad, then loadAd() again
                loadAd();
            }
        }
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

    // interface for showing Google AdMob ads
    public interface AfterDismissFunctionOfShowGoogleAdMobAd {
        void executeAfterDismissAds(int endPoint);
    }
    public class ShowGoogleAdMobAdAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final AppCompatActivity activity;
        private final int endPoint;
        private final AfterDismissFunctionOfShowGoogleAdMobAd afterDismissFunction;

        public ShowGoogleAdMobAdAsyncTask(final AppCompatActivity activity, final int endPoint) {
            this.activity = activity;
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
        }

        public ShowGoogleAdMobAdAsyncTask(final AppCompatActivity activity, final int endPoint, final AfterDismissFunctionOfShowGoogleAdMobAd afterDismissFunction) {

            this.activity = activity;
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
        }

        @Override
        protected void onPreExecute() {
            showAd();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final int timeDelay = 300;
            int i = 0;
            while (!adsShowDismissedOrStopped()) {
                SystemClock.sleep(timeDelay);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (afterDismissFunction != null) {
                afterDismissFunction.executeAfterDismissAds(endPoint);
            }
        }
    }
}
