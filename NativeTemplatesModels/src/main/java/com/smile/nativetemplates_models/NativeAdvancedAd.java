package com.smile.nativetemplates_models;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class NativeAdvancedAd {
    private static final String TAG = "NativeAdvancedAd";

    private final Context mContext;
    private final ViewGroup mParentView;
    private final int mNativeAdViewLayoutId;
    private final int maxNumberOfLoad = 15;
    private final AdLoader mNativeAdLoader;
    private NativeAd mNativeAd;
    private boolean isNativeAdLoaded;
    private int mNumberOfLoad;

    public NativeAdvancedAd(Context context, String nativeAdID, ViewGroup viewGroup, int layoutId) {
        mContext = context;
        mParentView = viewGroup;
        mNativeAdViewLayoutId = layoutId;
        mNativeAd = null;
        mNumberOfLoad = 0;
        AdLoader.Builder builder = new AdLoader.Builder(mContext, nativeAdID);
        // OnNativeAdLoadedListener() implementation.
        builder.forNativeAd(nativeAd -> {
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            if (mNativeAd != null) {
                mNativeAd.destroy();
            }
            mNativeAd = nativeAd;
            isNativeAdLoaded = true;
            mNumberOfLoad = 0;
            displayNativeAd(mParentView, mNativeAdViewLayoutId);
            Log.d(TAG, "onNativeAdLoaded.Succeeded to load NativeAd.");
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);
        mNativeAdLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d(TAG, "onAdFailedToLoad");
                Log.d(TAG, "onAdFailedToLoad.mNumberOfLoad = " + mNumberOfLoad);
                isNativeAdLoaded = false;
                if (mNumberOfLoad<maxNumberOfLoad) {
                    loadOneAd();
                    Log.d(TAG, "onAdFailedToLoad.mNumberOfLoad = " + mNumberOfLoad);
                } else {
                    mNumberOfLoad = 0;   // set back to zero
                    Log.d(TAG, "Failed to load NativeAd more than 5.\nSo stopped loading this time. ");
                }
            }
        }).build();
    }

    public void loadOneAd() {
        mNativeAdLoader.loadAd(new AdRequest.Builder().build());
        mNumberOfLoad++;
    }

    public void displayNativeAd(ViewGroup parent, int nativead_view_layout) {

        // Inflate a layout and add it to the parent ViewGroup.
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        NativeAdView nativeAdView = (NativeAdView) inflater
                .inflate(nativead_view_layout, null);

        // Locate the view that will hold the headline, set its text, and call the
        // NativeAdView's setHeadlineView method to register it.
        // TextView headlineView = nativeAdView.findViewById(R.id.native_ad_headline);
        // ScreenUtil.resizeTextSize(headlineView, textSize, ScreenUtil.FontSize_Pixel_Type);
        // headlineView.setText(nativeAd.getHeadline());
        // nativeAdView.setHeadlineView(headlineView);

        // ...
        // Repeat the above process for the other assets in the NativeAd
        // using additional view objects (Buttons, ImageViews, etc).
        // ...

        // If the app is using a MediaView, it should be
        // instantiated and passed to setMediaView. This view is a little different
        // in that the asset is populated automatically, so there's one less step.

        // MediaView mediaView = (MediaView) nativeAdView.findViewById(R.id.native_ad_media);
        MediaView mediaView = new MediaView(mContext);
        nativeAdView.addView(mediaView);

        nativeAdView.setMediaView(mediaView);

        // Call the NativeAdView's setNativeAd method to register the
        // NativeAdObject.
        nativeAdView.setNativeAd(mNativeAd);

        // Ensure that the parent view doesn't already contain an ad view.
        parent.removeAllViews();

        // Place the AdView into the parent.
        parent.addView(nativeAdView);
    }

    public AdLoader getNativeAdLoader() {
        return mNativeAdLoader;
    }

    public NativeAd getNativeAd() {
        return mNativeAd;
    }

    public boolean isNativeAdLoaded() {
        return isNativeAdLoaded;
    }

    public void releaseNativeAd() {
        if (mNativeAd != null) {
            mNativeAd.destroy();
        }
    }
}
