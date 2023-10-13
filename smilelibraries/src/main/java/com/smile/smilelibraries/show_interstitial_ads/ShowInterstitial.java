package com.smile.smilelibraries.show_interstitial_ads;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.util.VisibleForTesting;
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitial;
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial;
import com.smile.smilelibraries.interfaces.DismissFunction;

public class ShowInterstitial {
    private final static String TAG = ShowInterstitial.class.getName();
    private final FacebookInterstitial facebookAd;
    private final AdMobInterstitial adMobAd;
    private final Activity mActivity;
    private final Handler synchronizedHandler = new Handler(Looper.getMainLooper());
    private boolean succeededAdMob = false;
    private boolean succeededFacebook = false;
    private boolean finishedPreviousStep = false;

    public ShowInterstitial(Activity activity, FacebookInterstitial facebookAd, AdMobInterstitial adMobAd) {
        this.mActivity = activity;
        this.facebookAd = facebookAd;
        if (this.facebookAd != null) {
            this.facebookAd.loadAd();
        }
        this.adMobAd = adMobAd;
        if (this.adMobAd != null) {
            this.adMobAd.loadAd();
        }
    }

    public void close() {
        try {
            if (facebookAd != null) {
                facebookAd.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class ShowAdThread extends Thread {
        private boolean isAdShown;
        private int provider = 0;   // AdMob first

        public ShowAdThread() {
            this(null);
        }

        public ShowAdThread(DismissFunction dismissFunction) {
            if (adMobAd != null) adMobAd.setDismissFunc(dismissFunction);
            if (facebookAd != null) facebookAd.setDismissFunc(dismissFunction);
            isAdShown = false;
        }

        @VisibleForTesting
        private boolean showOneAd() {
            succeededAdMob = false;
            succeededFacebook = false;
            finishedPreviousStep = false;

            mActivity.runOnUiThread(() -> {
                synchronized (synchronizedHandler) {
                    try {
                        Log.d(TAG, "showOneAd.Start showing");
                        succeededAdMob = false;
                        succeededFacebook = false;
                        if (provider == 0) {
                            // AdMob first
                            if (showAdMob()) {
                                if (!succeededAdMob) {
                                    showFacebook();
                                }
                            } else {
                                showFacebook();
                            }
                        } else {
                            // provider = 1 // facebook first
                            if (showFacebook()) {
                                if (!succeededFacebook) {
                                    showAdMob();
                                }
                            } else {
                                showAdMob();
                            }
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "showOneAd.Exception");
                        ex.printStackTrace();
                    }

                    finishedPreviousStep = true;
                    synchronizedHandler.notifyAll();
                    Log.d(TAG, "showOneAd.synchronizedHandler.notifyAll()");
                }
            });

            synchronized (synchronizedHandler) {
                while (!finishedPreviousStep) {
                    try {
                        synchronizedHandler.wait();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "showOneAd.synchronizedHandler.exception");
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "showOneAd.succeededAdMob = " + succeededAdMob +
                    ", succeededFacebook= " + succeededFacebook);

            return (succeededAdMob || succeededFacebook);
        }

        private boolean showAdMob() {
            boolean hasAdMob = false;
            if (adMobAd != null) {
                if (adMobAd.isLoaded()) {
                    succeededAdMob = adMobAd.showAd(mActivity);
                }
                adMobAd.loadAd();
                hasAdMob = true;
            }
            return hasAdMob;
        }
        private boolean showFacebook() {
            boolean hasFacebook = false;
            if (facebookAd != null) {
                if (facebookAd.isLoaded()) {
                    succeededFacebook = facebookAd.showAd();
                }
                facebookAd.loadAd();
                hasFacebook = true;
            }
            return hasFacebook;
        }

        private synchronized void onPreExecute() {
            isAdShown = showOneAd();
            Log.d(TAG, "ShowAdThread.onPreExecute().isAdShown = " + isAdShown);
        }

        private synchronized void doInBackground() {
            Log.d(TAG, "ShowAdThread.doInBackground.");
        }

        private synchronized void onPostExecute() {
            Log.d(TAG, "ShowAdThread.onPostExecute.succeededFacebook = " + succeededFacebook +
                    ", succeededAdMob = " + succeededAdMob);
        }

        public void startShowAd(int provider) {
            // Google AdMob (Banner Ad) first if provider = 0
            Log.d(TAG, "ShowAdThread.startShowAd()");
            this.provider = provider;
            start();
        }

        public void releaseInterstitial() {
            Log.d(TAG, "ShowAdThread.releaseInterstitial()");
            close();
        }

        @Override
        public synchronized void run() {
            Log.d(TAG, "ShowAdThread.run()");
            super.run();
            onPreExecute();
            doInBackground();
            onPostExecute();
        }
    }
}
