package com.smile.colorballs;

import static com.smile.colorballs.R.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Handler;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.smile.colorballs.constants.Constants;
import com.smile.colorballs.services.GlobalTop10Service;
import com.smile.colorballs.services.LocalTop10Service;
import com.smile.colorballs.views.MyView;
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate;
import com.smile.colorballs.presenters.MyPresenter;
import com.smile.smilelibraries.models.ExitAppTimer;
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil;
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView;
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial;
import com.smile.smilelibraries.utilities.ScreenUtil;
import com.smile.colorballs.coroutines.*;
import java.util.ArrayList;

public class MyActivity extends MyView {

    // private properties
    private static final String TAG = "MyActivity";
    private static final String Top10FragmentTag = "Top10FragmentTag";
    private float fontScale;
    private float screenWidth;
    private float screenHeight;
    private Toolbar supportToolbar;
    private Top10Fragment top10Fragment = null;
    private MyBroadcastReceiver myReceiver;
    private float mainGameViewWidth;
    private int cellWidth;
    private int cellHeight;
    private GoogleAdMobNativeTemplate nativeTemplate;
    private SetBannerAdView myBannerAdView;
    private SetBannerAdView myBannerAdView2;
    private ActivityResultLauncher<Intent> settingLauncher;
    private ActivityResultLauncher<Intent> top10Launcher;
    private ShowInterstitial interstitialAd = null;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() is called");
        mPresenter = new MyPresenter(this,this);
        ColorBallsApp application = (ColorBallsApp)getApplication();
        interstitialAd = new ShowInterstitial(this, application.facebookAds,
                    application.googleInterstitialAd);
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            if (ScreenUtil.isTablet(this)) {
                // Table then change orientation to Landscape
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                // phone then change orientation to Portrait
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        setContentView(layout.activity_my);

        createActivityUI();

        createGameView();
        createColorBallsGame(savedInstanceState);

        setBannerAndNativeAdUI();

        setBroadcastReceiver();

        settingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "settingLauncher.result received");
                    int resultCode = result.getResultCode();
                    if (resultCode == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) return;
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            boolean hasSound = extras.getBoolean(Constants.HAS_SOUND);
                            mPresenter.setHasSound(hasSound);
                            boolean isEasyLevel = extras.getBoolean(Constants.IS_EASY_LEVEL);
                            mPresenter.setEasyLevel(isEasyLevel);
                            boolean hasNextBall = extras.getBoolean(Constants.HAS_NEXT_BALL);
                            mPresenter.setHasNextBall(hasNextBall, true);
                        }
                    }
                    ColorBallsApp.isProcessingJob = false;  // setting activity finished
                });
        top10Launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "top10Launcher.result received");
                    int resultCode = result.getResultCode();
                    if (resultCode == Activity.RESULT_OK) {
                        Log.d(TAG, "top10Launcher.Showing interstitial ads");
                        showInterstitialAd();
                    }
                    ColorBallsApp.isShowingLoadingMessage = false;
                    ColorBallsApp.isProcessingJob = false;
                });

        Log.d(TAG, "onCreate() is finished.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);

        // final Context wrapper = new ContextThemeWrapper(this, R.style.menu_text_style);
        // or
        final int popupThemeId = supportToolbar.getPopupTheme();
        final Context wrapper = new ContextThemeWrapper(this, popupThemeId);
        //

        // ScreenUtil.buildActionViewClassMenu(this, wrapper, menu, fScale, ScreenUtil.FontSize_Pixel_Type);
        ScreenUtil.resizeMenuTextIconSize(wrapper, menu, fontScale);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        boolean isProcessingJob = ColorBallsApp.isProcessingJob;

        if (isProcessingJob) {
            return false;
        }
        int id = item.getItemId();
        if (id == R.id.undoGame) {
            mPresenter.undoTheLast();
        } else if (id == R.id.globalTop10) {
            showTop10ScoreHistory(false);
        } else if (id == R.id.localTop10) {
            showTop10ScoreHistory(true);
        } else if (id == R.id.setting) {
            Log.d(TAG, "onOptionsItemSelected.settingLauncher.launch(intent)");
            ColorBallsApp.isProcessingJob = true;    // started procession job
            Intent intent = new Intent(this, SettingActivity.class);
            Bundle extras = new Bundle();
            extras.putBoolean(Constants.HAS_SOUND, mPresenter.hasSound());
            extras.putBoolean(Constants.IS_EASY_LEVEL, mPresenter.isEasyLevel());
            extras.putBoolean(Constants.HAS_NEXT_BALL, mPresenter.hasNextBall());
            intent.putExtras(extras);
            settingLauncher.launch(intent);
        } else if (id == R.id.saveGame) {
            mPresenter.saveGame();
        } else if (id == R.id.loadGame) {
            mPresenter.loadGame();
        } else if (id == R.id.newGame) {
            mPresenter.newGame();
        } else if (id == R.id.quitGame) {
            mPresenter.quitGame(); //  exit game
        } else if (id == R.id.privacyPolicy) {
            PrivacyPolicyUtil.startPrivacyPolicyActivity(this, 10);
        } else {
            // return super.onOptionsItemSelected(item);
            return false;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "MyActivity.onSaveInstanceState() is called");

        if (isChangingConfigurations()) {
            // configuration is changing then remove top10Fragment
            if (top10Fragment != null) {
                // remove top10Fragment
                FragmentManager fmManager = getSupportFragmentManager();
                FragmentTransaction ft = fmManager.beginTransaction();
                ft.remove(top10Fragment);
                if (top10Fragment.isStateSaved()) {
                    Log.d(TAG, "top10Fragment.isStateSaved() = true");
                } else {
                    Log.d(TAG, "top10Fragment.isStateSaved() = false");
                    // ft.commit(); // removed on 2021-01-24
                }
                ft.commitAllowingStateLoss();   // added on 2021-01-24
                ColorBallsApp.isShowingLoadingMessage = false;
                ColorBallsApp.isProcessingJob = false;
            }
        }

        if (saveScoreAlertDialog != null) {
            saveScoreAlertDialog.dismiss();
        }

        mPresenter.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MyActivity.onStart() is called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MyActivity.onResume() is called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MyActivity.onPause() is called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MyActivity.onStop() is called");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() is called");
        if (mPresenter != null) {
            mPresenter.release();
        }
        if (interstitialAd != null) interstitialAd.releaseInterstitial();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(myReceiver);
        if (myBannerAdView != null) {
            myBannerAdView.pause();
            myBannerAdView.destroy();
            myBannerAdView = null;
        }
        if (myBannerAdView2 != null) {
            myBannerAdView2.pause();
            myBannerAdView2.destroy();
            myBannerAdView2 = null;
        }
        if (nativeTemplate != null) {
            nativeTemplate.release();
        }
        if (sureSaveDialog != null) {
            sureSaveDialog.dismissAllowingStateLoss();
        }
        if (warningSaveGameDialog != null) {
            warningSaveGameDialog.dismissAllowingStateLoss();
        }
        if (sureLoadDialog != null) {
            sureLoadDialog.dismissAllowingStateLoss();
        }
        if (gameOverDialog != null) {
            gameOverDialog.dismissAllowingStateLoss();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        ExitAppTimer exitAppTimer = ExitAppTimer.getInstance(1000); // singleton class
        if (exitAppTimer.canExit()) {
            mPresenter.quitGame();   //   from   END PROGRAM
        } else {
            exitAppTimer.start();
            float toastFontSize = getTextFontSize() * 0.7f;
            Log.d(TAG, "toastFontSize = " + toastFontSize);
            ScreenUtil.showToast(MyActivity.this, getString(string.backKeyToExitApp), toastFontSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
            // ShowToastMessage.showToast(this, getString(R.string.backKeyToExitApp), toastFontSize, ScreenUtil.FontSize_Pixel_Type, 2000);
        }
    }
    private void findOutTextFontSize() {
        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, ScreenUtil.FontSize_Pixel_Type, null);
        setTextFontSize(ScreenUtil.suitableFontSize(this, defaultTextFontSize, ScreenUtil.FontSize_Pixel_Type, 0.0f));
        fontScale = ScreenUtil.suitableFontScale(this, ScreenUtil.FontSize_Pixel_Type, 0.0f);
    }

    private void findOutScreenSize() {
        Point size = ScreenUtil.getScreenSize(this);
        screenWidth = size.x;
        screenHeight = size.y;
        Log.d(TAG, "screenWidth = " + screenWidth);
        Log.d(TAG, "screenHeight = " + screenHeight);
        float statusBarHeight = ScreenUtil.getStatusBarHeight(this);
        Log.d(TAG, "statusBarHeight = " + statusBarHeight);
        float actionBarHeight = ScreenUtil.getActionBarHeight(this);
        Log.d(TAG, "actionBarHeight = " + actionBarHeight);
        float navigationBarHeight = ScreenUtil.getNavigationBarHeight(this);
        Log.d(TAG, "navigationBarHeight = " + navigationBarHeight);
        // keep navigation bar
        screenHeight = screenHeight - statusBarHeight - actionBarHeight;
    }

    private void setUpSupportActionBar() {
        supportToolbar = findViewById(id.colorBallToolbar);
        setSupportActionBar(supportToolbar);
        androidx.appcompat.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void createActivityUI() {
        findOutTextFontSize();
        findOutScreenSize();
        setUpSupportActionBar();
    }

    private void createGameView() {
        // find Out Width and Height of GameView
        LinearLayout linearLayout_myActivity = findViewById(id.linearLayout_myActivity);
        float main_WeightSum = linearLayout_myActivity.getWeightSum();
        Log.d(TAG, "main_WeightSum = " + main_WeightSum);

        LinearLayout gameViewLinearLayout = findViewById(id.gameViewLinearLayout);
        LinearLayout.LayoutParams gameViewLp = (LinearLayout.LayoutParams) gameViewLinearLayout.getLayoutParams();
        float gameView_Weight = gameViewLp.weight;
        Log.d(TAG, "gameView_Weight = " + gameView_Weight);
        float mainGameViewHeight = screenHeight * gameView_Weight / main_WeightSum;
        Log.d(TAG, "mainGameViewHeight = " + mainGameViewHeight);

        float gameViewWeightSum = gameViewLinearLayout.getWeightSum();
        Log.d(TAG, "gameViewWeightSum = " + gameViewWeightSum);
        LinearLayout mainGameViewUiLayout = findViewById(id.gameViewLayout);
        LinearLayout.LayoutParams mainGameViewUiLayoutParams = (LinearLayout.LayoutParams) mainGameViewUiLayout.getLayoutParams();
        float mainGameViewUi_weight = mainGameViewUiLayoutParams.weight;
        Log.d(TAG, "mainGameViewUi_weight = " + mainGameViewUi_weight);
        mainGameViewWidth = screenWidth * (mainGameViewUi_weight / gameViewWeightSum);
        Log.d(TAG, "mainGameViewWidth = " + mainGameViewWidth);

        // display the highest score and current score
        highestScoreTextView = supportToolbar.findViewById(id.highestScoreTextView);
        ScreenUtil.resizeTextSize(highestScoreTextView, getTextFontSize(), ScreenUtil.FontSize_Pixel_Type);

        currentScoreTextView = supportToolbar.findViewById(id.currentScoreTextView);
        ScreenUtil.resizeTextSize(currentScoreTextView, getTextFontSize(), ScreenUtil.FontSize_Pixel_Type);

        FrameLayout gridPartFrameLayout = findViewById(id.gridPartFrameLayout);
        LinearLayout.LayoutParams frameLp = (LinearLayout.LayoutParams) gridPartFrameLayout.getLayoutParams();

        // for 9 x 9 grid: main part of this game
        GridLayout gridCellsLayout = findViewById(id.gridCellsLayout);
        int rowCounts = gridCellsLayout.getRowCount();
        int colCounts = gridCellsLayout.getColumnCount();

        cellWidth = (int) (mainGameViewWidth / colCounts);
        Log.d(TAG, "cellWidth = " + cellWidth);
        if (mainGameViewWidth > mainGameViewHeight) {
            // if screen width greater than 8-10th of screen height
            cellWidth = (int)(mainGameViewHeight / rowCounts);
        }
        cellHeight = cellWidth;
        Log.d(TAG, "cellHeight = " + cellHeight);

        // added on 2018-10-02 to test and it works
        // setting the width and the height of GridLayout by using the FrameLayout that is on top of it
        frameLp.width = cellWidth * colCounts;
        frameLp.topMargin = 20;
        frameLp.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams oneBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneBallLp.width = cellWidth;
        oneBallLp.height = cellHeight;
        oneBallLp.gravity = Gravity.CENTER;

        mPresenter.setRowCounts(rowCounts);
        mPresenter.setColCounts(colCounts);

        // set listener for each ImageView
        ImageView imageView;
        int imId;
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < colCounts; j++) {
                // imId = i * rowCounts + j;
                imId = mPresenter.getImageId(i, j);
                imageView = new ImageView(this);
                imageView.setId(imId);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setBackgroundResource(drawable.box_image);
                imageView.setClickable(true);
                imageView.setOnClickListener(v -> {
                    if ((mPresenter.completedAll()) && (!ColorBallsApp.isProcessingJob)) {
                        Log.d(TAG, "createGameView.onClick");
                        mPresenter.drawBallsAndCheckListener(v);
                    }
                });
                gridCellsLayout.addView(imageView, imId, oneBallLp);
            }
        }

        scoreImageView = findViewById(id.scoreImageView);
        scoreImageView.setVisibility(View.GONE);
    }

    private void createColorBallsGame(Bundle savedInstanceState) {
        saveScoreAlertDialog = null;
        mPresenter.initializeColorBallsGame(cellWidth, cellHeight, savedInstanceState);
    }

    @Override
    public void setDialogStyle(@NonNull DialogInterface dialog) {
        AlertDialog dlg = (AlertDialog)dialog;
        Window window = dlg.getWindow();
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.0f); // no dim for background screen
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(drawable.dialog_board_image);
        }

        Button nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
        ScreenUtil.resizeTextSize(nBtn, getTextFontSize(), ScreenUtil.FontSize_Pixel_Type);
        nBtn.setTypeface(Typeface.DEFAULT_BOLD);
        nBtn.setTextColor(Color.RED);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)nBtn.getLayoutParams();
        layoutParams.weight = 10;
        nBtn.setLayoutParams(layoutParams);

        Button pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        ScreenUtil.resizeTextSize(pBtn, getTextFontSize(), ScreenUtil.FontSize_Pixel_Type);
        pBtn.setTypeface(Typeface.DEFAULT_BOLD);
        pBtn.setTextColor(Color.rgb(0x00,0x64,0x00));
        pBtn.setLayoutParams(layoutParams);
    }

    @Override
    public void showInterstitialAd() {
        Log.d(TAG, "showInterstitialAd = " + interstitialAd);
        if (interstitialAd == null) {
            return;
        }
        interstitialAd.new ShowAdThread().startShowAd(0);    // AdMob first
    }

    @Override
    public void quitOrNewGame(final int entryPoint) {
        if (entryPoint==0) {
            //  END PROGRAM
            exitApplication();
        } else if (entryPoint==1) {
            //  NEW GAME
            createColorBallsGame(null);
        }
        ColorBallsApp.isProcessingJob = false;
    }

    private void showTop10ScoreHistory(boolean isLocal) {
        Log.d(TAG, "showTop10ScoreHistory.");
        ColorBallsApp.isProcessingJob = true;
        ColorBallsApp.isShowingLoadingMessage = true;
        showMessageOnScreen(getString(string.loadingStr));
        Intent myIntent;
        if (isLocal) {
            // myIntent = new Intent(this, LocalTop10Service.class);
            LocalTop10Coroutine.Companion.getLocalTop10(getApplicationContext());
        } else {
            myIntent = new Intent(this, GlobalTop10Service.class);
            myIntent.putExtra(Constants.GAME_ID_STRING, "1");
            startService(myIntent);
        }
    }

    private void setBannerAndNativeAdUI() {
        LinearLayout bannerLinearLayout = findViewById(id.linearlayout_banner_myActivity);
        LinearLayout adaptiveBannerLayout = findViewById(id.linearlayout_adaptiveBanner_myActivity);
        String testString = "";
        // for debug mode
        if (com.smile.colorballs.BuildConfig.DEBUG) {
            testString = "IMG_16_9_APP_INSTALL#";
        }
        String facebookBannerID = testString + ColorBallsApp.facebookBannerID;
        String facebookBannerID2 = testString + ColorBallsApp.facebookBannerID2;
        //
        int adaptiveBannerWidth, adaptiveBannerDpWidth;

        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // show AdMob native ad if the device is tablet
            String nativeAdvancedId0 = ColorBallsApp.googleAdMobNativeID;     // real native ad unit id
            FrameLayout nativeAdsFrameLayout = findViewById(id.nativeAdsFrameLayout);
            com.google.android.ads.nativetemplates.TemplateView nativeAdTemplateView
                    = findViewById(id.nativeAdTemplateView);
            nativeTemplate = new GoogleAdMobNativeTemplate(this, nativeAdsFrameLayout
                    , nativeAdvancedId0, nativeAdTemplateView);
            nativeTemplate.showNativeAd();
            adaptiveBannerWidth = (int)(screenWidth - mainGameViewWidth);
            adaptiveBannerDpWidth = ScreenUtil.pixelToDp(this, adaptiveBannerWidth);
        } else {
            // one more banner (adaptive banner) ad for orientation is portrait
            adaptiveBannerWidth = (int)mainGameViewWidth;
            adaptiveBannerDpWidth = ScreenUtil.pixelToDp(this, adaptiveBannerWidth);
            myBannerAdView2 = new SetBannerAdView(this, null,
                    adaptiveBannerLayout, ColorBallsApp.googleAdMobBannerID2,
                    facebookBannerID2, adaptiveBannerDpWidth);
            myBannerAdView2.showBannerAdView(0);    // AdMob first
        }
        // normal banner
        Log.d(TAG, "adaptiveBannerDpWidth = " + adaptiveBannerDpWidth);
        myBannerAdView = new SetBannerAdView(this, null,
                bannerLinearLayout, ColorBallsApp.googleAdMobBannerID,
                facebookBannerID, adaptiveBannerDpWidth);
        myBannerAdView.showBannerAdView(0); // AdMob first
    }

    private void setBroadcastReceiver() {
        myReceiver = new MyBroadcastReceiver();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(GlobalTop10Service.ACTION_NAME);
        myIntentFilter.addAction(LocalTop10Service.ACTION_NAME);
        myIntentFilter.addAction(LocalTop10Coroutine.ACTION_NAME);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(myReceiver, myIntentFilter);
    }

    private void exitApplication() {
        final Handler handlerClose = new Handler(Looper.getMainLooper());
        final int timeDelay = 200;
        // exit application
        handlerClose.postDelayed(this::finish,timeDelay);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionName = intent.getAction();
            Log.d(TAG, "MyBroadcastReceiver.actionName = " + actionName);
            if (actionName != null) {
                Bundle extras;
                ArrayList<String> playerNames = null;
                ArrayList<Integer> playerScores = null;
                View historyView;
                int top10LayoutId = id.top10Layout;
                String top10ScoreTitle;
                if (actionName.equals(GlobalTop10Service.ACTION_NAME)) {
                    top10ScoreTitle = getString(string.globalTop10Score);
                } else {
                    top10ScoreTitle = getString(string.localTop10Score);
                }
                extras = intent.getExtras();
                if (extras != null) {
                    playerNames = extras.getStringArrayList(Constants.PLAYER_NAMES);
                    playerScores = extras.getIntegerArrayList(Constants.PLAYER_SCORES);
                }
                if (playerNames == null || playerScores == null) {
                    playerNames = new ArrayList<>();
                    playerScores = new ArrayList<>();
                    // failed
                    playerNames.add("MyBroadcastReceiver.Failed to access Score SQLite database");
                    playerScores.add(0);
                }
                Log.d(TAG, "MyBroadcastReceiver.playerNames.size() = " + playerNames.size());
                historyView = findViewById(top10LayoutId);
                if (historyView != null) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Log.d(TAG, "MyBroadcastReceiver.ORIENTATION_LANDSCAPE");
                        top10Fragment = Top10Fragment.Companion.newInstance(top10ScoreTitle, playerNames,
                                playerScores, activity -> {
                                    if (top10Fragment != null) {
                                        Log.d(TAG, "MyBroadcastReceiver.Top10OkButtonListener");
                                        // remove top10Fragment to dismiss the top 10 score screen
                                        FragmentManager fmManager = getSupportFragmentManager();
                                        FragmentTransaction ft = fmManager.beginTransaction();
                                        ft.remove(top10Fragment);
                                        // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                                        ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                                        showInterstitialAd();
                                    }
                                });
                        FragmentManager fmManager = getSupportFragmentManager();
                        FragmentTransaction ft = fmManager.beginTransaction();
                        Fragment currentTop10Fragment = fmManager.findFragmentByTag(Top10FragmentTag);
                        if (currentTop10Fragment == null) {
                            ft.add(top10LayoutId, top10Fragment, Top10FragmentTag);
                        } else {
                            ft.replace(top10LayoutId, top10Fragment, Top10FragmentTag);
                        }
                        // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                        ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                    }
                } else {
                    // for Portrait
                    Log.d(TAG, "MyBroadcastReceiver.ORIENTATION_PORTRAIT");
                    top10Fragment = null;
                    Intent top10Intent = new Intent(getApplicationContext(), Top10Activity.class);
                    Bundle top10Extras = new Bundle();
                    top10Extras.putString(Constants.TOP10_TITLE_NAME, top10ScoreTitle);
                    top10Extras.putStringArrayList(Constants.TOP10_PLAYERS, playerNames);
                    top10Extras.putIntegerArrayList(Constants.TOP10_SCORES, playerScores);
                    top10Intent.putExtras(top10Extras);
                    top10Launcher.launch(top10Intent);
                }
                dismissShowMessageOnScreen();
            }
            ColorBallsApp.isShowingLoadingMessage = false;
            ColorBallsApp.isProcessingJob = false;
        }
    }
}
