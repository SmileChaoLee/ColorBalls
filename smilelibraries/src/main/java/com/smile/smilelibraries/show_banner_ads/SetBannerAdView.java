package com.smile.smilelibraries.show_banner_ads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.common.util.VisibleForTesting;

public class SetBannerAdView {
    private final static String TAG = "SetBannerAdView";
    private final Context context;
    private final LinearLayout companyInfoLayout;
    private final LinearLayout bannerLinearLayout;
    private final String googleAdMobBannerID;
    private final String facebookBannerID;
    private final boolean isAdMobAvailable;
    private final boolean isFacebookAvailable;
    private final int bannerDpWidth;
    private com.google.android.gms.ads.AdView adMobBannerAdView;
    private com.facebook.ads.AdView facebookAdView;
    private com.facebook.ads.AdView.AdViewLoadConfig facebookAdViewLoadConfig;
    private AdRequest adMobBannerAdRequest;
    private final Handler loggingAdmobHandler = new Handler(Looper.getMainLooper());
    @VisibleForTesting
    private final Runnable loggingAdmobRunnable = new Runnable() {
        @Override
        public void run() {
            loggingAdmobHandler.removeCallbacksAndMessages(null);
            if (adMobBannerAdView!=null) {
                Log.d(TAG, "Reloading Banner Ad of Google AdMob.");
                adMobBannerAdRequest = new AdRequest.Builder().build();
                adMobBannerAdView.loadAd(adMobBannerAdRequest);
            }
        }
    };
    private int numberOfLoadingAdMobBannerAd = 0;
    private final int secondsToReLoadAd = 180000;  // 3 minutes

    private com.facebook.ads.AdListener facebookAdListener;
    private final Handler loggingFacebookHandler = new Handler(Looper.getMainLooper());
    private final Runnable loggingFacebookRunnable = new Runnable() {
        @Override
        public void run() {
            loggingFacebookHandler.removeCallbacksAndMessages(null);
            facebookAdViewLoadConfig = facebookAdView.buildLoadAdConfig()
                    .withAdListener(facebookAdListener).build();
            if (facebookAdViewLoadConfig !=null && facebookAdView!=null) {
                Log.d(TAG, "Reloading Banner Ad of Facebook.");
                facebookAdView.loadAd(facebookAdViewLoadConfig);
            }
        }
    };
    private final int maxNumberLoadingBannerAd = 5;
    private int numberOfLoadingFacebookBannerAd = 0;

    public SetBannerAdView(Context context, LinearLayout companyInfoLayout
            , LinearLayout bannerLinearLayout
            , String googleAdMobBannerID, String facebookBannerID) {
        this(context, companyInfoLayout, bannerLinearLayout, googleAdMobBannerID, facebookBannerID, 0);
    }

    public SetBannerAdView(Context context, LinearLayout companyInfoLayout
            , LinearLayout bannerLinearLayout
            , String googleAdMobBannerID, String facebookBannerID
            , int bannerDpWidth) {
        this.context = context;
        this.companyInfoLayout = companyInfoLayout;
        this.bannerLinearLayout = bannerLinearLayout;
        this.googleAdMobBannerID = googleAdMobBannerID;
        this.facebookBannerID = facebookBannerID;
        this.bannerDpWidth = bannerDpWidth;
        isAdMobAvailable = (googleAdMobBannerID!=null && !googleAdMobBannerID.isEmpty());
        isFacebookAvailable = (facebookBannerID!=null && !facebookBannerID.isEmpty());
        if (bannerLinearLayout != null) bannerLinearLayout.removeAllViews();
    }

    public void showBannerAdView(int provider) {
        // provider = 0 --> AdMob first, = 0 --> Facebook first
        if (!isAdMobAvailable && !isFacebookAvailable) {
            // no banner ads so show company information
            if (bannerLinearLayout != null) {
                bannerLinearLayout.setVisibility(View.GONE);
            }
            if (companyInfoLayout != null) {
                companyInfoLayout.setVisibility(View.VISIBLE);
            }
            return;
        }
        if (companyInfoLayout != null) {
            companyInfoLayout.setVisibility(View.GONE);
        }
        // Google AdMob (Banner Ad) first if provider = 0
        if (provider == 0) setAdMobBannerAdView(); else setFacebookBannerAdView();
    }

    public void resume() {
        if (adMobBannerAdView != null) {
            adMobBannerAdView.resume();
        }
        // no resume() on Facebook Audience Network
    }

    public void pause() {
        if (adMobBannerAdView != null) {
            adMobBannerAdView.pause();
        }
        // no pause() on Facebook Audience Network
    }

    public void destroy() {
        if (adMobBannerAdView != null) {
            adMobBannerAdView.destroy();
        }
        if (facebookAdView != null) {
            facebookAdView.destroy();
        }
        facebookAdViewLoadConfig = null;
    }

    @VisibleForTesting
    private void setAdMobBannerAdView() {
        Log.d(TAG, "setAdMobBannerAdView() is called.");
        if (!isAdMobAvailable) {
            setFacebookBannerAdView();
            return;
        }
        Log.d(TAG, "setAdMobBannerAdView.Starting AdMob Banner Ad.");
        numberOfLoadingAdMobBannerAd = 0;
        adMobBannerAdView = new com.google.android.gms.ads.AdView(context);
        if (bannerDpWidth <= 0) {
            adMobBannerAdView.setAdSize(AdSize.BANNER);
        } else {
            // adaptive banner
            AdSize adMobAdSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize (context, bannerDpWidth);
            adMobBannerAdView.setAdSize(adMobAdSize);
        }
        adMobBannerAdView.setVisibility(View.VISIBLE);
        adMobBannerAdView.setAdUnitId(googleAdMobBannerID);
        bannerLinearLayout.addView(adMobBannerAdView);
        AdListener adMobBannerListener = new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG, "Could not load Google AdMob Banner ad.numberOfLoadingAdMobBannerAd = " +
                        numberOfLoadingAdMobBannerAd);
                loggingAdmobHandler.removeCallbacksAndMessages(null);
                numberOfLoadingAdMobBannerAd++;
                if (numberOfLoadingAdMobBannerAd >= maxNumberLoadingBannerAd) {
                    if (numberOfLoadingFacebookBannerAd < maxNumberLoadingBannerAd) {
                        if (adMobBannerAdView != null) {
                            adMobBannerAdView.setVisibility(View.GONE);
                            bannerLinearLayout.removeAllViews();
                            adMobBannerAdView.destroy();
                            adMobBannerAdView = null;
                            // switch to Facebook Audience Network
                            setFacebookBannerAdView();
                        }
                    }
                    numberOfLoadingAdMobBannerAd = 0;   // start over
                } else {
                    loggingAdmobHandler.postDelayed(loggingAdmobRunnable, 10000);
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "Succeeded to load AdMob Banner ad.googleAdMobBannerID = " + googleAdMobBannerID +
                        ", facebookBannerID = " + facebookBannerID);
                numberOfLoadingAdMobBannerAd = 0;
                loggingAdmobHandler.removeCallbacksAndMessages(null);
                loggingAdmobHandler.postDelayed(loggingAdmobRunnable, secondsToReLoadAd);
            }
        };

        adMobBannerAdView.setAdListener(adMobBannerListener);
        adMobBannerAdRequest = new AdRequest.Builder().build();
        adMobBannerAdView.loadAd(adMobBannerAdRequest);
    }

    private void setFacebookBannerAdView() {
        Log.d(TAG, "setFacebookBannerAdView() is called.");
        if (!isFacebookAvailable) {
            setAdMobBannerAdView();
            return;
        }
        Log.d(TAG, "setFacebookBannerAdView.Starting Facebook Banner Ad.");
        numberOfLoadingFacebookBannerAd = 0;
        facebookAdView = new com.facebook.ads.AdView(context, facebookBannerID, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        facebookAdView.setVisibility(View.VISIBLE);
        bannerLinearLayout.addView(facebookAdView);
        facebookAdListener = new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.d(TAG, "Could not load Facebook Banner ad.numberOfLoadingFacebookBannerAd = " +
                        numberOfLoadingFacebookBannerAd);
                loggingFacebookHandler.removeCallbacksAndMessages(null);
                numberOfLoadingFacebookBannerAd++;
                if (numberOfLoadingFacebookBannerAd>= maxNumberLoadingBannerAd) {
                    if (numberOfLoadingAdMobBannerAd < maxNumberLoadingBannerAd) {
                        if (facebookAdView != null) {
                            facebookAdView.setVisibility(View.GONE);
                            bannerLinearLayout.removeAllViews();
                            facebookAdView.destroy();
                            facebookAdView = null;
                            facebookAdViewLoadConfig = null;
                            setAdMobBannerAdView();   // switch to Google AdMob
                        }
                    }
                    numberOfLoadingFacebookBannerAd = 0;
                } else {
                    loggingFacebookHandler.postDelayed(loggingFacebookRunnable, 10000);
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Ad loaded callback
                Log.d(TAG, "Succeeded to load Facebook Banner ad.googleAdMobBannerID = " + googleAdMobBannerID +
                        ", facebookBannerID = " + facebookBannerID);
                numberOfLoadingFacebookBannerAd = 0;
                loggingFacebookHandler.removeCallbacksAndMessages(null);
                loggingFacebookHandler.postDelayed(loggingFacebookRunnable, secondsToReLoadAd);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d(TAG, "Clicked Facebook Banner ad.");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                Log.d(TAG, "Logging impression of Facebook Banner ad.");
            }
        };

        facebookAdViewLoadConfig = facebookAdView.buildLoadAdConfig()
                .withAdListener(facebookAdListener).build();
        facebookAdView.loadAd(facebookAdViewLoadConfig);
    }
}
