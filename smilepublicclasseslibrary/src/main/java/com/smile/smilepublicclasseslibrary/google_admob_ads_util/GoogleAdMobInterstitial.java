package com.smile.smilepublicclasseslibrary.google_admob_ads_util;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.smile.smilepublicclasseslibrary.alertdialogfragment.AlertDialogFragment;

public class GoogleAdMobInterstitial {

    private InterstitialAd mInterstitialAd;

    private boolean isLoaded;
    private boolean isDisplayed;
    private boolean isDismissed;
    private boolean isError;


    public GoogleAdMobInterstitial(Context context, String interstitialID) {
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(interstitialID);

        isLoaded = false;
        isDisplayed = false;
        isDismissed = false;
        isError = false;

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                isLoaded = true;
                isDisplayed = false;
                isDismissed = false;
                isError = false;
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                isLoaded = false;
                isDisplayed = false;
                isDismissed = false;
                isError = true;
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                isLoaded = true;
                isDisplayed = true;
                isDismissed = false;
                isError = false;
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                mInterstitialAd = null;
                isLoaded = false;
                isDisplayed = false;
                isDismissed = false;
                isError = false;
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                isLoaded = true;
                isDisplayed = true;
                isDismissed = true;
                isError = false;
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }
    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        isLoaded = false;
        isDismissed = false;
        isDisplayed = false;
        isError = false;
        mInterstitialAd.loadAd(adRequest);
    }
    public void showAd() {
        if (mInterstitialAd.isLoaded()) {
            isLoaded = true;
            isDismissed = false;
            isDisplayed = false;
            isError = false;
            mInterstitialAd.show();
        } else {
            isLoaded = false;
        }
    }
    public boolean adsShowDisplayedOrStopped() {
        return (isDisplayed || (isError));
    }
    public boolean adsShowDismissedOrStopped() {
        return (isDismissed || (isError));
    }

    // interface for showing Google AdMob ads
    public interface AfterDismissFunctionOfShowGoogleAdMobAds {
        void executeAfterDismissAds(int endPoint);
    }
    public class ShowGoogleAdMobAdsAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final AppCompatActivity activity;
        private final int endPoint;
        private final AfterDismissFunctionOfShowGoogleAdMobAds afterDismissFunction;

        public ShowGoogleAdMobAdsAsyncTask(final AppCompatActivity activity, final int endPoint) {
            this.activity = activity;
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
        }

        public ShowGoogleAdMobAdsAsyncTask(final AppCompatActivity activity, final int endPoint, final AfterDismissFunctionOfShowGoogleAdMobAds afterDismissFunction) {

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
