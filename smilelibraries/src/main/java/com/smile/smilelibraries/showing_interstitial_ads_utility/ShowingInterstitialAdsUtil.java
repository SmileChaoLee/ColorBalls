package com.smile.smilelibraries.showing_interstitial_ads_utility;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.util.VisibleForTesting;
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitialAds;
import com.smile.smilelibraries.google_admob_ads_util.GoogleAdMobInterstitial;

public class ShowingInterstitialAdsUtil {

    public final static int GoogleAdMobAdProvider = 0;
    public final static int FacebookAdProvider = 1;

    private final static String TAG = ShowingInterstitialAdsUtil.class.getName();
    private final FacebookInterstitialAds facebookAd;
    private final GoogleAdMobInterstitial adMobAd;
    private final Activity mActivity;
    private final Handler synchronizedHandler = new Handler(Looper.getMainLooper());

    private boolean isShowingFacebookAd;
    private boolean succeededAdMob = false;
    private boolean succeededFacebook = false;
    private boolean finishedPreviousStep = false;

    public ShowingInterstitialAdsUtil(Activity activity, FacebookInterstitialAds facebookAd, GoogleAdMobInterstitial adMobAd) {
        this.mActivity = activity;
        this.facebookAd = facebookAd;
        if (this.facebookAd != null) {
            this.facebookAd.loadAd();
        }
        this.adMobAd = adMobAd;
        if (this.adMobAd != null) {
            this.adMobAd.loadAd();
        }
        isShowingFacebookAd = false;
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
        private boolean isAdShown;
        private final int adProvider;

        public ShowInterstitialAdThread(final int endPoint) {
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
            isAdShown = false;
            this.adProvider = GoogleAdMobAdProvider; // default is Google AdMob
        }

        public ShowInterstitialAdThread(final int endPoint, int adProvider) {
            this.endPoint = endPoint;
            this.afterDismissFunction = null;
            isAdShown = false;
            this.adProvider = adProvider;
        }

        public ShowInterstitialAdThread(final int endPoint, final AfterDismissFunctionOfShowAd afterDismissFunction) {
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
            isAdShown = false;
            this.adProvider = GoogleAdMobAdProvider; // default is Google AdMob
        }

        public ShowInterstitialAdThread(final int endPoint, int adProvider, final AfterDismissFunctionOfShowAd afterDismissFunction) {
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
            isAdShown = false;
            this.adProvider = adProvider;
        }

        @VisibleForTesting
        private boolean showFacebookAdFirst() {
            succeededAdMob = false;
            succeededFacebook = false;
            finishedPreviousStep = false;

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (synchronizedHandler) {
                        try {
                            Log.d(TAG, "showFacebookAdFirst.Checking to show Facebook Ad.");
                            isShowingFacebookAd = true;
                            succeededFacebook = false;
                            if (facebookAd != null) {
                                succeededFacebook = facebookAd.showAd();
                                facebookAd.loadAd();
                            }
                            succeededAdMob = false;
                            if (!succeededFacebook) {
                                isShowingFacebookAd = false;
                                if (adMobAd != null) {
                                    succeededAdMob = adMobAd.showAd(mActivity);
                                    adMobAd.loadAd();
                                }
                            }
                            /*
                            if ( (facebookAd!=null) && facebookAd.isLoaded() && !facebookAd.isError()) {
                                Log.d(TAG, "showFacebookAdFirst.Started to show Facebook Ad.");
                                // facebook ad is loaded, then show facebook
                                isShowingFacebookAd = true;
                                succeededFacebook = facebookAd.showAd();
                                if (!succeededFacebook) {
                                    // If no facebook ad showing
                                    // then load next facebook ad
                                    // facebookAd.loadAd();
                                    Log.d(TAG, "showFacebookAdFirst.Failed to show FacebookAd Ad.");
                                } else {
                                    Log.d(TAG, "showFacebookAdFirst.Succeeded to show FacebookAd Ad.");
                                }
                                facebookAd.loadAd();
                                // if (!adMobAd.isLoading()) {
                                //     // not loading
                                //     adMobAd.loadAd();
                                // }
                                adMobAd.loadAd();
                            } else {
                                // if facebook ad is not loaded, then show AdMob ad
                                Log.d(TAG, "showFacebookAdFirst.Checking to show Google Ad.");
                                isShowingFacebookAd = false;
                                if (adMobAd != null) {
                                    if (adMobAd.isLoaded()) {
                                        Log.d(TAG, "showFacebookAdFirst.Started to show Google Ad.");
                                        succeededAdMob = adMobAd.showAd(mActivity);
                                    }
                                    if (!succeededAdMob) {
                                        // If no AdMob ad showing
                                        // then load the next AdMob ad
                                        // adMobAd.loadAd();
                                        Log.d(TAG, "showFacebookAdFirst.Failed to show Google Ad.");
                                    } else {
                                        Log.d(TAG, "showFacebookAdFirst.Succeeded to show Google Ad.");
                                    }
                                    adMobAd.loadAd();
                                }
                                // if (facebookAd != null) {
                                //     if (facebookAd.isError()) {
                                //         facebookAd.loadAd();    // load the next facebook ad
                                //     }
                                // }
                                facebookAd.loadAd();    // load the next facebook ad
                            }
                            */
                        } catch (Exception ex) {
                            Log.d(TAG, "showFacebookAdFirst.Exception");
                            ex.printStackTrace();
                        }

                        finishedPreviousStep = true;
                        synchronizedHandler.notifyAll();
                        Log.d(TAG, "showFacebookAdFirst.synchronizedHandler.notifyAll()");
                    }
                }
            });

            synchronized (synchronizedHandler) {
                while (!finishedPreviousStep) {
                    try {
                        synchronizedHandler.wait();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "showFacebookAdFirst.synchronizedHandler.wait() exception");
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "showFacebookAdFirst.return value = " + (succeededAdMob || succeededFacebook));

            return (succeededAdMob || succeededFacebook);
        }

        @VisibleForTesting
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
                            isShowingFacebookAd = false;
                            succeededAdMob = false;
                            if (adMobAd != null) {
                                succeededAdMob = adMobAd.showAd(mActivity);
                                adMobAd.loadAd();
                            }
                            succeededFacebook = false;
                            if (!succeededAdMob) {
                                isShowingFacebookAd = true;
                                if (facebookAd != null) {
                                    succeededFacebook = facebookAd.showAd();
                                    facebookAd.loadAd();
                                }
                            }
                            /*
                            if ( (adMobAd!=null) && adMobAd.isLoaded()) {
                                isShowingFacebookAd = false;
                                Log.d(TAG, "showGoogleAdMobAdFirst.Starting to show Google Ad.");
                                succeededAdMob = adMobAd.showAd(mActivity);
                                if (!succeededAdMob) {
                                    // if no AdMob ad showing
                                    // then load next AdMob ad
                                    // adMobAd.loadAd();
                                    Log.d(TAG, "showGoogleAdMobAdFirst.Failed to show Google Ad.");
                                } else {
                                    Log.d(TAG, "showGoogleAdMobAdFirst.Succeeded to show Google Ad.");
                                }
                                adMobAd.loadAd();
                                // if (facebookAd.isError()) {
                                //     facebookAd.loadAd();    // load the next facebook ad
                                // }
                                facebookAd.loadAd();    // load the next facebook ad
                            } else {
                                // AdMob ad is not loaded, then show facebook
                                Log.d(TAG, "showGoogleAdMobAdFirst.Checking to show Facebook Ad.");
                                isShowingFacebookAd = true;
                                if (facebookAd != null) {
                                    if (facebookAd.isLoaded() && (!facebookAd.isError())) {
                                        Log.d(TAG, "showGoogleAdMobAdFirst.Starting to show Facebook Ad.");
                                        succeededFacebook = facebookAd.showAd();
                                    }
                                    if (!succeededFacebook) {
                                        // If no facebook ad showing
                                        // load next facebook ad
                                        // facebookAd.loadAd();
                                        Log.d(TAG, "showGoogleAdMobAdFirst.Failed to show FacebookAd Ad.");
                                    } else {
                                        Log.d(TAG, "showGoogleAdMobAdFirst.Succeeded to show FacebookAd Ad.");
                                    }
                                    facebookAd.loadAd();
                                }
                                if (adMobAd != null) {
                                    // if (!adMobAd.isLoading()) {
                                    //     // not loading
                                    //     adMobAd.loadAd();   // load next AdMob ad
                                    // }
                                    adMobAd.loadAd();   // load next AdMob ad
                                }
                            }
                            */
                        } catch (Exception ex) {
                            Log.d(TAG, "showGoogleAdMobAdFirst.Exception on showGoogleAdMobAdFirst().");
                            ex.printStackTrace();
                        }

                        finishedPreviousStep = true;
                        synchronizedHandler.notifyAll();
                        Log.d(TAG, "showGoogleAdMobAdFirst.synchronizedHandler.notifyAll()");
                    }
                }
            });

            synchronized (synchronizedHandler) {
                while (!finishedPreviousStep) {
                    try {
                        synchronizedHandler.wait();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "showGoogleAdMobAdFirst.synchronizedHandler.wait() exception");
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "showGoogleAdMobAdFirst.return value = " + (succeededAdMob || succeededFacebook));

            return (succeededAdMob || succeededFacebook);
        }

        @VisibleForTesting
        private boolean showAdMobOrFacebookAd() {
            succeededAdMob = false;
            succeededFacebook = false;
            finishedPreviousStep = false;

            mActivity.runOnUiThread(() -> {
                synchronized (synchronizedHandler) {
                    try {
                        Log.d(TAG, "showAdMobOrFacebookAd.Start showing");
                        isShowingFacebookAd = false;
                        succeededAdMob = false;
                        succeededFacebook = false;
                        if (adMobAd != null) {
                            if (adMobAd.isLoaded()) succeededAdMob = adMobAd.showAd(mActivity);
                            adMobAd.loadAd();
                            if (succeededAdMob) isShowingFacebookAd = false;
                        } else {
                            if (facebookAd != null) {
                                if (facebookAd.isLoaded()) succeededFacebook = facebookAd.showAd();
                                facebookAd.loadAd();
                                if (succeededFacebook) isShowingFacebookAd = true;
                            }
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "showAdMobOrFacebookAd.Exception");
                        ex.printStackTrace();
                    }

                    finishedPreviousStep = true;
                    synchronizedHandler.notifyAll();
                    Log.d(TAG, "showAdMobOrFacebookAd.synchronizedHandler.notifyAll()");
                }
            });

            synchronized (synchronizedHandler) {
                while (!finishedPreviousStep) {
                    try {
                        synchronizedHandler.wait();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "showAdMobOrFacebookAd.synchronizedHandler.exception");
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "showAdMobOrFacebookAd.succeededAdMob = " + succeededAdMob +
                    ", succeededFacebook= " + succeededFacebook);

            return (succeededAdMob || succeededFacebook);
        }

        private synchronized void onPreExecute() {
            isAdShown = showAdMobOrFacebookAd();
            Log.d(TAG, "ShowInterstitialAdThread.onPreExecute().isAdShown = " + isAdShown);
        }

        private synchronized void doInBackground() {
            Log.d(TAG, "ShowInterstitialAdThread.doInBackground.");
        }

        private synchronized void onPostExecute() {
            Log.d(TAG, "ShowInterstitialAdThread.onPostExecute().");
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
        }

        public void startShowAd() {
            start();
            Log.d(TAG, "ShowInterstitialAdThread.startShowAd()");
        }

        public void releaseShowInterstitialAdThread() {
            close();
            Log.d(TAG, "ShowInterstitialAdThread.releaseShowInterstitialAdThread()");
        }

        @Override
        public synchronized void run() {
            Log.d(TAG, "ShowInterstitialAdThread.run()");
            super.run();
            onPreExecute();
            doInBackground();
            onPostExecute();
        }
    }
}
