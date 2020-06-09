package com.smile.nativetemplates_models;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.smile.nativetemplates_models.NativeTemplateAd;

public class GoogleAdMobNativeTemplate {
    private static final String TAG = new String(".GoogleAdMobNativeTemplate");
    private final Context context;
    private final FrameLayout nativeAdsFrameLayout;
    private final String nativeAdvancedId0;
    private final NativeTemplateAd nativeTemplateAd;
    private final com.google.android.ads.nativetemplates.TemplateView nativeAdTemplateView;

    private final Handler showNativeAdTimerHandler = new Handler(Looper.getMainLooper());
    private final Runnable showNativeAdTimerRunnable = new Runnable() {
        @Override
        public void run() {
            showNativeAdTimerHandler.removeCallbacksAndMessages(null);
            if (nativeTemplateAd != null) {
                Log.d(TAG, "Loading AdMob native ad.");
                nativeTemplateAd.loadOneAd();
                showNativeAdTimerHandler.postDelayed(this, 300000); // 5 minutes
            }
        }
    };

    public GoogleAdMobNativeTemplate(Context context, FrameLayout nativeAdsFrameLayout, String nativeAdvancedId0
            ,com.google.android.ads.nativetemplates.TemplateView nativeAdTemplateView) {
        this.context = context;
        this.nativeAdsFrameLayout = nativeAdsFrameLayout;
        this.nativeAdvancedId0 = nativeAdvancedId0;
        this.nativeAdTemplateView = nativeAdTemplateView;
        boolean hasNativeId = false;
        if (nativeAdvancedId0 != null) {
            if (!nativeAdvancedId0.isEmpty()) {
                hasNativeId = true;
            }
        }
        if (hasNativeId) {
            this.nativeTemplateAd = new NativeTemplateAd(context, nativeAdvancedId0
                    , nativeAdTemplateView);
        } else {
            this.nativeTemplateAd = null;
            hideNativeAd();
        }
    }

    public void showNativeAd() {
        Log.d(TAG, "showNativeAd() is called.");
        if ( (nativeAdsFrameLayout != null) && (nativeTemplateAd != null) ) {
            nativeAdsFrameLayout.setVisibility(View.VISIBLE);
            if (showNativeAdTimerHandler != null) {
                showNativeAdTimerHandler.post(showNativeAdTimerRunnable);
            }
        }
    }

    public void hideNativeAd() {
        Log.d(TAG, "hideNativeAd() is called.");
        if (nativeAdsFrameLayout != null) {
            nativeAdsFrameLayout.setVisibility(View.GONE);
        }
        if (showNativeAdTimerHandler != null) {
            showNativeAdTimerHandler.removeCallbacksAndMessages(null);
        }
    }

    public void release() {
        if (nativeTemplateAd != null) {
            nativeTemplateAd.releaseNativeAd();
        }
        if (showNativeAdTimerHandler != null) {
            showNativeAdTimerHandler.removeCallbacksAndMessages(null);
        }
    }
}
