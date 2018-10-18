package com.smile.smilepublicclasseslibrary.facebookadsutil;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;

public class FacebookBannerAds {

    private final String TAG = new String("com.smile.facebookadsutil.FacebookBannerAds");
    private static boolean shouldLoadAd = true;

    private final Context context;
    private Handler adHandler;
    private Runnable adRunnable;
    private AdView bannerAdView;

    public FacebookBannerAds(Context context, String placementID, int adSizeID) {

        this.context = context;

        AdSize adSize = AdSize.BANNER_HEIGHT_50;    // default
        if (adSizeID != 1) {
            adSize = AdSize.BANNER_HEIGHT_90;   // tablet
        }

        bannerAdView = new AdView(context, placementID, adSize);

        adHandler = new Handler(context.getMainLooper());
        adRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    bannerAdView.loadAd();  // load again
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                // Toast.makeText(MainActivity.this, "Error: " + adError.getErrorMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, "Error: " + adError.getErrorMessage());
                if (shouldLoadAd) {
                    try {
                        adHandler.removeCallbacksAndMessages(null); // remove previous message
                        adHandler.postDelayed(adRunnable, 1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Ad loaded callback
                // Toast.makeText(MainActivity.this, "Ad has been Loaded.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Ad has been Loaded.");
                shouldLoadAd = false;
                try {
                    // adHandler.removeCallbacks(adRunnable);
                    adHandler.removeCallbacksAndMessages(null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                // Toast.makeText(MainActivity.this, "Ad has been Clicked.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Ad has been Clicked.");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                // Toast.makeText(MainActivity.this, "onLoggingImpression.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "onLoggingImpression.");
            }
        });
    }

    public AdView getBannerAdView() {
        return bannerAdView;
    }

    public void showAd(String callingObject) {
        Log.e(TAG, callingObject + " calling showAd() method.");
        if (shouldLoadAd) {
            Log.e(TAG, callingObject + " loading Ad now.");
            try {
                bannerAdView.loadAd();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void close() {
        // destroy the instance of facebook ads
        try {
            if (bannerAdView != null) {
                adHandler.removeCallbacksAndMessages(null);
                bannerAdView.destroy();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
