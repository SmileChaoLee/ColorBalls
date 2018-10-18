package com.smile.smilepublicclasseslibrary.facebookadsutil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import com.facebook.ads.*;

public class FacebookInterstitialAds {

    private final String TAG = new String("com.smile.facebookadsutil.FacebookInterstitialAds");
    private static final int MAX_NUMBER_OF_RETRIES = 3;
    private boolean shouldLoadAd = true;

    private final Context context;
    private InterstitialAd interstitialAd;
    private int retryCount = 0;
    private boolean isDisplayed;
    private boolean isDismissed;
    private boolean isError;

    public FacebookInterstitialAds(final Context context, String placementID) {

        this.context = context;

        shouldLoadAd = true;
        isDisplayed = false;
        isDismissed = false;
        isError = false;

        Log.d(TAG, "isDisplayed = " + isDisplayed);
        Log.d(TAG, "shouldLoadAd = " + shouldLoadAd);

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
                shouldLoadAd = true;
                retryCount = 0;
                isDisplayed = true;
                Log.e(TAG, "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                shouldLoadAd = true;
                retryCount = 0;
                isDisplayed = true;
                isDismissed = true;
                Log.e(TAG, "Interstitial ad dismissed.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                // Ad error callback
                // Stop retrying when it reaches to MAX_NUMBER_OF_RETRIES
                if(retryCount < FacebookInterstitialAds.MAX_NUMBER_OF_RETRIES) {
                    shouldLoadAd = false;
                    ++retryCount;
                    interstitialAd.loadAd();
                } else {
                    // no more trying so can do next ad showing
                    shouldLoadAd = true;
                    retryCount = 0;
                    isError = true;
                }
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                shouldLoadAd = false;
                isDisplayed = false;
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                shouldLoadAd = true;
                isDisplayed = true;
                Log.d(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                // after displayed
                shouldLoadAd = true;
                isDisplayed = true;
                Log.d(TAG, "Interstitial ad impression logged!");
            }
        });
    }

    public void showAd(String callingObject) {
        Log.e(TAG, callingObject + " calling showAd() method.");
        if (shouldLoadAd) {
            Log.e(TAG, callingObject + " loading Ad now.");
            isDisplayed = false;
            isDismissed = false;
            isError = false;
            interstitialAd.loadAd();
        }
    }

    public boolean adsShowDisplayedOrStopped() {
        return (isDisplayed || (isError));
    }

    public boolean adsShowDismissedOrStopped() {
        System.out.println("shouldLoadAd = " + shouldLoadAd);
        System.out.println("isDisplayed = " + isDisplayed);
        System.out.println("isDismissed = " + isDismissed);
        System.out.println("retryCount = " + retryCount);
        return (isDismissed || (isError));
    }

    public void close() {
        // destroy the instance of facebook ads
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
    }

    // interface for showing Facebook ads
    public interface AfterDismissFunctionOfShowFacebookAds {
        void executeAfterDismissAds(int endPoint);
    }

    public class ShowFacebookAdsAsyncTask extends AsyncTask<Void, Integer, Void> {
        private final String ClassTAG = "ShowFacebookAdsAsyncTask";
        private Animation animationText = null;
        private TextView loadingTextView = null;
        private AlertDialog loadingDialog = null;

        private final Activity activity;
        private final String messageString;
        private final float fontSize;
        private final int endPoint;
        private final AfterDismissFunctionOfShowFacebookAds afterDismissFunction;

        private void init() {
            animationText = new AlphaAnimation(0.0f,1.0f);
            animationText.setDuration(300);
            animationText.setStartOffset(0);
            animationText.setRepeatMode(Animation.REVERSE);
            animationText.setRepeatCount(Animation.INFINITE);

            loadingTextView = new TextView(activity);
            loadingTextView.setText(messageString);
            loadingTextView.setTextColor(Color.RED);
            loadingTextView.setTextSize(fontSize);
            loadingTextView.setTypeface(Typeface.DEFAULT_BOLD);
            loadingTextView.setGravity(Gravity.CENTER);
            loadingTextView.setBackgroundResource(android.R.color.transparent);

            loadingDialog = new AlertDialog.Builder(activity).create();
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setView(loadingTextView);

            loadingDialog.setCancelable(false);
            loadingDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    AlertDialog alertD = (AlertDialog)dialog;
                    Window window = alertD.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setDimAmount(0.0f); // no dim for background screen
                    window.setGravity(Gravity.CENTER);
                    window.setBackgroundDrawableResource(android.R.color.holo_orange_light);
                }
            });
        }

        public ShowFacebookAdsAsyncTask(final Activity activity, final String messageString, final float fontSize, final int endPoint) {
            this.activity = activity;
            this.messageString = messageString;
            this.fontSize = fontSize;
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
            init();
        }

        public ShowFacebookAdsAsyncTask(final Activity activity, final String messageString, final float fontSize, final int endPoint, final AfterDismissFunctionOfShowFacebookAds afterDismissFunction) {
            this.activity = activity;
            this.messageString = messageString;
            this.fontSize = fontSize;
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
            init();
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.show();
            showAd(ClassTAG);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final int timeDelay = 300;
            int i = 0;
            publishProgress(i);
            while (!adsShowDismissedOrStopped()) {
                SystemClock.sleep(timeDelay);
            }
            i = 1;
            publishProgress(i);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (!isCancelled()) {
                try {
                    if ( (animationText != null) && (loadingTextView != null) ) {
                        loadingTextView.startAnimation(animationText);
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
                loadingDialog.dismiss();
            }

            if (afterDismissFunction != null) {
                afterDismissFunction.executeAfterDismissAds(endPoint);
            }
        }
    }
}
