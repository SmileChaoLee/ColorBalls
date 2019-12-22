package com.smile.smilelibraries.showing_instertitial_ads_utility;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitialAds;
import com.smile.smilelibraries.google_admob_ads_util.GoogleAdMobInterstitial;

public class ShowingInterstitialAdsUtil {

    private final static String TAG = new String(".ShowingInterstitialAdsUtil");
    private final FacebookInterstitialAds facebookAd;
    private final GoogleAdMobInterstitial adMobAd;
    private boolean isShowingFacebookAd;

    public ShowingInterstitialAdsUtil(FacebookInterstitialAds facebookAd, GoogleAdMobInterstitial adMobAd) {
        this.facebookAd = facebookAd;
        this.adMobAd = adMobAd;
        isShowingFacebookAd = true;
    }

    public boolean showFacebookAdFirst() {
        boolean succeededAdMob = false;
        boolean succeededFacebook = false;

        try {
            if (facebookAd.isLoaded() && (!facebookAd.isError())) {
                // facebook ad is loaded, then show facebook
                isShowingFacebookAd = true;
                succeededFacebook = facebookAd.showAd();
                if (!succeededFacebook) {
                    // If no facebook ad showing
                    // then load next facebook ad
                    facebookAd.loadAd();
                }
                if (!adMobAd.isLoading()) {
                    // not loading
                    adMobAd.loadAd();
                }
            } else {
                // if facebook ad is not loaded, then show AdMob ad
                isShowingFacebookAd = false;
                if (adMobAd.isLoaded()) {
                    succeededAdMob = adMobAd.showAd();
                }
                if (!succeededAdMob) {
                    // If no AdMob ad showing
                    // then load the next AdMob ad
                    adMobAd.loadAd();
                }
                if (facebookAd.isError()) {
                    facebookAd.loadAd();    // load the next facebook ad
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return (succeededAdMob || succeededFacebook);
    }

    public boolean showGoogleAdMobAdFirst() {
        boolean succeededAdMob = false;
        boolean succeededFacebook = false;

        try {
            if (adMobAd.isLoaded()) {
                isShowingFacebookAd = false;
                succeededAdMob = adMobAd.showAd();
                if (!succeededAdMob) {
                    // if no AdMob ad showing
                    // then load next AdMob ad
                    adMobAd.loadAd();
                }
                if (facebookAd.isError()) {
                    facebookAd.loadAd();    // load the next facebook ad
                }
            } else {
                // AdMob ad is not loaded, then show facebook
                isShowingFacebookAd = true;
                if (facebookAd.isLoaded() && (!facebookAd.isError())) {
                    succeededFacebook = facebookAd.showAd();
                }
                if (!succeededFacebook) {
                    // If no facebook ad showing
                    // load next facebook ad
                    facebookAd.loadAd();
                }
                if (!adMobAd.isLoading()) {
                    // not loading
                    adMobAd.loadAd();   // load next AdMob ad
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return (succeededAdMob || succeededFacebook);
    }
    public void close() {
        try {
            facebookAd.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // interface for showing Google AdMob ads
    public interface AfterDismissFunctionOfShowAd {
        void executeAfterDismissAds(int endPoint);
    }
    public class ShowAdAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final int endPoint;
        private final AfterDismissFunctionOfShowAd afterDismissFunction;
        private  boolean isAdShown = false;

        public ShowAdAsyncTask(final int endPoint) {
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
            isAdShown = false;
        }

        public ShowAdAsyncTask(final int endPoint, final AfterDismissFunctionOfShowAd afterDismissFunction) {
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
        }

        @Override
        protected void onPreExecute() {
            isAdShown = showGoogleAdMobAdFirst();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final int timeDelay = 300;
            int i = 0;
            if (isAdShown) {
                try {
                    if (isShowingFacebookAd) {
                        Log.d(TAG, "Showing Facebook Ads");
                        while (!facebookAd.adsShowDismissedOrStopped()) {
                            SystemClock.sleep(timeDelay);
                        }
                    } else {
                        // is showing google AdMob ad
                        Log.d(TAG, "Showing Google Ads");
                        while (!adMobAd.adsShowDismissedOrStopped()) {
                            SystemClock.sleep(timeDelay);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (afterDismissFunction != null) {
                    afterDismissFunction.executeAfterDismissAds(endPoint);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
