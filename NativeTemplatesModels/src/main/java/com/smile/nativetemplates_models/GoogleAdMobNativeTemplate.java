package com.smile.nativetemplates_models;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class GoogleAdMobNativeTemplate {
    private static final String TAG = "GoogleNativeTemplate";
    private final FrameLayout mNativeAdsFrameLayout;
    private final NativeTemplateAd mNativeTemplateAd;
    private final com.google.android.ads.nativetemplates.TemplateView mNativeAdTemplateView;

    private final Handler showNativeAdTimerHandler = new Handler(Looper.getMainLooper());
    private final Runnable showNativeAdTimerRunnable = new Runnable() {
        @Override
        public void run() {
            showNativeAdTimerHandler.removeCallbacksAndMessages(null);
            if (mNativeTemplateAd != null) {
                Log.d(TAG, "showNativeAdTimerRunnable.Loading AdMob native ad.");
                mNativeTemplateAd.loadOneAd();
                showNativeAdTimerHandler.postDelayed(this, 300000); // 5 minutes
            }
        }
    };

    public GoogleAdMobNativeTemplate(Context context, FrameLayout nativeAdsFrameLayout, String nativeAdvancedId0
            ,com.google.android.ads.nativetemplates.TemplateView nativeAdTemplateView) {
        this.mNativeAdsFrameLayout = nativeAdsFrameLayout;
        this.mNativeAdTemplateView = nativeAdTemplateView;
        boolean hasNativeId = false;
        if (nativeAdvancedId0 != null) {
            if (!nativeAdvancedId0.isEmpty()) {
                hasNativeId = true;
            }
        }
        if (hasNativeId) {
            this.mNativeTemplateAd = new NativeTemplateAd(context, nativeAdvancedId0
                    , nativeAdTemplateView);
        } else {
            this.mNativeTemplateAd = null;
            hideNativeAd();
        }
    }

    public void showNativeAd() {
        Log.d(TAG, "showNativeAd() is called.");
        if ( (mNativeAdsFrameLayout != null) && (mNativeTemplateAd != null) ) {
            Log.d(TAG, "showNativeAd.nativeAdsFrameLayout.setVisibility(View.VISIBLE)");
            mNativeAdsFrameLayout.setVisibility(View.VISIBLE);
            showNativeAdTimerHandler.post(showNativeAdTimerRunnable);
        }
    }

    public void hideNativeAd() {
        Log.d(TAG, "hideNativeAd() is called.");
        if (mNativeAdsFrameLayout != null) {
            mNativeAdsFrameLayout.setVisibility(View.GONE);
        }
        showNativeAdTimerHandler.removeCallbacksAndMessages(null);
    }

    public void release() {
        if (mNativeTemplateAd != null) {
            mNativeTemplateAd.releaseNativeAd();
        }
        showNativeAdTimerHandler.removeCallbacksAndMessages(null);
    }
}
