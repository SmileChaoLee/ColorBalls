package com.smile.smilelibraries.showing_interstitial_ads_utility;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
    private final Context mContext;
    private final Activity mActivity;
    private final Handler synchronizedHandler = new Handler(Looper.getMainLooper());

    private boolean isShowingFacebookAd;
    private boolean succeededAdMob = false;
    private boolean succeededFacebook = false;
    private boolean finishedPreviousStep = false;

    public ShowingInterstitialAdsUtil(Context context, FacebookInterstitialAds facebookAd, GoogleAdMobInterstitial adMobAd) {
        this.mContext = context;
        this.mActivity = (Activity)mContext;
        this.facebookAd = facebookAd;
        this.adMobAd = adMobAd;
        isShowingFacebookAd = false;
    }

    private void close() {
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

        private boolean showFacebookAdFirst() {
            succeededAdMob = false;
            succeededFacebook = false;
            finishedPreviousStep = false;

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (synchronizedHandler) {
                        try {
                            Log.d(TAG, "showFacebookAdFirst() --> Checking to show Facebook Ad.");
                            if ( (facebookAd!=null) && facebookAd.isLoaded() && !facebookAd.isError()) {
                                Log.d(TAG, "showFacebookAdFirst() --> Started to show Facebook Ad.");
                                // facebook ad is loaded, then show facebook
                                isShowingFacebookAd = true;
                                succeededFacebook = facebookAd.showAd();
                                if (!succeededFacebook) {
                                    // If no facebook ad showing
                                    // then load next facebook ad
                                    facebookAd.loadAd();
                                    Log.d(TAG, "showFacebookAdFirst() --> Failed to show FacebookAd Ad.");
                                } else {
                                    Log.d(TAG, "showFacebookAdFirst() --> Succeeded to show FacebookAd Ad.");
                                }
                                if (!adMobAd.isLoading()) {
                                    // not loading
                                    adMobAd.loadAd();
                                }
                            } else {
                                // if facebook ad is not loaded, then show AdMob ad
                                Log.d(TAG, "showFacebookAdFirst() --> Checking to show Google Ad.");
                                isShowingFacebookAd = false;
                                if (adMobAd != null) {
                                    if (adMobAd.isLoaded()) {
                                        Log.d(TAG, "showFacebookAdFirst() --> Started to show Google Ad.");
                                        succeededAdMob = adMobAd.showAd();
                                    }
                                    if (!succeededAdMob) {
                                        // If no AdMob ad showing
                                        // then load the next AdMob ad
                                        adMobAd.loadAd();
                                        Log.d(TAG, "showFacebookAdFirst() --> Failed to show Google Ad.");
                                    } else {
                                        Log.d(TAG, "showFacebookAdFirst() --> Succeeded to show Google Ad.");
                                    }
                                }
                                if (facebookAd != null) {
                                    if (facebookAd.isError()) {
                                        facebookAd.loadAd();    // load the next facebook ad
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Log.d(TAG, "Exception on showFacebookAdFirst().");
                            ex.printStackTrace();
                        }

                        finishedPreviousStep = true;
                        synchronizedHandler.notifyAll();
                        Log.d(TAG, "showFacebookAdFirst() -- >synchronizedHandler.notifyAll()");
                    }
                }
            });

            synchronized (synchronizedHandler) {
                while (!finishedPreviousStep) {
                    try {
                        synchronizedHandler.wait();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "showFacebookAdFirst() --> synchronizedHandler.wait() exception");
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "showFacebookAdFirst() --> return value = " + (succeededAdMob || succeededFacebook));

            return (succeededAdMob || succeededFacebook);
        }

        private boolean showGoogleAdMobAdFirst() {
            succeededAdMob = false;
            succeededFacebook = false;
            finishedPreviousStep = false;

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (synchronizedHandler) {
                        try {
                            Log.d(TAG, "showGoogleAdMobAdFirst() -->Checking to show Google Ad.");
                            if ( (adMobAd!=null) && adMobAd.isLoaded()) {
                                isShowingFacebookAd = false;
                                Log.d(TAG, "showGoogleAdMobAdFirst() --> Starting to show Google Ad.");
                                succeededAdMob = adMobAd.showAd();
                                if (!succeededAdMob) {
                                    // if no AdMob ad showing
                                    // then load next AdMob ad
                                    adMobAd.loadAd();
                                    Log.d(TAG, "showGoogleAdMobAdFirst() --> Failed to show Google Ad.");
                                } else {
                                    Log.d(TAG, "showGoogleAdMobAdFirst() --> Succeeded to show Google Ad.");
                                }
                                if (facebookAd.isError()) {
                                    facebookAd.loadAd();    // load the next facebook ad
                                }
                            } else {
                                // AdMob ad is not loaded, then show facebook
                                Log.d(TAG, "showGoogleAdMobAdFirst() --> Checking to show Facebook Ad.");
                                isShowingFacebookAd = true;
                                if (facebookAd != null) {
                                    if (facebookAd.isLoaded() && (!facebookAd.isError())) {
                                        Log.d(TAG, "showGoogleAdMobAdFirst() --> Starting to show Facebook Ad.");
                                        succeededFacebook = facebookAd.showAd();
                                    }
                                    if (!succeededFacebook) {
                                        // If no facebook ad showing
                                        // load next facebook ad
                                        facebookAd.loadAd();
                                        Log.d(TAG, "showGoogleAdMobAdFirst() --> Failed to show FacebookAd Ad.");
                                    } else {
                                        Log.d(TAG, "showGoogleAdMobAdFirst() --> Succeeded to show FacebookAd Ad.");
                                    }
                                }
                                if (adMobAd != null) {
                                    if (!adMobAd.isLoading()) {
                                        // not loading
                                        adMobAd.loadAd();   // load next AdMob ad
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Log.d(TAG, "showGoogleAdMobAdFirst() --> Exception on showGoogleAdMobAdFirst().");
                            ex.printStackTrace();
                        }

                        finishedPreviousStep = true;
                        synchronizedHandler.notifyAll();
                        Log.d(TAG, "showGoogleAdMobAdFirst() --> synchronizedHandler.notifyAll()");
                    }
                }
            });

            synchronized (synchronizedHandler) {
                while (!finishedPreviousStep) {
                    try {
                        synchronizedHandler.wait();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "showGoogleAdMobAdFirst() --> synchronizedHandler.wait() exception");
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "showGoogleAdMobAdFirst() --> return value = " + (succeededAdMob || succeededFacebook));

            return (succeededAdMob || succeededFacebook);
        }

        private synchronized void onPreExecute() {
            switch (adProvider) {
                case FacebookAdProvider:
                    // facebook ad
                    isAdShown = showFacebookAdFirst();
                    Log.d(TAG, "ShowInterstitialAdThread.onPreExecute --> Started showing Facebook Ad.");
                    break;
                default:
                    // Google AdMob for 0 or others
                    isAdShown = showGoogleAdMobAdFirst();
                    Log.d(TAG, "ShowInterstitialAdThread.onPreExecute() --> Started showing Google AdMob Ad.");
                    break;
            }

            Log.d(TAG, "ShowInterstitialAdThread.onPreExecute() --> isAdShown = " + isAdShown);
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
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // may update th main thread (UI)
                            afterDismissFunction.executeAfterDismissAds(endPoint);
                        }
                    });
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

        public void finishShowInterstitialAdThread() {
            keepRunning = false;
            Log.d(TAG, "Ending ShowInterstitialAdThread .");
        }

        public void releaseShowInterstitialAdThread() {
            keepRunning = false;
            close();
            Log.d(TAG, "Releasing ShowInterstitialAdThread.");
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
