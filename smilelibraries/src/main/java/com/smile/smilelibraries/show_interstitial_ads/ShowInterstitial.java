package com.smile.smilelibraries.show_interstitial_ads;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.smile.smilelibraries.google_ads_util.AdMobInterstitial;
import com.smile.smilelibraries.interfaces.DismissFunction;

public class ShowInterstitial {
    private final static String TAG = ShowInterstitial.class.getName();
    private final AdMobInterstitial adMobAd;
    private final Activity mActivity;
    private final Handler synchronizedHandler = new Handler(Looper.getMainLooper());
    private boolean succeededAdMob = false;
    private boolean finishedPreviousStep = false;
    private DismissFunction mDismissFunction = null;

    public ShowInterstitial(Activity activity, AdMobInterstitial adMobAd) {
        this.mActivity = activity;
        this.adMobAd = adMobAd;
        if (this.adMobAd != null) {
            this.adMobAd.loadAd();
        }
    }

    public void releaseInterstitial() {
        Log.d(TAG, "ShowInterstitial.releaseInterstitial()");
    }

    public class ShowAdThread extends Thread {
        private boolean isAdShown;

        public ShowAdThread() {
            this(null);
        }

        public ShowAdThread(DismissFunction dismissFunction) {
            mDismissFunction = dismissFunction;
            if (adMobAd != null) adMobAd.setDismissFunc(mDismissFunction);
            isAdShown = false;
        }

        private boolean showOneAd() {
            succeededAdMob = false;
            finishedPreviousStep = false;

            mActivity.runOnUiThread(() -> {
                synchronized (synchronizedHandler) {
                    Log.d(TAG, "showOneAd().synchronized.synchronizedHandler");
                    try {
                        Log.d(TAG, "showOneAd().showOneAd.Start showing");
                        succeededAdMob = false;
                        showAdMob();
                    } catch (Exception ex) {
                        Log.e(TAG, "showOneAd.Exception", ex);
                    }

                    finishedPreviousStep = true;
                    synchronizedHandler.notifyAll();
                    Log.d(TAG, "showOneAd.synchronizedHandler.notifyAll()");
                }
            });

            synchronized (synchronizedHandler) {
                while (!finishedPreviousStep) {
                    try {
                        Log.d(TAG, "showOneAd.synchronizedHandler.wait()");
                        synchronizedHandler.wait();
                    } catch (InterruptedException ex) {
                        Log.e(TAG, "showOneAd.synchronizedHandler.exception", ex);
                    }
                }
            }

            Log.d(TAG, "showOneAd.succeededAdMob = " + succeededAdMob);

            return (succeededAdMob);
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

        private synchronized void onPreExecute() {
            isAdShown = showOneAd();
            Log.d(TAG, "ShowAdThread.onPreExecute().isAdShown = " + isAdShown);
        }

        private synchronized void doInBackground() {
            Log.d(TAG, "ShowAdThread.doInBackground.");
            if (mDismissFunction != null) {
                mDismissFunction.backgroundWork();
            }
        }

        private synchronized void onPostExecute() {
            Log.d(TAG, "ShowAdThread.onPostExecute.succeededAdMob = " + succeededAdMob);
            if (mDismissFunction != null) {
                mDismissFunction.afterFinished(isAdShown);
            }
        }

        public void startShowAd() {
            Log.d(TAG, "ShowAdThread.startShowAd()");
            start();
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
