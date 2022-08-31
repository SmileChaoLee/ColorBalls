package com.smile.colorballs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Handler;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.Service.MyGlobalTop10Service;
import com.smile.Service.MyTop10ScoresService;
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate;
import com.smile.presenters.MyActivityPresenter;
import com.smile.smilelibraries.models.ExitAppTimer;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil;
import com.smile.smilelibraries.showing_banner_ads_utility.SetBannerAdView;
import com.smile.smilelibraries.showing_interstitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.Locale;

public class MyActivity extends AppCompatActivity implements MyActivityPresenter.PresentView {

    // private properties
    private static final String TAG = "MyActivity";
    private static final String GlobalTop10FragmentTag = "GlobalTop10FragmentTag";
    private static final String LocalTop10FragmentTag = "LocalTop10FragmentTag";
    private final int Max_Saved_Games = 5;
    private final int PrivacyPolicyActivityRequestCode = 10;
    private ShowingInterstitialAdsUtil interstitialAd;
    private MyActivityPresenter mPresenter;
    private float textFontSize;
    private float fontScale;
    private float screenWidth;
    private float screenHeight;
    private Toolbar supportToolbar;
    private Top10ScoreFragment top10ScoreFragment = null;
    private Top10ScoreFragment globalTop10Fragment = null;
    private MyBroadcastReceiver myReceiver;
    private IntentFilter myIntentFilter;
    private float mainGameViewWidth;
    private float mainGameViewHeight;
    private int cellWidth;
    private int cellHeight;
    private LinearLayout bannerLinearLayout;
    private GoogleAdMobNativeTemplate nativeTemplate;
    private SetBannerAdView myBannerAdView;
    private LinearLayout adaptiveBannerLinearLayout;
    private SetBannerAdView myBannerAdView2;
    private ShowingInterstitialAdsUtil.ShowInterstitialAdThread showInterstitialAdThread = null;
    private final static String GameOverDialogTag = "GameOverDialogFragmentTag";
    private ImageView scoreImageView = null;
    private TextView highestScoreTextView;
    private TextView currentScoreTextView;
    private int rowCounts = 9;
    private int colCounts = 9;
    private AlertDialog saveScoreAlertDialog;
    private AlertDialogFragment sureSaveDialog;
    private AlertDialogFragment warningSaveGameDialog;
    private AlertDialogFragment sureLoadDialog;
    private AlertDialogFragment gameOverDialog;
    private ActivityResultLauncher<Intent> settingActivityResultLauncher;
    private ActivityResultLauncher<Intent> localTop10ActivityResultLauncher;
    private ActivityResultLauncher<Intent> globalTop10ActivityResultLauncher;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() is called");

        mPresenter = new MyActivityPresenter(this, this);

        super.onCreate(savedInstanceState);

        ColorBallsApp application = (ColorBallsApp) getApplication();
        interstitialAd = new ShowingInterstitialAdsUtil(this, application.facebookAds, application.googleInterstitialAd);

        if (!BuildConfig.DEBUG) {
            if (ScreenUtil.isTablet(this)) {
                // Table then change orientation to Landscape
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                // phone then change orientation to Portrait
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        setContentView(R.layout.activity_my);

        createActivityUI();

        createGameView(savedInstanceState);

        setBannerAndNativeAdUI();

        setBroadcastReceiver();

        settingActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        int resultCode = result.getResultCode();
                        if (resultCode == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Bundle extras = data.getExtras();
                            if (extras != null) {
                                boolean hasSound = extras.getBoolean(SettingActivity.HasSoundKey);
                                mPresenter.setHasSound(hasSound);
                                boolean isEasyLevel = extras.getBoolean(SettingActivity.IsEasyLevelKey);
                                mPresenter.setEasyLevel(isEasyLevel);
                                boolean hasNextBall = extras.getBoolean(SettingActivity.HasNextBallKey);
                                mPresenter.setHasNextBall(hasNextBall, true);
                            }
                        }
                    }
                });
        localTop10ActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        int resultCode = result.getResultCode();
                        if (resultCode == Activity.RESULT_OK) {
                            Log.d(TAG, "localTop10ActivityResultLauncher --> Showing interstitial ads");
                            showAdUntilDismissed();   // removed for testing
                            ColorBallsApp.isShowingLoadingMessage = false;
                            ColorBallsApp.isProcessingJob = false;
                        }
                    }
                });

        globalTop10ActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        showAdUntilDismissed();
                        ColorBallsApp.isShowingLoadingMessage = false;
                        ColorBallsApp.isProcessingJob = false;
                    }
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

        // ScreenUtil.buildActionViewClassMenu(this, wrapper, menu, fScale, ColorBallsApp.FontSize_Scale_Type);
        ScreenUtil.resizeMenuTextIconSize(wrapper, menu, fontScale);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        boolean isProcessingJob = ColorBallsApp.isProcessingJob;

        int id = item.getItemId();
        if (id == R.id.quitGame) {
            mPresenter.quitGame(); //  exit game
            return true;
        }
        if (id == R.id.newGame) {
            mPresenter.newGame();
            return true;
        }

        if (!isProcessingJob) {
            if (id == R.id.undoGame) {
                mPresenter.undoTheLast();
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.top10) {
                showTop10ScoreHistory();
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.globalTop10) {
                showGlobalTop10History();
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.saveGame) {
                mPresenter.saveGame();
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.loadGame) {
                mPresenter.loadGame();
                return true;
            }
            if (id == R.id.setting) {
                ColorBallsApp.isProcessingJob = true;    // started procession job
                Intent intent = new Intent(this, SettingActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(SettingActivity.HasSoundKey, mPresenter.hasSound());
                extras.putBoolean(SettingActivity.IsEasyLevelKey, mPresenter.isEasyLevel());
                extras.putBoolean(SettingActivity.HasNextBallKey, mPresenter.hasNextBall());
                intent.putExtras(extras);
                settingActivityResultLauncher.launch(intent);

                ColorBallsApp.isProcessingJob = false;
                return true;
            }
            if (id == R.id.privacyPolicy) {
                PrivacyPolicyUtil.startPrivacyPolicyActivity(this, PrivacyPolicyActivityRequestCode);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "MyActivity.onSaveInstanceState() is called");

        if (isChangingConfigurations()) {
            // configuration is changing then remove top10ScoreFragment and globalTop10Fragment
            if (top10ScoreFragment != null) {
                // remove top10ScoreFragment
                FragmentManager fmManager = getSupportFragmentManager();
                FragmentTransaction ft = fmManager.beginTransaction();
                ft.remove(top10ScoreFragment);
                if (top10ScoreFragment.isStateSaved()) {
                    Log.d(TAG, "top10ScoreFragment.isStateSaved() = true");
                } else {
                    Log.d(TAG, "top10ScoreFragment.isStateSaved() = false");
                    // ft.commit(); // removed on 2021-01-24
                }
                ft.commitAllowingStateLoss();   // added on 2021-01-24
                ColorBallsApp.isShowingLoadingMessage = false;
                ColorBallsApp.isProcessingJob = false;
            }
            if (globalTop10Fragment != null) {
                // remove globalTop10Fragment
                FragmentManager fmManager = getSupportFragmentManager();
                FragmentTransaction ft = fmManager.beginTransaction();
                ft.remove(globalTop10Fragment);
                if (globalTop10Fragment.isStateSaved()) {
                    Log.d(TAG, "globalTop10Fragment.isStateSaved() = true");
                } else {
                    Log.d(TAG, "globalTop10Fragment.isStateSaved() = false");
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
        Log.d(TAG, "MyActivity.onDestroy() is called");

        if (mPresenter != null) {
            mPresenter.release();
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(myReceiver);

        if (myBannerAdView != null) {
            myBannerAdView.destroy();
            myBannerAdView = null;
        }
        if (myBannerAdView2 != null) {
            myBannerAdView2.destroy();
            myBannerAdView2 = null;
        }
        if (nativeTemplate != null) {
            nativeTemplate.release();
        }
        if (interstitialAd != null) {
            interstitialAd.close();
            interstitialAd = null;
        }

        if (showInterstitialAdThread != null) {
            // avoiding memory leak
            showInterstitialAdThread.releaseShowInterstitialAdThread();
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
            float toastFontSize = textFontSize * 0.7f;
            Log.d(TAG, "toastFontSize = " + toastFontSize);
            ScreenUtil.showToast(MyActivity.this, getString(R.string.backKeyToExitApp), toastFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
            // ShowToastMessage.showToast(this, getString(R.string.backKeyToExitApp), toastFontSize, ColorBallsApp.FontSize_Scale_Type, 2000);
        }
    }
    private void findOutTextFontSize() {
        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, ColorBallsApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, ColorBallsApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, ColorBallsApp.FontSize_Scale_Type, 0.0f);
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
        supportToolbar = findViewById(R.id.colorballs_toolbar);
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

    private void createGameView(Bundle savedInstanceState) {
        // find Out Width and Height of GameView
        LinearLayout linearLayout_myActivity = findViewById(R.id.linearLayout_myActivity);
        float main_WeightSum = linearLayout_myActivity.getWeightSum();
        Log.d(TAG, "main_WeightSum = " + main_WeightSum);

        LinearLayout gameViewLinearLayout = findViewById(R.id.gameViewLinearLayout);
        LinearLayout.LayoutParams gameViewLp = (LinearLayout.LayoutParams) gameViewLinearLayout.getLayoutParams();
        float gameView_Weight = gameViewLp.weight;
        Log.d(TAG, "gameView_Weight = " + gameView_Weight);
        mainGameViewHeight = screenHeight * gameView_Weight / main_WeightSum;
        Log.d(TAG, "mainGameViewHeight = " + mainGameViewHeight);

        float gameViewWeightSum = gameViewLinearLayout.getWeightSum();
        Log.d(TAG, "gameViewWeightSum = " + gameViewWeightSum);
        LinearLayout mainGameViewUiLayout = findViewById(R.id.gameViewLayout);
        LinearLayout.LayoutParams mainGameViewUiLayoutParams = (LinearLayout.LayoutParams) mainGameViewUiLayout.getLayoutParams();
        float mainGameViewUi_weight = mainGameViewUiLayoutParams.weight;
        Log.d(TAG, "mainGameViewUi_weight = " + mainGameViewUi_weight);
        mainGameViewWidth = screenWidth * (mainGameViewUi_weight / gameViewWeightSum);
        Log.d(TAG, "mainGameViewWidth = " + mainGameViewWidth);

        // display the highest score and current score
        highestScoreTextView = supportToolbar.findViewById(R.id.highestScoreTextView);
        ScreenUtil.resizeTextSize(highestScoreTextView, textFontSize, ColorBallsApp.FontSize_Scale_Type);

        currentScoreTextView = supportToolbar.findViewById(R.id.currentScoreTextView);
        ScreenUtil.resizeTextSize(currentScoreTextView, textFontSize, ColorBallsApp.FontSize_Scale_Type);

        FrameLayout gridPartFrameLayout = findViewById(R.id.gridPartFrameLayout);
        LinearLayout.LayoutParams frameLp = (LinearLayout.LayoutParams) gridPartFrameLayout.getLayoutParams();

        // for 9 x 9 grid: main part of this game
        GridLayout gridCellsLayout = findViewById(R.id.gridCellsLayout);
        rowCounts = gridCellsLayout.getRowCount();
        colCounts = gridCellsLayout.getColumnCount();

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

        // set listener for each ImageView
        ImageView imageView;
        int imId;
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < colCounts; j++) {
                // imId = i * colCounts + j;
                imId = i * rowCounts + j;
                imageView = new ImageView(this);
                imageView.setId(imId);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setBackgroundResource(R.drawable.boximage);
                imageView.setClickable(true);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ((mPresenter.completedAll()) && (!ColorBallsApp.isProcessingJob)) {
                            Log.d(TAG, "createGameView.onClick");
                            mPresenter.doDrawBallsAndCheckListener(v);
                        }
                    }
                });
                gridCellsLayout.addView(imageView, imId, oneBallLp);
            }
        }

        scoreImageView = findViewById(R.id.scoreImageView);
        scoreImageView.setVisibility(View.GONE);

        createColorBallsGame(savedInstanceState);
    }

    private void createColorBallsGame(Bundle savedInstanceState) {
        saveScoreAlertDialog = null;
        boolean isNewGame = mPresenter.initializeColorBallsGame(rowCounts, colCounts, cellWidth, cellHeight, savedInstanceState);
    }

    private void setDialogStyle(DialogInterface dialog) {
        AlertDialog dlg = (AlertDialog)dialog;
        dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dlg.getWindow().setDimAmount(0.0f); // no dim for background screen
        dlg.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dlg.getWindow().setBackgroundDrawableResource(R.drawable.dialog_board_image);

        Button nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
        ScreenUtil.resizeTextSize(nBtn, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        nBtn.setTypeface(Typeface.DEFAULT_BOLD);
        nBtn.setTextColor(Color.RED);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)nBtn.getLayoutParams();
        layoutParams.weight = 10;
        nBtn.setLayoutParams(layoutParams);

        Button pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        ScreenUtil.resizeTextSize(pBtn, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        pBtn.setTypeface(Typeface.DEFAULT_BOLD);
        pBtn.setTextColor(Color.rgb(0x00,0x64,0x00));
        pBtn.setLayoutParams(layoutParams);
    }

    private void quitOrNewGame(final int entryPoint) {
        if (entryPoint==0) {
            //  END PROGRAM
            exitApplication();
        } else if (entryPoint==1) {
            //  NEW GAME
            createColorBallsGame(null);
        }
        ColorBallsApp.isProcessingJob = false;
    }

    private void showInterstitialAdAndNewGameOrQuit(final int entryPoint) {
        if (interstitialAd != null) {
            showInterstitialAdThread = interstitialAd.new
                    ShowInterstitialAdThread(entryPoint, ColorBallsApp.AdProvider);
            showInterstitialAdThread.startShowAd();
        }
        quitOrNewGame(entryPoint);
    }

    private void showTop10ScoreHistory() {
        ColorBallsApp.isProcessingJob = true;
        ColorBallsApp.isShowingLoadingMessage = true;
        showMessageOnScreen(getString(R.string.loadingString));
        Intent myService = new Intent(this, MyTop10ScoresService.class);
        startService(myService);
    }

    private void showGlobalTop10History() {
        ColorBallsApp.isProcessingJob = true;
        ColorBallsApp.isShowingLoadingMessage = true;
        showMessageOnScreen(getString(R.string.loadingString));
        Intent myService = new Intent(this, MyGlobalTop10Service.class);
        String webUrl = ColorBallsApp.REST_Website + "/GetTop10PlayerscoresREST";  // ASP.NET Core
        webUrl += "?gameId=" + ColorBallsApp.GameId;   // parameters
        myService.putExtra("WebUrl", webUrl);
        startService(myService);
    }

    private void setBannerAndNativeAdUI() {
        bannerLinearLayout = findViewById(R.id.linearlayout_banner_myActivity);
        adaptiveBannerLinearLayout = findViewById(R.id.linearlayout_adaptiveBanner_myActivity);
        String testString = "";
        // for debug mode
        if (com.smile.colorballs.BuildConfig.DEBUG) {
            testString = "IMG_16_9_APP_INSTALL#";
        }
        String facebookBannerID = testString + ColorBallsApp.facebookBannerID;
        String facebookBannerID2 = testString + ColorBallsApp.facebookBannerID2;
        //
        int adaptiveBannerWidth = (int)mainGameViewWidth;
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adaptiveBannerWidth = (int)(screenWidth - mainGameViewWidth);
        }
        int adaptiveBannerDpWidth = ScreenUtil.pixelToDp(this, adaptiveBannerWidth);
        Log.d(TAG, "adaptiveBannerDpWidth = " + adaptiveBannerDpWidth);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // show AdMob native ad if the device is tablet
            String nativeAdvancedId0 = ColorBallsApp.googleAdMobNativeID;     // real native ad unit id
            FrameLayout nativeAdsFrameLayout = findViewById(R.id.nativeAdsFrameLayout);
            com.google.android.ads.nativetemplates.TemplateView nativeAdTemplateView = findViewById(R.id.nativeAdTemplateView);
            nativeTemplate = new GoogleAdMobNativeTemplate(this, nativeAdsFrameLayout
                    , nativeAdvancedId0, nativeAdTemplateView);
            nativeTemplate.showNativeAd();
        } else {
            // one more banner ad for orientation is portrait
            myBannerAdView2 = new SetBannerAdView(this, null, adaptiveBannerLinearLayout
                    , ColorBallsApp.googleAdMobBannerID2, facebookBannerID2, adaptiveBannerDpWidth);
            // AdMob ad first
            myBannerAdView2.showBannerAdView(ColorBallsApp.AdProvider);
        }
        myBannerAdView = new SetBannerAdView(this, null, bannerLinearLayout
               , ColorBallsApp.googleAdMobBannerID, facebookBannerID);
        myBannerAdView.showBannerAdView(ColorBallsApp.AdProvider);
    }

    private void setBroadcastReceiver() {
        myReceiver = new MyBroadcastReceiver();
        myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(MyTop10ScoresService.Action_Name);
        myIntentFilter.addAction(MyGlobalTop10Service.Action_Name);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(myReceiver, myIntentFilter);
    }

    private void exitApplication() {
        final Handler handlerClose = new Handler(Looper.getMainLooper());
        final int timeDelay = 200;
        handlerClose.postDelayed( ()-> {
            // exit application
            finish();
        },timeDelay);
    }

    private void showAdUntilDismissed() {
        if (interstitialAd == null) {
            return;
        }

        showInterstitialAdThread = interstitialAd.new ShowInterstitialAdThread(0, ColorBallsApp.AdProvider);
        showInterstitialAdThread.startShowAd();
    }

    // implementing MyActivity.PresentView

    @Override
    public ImageView getImageViewById(int id) {
        return findViewById(id);
    }

    public void updateHighestScoreOnUi(int highestScore) {
        highestScoreTextView.setText(String.format(Locale.getDefault(), "%8d", highestScore));
    }

    @Override
    public void updateCurrentScoreOnUi(int score) {
        currentScoreTextView.setText(String.format(Locale.getDefault(), "%8d", score));
    }

    @Override
    public void showMessageOnScreen(String messageString) {
        Bitmap dialog_board_image = BitmapFactory.decodeResource(ColorBallsApp.AppResources, R.drawable.dialog_board_image);
        Bitmap showBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(dialog_board_image, messageString, Color.RED);
        scoreImageView.setVisibility(View.VISIBLE);
        scoreImageView.setImageBitmap(showBitmap);
    }

    @Override
    public void dismissShowMessageOnScreen() {
        scoreImageView.setImageBitmap(null);
        scoreImageView.setVisibility(View.GONE);
    }

    @Override
    public void showSaveGameDialog() {
        sureSaveDialog = AlertDialogFragment.newInstance(new AlertDialogFragment.DialogButtonListener() {
            @Override
            public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                // cancel the action of saving game
                dialogFragment.dismissAllowingStateLoss();
                mPresenter.setShowingSureSaveDialog(false);
            }

            @Override
            public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                // start saving game to internal storage
                dialogFragment.dismissAllowingStateLoss();
                mPresenter.setShowingSureSaveDialog(false);
                int numOfSaved = mPresenter.readNumberOfSaved();
                if (numOfSaved < Max_Saved_Games) {
                    boolean succeeded = mPresenter.startSavingGame(numOfSaved);
                    if (succeeded) {
                        ScreenUtil.showToast(MyActivity.this, getString(R.string.succeededSaveGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                    } else {
                        ScreenUtil.showToast(MyActivity.this, getString(R.string.failedSaveGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                    }
                } else {
                    // display warning to users
                    // final int finalNumOfSaved = numOfSaved;
                    showingWarningSaveGameDialog(numOfSaved);
                }
            }
        });
        Bundle args = new Bundle();
        args.putString("TextContent", getString(R.string.sureToSaveGameString));
        args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
        args.putFloat("TextFontSize", textFontSize);
        args.putInt("Color", Color.BLUE);
        args.putInt("Width", 0);    // wrap_content
        args.putInt("Height", 0);   // wrap_content
        args.putInt("NumButtons", 2);
        args.putBoolean("IsAnimation", false);

        mPresenter.setShowingSureSaveDialog(true);
        sureSaveDialog.setArguments(args);
        sureSaveDialog.show(getSupportFragmentManager(), "SureSaveDialogTag");
    }

    @Override
    public void showingWarningSaveGameDialog(int finalNumOfSaved) {
        warningSaveGameDialog = AlertDialogFragment.newInstance(new AlertDialogFragment.DialogButtonListener() {
            @Override
            public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                dialogFragment.dismissAllowingStateLoss();
                mPresenter.setShowingWarningSaveGameDialog(false);
            }

            @Override
            public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                dialogFragment.dismissAllowingStateLoss();
                mPresenter.setShowingWarningSaveGameDialog(false);
                boolean succeeded = mPresenter.startSavingGame(finalNumOfSaved);
                if (succeeded) {
                    ScreenUtil.showToast(MyActivity.this, getString(R.string.succeededSaveGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                } else {
                    ScreenUtil.showToast(MyActivity.this, getString(R.string.failedSaveGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                }
                showAdUntilDismissed();
            }
        });
        Bundle args = new Bundle();
        String warningSaveGameString0 = getString(R.string.warningSaveGameString) + " ("
                + Max_Saved_Games + " "
                + getString(R.string.howManyTimesString) + " )"
                + "\n" + getString(R.string.continueString) + "?";
        args.putString("TextContent", warningSaveGameString0); // excessive the number (5)
        args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
        args.putFloat("TextFontSize", textFontSize);
        args.putInt("Color", Color.BLUE);
        args.putInt("Width", 0);    // wrap_content
        args.putInt("Height", 0);   // wrap_content
        args.putInt("NumButtons", 2);
        args.putBoolean("IsAnimation", false);

        mPresenter.setShowingWarningSaveGameDialog(true);
        warningSaveGameDialog.setArguments(args);
        warningSaveGameDialog.show(getSupportFragmentManager(), "SaveGameWarningDialogTag");
    }

    @Override
    public void showLoadGameDialog() {
        sureLoadDialog = AlertDialogFragment.newInstance(new AlertDialogFragment.DialogButtonListener() {
            @Override
            public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                // cancel the action of loading game
                dialogFragment.dismissAllowingStateLoss();
                mPresenter.setShowingSureLoadDialog(false);
            }

            @Override
            public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                // start loading game to internal storage
                dialogFragment.dismissAllowingStateLoss();
                mPresenter.setShowingSureLoadDialog(false);
                boolean succeeded = mPresenter.startLoadingGame();
                if (succeeded) {
                    ScreenUtil.showToast(MyActivity.this, getString(R.string.succeededLoadGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                } else {
                    ScreenUtil.showToast(MyActivity.this, getString(R.string.failedLoadGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                }
            }
        });
        Bundle args = new Bundle();
        args.putString("TextContent", getString(R.string.sureToLoadGameString));
        args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
        args.putFloat("TextFontSize", textFontSize);
        args.putInt("Color", Color.BLUE);
        args.putInt("Width", 0);    // wrap_content
        args.putInt("Height", 0);   // wrap_content
        args.putInt("NumButtons", 2);
        args.putBoolean("IsAnimation", false);

        mPresenter.setShowingSureLoadDialog(true);
        sureLoadDialog.setArguments(args);
        sureLoadDialog.show(getSupportFragmentManager(), "SureLoadDialogTag");
    }

    @Override
    public void showGameOverDialog() {
        gameOverDialog = AlertDialogFragment.newInstance(new AlertDialogFragment.DialogButtonListener() {
            @Override
            public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss();
                mPresenter.setShowingGameOverDialog(false);
                mPresenter.quitGame();   //   Ending the game
            }

            @Override
            public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss();
                mPresenter.setShowingGameOverDialog(false);
                mPresenter.newGame();
            }
        });
        Bundle args = new Bundle();
        args.putString("TextContent", getString(R.string.gameOverStr));
        args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
        args.putFloat("TextFontSize", textFontSize);
        args.putInt("Color", Color.BLUE);
        args.putInt("Width", 0);    // wrap_content
        args.putInt("Height", 0);   // wrap_content
        args.putInt("NumButtons", 2);
        args.putBoolean("IsAnimation", false);

        mPresenter.setShowingGameOverDialog(true);
        gameOverDialog.setArguments(args);
        gameOverDialog.show(getSupportFragmentManager(), GameOverDialogTag);

        Log.d(TAG, "gameOverDialog.show() has been called.");
    }

    @Override
    public void showSaveScoreAlertDialog(final int entryPoint, final int score) {
        mPresenter.setSaveScoreAlertDialogState(entryPoint, true);
        final EditText et = new EditText(this);
        et.setTextColor(Color.BLUE);
        // et.setBackground(new ColorDrawable(Color.TRANSPARENT));
        // et.setBackgroundColor(Color.TRANSPARENT);
        et.setHint(getString(R.string.nameStr));
        ScreenUtil.resizeTextSize(et, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        et.setGravity(Gravity.CENTER);
        saveScoreAlertDialog = new AlertDialog.Builder(this).create();
        saveScoreAlertDialog.setTitle(null);
        saveScoreAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveScoreAlertDialog.setCancelable(false);
        saveScoreAlertDialog.setView(et);
        saveScoreAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancelStr), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showInterstitialAdAndNewGameOrQuit(entryPoint);
                mPresenter.setSaveScoreAlertDialogState(entryPoint, false);
                saveScoreAlertDialog = null;
            }
        });
        saveScoreAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.submitStr), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.saveScore(et.getText().toString(), score);
                dialog.dismiss();
                showInterstitialAdAndNewGameOrQuit(entryPoint);
                mPresenter.setSaveScoreAlertDialogState(entryPoint, false);
                saveScoreAlertDialog = null;
            }
        });

        saveScoreAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                setDialogStyle(dialog);
            }
        });

        saveScoreAlertDialog.show();
    }

    // end of implementing

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private final String FAILED = "1";

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "MyActivity.MyBroadcastReceiver.onReceive() is called.");

            String actionName = intent.getAction();
            if (actionName == null) {
                return;
            }

            Bundle extras;
            ArrayList<String> playerNames = new ArrayList<>();
            ArrayList<Integer> playerScores = new ArrayList<>();
            View historyView;
            int top10LayoutId = R.id.top10Layout;
            switch (actionName) {
                case MyTop10ScoresService.Action_Name:

                    String top10ScoreTitle = getString(R.string.top10Score);
                    extras = intent.getExtras();
                    if (extras != null) {
                        playerNames = extras.getStringArrayList("PlayerNames");
                        playerScores = extras.getIntegerArrayList("PlayerScores");
                    } else {
                        // failed
                        playerNames.add("Failed to access Score SQLite database");
                        playerScores.add(0);
                    }
                    historyView = findViewById(top10LayoutId);
                    if (historyView != null) {
                        // if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if (ColorBallsApp.AppResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            top10ScoreFragment = Top10ScoreFragment.newInstance(top10ScoreTitle, playerNames, playerScores, new Top10ScoreFragment.Top10OkButtonListener() {
                                @Override
                                public void buttonOkClick(Activity activity) {
                                    if (top10ScoreFragment != null) {
                                        // remove top10ScoreFragment to dismiss the top 10 score screen
                                        FragmentManager fmManager = getSupportFragmentManager();
                                        FragmentTransaction ft = fmManager.beginTransaction();
                                        ft.remove(top10ScoreFragment);
                                        // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                                        ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                                        showAdUntilDismissed();
                                    }
                                }
                            });
                            FragmentManager fmManager = getSupportFragmentManager();
                            FragmentTransaction ft = fmManager.beginTransaction();
                            Fragment currentTop10ScoreFragment = fmManager.findFragmentByTag(LocalTop10FragmentTag);
                            if (currentTop10ScoreFragment == null) {
                                ft.add(top10LayoutId, top10ScoreFragment, LocalTop10FragmentTag);
                            } else {
                                ft.replace(top10LayoutId, top10ScoreFragment, LocalTop10FragmentTag);
                            }
                            // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                            ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                        }
                    } else {
                        // for Portrait
                        top10ScoreFragment = null;
                        Intent top10Intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                        Bundle top10Extras = new Bundle();
                        top10Extras.putString("Top10TitleName", top10ScoreTitle);
                        top10Extras.putStringArrayList("Top10Players", playerNames);
                        top10Extras.putIntegerArrayList("Top10Scores", playerScores);
                        top10Intent.putExtras(top10Extras);
                        localTop10ActivityResultLauncher.launch(top10Intent);
                    }
                    dismissShowMessageOnScreen();
                    ColorBallsApp.isShowingLoadingMessage = false;
                    ColorBallsApp.isProcessingJob = false;
                    break;

                case MyGlobalTop10Service.Action_Name:

                    String globalTop10ScoreTitle = getString(R.string.globalTop10Score);
                    extras = intent.getExtras();
                    if (extras != null) {
                        playerNames = extras.getStringArrayList("PlayerNames");
                        playerScores = extras.getIntegerArrayList("PlayerScores");
                    } else {
                        // failed
                        playerNames.add("Failed to access data from MyGlobalTop10IntentService.");
                        playerScores.add(0);
                    }

                    historyView = findViewById(top10LayoutId);
                    if (historyView != null) {
                        // if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if (ColorBallsApp.AppResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            globalTop10Fragment = Top10ScoreFragment.newInstance(globalTop10ScoreTitle, playerNames, playerScores, new Top10ScoreFragment.Top10OkButtonListener() {
                                @Override
                                public void buttonOkClick(Activity activity) {
                                    if (globalTop10Fragment != null) {
                                        FragmentManager fmManager = getSupportFragmentManager();
                                        FragmentTransaction ft = fmManager.beginTransaction();
                                        ft.remove(globalTop10Fragment);
                                        // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                                        ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                                        showAdUntilDismissed();
                                    }
                                }
                            });
                            FragmentManager fmManager = getSupportFragmentManager();
                            FragmentTransaction ft = fmManager.beginTransaction();
                            Fragment currentGlobalTop10Fragment = fmManager.findFragmentByTag(GlobalTop10FragmentTag);
                            if (currentGlobalTop10Fragment == null) {
                                ft.add(top10LayoutId, globalTop10Fragment, GlobalTop10FragmentTag);
                            } else {
                                ft.replace(top10LayoutId, globalTop10Fragment, GlobalTop10FragmentTag);
                            }
                            // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                            ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                        }
                    } else {
                        // for Portrait
                        globalTop10Fragment = null;
                        Intent globalTop10Intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                        Bundle globalTop10Extras = new Bundle();
                        globalTop10Extras.putString("Top10TitleName", globalTop10ScoreTitle);
                        globalTop10Extras.putStringArrayList("Top10Players", playerNames);
                        globalTop10Extras.putIntegerArrayList("Top10Scores", playerScores);
                        globalTop10Intent.putExtras(globalTop10Extras);
                        globalTop10ActivityResultLauncher.launch(globalTop10Intent);
                    }
                    dismissShowMessageOnScreen();
                    ColorBallsApp.isShowingLoadingMessage = false;
                    ColorBallsApp.isProcessingJob = false;
                    break;
            }
        }
    }
}
