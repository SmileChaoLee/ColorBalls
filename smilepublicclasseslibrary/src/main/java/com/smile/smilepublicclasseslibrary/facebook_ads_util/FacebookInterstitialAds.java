package com.smile.smilepublicclasseslibrary.facebook_ads_util;

import com.smile.smilepublicclasseslibrary.alertdialogfragment.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
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
    public void showAd() {
        isDisplayed = false;
        isDismissed = false;
        if (interstitialAd.isAdLoaded()) {
            isLoaded = true;
            isClicked = false;
            isLoggingImpression = false;
            isError = false;
            interstitialAd.show();
        } else {
            isLoaded = false;
            isClicked = false;
            isLoggingImpression = false;
            isError = true;
            loadAd();
        }
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

    // interface for showing Facebook ads
    public interface AfterDismissFunctionOfShowFacebookAd {
        void executeAfterDismissAds(int endPoint);
    }

    public class ShowFacebookAdAsyncTask extends AsyncTask<Void, Integer, Void> {
        private final String LoadingDialogTag = "LoadingDialogTag";
        private Animation animationText = null;
        private TextView loadingTextView = null;
        private AlertDialogFragment loadingDialog = null;

        private final AppCompatActivity activity;
        private final String messageString;
        private final float fontSize;
        private final int endPoint;
        private final AfterDismissFunctionOfShowFacebookAd afterDismissFunction;

        private void init() {
            animationText = new AlphaAnimation(0.0f,1.0f);
            animationText.setDuration(300);
            animationText.setStartOffset(0);
            animationText.setRepeatMode(Animation.REVERSE);
            animationText.setRepeatCount(Animation.INFINITE);

            loadingDialog = new AlertDialogFragment();
            Bundle args = new Bundle();
            args.putString("textContent", messageString);
            args.putFloat("textSize", fontSize);
            args.putInt("color", Color.RED);
            args.putInt("width", 0);    // wrap_content
            args.putInt("height", 0);   // wrap_content
            args.putInt("numButtons", 0);
            loadingDialog.setArguments(args);

            animationText = new AlphaAnimation(0.0f,1.0f);
            animationText.setDuration(300);
            animationText.setStartOffset(0);
            animationText.setRepeatMode(Animation.REVERSE);
            animationText.setRepeatCount(Animation.INFINITE);
        }

        public ShowFacebookAdAsyncTask(final AppCompatActivity activity, final String messageString, final float fontSize, final int endPoint) {
            this.activity = activity;
            this.messageString = messageString;
            this.fontSize = fontSize;
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
            init();
        }

        public ShowFacebookAdAsyncTask(final AppCompatActivity activity, final String messageString, final float fontSize, final int endPoint, final AfterDismissFunctionOfShowFacebookAd afterDismissFunction) {

            this.activity = activity;
            this.messageString = messageString;
            this.fontSize = fontSize;
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
            init();
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.show(activity.getSupportFragmentManager(), LoadingDialogTag);
            showAd();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final int timeDelay = 300;
            int i = 0;

            publishProgress(i);
            // wait for one second
            try { Thread.sleep(timeDelay); } catch (InterruptedException ex) { ex.printStackTrace(); }

            i = 1;
            while (loadingTextView == null) {
                loadingTextView = loadingDialog.getText_shown();
                SystemClock.sleep(timeDelay);
            }

            publishProgress(i);

            i = 2;
            while (!adsShowDismissedOrStopped()) {
                SystemClock.sleep(timeDelay);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (!isCancelled()) {
                try {
                    if (progress[0] == 1) {
                        if ( (animationText != null) && (loadingTextView != null) ) {
                            loadingTextView.startAnimation(animationText);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (!isCancelled()) {
                if (animationText != null) {
                    if (loadingTextView != null) {
                        loadingTextView.clearAnimation();
                        loadingTextView.setText("");
                    }
                    animationText = null;
                }
                loadingDialog.dismissAllowingStateLoss();
            }

            if (afterDismissFunction != null) {
                afterDismissFunction.executeAfterDismissAds(endPoint);
            }
        }
    }
}
