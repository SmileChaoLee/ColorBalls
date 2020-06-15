package com.smile.smilelibraries.showing_instertitial_ads_utility;
import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitialAds;
import com.smile.smilelibraries.google_admob_ads_util.GoogleAdMobInterstitial;

public class ShowingInterstitialAdsUtil {

    public final static int GoogleAdMobAdProvider = 0;
    public final static int FacebookAdProvider = 1;

    private final static String TAG = new String(".ShowingInterstitialAdsUtil");
    private final FacebookInterstitialAds facebookAd;
    private final GoogleAdMobInterstitial adMobAd;
    private boolean isShowingFacebookAd;

    public ShowingInterstitialAdsUtil(FacebookInterstitialAds facebookAd, GoogleAdMobInterstitial adMobAd) {
        this.facebookAd = facebookAd;
        this.adMobAd = adMobAd;
        isShowingFacebookAd = false;
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

    public class ShowInterstitialAdThread extends Thread {
        private final int endPoint;
        private final AfterDismissFunctionOfShowAd afterDismissFunction;
        private boolean isAdShown = false;
        private int adProvider;
        private boolean keepRunning = true;

        public ShowInterstitialAdThread(final int endPoint) {
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
            isAdShown = false;
            this.adProvider = GoogleAdMobAdProvider; // default is Google AdMob
            keepRunning = true;
        }

        public ShowInterstitialAdThread(final int endPoint, int adProvider) {
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
            isAdShown = false;
            this.adProvider = adProvider;
            keepRunning = true;
        }

        public ShowInterstitialAdThread(final int endPoint, final AfterDismissFunctionOfShowAd afterDismissFunction) {
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
            isAdShown = false;
            this.adProvider = GoogleAdMobAdProvider; // default is Google AdMob
            keepRunning = true;
        }

        public ShowInterstitialAdThread(final int endPoint, int adProvider, final AfterDismissFunctionOfShowAd afterDismissFunction) {
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
            isAdShown = false;
            this.adProvider = adProvider;
            keepRunning = true;
        }

        private synchronized void onPreExecute() {
            switch (adProvider) {
                case FacebookAdProvider:
                    // facebook ad
                    isAdShown = showFacebookAdFirst();
                    Log.d(TAG, "ShowInterstitialAdThread --> Started showing Facebook Ad.");
                    break;
                default:
                    // Google AdMob for 0 or others
                    isAdShown = showGoogleAdMobAdFirst();
                    Log.d(TAG, "ShowInterstitialAdThread --> Started showing Google AdMob Ad.");
                    break;
            }
        }

        private synchronized void doInBackground() {
            final int timeDelay = 300;
            int i = 0;
            if (isAdShown) {
                Log.d(TAG, "ShowInterstitialAdThread --> Ad is showing.");
                try {
                    if (isShowingFacebookAd) {
                        Log.d(TAG, "ShowInterstitialAdThread --> Facebook Ad was shown.");
                        while (!facebookAd.adsShowDismissedOrStopped() && keepRunning) {
                            SystemClock.sleep(timeDelay);
                        }
                        Log.d(TAG, "ShowInterstitialAdThread --> Facebook Ad dismissed.");
                    } else {
                        // is showing google AdMob ad
                        Log.d(TAG, "ShowInterstitialAdThread --> Google Ad was shown.");
                        while (!adMobAd.adsShowDismissedOrStopped() && keepRunning) {
                            SystemClock.sleep(timeDelay);
                        }
                        Log.d(TAG, "ShowInterstitialAdThread --> Google Ad dismissed.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Log.d(TAG, "ShowInterstitialAdThread --> Ad is not showing.");
            }
        }

        private synchronized void onPostExecute() {
            try {
                if (afterDismissFunction != null) {
                    afterDismissFunction.executeAfterDismissAds(endPoint);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Log.d(TAG, "ShowInterstitialAdThread --> onPostExecute().");
        }

        public void startShowAd() {
            keepRunning = true;
            start();
            Log.d(TAG, "ShowInterstitialAdThread started.");
        }
        public void finishThread() {
            keepRunning = false;
            Log.d(TAG, "ShowInterstitialAdThread finished.");
        }

        @Override
        public synchronized void run() {
            super.run();
            onPreExecute();
            doInBackground();
            onPostExecute();
        }
    }
}
