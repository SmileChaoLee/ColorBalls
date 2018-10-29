package com.smile.smilepublicclasseslibrary.showing_instertitial_ads_utility;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.smile.smilepublicclasseslibrary.facebook_ads_util.FacebookInterstitialAds;
import com.smile.smilepublicclasseslibrary.google_admob_ads_util.GoogleAdMobInterstitial;

public class ShowingInterstitialAdsUtil {

    private final FacebookInterstitialAds facebookAd;
    private final GoogleAdMobInterstitial adMobAd;
    private boolean isShowingFacebookAd;

    public ShowingInterstitialAdsUtil(FacebookInterstitialAds facebookAd, GoogleAdMobInterstitial adMobAd) {
        this.facebookAd = facebookAd;
        this.adMobAd = adMobAd;
        isShowingFacebookAd = true;
    }

    public void showFacebookAdFirst() {
        if (facebookAd.isLoaded() && (!facebookAd.isError())) {
            // facebook ad is loaded, then show facebook
            isShowingFacebookAd = true;
            facebookAd.showAd();
        } else {
            // if facebook ad is not loaded, then show AdMob ad
            isShowingFacebookAd = false;
            adMobAd.showAd();
            facebookAd.loadAd();    // load the next facebook ad
        }
    }
    public void showGoogleAdMobAdFirst() {
        if (adMobAd.isLoaded()) {
            isShowingFacebookAd = false;
            adMobAd.showAd();
            facebookAd.loadAd();    // load the next facebook ad
        } else {
            isShowingFacebookAd = true;
            if (facebookAd.isLoaded() && (!facebookAd.isError())) {
                // facebook ad is loaded, then show facebook
                facebookAd.showAd();
            } else {
                facebookAd.loadAd();
            }
            adMobAd.loadAd();   // load next AdMob ad
        }
    }
    public void close() {
        facebookAd.close();
    }

    // interface for showing Google AdMob ads
    public interface AfterDismissFunctionOfShowAd {
        void executeAfterDismissAds(int endPoint);
    }
    public class ShowAdAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final AppCompatActivity activity;
        private final int endPoint;
        private final AfterDismissFunctionOfShowAd afterDismissFunction;

        public ShowAdAsyncTask(final AppCompatActivity activity, final int endPoint) {
            this.activity = activity;
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
        }

        public ShowAdAsyncTask(final AppCompatActivity activity, final int endPoint, final AfterDismissFunctionOfShowAd afterDismissFunction) {

            this.activity = activity;
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
        }

        @Override
        protected void onPreExecute() {
            showGoogleAdMobAdFirst();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final int timeDelay = 300;
            int i = 0;
            if (isShowingFacebookAd) {
                while (!facebookAd.adsShowDismissedOrStopped()) {
                    SystemClock.sleep(timeDelay);
                }
            } else {
                // is showing google AdMob ad
                while (!adMobAd.adsShowDismissedOrStopped()) {
                    SystemClock.sleep(timeDelay);
                }
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
