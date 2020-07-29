package com.smile.smilelibraries.showing_banner_ads_utility;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.smile.smilelibraries.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;

public class SetBannerAdViewForAdMobOrFacebook {
    private final static String TAG = new String(".SetBannerAdViewForAdMobOrFacebook");
    private final Context context;
    private final LinearLayout companyInfoLayout;
    private final LinearLayout bannerLinearLayout;
    private final String googleAdMobBannerID;
    private final String facebookBannerID;
    private final int bannerDpWidth;
    private com.google.android.gms.ads.AdView adMobBannerAdView;
    private com.facebook.ads.AdView facebookAdView;
    private com.facebook.ads.AdView.AdViewLoadConfig facebookAdViewLoadConfig;

    private final AdRequest adMobBannerAdRequest = new AdRequest.Builder().build();
    private final Handler loggingGoogleImpressionHandler = new Handler(Looper.getMainLooper());
    private final Runnable loggingGoogleImpressionRunnable = new Runnable() {
        @Override
        public void run() {
            loggingGoogleImpressionHandler.removeCallbacksAndMessages(null);
            if (adMobBannerAdView!=null) {
                Log.d(TAG, "Reloading Banner Ad of Google AdMob.");
                adMobBannerAdView.loadAd(adMobBannerAdRequest);
            }
        }
    };
    private final int maxNumberLoadingGoogleAdMobBannerAd = 10;
    private int numberOfLoadingGoogleAdMobBannerAd = 0;
    private int secondsToRetryAdMob = 10000; // 10 seconds

    private final Handler loggingFacebookImpressionHandler = new Handler(Looper.getMainLooper());
    private final Runnable loggingFaceboookImpressionRunnable = new Runnable() {
        @Override
        public void run() {
            loggingFacebookImpressionHandler.removeCallbacksAndMessages(null);
            if (facebookAdViewLoadConfig !=null && facebookAdView!=null) {
                Log.d(TAG, "Reloading Banner Ad of Facebook.");
                facebookAdView.loadAd(facebookAdViewLoadConfig);
            }
        }
    };
    private final int maxNumberLoadingFacebookBannerAd = 10;
    private int numberOfLoadingFacebookBannerAd = 0;
    private int secondsToRetryFacebook = 10000; // 10 seconds

    public SetBannerAdViewForAdMobOrFacebook(Context context, LinearLayout companyInfoLayout
            , LinearLayout bannerLinearLayout
            , String googleAdMobBannerID, String facebookBannerID) {
        this(context, companyInfoLayout, bannerLinearLayout, googleAdMobBannerID, facebookBannerID, 0);
    }

    public SetBannerAdViewForAdMobOrFacebook(Context context, LinearLayout companyInfoLayout
            , LinearLayout bannerLinearLayout
            , String googleAdMobBannerID, String facebookBannerID
            , int bannerDpWidth) {
        this.context = context;
        this.companyInfoLayout = companyInfoLayout;
        this.bannerLinearLayout = bannerLinearLayout;
        this.googleAdMobBannerID = googleAdMobBannerID;
        this.facebookBannerID = facebookBannerID;
        this.bannerDpWidth = bannerDpWidth;
    }

    public void showBannerAdViewFromAdMobOrFacebook(int adProvider) {
        boolean isGoogleAdMobBannerAvaiable = true;
        if (googleAdMobBannerID == null || googleAdMobBannerID.isEmpty()) {
            isGoogleAdMobBannerAvaiable = false;
        }
        boolean isFacebookBannerAvaiable = true;
        if (facebookBannerID == null || facebookBannerID.isEmpty()) {
            isFacebookBannerAvaiable = false;
        }
        if (isGoogleAdMobBannerAvaiable || isFacebookBannerAvaiable) {
            if (companyInfoLayout != null) {
                companyInfoLayout.setVisibility(View.GONE);
            }

            boolean adMobFirst = true;    // true for google, false for facebook
            if (adProvider == ShowingInterstitialAdsUtil.FacebookAdProvider) {
                Log.d(TAG, "ShowingInterstitialAdsUtil.FacebookAdProvider.");
                if (isFacebookBannerAvaiable) {
                    adMobFirst = false;   // facebook first
                    Log.d(TAG, "facebookBannerID is not empty.");
                } else {
                    Log.d(TAG, "facebookBannerID is empty.");
                }
            }
            if (adMobFirst) {
                Log.d(TAG, "ShowingInterstitialAdsUtil.GoogleAdMobAdProvider.");
                // google first
                if (!isGoogleAdMobBannerAvaiable) {
                    // google is is empty so facebook id will not be empty
                    adMobFirst = false;    // no google so facebook first
                }
            }
            if (adMobFirst) {
                // Google AdMob (Banner Ad)
                setGoogleAdMobBannerAdView();
            } else {
                // Facebook Ad (Banner Ad)
                setFacebookAudienceNetworkBannerAdView();
            }
        } else {
            // show company information
            if (bannerLinearLayout != null) {
                bannerLinearLayout.setVisibility(View.GONE);
            }
            if (companyInfoLayout != null) {
                companyInfoLayout.setVisibility(View.VISIBLE);
            }
        }
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

    private void setGoogleAdMobBannerAdView() {
        Log.d(TAG, "Starting the initialization for Banner Ad of Google AdMob.");
        numberOfLoadingGoogleAdMobBannerAd = 0;
        adMobBannerAdView = new com.google.android.gms.ads.AdView(context);
        if (bannerDpWidth <= 0) {
            adMobBannerAdView.setAdSize(AdSize.BANNER);
        } else {
            // adaptive banner
            AdSize adMobAdSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize (context, bannerDpWidth);
            adMobBannerAdView.setAdSize(adMobAdSize);
        }
        adMobBannerAdView.setAdUnitId(googleAdMobBannerID);
        bannerLinearLayout.addView(adMobBannerAdView);
        AdListener adMobBannerListener = new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG, "Could not load Google AdMob Banner ad.");
                numberOfLoadingGoogleAdMobBannerAd++;
                loggingGoogleImpressionHandler.removeCallbacksAndMessages(null);
                if (numberOfLoadingGoogleAdMobBannerAd >= maxNumberLoadingGoogleAdMobBannerAd) {
                    if (adMobBannerAdView != null) {
                        adMobBannerAdView.setVisibility(View.GONE);
                        bannerLinearLayout.removeView(adMobBannerAdView);
                        adMobBannerAdView.destroy();
                        adMobBannerAdView = null;
                        if (numberOfLoadingFacebookBannerAd < maxNumberLoadingFacebookBannerAd) {
                            setFacebookAudienceNetworkBannerAdView();   // switch to Facebook Audience Network
                        }
                    }
                } else {
                    loggingGoogleImpressionHandler.postDelayed(loggingGoogleImpressionRunnable, secondsToRetryAdMob);
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "Succeeded to load Google AdMob Banner ad.");
                numberOfLoadingGoogleAdMobBannerAd = 0;
                loggingGoogleImpressionHandler.removeCallbacksAndMessages(null);
            }
        };
        adMobBannerAdView.setAdListener(adMobBannerListener);
        adMobBannerAdView.loadAd(adMobBannerAdRequest);
    }

    private void setFacebookAudienceNetworkBannerAdView() {
        Log.d(TAG, "Starting the initialization for Banner Ad of Facebook.");
        numberOfLoadingFacebookBannerAd = 0;
        facebookAdView = new com.facebook.ads.AdView(context, facebookBannerID, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        com.facebook.ads.AdListener facebookAdListener = new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.d(TAG, "Could not load Facebook Banner ad.");
                numberOfLoadingFacebookBannerAd++;
                loggingFacebookImpressionHandler.removeCallbacksAndMessages(null);
                if (numberOfLoadingFacebookBannerAd>=maxNumberLoadingFacebookBannerAd) {
                    if (facebookAdView != null) {
                        facebookAdView.setVisibility(View.GONE);
                        bannerLinearLayout.removeView(facebookAdView);
                        facebookAdView.destroy();
                        facebookAdView = null;
                        facebookAdViewLoadConfig = null;
                        if (numberOfLoadingGoogleAdMobBannerAd < maxNumberLoadingGoogleAdMobBannerAd) {
                            setGoogleAdMobBannerAdView();   // switch to Google AdMob
                        }
                    }
                } else {
                    loggingFacebookImpressionHandler.postDelayed(loggingFaceboookImpressionRunnable, secondsToRetryFacebook);
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Ad loaded callback
                Log.d(TAG, "Succeeded to load Facebook Banner ad.");
                numberOfLoadingFacebookBannerAd = 0;
                loggingFacebookImpressionHandler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d(TAG, "Clicked Facebook Banner ad.");
                loggingFacebookImpressionHandler.removeCallbacksAndMessages(null);
                loggingFacebookImpressionHandler.postDelayed(loggingFaceboookImpressionRunnable, 120000); // 2 minute
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                Log.d(TAG, "Logging impression of Facebook Banner ad.");
                loggingFacebookImpressionHandler.postDelayed(loggingFaceboookImpressionRunnable, 120000); // 2 minute
            }
        };

        facebookAdViewLoadConfig = facebookAdView.buildLoadAdConfig()
                .withAdListener(facebookAdListener).build();
        bannerLinearLayout.addView(facebookAdView);
        facebookAdView.loadAd(facebookAdViewLoadConfig);
    }
}
