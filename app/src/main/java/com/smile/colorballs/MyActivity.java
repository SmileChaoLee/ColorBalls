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
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.os.Looper;
import android.os.SystemClock;
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
import com.smile.model.GridData;
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate;
import com.smile.smilelibraries.Models.ExitAppTimer;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest;
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil;
import com.smile.smilelibraries.showing_banner_ads_utility.SetBannerAdViewForAdMobOrFacebook;
import com.smile.smilelibraries.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;
import com.smile.smilelibraries.utilities.SoundPoolUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class MyActivity extends AppCompatActivity {

    // private properties
    private final String TAG = "MyActivity";
    private final String GlobalTop10FragmentTag = "GlobalTop10FragmentTag";
    private final String LocalTop10FragmentTag = "LocalTop10FragmentTag";
    private final int SettingActivityRequestCode = 1;
    private final int Top10ScoreActivityRequestCode = 2;
    private final int GlobalTop10ActivityRequestCode = 3;
    private final int PrivacyPolicyActivityRequestCode = 10;

    private float textFontSize;
    private float fontScale;

    private float screenWidth;
    private float screenHeight;

    private Toolbar supportToolbar;

    private Top10ScoreFragment top10ScoreFragment = null;
    private Top10ScoreFragment globalTop10Fragment = null;
    private float mainGameViewHeight;
    private float mainGameViewWidth;

    private MyBroadcastReceiver myReceiver;
    private IntentFilter myIntentFilter;

    private LinearLayout bannerLinearLayout;
    private SetBannerAdViewForAdMobOrFacebook myBannerAdView;
    private GoogleAdMobNativeTemplate nativeTemplate;

    private ShowingInterstitialAdsUtil.ShowInterstitialAdThread showInterstitialAdThread = null;

    // original from MainFragmentUI.java
    private final static String GameOverDialogTag = "GameOverDialogFragmentTag";
    private final int nextBallsViewIdStart = 100;
    private final String savedGameFileName = "saved_game";

    private ImageView scoreImageView = null;

    private Runnable bouncyRunnable; // needed to be tested 2018-0609
    private Handler bouncyHandler;   // needed to be tested
    private GridData gridData;

    private int highestScore;
    private TextView toolbarTitleTextView;
    private TextView currentScoreView;

    private int rowCounts = 9;
    private int colCounts = 9;
    private int cellWidth = 0;
    private int cellHeight = 0;

    private final boolean[] threadCompleted =  {true,true,true,true,true,true,true,true,true,true};
    private int bouncyBallIndexI = -1, bouncyBallIndexJ = -1;   // the array index that the ball has been selected
    private int bouncingStatus = 0; //  no cell selected
    //  one cell with a ball selected
    private boolean undoEnable = false;
    private int currentScore = 0;
    private int undoScore = 0;
    private boolean isEasyLevel = true;
    private boolean hasSound = true;    // has sound effect

    private SoundPoolUtil soundPoolUtil;
    private String yesStr = "";
    private String noStr = "";
    private String nameStr = "";
    private String submitStr = "";
    private String cancelStr = "";
    private String gameOverStr = "";

    private String loadingString;
    private String savingGameString;
    private String succeededSaveGameString;
    private String failedSaveGameString;
    private String loadingGameString;
    private String succeededLoadGameString;
    private String failedLoadGameString;
    private String sureToSaveGameString;
    private String sureToLoadGameString;
    private String warningSaveGameString;
    // end of original from MainFragmentUI.java

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() is called");

        ColorBallsApp.InterstitialAd = new ShowingInterstitialAdsUtil(this, ColorBallsApp.facebookAds, ColorBallsApp.googleInterstitialAd);

        if (ScreenUtil.isTablet(this)) {
            // Table then change orientation to Landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            // phone then change orientation to Portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        yesStr = ColorBallsApp.AppResources.getString(R.string.yesStr);
        noStr = ColorBallsApp.AppResources.getString(R.string.noStr);
        nameStr = ColorBallsApp.AppResources.getString(R.string.nameStr);
        submitStr = ColorBallsApp.AppResources.getString(R.string.submitStr);
        cancelStr = ColorBallsApp.AppResources.getString(R.string.cancelStr);
        gameOverStr = ColorBallsApp.AppResources.getString(R.string.gameOverStr);
        loadingString = ColorBallsApp.AppResources.getString(R.string.loadingString);
        savingGameString = ColorBallsApp.AppResources.getString(R.string.savingGameString);
        loadingGameString = ColorBallsApp.AppResources.getString(R.string.loadingGameString);
        succeededSaveGameString = ColorBallsApp.AppResources.getString(R.string.succeededSaveGameString);
        failedSaveGameString = ColorBallsApp.AppResources.getString(R.string.failedSaveGameString);
        succeededLoadGameString = ColorBallsApp.AppResources.getString(R.string.succeededLoadGameString);
        failedLoadGameString = ColorBallsApp.AppResources.getString(R.string.failedLoadGameString);
        sureToSaveGameString = ColorBallsApp.AppResources.getString(R.string.sureToSaveGameString);
        sureToLoadGameString = ColorBallsApp.AppResources.getString(R.string.sureToLoadGameString);
        warningSaveGameString = ColorBallsApp.AppResources.getString(R.string.warningSaveGameString) + " ("
                + ColorBallsApp.Max_Saved_Games + " "
                + ColorBallsApp.AppResources.getString(R.string.howManyTimesString) + " )"
                + "\n" + ColorBallsApp.AppResources.getString(R.string.continueString) + "?";

        soundPoolUtil = new SoundPoolUtil(this, R.raw.uhoh);

        setContentView(R.layout.activity_my);

        createActivityUI();

        createGameView(savedInstanceState);

        setBroadcastReceiver();

        Log.d(TAG, "onCreate() is finished.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case SettingActivityRequestCode:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        boolean hasSound = extras.getBoolean("HasSound");
                        setHasSound(hasSound);
                        boolean isEasyLevel = extras.getBoolean("IsEasyLevel");
                        setIsEasyLevel(isEasyLevel);
                    }
                }
                break;
            case Top10ScoreActivityRequestCode:
                showAdUntilDismissed(this);
                ColorBallsApp.isShowingLoadingMessage = false;
                ColorBallsApp.isProcessingJob = false;
                break;
            case GlobalTop10ActivityRequestCode:
                showAdUntilDismissed(this);
                ColorBallsApp.isShowingLoadingMessage = false;
                ColorBallsApp.isProcessingJob = false;
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);

        /*
        final int popupThemeId = supportToolbar.getPopupTheme();
        // final Context wrapper = new ContextThemeWrapper(this, R.style.menu_text_style);
        final Context wrapper = new ContextThemeWrapper(this, popupThemeId);
        */

        final float fScale = fontScale;
        // ScreenUtil.buildActionViewClassMenu(this, wrapper, menu, fScale, ColorBallsApp.FontSize_Scale_Type);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // API >= 18 works on SpannableString on Main menu items
            ScreenUtil.resizeMenuTextSize(menu, fScale);
        } else {
            // API < 18 does not work on SpannableString on Main menu
            // only sub menu works on SpannableString
            // Text size of Menu items use dimens
            int menuSize = menu.size();
            Menu subMenu;
            for (int i=0; i<menuSize; i++) {
                subMenu = menu.getItem(i).getSubMenu();
                ScreenUtil.resizeMenuTextSize(subMenu, fScale);
            }
        }

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
            recordScore(0);   //   from   END PROGRAM
            return true;
        }
        if (id == R.id.newGame) {
            newGame();
            return true;
        }

        if (!isProcessingJob) {
            if (id == R.id.undoGame) {
                undoTheLast();
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
                saveGame();
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.loadGame) {
                loadGame();
                return true;
            }
            if (id == R.id.setting) {
                ColorBallsApp.isProcessingJob = true;    // started procession job
                Intent intent = new Intent(this, SettingActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean("HasSound", getHasSound());
                extras.putBoolean("IsEasyLevel", getIsEasyLevel());
                intent.putExtras(extras);
                startActivityForResult(intent, SettingActivityRequestCode);
                ColorBallsApp.isProcessingJob = false;
                return true;
            }
            if (id == R.id.privacyPolicy) {
                // Intent privacyPolicyIntent = new Intent(this, PrivacyPolicyActivity.class);
                // startActivity(privacyPolicyIntent);
                PrivacyPolicyUtil.startPrivacyPolicyActivity(this, ColorBallsApp.PrivacyPolicyUrl, PrivacyPolicyActivityRequestCode);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (isChangingConfigurations()) {
            // configuration is changing then remove top10ScoreFragment and globalTop10Fragment
            if (top10ScoreFragment != null) {
                // remove top10ScoreFragment
                FragmentManager fmManager = getSupportFragmentManager();
                FragmentTransaction ft = fmManager.beginTransaction();
                ft.remove(top10ScoreFragment);
                if (top10ScoreFragment.isStateSaved()) {
                    Log.d(TAG, "top10ScoreFragment.isStateSaved() = true");
                    ft.commitAllowingStateLoss();   // temporarily solved
                } else {
                    Log.d(TAG, "top10ScoreFragment.isStateSaved() = false");
                    ft.commit();
                }
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
                    ft.commitAllowingStateLoss();   // temporarily solved
                } else {
                    Log.d(TAG, "globalTop10Fragment.isStateSaved() = false");
                    ft.commit();
                }

                ColorBallsApp.isShowingLoadingMessage = false;
                ColorBallsApp.isProcessingJob = false;
            }
        }

        Log.d(TAG, "MyActivity.onSaveInstanceState() is called");

        super.onSaveInstanceState(outState);
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
    public void onDestroy() {
        Log.d(TAG, "MyActivity.onDestroy() is called");

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(myReceiver);

        if (myBannerAdView != null) {
            myBannerAdView.destroy();
            myBannerAdView = null;
        }
        if (nativeTemplate != null) {
            nativeTemplate.release();
        }

        if (showInterstitialAdThread != null) {
            // avoiding memory leak
            showInterstitialAdThread.releaseShowInterstitialAdThread();
        }
        /*
        // the following were removed on 2019-06-25
        // int pid = android.os.Process.myPid();
        if (isQuitOrNewGame == QuitGame) {
            // Kill Current Process
            //  removed for testing (UI test)
            // android.os.Process.killProcess(pid);
            // System.exit(0);
        } else {
            // create a new game
            String packageName = getBaseContext().getPackageName();
            Intent myIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(packageName);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);

            // Kill Current Process
            // removed for testing (UI test)
            // android.os.Process.killProcess(pid);
            //
        }
        */

        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        ExitAppTimer exitAppTimer = ExitAppTimer.getInstance(1000); // singleton class
        if (exitAppTimer.canExit()) {
            recordScore(0);   //   from   END PROGRAM
        } else {
            exitAppTimer.start();
            float toastFontSize = textFontSize*0.7f;
            Log.d(TAG, "toastFontSize = " + toastFontSize);
            ScreenUtil.showToast(this, getString(R.string.backKeyToExitApp), toastFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
            // ShowToastMessage.showToast(this, getString(R.string.backKeyToExitApp), toastFontSize, ColorBallsApp.FontSize_Scale_Type, 2000);
        }
    }

    private void createActivityUI() {
        findOutTextFontSize();
        findOutScreenSize();
        setUpSupportActionBar();
        setBannerAndNativeAdUI();
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
        float statusBarHeight = ScreenUtil.getStatusBarHeight(this);
        float actionBarHeight = ScreenUtil.getActionBarHeight(this);
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

    private void findOutGameViewWidthHeight() {
        LinearLayout linearLayout_myActivity = findViewById(R.id.linearLayout_myActivity);
        float main_WeightSum = linearLayout_myActivity.getWeightSum();

        LinearLayout gameViewLinearLayout = findViewById(R.id.gameViewLinearLayout);
        LinearLayout.LayoutParams gameViewLp = (LinearLayout.LayoutParams) gameViewLinearLayout.getLayoutParams();
        float gameView_Weight = gameViewLp.weight;
        mainGameViewHeight = screenHeight * gameView_Weight / main_WeightSum;
        Log.d(TAG, "mainGameViewHeight = " + mainGameViewHeight);

        float gameViewWeightSum = gameViewLinearLayout.getWeightSum();
        LinearLayout mainGameViewUiLayout = findViewById(R.id.gameViewLayout);
        LinearLayout.LayoutParams mainGameViewtUiLayoutParams = (LinearLayout.LayoutParams)mainGameViewUiLayout.getLayoutParams();
        float mainGameViewUi_weight = mainGameViewtUiLayoutParams.weight;
        mainGameViewWidth = screenWidth * (mainGameViewUi_weight / gameViewWeightSum);
        Log.d(TAG, "mainGameViewWidth = " + mainGameViewWidth);
    }

    private void createGameView(Bundle savedInstanceState) {
        // original from MainFragmentUI.java
        boolean isNewGame = false;
        if ( (savedInstanceState == null) || (gridData == null) ) {
            isNewGame = true;
        }

        findOutGameViewWidthHeight();
        createGameViewUI(isNewGame);
        // end of original from MainFragmentUI.java
    }


    // original from MainFragmentUI.java

    private void createGameViewUI(boolean isNewGame) {
        // layout_for_game_view.xml
        float height_weightSum_GameViewUi = 100;    // default
        try {
            LinearLayout gameViewUiLayout = findViewById(R.id.linearlayout_for_game_view_ui);
            float temp = gameViewUiLayout.getWeightSum();
            if (temp != 0) {
                height_weightSum_GameViewUi = temp;
            }
        } catch (Exception ex) {
            Log.d(TAG, "Getting weightSum of Layout for Game View Ui was failed.");
            ex.printStackTrace();
        }

        LinearLayout scoreNextBallsLayout = findViewById(R.id.score_next_balls_layout);
        float width_weightSum_scoreNextBallsLayout = scoreNextBallsLayout.getWeightSum();
        LinearLayout.LayoutParams scoreNextBallsLayoutParams = (LinearLayout.LayoutParams) scoreNextBallsLayout.getLayoutParams();
        float height_weight_scoreNextBallsLayout = scoreNextBallsLayoutParams.weight;

        // display the highest score and current score
        toolbarTitleTextView = findViewById(R.id.toolbarTitleTextView);
        ScreenUtil.resizeTextSize(toolbarTitleTextView, textFontSize, ColorBallsApp.FontSize_Scale_Type);

        currentScoreView = findViewById(R.id.currentScoreTextView);
        ScreenUtil.resizeTextSize(currentScoreView, textFontSize, ColorBallsApp.FontSize_Scale_Type);

        // display the view of next balls
        GridLayout nextBallsLayout = findViewById(R.id.nextBallsLayout);
        int nextBallsRow = nextBallsLayout.getRowCount();
        int nextBallsColumn = nextBallsLayout.getColumnCount();
        LinearLayout.LayoutParams nextBallsLayoutParams = (LinearLayout.LayoutParams)nextBallsLayout.getLayoutParams();
        float width_weight_nextBalls = nextBallsLayoutParams.weight;

        int nextBallsViewWidth = (int)((float) mainGameViewWidth * width_weight_nextBalls / width_weightSum_scoreNextBallsLayout);   // 3/5 of screen width

        LinearLayout.LayoutParams oneNextBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneNextBallLp.width = nextBallsViewWidth / nextBallsColumn;
        // the layout_weight for height is 1
        oneNextBallLp.height = (int)((float) mainGameViewHeight * height_weight_scoreNextBallsLayout / height_weightSum_GameViewUi);
        oneNextBallLp.gravity = Gravity.CENTER;

        ImageView imageView;
        for (int i = 0; i < nextBallsRow; i++) {
            for (int j = 0; j < nextBallsColumn; j++) {
                imageView = new ImageView(this);
                imageView.setId(nextBallsViewIdStart + (nextBallsColumn * i + j));
                imageView.setClickable(false);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setBackgroundResource(R.drawable.next_ball_background_image);
                nextBallsLayout.addView(imageView, oneNextBallLp);
            }
        }

        FrameLayout gridPartFrameLayout = findViewById(R.id.gridPartFrameLayout);
        LinearLayout.LayoutParams frameLp = (LinearLayout.LayoutParams) gridPartFrameLayout.getLayoutParams();
        float height_weight_gridCellsLayout = frameLp.weight;

        // for 9 x 9 grid: main part of this game
        GridLayout gridCellsLayout = findViewById(R.id.gridCellsLayout);
        rowCounts = gridCellsLayout.getRowCount();
        Log.d(TAG, "createGameViewUI()-->rowCounts = " + rowCounts);
        colCounts = gridCellsLayout.getColumnCount();
        Log.d(TAG, "createGameViewUI()-->colCounts = " + colCounts);
        // LinearLayout.LayoutParams gridLp = (LinearLayout.LayoutParams) gridCellsLayout.getLayoutParams();
        // float height_weight_gridCellsLayout = gridLp.weight;

        cellWidth = (int)(mainGameViewWidth / colCounts);
        int eight10thOfHeight = (int)( (float) mainGameViewHeight / height_weightSum_GameViewUi * height_weight_gridCellsLayout);
        if ( mainGameViewWidth >  eight10thOfHeight) {
            // if screen width greater than 8-10th of screen height
            cellWidth = eight10thOfHeight / rowCounts;
        }
        cellHeight = cellWidth;

        // added on 2018-10-02 to test and it works
        // setting the width and the height of GridLayout by using the FrameLayout that is on top of it
        frameLp.width = cellWidth * colCounts;
        frameLp.topMargin = 20;
        frameLp.gravity = Gravity.CENTER;
        //

        LinearLayout.LayoutParams oneBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneBallLp.width = cellWidth;
        oneBallLp.height = cellHeight;
        oneBallLp.gravity = Gravity.CENTER;

        // set listener for each ImageView
        // ImageView imageView;
        int imId;
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < colCounts; j++) {
                // imId = i * colCounts + j;
                imId = i * rowCounts + j;
                imageView = new ImageView(this);
                imageView.setId(imId);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setBackgroundResource(R.drawable.boximage);
                imageView.setClickable(true);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ( (completedAll()) && (!ColorBallsApp.isProcessingJob) ) {
                            doDrawBallsAndCheckListener(v);
                        }
                    }
                });
                gridCellsLayout.addView(imageView, imId, oneBallLp);
                // gridCellsLayout.addView(imageView, imId);

            }
        }

        // For testing to display score using ImageView
        scoreImageView = findViewById(R.id.scoreImageView);
        scoreImageView.setVisibility(View.GONE);
        //

        if (isNewGame) {
            createNewGame();
        } else {
            // fragment recreated (keep the original state)
            // display the original state before changing configuration

            toolbarTitleTextView.setText(String.format(Locale.getDefault(), "%8d", highestScore));
            currentScoreView.setText(String.format(Locale.getDefault(), "%8d", currentScore));

            displayGameView();

            if (ColorBallsApp.isShowingLoadingMessage) {
                showMessageOnScreen(loadingString);
            }
            if (ColorBallsApp.isShowingSavingGameMessage) {
                showMessageOnScreen(savingGameString);
            }
            if (ColorBallsApp.isShowingLoadingGameMessage) {
                showMessageOnScreen(loadingGameString);
            }

            if (bouncingStatus == 1) {
                ImageView v = findViewById(bouncyBallIndexI * rowCounts + bouncyBallIndexJ);
                drawBouncyBall(v, gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
            }
        }
    }
    private void createNewGame() {
        ColorBallsApp.isShowingLoadingMessage = false;
        ColorBallsApp.isShowingSavingGameMessage = false;
        ColorBallsApp.isShowingLoadingGameMessage = false;
        ColorBallsApp.isProcessingJob = false;

        Arrays.fill(threadCompleted, true);

        bouncyBallIndexI = -1;
        bouncyBallIndexJ = -1;  // the array index that the ball has been selected
        bouncingStatus = 0;     //  no cell selected
        undoEnable = false;
        currentScore = 0;
        undoScore = 0;
        isEasyLevel = true; // start with easy level
        hasSound = true;    // has sound effect

        if (ColorBallsApp.ScoreSQLiteDB != null) {
            highestScore = ColorBallsApp.ScoreSQLiteDB.readHighestScore();
        }

        toolbarTitleTextView.setText(String.format(Locale.getDefault(), "%8d", highestScore));
        currentScoreView.setText(String.format(Locale.getDefault(), "%8d", currentScore));

        gridData = new GridData(rowCounts, colCounts, ColorBallsApp.NumOfColorsUsedByEasy);

        displayGameView();
        displayGridDataNextCells();

        Log.d(TAG, "createNewGame() is called.");
    }
    private boolean completedAll() {
        for (boolean thCompleted : threadCompleted) {
            if (!thCompleted) {
                return false;
            }
        }
        return true;
    }
    private void displayGridDataNextCells() {

        gridData.randCells();

        // int numOneTime = gridData.ballNumOneTime;
        int numOneTime = GridData.ballNumOneTime;

        int[] indexi = gridData.getNextCellIndexI();
        int[] indexj = gridData.getNextCellIndexJ();

        int id, n1, n2;
        ImageView imageView;
        for (int i = 0; i < numOneTime; i++) {
            n1 = indexi[i];
            n2 = indexj[i];
            if ((n1 >= 0) && (n2 >= 0)) {
                // id = n1 * colCounts + n2;
                id = n1 * rowCounts + n2;
                imageView = findViewById(id);
                drawBall(imageView,gridData.getCellValue(n1, n2));
            }
        }

        boolean hasMoreFive = false;
        HashSet<Point> linkedPoint = new HashSet<>();
        for (int i = 0; i < numOneTime; i++) {
            n1 = indexi[i];
            n2 = indexj[i];
            if ((n1 >= 0) && (n2 >= 0)) {
                if (gridData.getCellValue(n1, n2) != 0) {
                    //   has  color in this cell
                    if (gridData.check_moreThanFive(n1, n2) == 1) {
                        hasMoreFive = true;
                        for (Point point : gridData.getLight_line()) {
                            // if (!linkedPoint.contains(point)) {
                            linkedPoint.add(point);
                            // }
                        }
                    }
                }
            }
        }

        if (hasMoreFive) {
            CalculateScoreThread calculateScoreThread = new CalculateScoreThread(linkedPoint, true);
            calculateScoreThread.startCalculate();
        } else {
            // check if game over
            boolean gameOverYn = gridData.getGameOver();
            if (gameOverYn) {
                //  game over
                AlertDialogFragment gameOverDialog = new AlertDialogFragment(new AlertDialogFragment.DialogButtonListener() {
                    @Override
                    public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                        // dialogFragment.dismiss();
                        dialogFragment.dismissAllowingStateLoss();
                        recordScore(0);   //   Ending the game
                    }

                    @Override
                    public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                        // dialogFragment.dismiss();
                        dialogFragment.dismissAllowingStateLoss();
                        newGame();
                    }
                });
                Bundle args = new Bundle();
                args.putString("TextContent", gameOverStr);
                args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
                args.putFloat("TextFontSize", textFontSize);
                args.putInt("Color", Color.BLUE);
                args.putInt("Width", 0);    // wrap_content
                args.putInt("Height", 0);   // wrap_content
                args.putInt("NumButtons", 2);
                args.putBoolean("IsAnimation", false);
                gameOverDialog.setArguments(args);
                gameOverDialog.show(getSupportFragmentManager(), GameOverDialogTag);

                Log.d(TAG, "gameOverDialog.show() has been called.");
            } else {
                // game has not been over yet
                displayNextColorBalls();
            }
        }
    }

    private void clearCell(int i, int j) {
        // int id = i * colCounts + j;
        int id = i * rowCounts + j;
        ImageView imageView = findViewById(id);
        // imageView.setImageDrawable(null);
        imageView.setImageBitmap(null);
        gridData.setCellValue(i, j, 0);
    }

    private void doDrawBallsAndCheckListener(View v) {

        int i, j, id;
        id = v.getId();
        i = id / rowCounts;
        j = id % rowCounts;
        ImageView imageView;
        if (bouncingStatus == 0) {
            if (gridData.getCellValue(i, j) != 0) {
                if ((bouncyBallIndexI == -1) && (bouncyBallIndexJ == -1)) {
                    bouncingStatus = 1;
                    drawBouncyBall((ImageView) v, gridData.getCellValue(i, j));
                    bouncyBallIndexI = i;
                    bouncyBallIndexJ = j;
                }
            }
        } else {
            // cancel the timer
            if (gridData.getCellValue(i, j) == 0) {
                //   blank cell
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    if (gridData.moveCellToCell(new Point(bouncyBallIndexI, bouncyBallIndexJ), new Point(i, j))) {
                        // cancel the timer
                        bouncingStatus = 0;
                        cancelBouncyTimer();
                        int color = gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ);
                        gridData.setCellValue(i, j, color);
                        clearCell(bouncyBallIndexI, bouncyBallIndexJ);

                        bouncyBallIndexI = -1;
                        bouncyBallIndexJ = -1;

                        drawBallAlongPath(i,j,color);

                        undoEnable = true;
                    } else {
                        //    make a sound
                        if (hasSound) {
                            soundPoolUtil.playSound();
                        }
                    }
                }
            } else {
                //  cell is not blank
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    bouncingStatus = 0;
                    cancelBouncyTimer();
                    bouncingStatus = 1;
                    imageView = findViewById(bouncyBallIndexI * rowCounts + bouncyBallIndexJ);
                    drawBall(imageView , gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
                    drawBouncyBall((ImageView) v, gridData.getCellValue(i, j));
                    bouncyBallIndexI = i;
                    bouncyBallIndexJ = j;
                }
            }
        }
    }

    private void drawBouncyBall(final ImageView v, final int color) {
        bouncyHandler = new Handler(Looper.getMainLooper());
        bouncyRunnable = new Runnable() {
            boolean ballYN = false;
            @Override
            public void run() {
                if (bouncingStatus == 1) {
                    if (color != 0) {
                        if (ballYN) {
                            drawBall(v , color);
                        } else {
                            drawOval(v , color);
                        }
                        ballYN = !ballYN;
                        bouncyHandler.postDelayed(this, 200);
                    } else {
                        // v.setImageResource(R.drawable.boximage);
                        v.setImageDrawable(null);
                    }
                } else {
                    cancelBouncyTimer();
                }
            }
        };
        bouncyHandler.post(bouncyRunnable);
    }

    private void cancelBouncyTimer() {
        if (bouncyHandler != null) {
            bouncyHandler.removeCallbacksAndMessages(null);
            // bouncyHandler.removeCallbacks(bouncyRunnable);
            bouncyRunnable = null;
            bouncyHandler = null;
        }
        SystemClock.sleep(20);
    }

    private void drawBallAlongPath(final int ii , final int jj,final int color) {
        if (gridData.getPathPoint().size()<=0) {
            return;
        }

        final List<Point> tempList = new ArrayList<>(gridData.getPathPoint());
        final Handler drawHandler = new Handler(Looper.getMainLooper());
        Runnable runnablePath = new Runnable() {
            boolean ballYN = true;
            ImageView imageView = null;
            int countDown = tempList.size()*2 - 1;
            @Override
            public void run() {
                threadCompleted[0] = false;
                if (countDown >= 2) {   // eliminate start point
                    int i = countDown / 2;
                    imageView = findViewById(tempList.get(i).x * rowCounts + tempList.get(i).y);
                    if (ballYN) {
                        drawBall(imageView, color);
                    } else {
                        // imageView.setImageDrawable(null);
                        imageView.setImageBitmap(null);
                    }
                    ballYN = !ballYN;
                    countDown--;
                    drawHandler.postDelayed(this,20);
                } else {
                    drawHandler.removeCallbacksAndMessages(null);
                    //   do the next
                    threadCompleted[0] = true;
                    doNextAction(ii,jj);
                }
            }
        };
        drawHandler.post(runnablePath);
    }

    private void doNextAction(final int i,final int j) {
        // may need to run runOnUiThread()
        ImageView v = findViewById(i * rowCounts + j);
        drawBall(v, gridData.getCellValue(i, j));
        //  check if there are more than five balls with same color connected together
        if (gridData.check_moreThanFive(i, j) == 1) {
            CalculateScoreThread calculateScoreThread = new CalculateScoreThread(gridData.getLight_line(), false);
            calculateScoreThread.startCalculate();
        } else {
            displayGridDataNextCells();   // has a problem
        }
    }

    private int scoreCalculate(int numBalls) {
        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*5 + ... + (n-5)*2
        int minBalls = 5;
        int minScore = 5;
        int score = minScore;
        if (numBalls > minScore) {
            // greater than 5 balls
            int rate  = 2;
            for (int i=1 ; i<=Math.abs(numBalls-minBalls) ; i++) {
                // rate = 2;   // added on 2018-10-02
                score += i * rate ;
            }
        }

        if (!isEasyLevel) {
            // difficult level
            score = score * 2;   // double of easy level
        }

        return score;
    }

    private void flushALLandBegin() {
        // myActivity.reStartApplication();   // restart the game
        Log.d(TAG, "flushALLandBegin() is called.");
        createNewGame();
    }

    private void setDialogStyle(DialogInterface dialog) {
        AlertDialog dlg = (AlertDialog)dialog;
        dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dlg.getWindow().setDimAmount(0.0f); // no dim for background screen
        dlg.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dlg.getWindow().setBackgroundDrawableResource(R.drawable.dialog_board_image);

        Button nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
        ScreenUtil.resizeTextSize((TextView) nBtn, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        nBtn.setTypeface(Typeface.DEFAULT_BOLD);
        nBtn.setTextColor(Color.RED);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)nBtn.getLayoutParams();
        layoutParams.weight = 10;
        nBtn.setLayoutParams(layoutParams);

        Button pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        ScreenUtil.resizeTextSize((TextView) pBtn, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        pBtn.setTypeface(Typeface.DEFAULT_BOLD);
        pBtn.setTextColor(Color.rgb(0x00,0x64,0x00));
        pBtn.setLayoutParams(layoutParams);
    }

    private void drawBall(ImageView imageView, int color) {
        imageView.setImageBitmap(ColorBallsApp.colorBallMap.get(color));
    }

    private void drawOval(ImageView imageView,int color) {
        imageView.setImageBitmap(ColorBallsApp.colorOvalBallMap.get(color));
    }

    private void displayNextBallsView() {
        // display the view of next balls
        ImageView imageView;
        // int numOneTime = gridData.ballNumOneTime;
        int numOneTime = GridData.ballNumOneTime;
        for (int i = 0; i < numOneTime; i++) {
            imageView = findViewById(nextBallsViewIdStart + i);
            drawBall(imageView, gridData.getNextBalls()[i]);
        }
    }

    private void displayGameGridView() {
        // display the 9 x 9 game view
        ImageView imageView;
        Log.d(TAG, "rowCounts = " + rowCounts);
        Log.d(TAG, "colCounts = " + colCounts);
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < colCounts; j++) {
                int id = i * rowCounts + j;
                imageView = findViewById(id);
                int color = gridData.getCellValue(i, j);
                if (color == 0) {
                    // imageView.setImageDrawable(null);
                    imageView.setImageBitmap(null);
                } else {
                    drawBall(imageView, color);
                }
            }
        }
    }

    private void displayGameView() {

        // display the view of next balls
        displayNextBallsView();

        // display the 9 x 9 game view
        displayGameGridView();
    }

    private GridData getGridData() {
        return this.gridData;
    }

    private void displayNextColorBalls() {
        gridData.randColors();  //   next  balls
        //   display the balls on the nextBallsView
        displayNextBallsView();
    }

    private void quitOrNewGame(final int entryPoint) {
        if (entryPoint==0) {
            //  END PROGRAM
            exitApplication();
        } else if (entryPoint==1) {
            //  NEW GAME
            flushALLandBegin();
        }
        ColorBallsApp.isProcessingJob = false;
    }
    private void showInterstitialAdAndNewGameOrQuit(final int entryPoint) {

        if (ColorBallsApp.InterstitialAd == null) {
            quitOrNewGame(entryPoint);
        } else {
            showInterstitialAdThread = ColorBallsApp.InterstitialAd.new
                    ShowInterstitialAdThread(entryPoint, ColorBallsApp.AdProvider, new ShowingInterstitialAdsUtil.AfterDismissFunctionOfShowAd() {
                @Override
                public void executeAfterDismissAds(int endPoint) {
                    // update the main UI
                    quitOrNewGame(endPoint);
                }
            });
            showInterstitialAdThread.startShowAd();
        }
    }

    private boolean startSavingGame(int numOfSaved, final boolean isShowAd) {
        ColorBallsApp.isProcessingJob = true;
        ColorBallsApp.isShowingSavingGameMessage = true;
        showMessageOnScreen(savingGameString);

        boolean succeeded = true;
        try {
            File outputFile = new File(ColorBallsApp.AppContext.getFilesDir(), savedGameFileName);
            FileOutputStream foStream = new FileOutputStream(outputFile);
            // save settings
            if (hasSound) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            if (isEasyLevel) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            // save next balls
            // foStream.write(gridData.ballNumOneTime);
            foStream.write(GridData.ballNumOneTime);
            for (int i=0; i<ColorBallsApp.NumOfColorsUsedByDifficult; i++) {
                foStream.write(gridData.getNextBalls()[i]);
            }
            // save values on 9x9 grid
            for (int i=0; i<rowCounts; i++) {
                for (int j=0; j<colCounts; j++) {
                    foStream.write(gridData.getCellValue(i, j));
                }
            }
            // save current score
            byte[] scoreByte = ByteBuffer.allocate(4).putInt(currentScore).array();
            foStream.write(scoreByte);
            // save undoEnable
            if (undoEnable) {
                // can undo
                foStream.write(1);
                // foStream.write(gridData.ballNumOneTime);
                foStream.write(GridData.ballNumOneTime);
                // save undoNextBalls
                for (int i=0; i<ColorBallsApp.NumOfColorsUsedByDifficult; i++) {
                    foStream.write(gridData.getUndoNextBalls()[i]);
                }
                // save backupCells
                for (int i=0; i<rowCounts; i++) {
                    for (int j=0; j<colCounts; j++) {
                        foStream.write(gridData.getBackupCells()[i][j]);
                    }
                }
                byte[] undoScoreByte = ByteBuffer.allocate(4).putInt(undoScore).array();
                foStream.write(undoScoreByte);
                // end of writing
            } else {
                // no undo
                foStream.write(0);
                // end of writing
            }

            foStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            succeeded = false;
        }

        dismissShowMessageOnScreen();
        ColorBallsApp.isShowingSavingGameMessage = false;
        ColorBallsApp.isProcessingJob = false;

        String textContent;
        if (succeeded) {
            textContent = succeededSaveGameString;
            numOfSaved++;
            // save numOfSaved back to file (ColorBallsApp.NumOfSavedGameFileName)
            try {
                File outputFile = new File(ColorBallsApp.AppContext.getFilesDir(), ColorBallsApp.NumOfSavedGameFileName);
                FileOutputStream foStream = new FileOutputStream(outputFile);
                foStream.write(numOfSaved);
                foStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            textContent = failedSaveGameString;
        }
        AlertDialogFragment gameSavedDialog = new AlertDialogFragment(new AlertDialogFragment.DialogButtonListener() {
            @Override
            public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                dialogFragment.dismissAllowingStateLoss();
                if (isShowAd) {
                    // excess 5 times saving game, then show ad
                    showAdUntilDismissed(MyActivity.this);
                }
            }
        });
        Bundle args = new Bundle();
        args.putString("TextContent", textContent);
        args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
        args.putFloat("TextFontSize", textFontSize);
        args.putInt("Color", Color.BLUE);
        args.putInt("Width", 0);    // wrap_content
        args.putInt("Height", 0);   // wrap_content
        args.putInt("NumButtons", 1);
        args.putBoolean("IsAnimation", false);
        gameSavedDialog.setArguments(args);
        gameSavedDialog.show(getSupportFragmentManager(), "GameSavedDialogTag");

        return succeeded;
    }

    private void startLoadingGame() {

        ColorBallsApp.isProcessingJob = true;

        ColorBallsApp.isShowingLoadingGameMessage = true;
        showMessageOnScreen(savingGameString);

        boolean succeeded = true;
        boolean soundYn = hasSound;
        boolean easyYn = isEasyLevel;
        // int ballNumOneTime = GridData.ballNumOneTime;
        int ballNumOneTime;
        int[] nextBalls = new int[ColorBallsApp.NumOfColorsUsedByDifficult];
        int[][] gameCells = new int[rowCounts][colCounts];
        int cScore = currentScore;
        boolean undoYn = undoEnable;
        // int undoNumOneTime = gridData.ballNumOneTime;
        // int undoNumOneTime = GridData.ballNumOneTime;
        int[] undoNextBalls = new int[ColorBallsApp.NumOfColorsUsedByDifficult];
        int[][] backupCells = new int[rowCounts][colCounts];
        int unScore = undoScore;

        try {
            File inputFile = new File(ColorBallsApp.AppContext.getFilesDir(), savedGameFileName);
            FileInputStream fiStream = new FileInputStream(inputFile);
            int bValue = fiStream.read();
            if (bValue == 1) {
                // has sound
                Log.i(TAG, "FileInputStream Read: Game has sound");
                soundYn = true;
            } else {
                // has no sound
                Log.i(TAG, "FileInputStream Read: Game has no sound");
                soundYn = true;
            }
            bValue = fiStream.read();
            if (bValue == 1) {
                // easy level
                Log.i(TAG, "FileInputStream Read: Game is easy level");
                easyYn = true;

            } else {
                // difficult level
                Log.i(TAG, "FileInputStream Read: Game is difficult level");
                easyYn = false;
            }
            ballNumOneTime = fiStream.read();
            Log.i(TAG, "FileInputStream Read: Game has " + ballNumOneTime + " next balls");
            int ballValue;
            for (int i=0; i<ColorBallsApp.NumOfColorsUsedByDifficult; i++) {
                nextBalls[i] = fiStream.read();
                Log.i(TAG, "FileInputStream Read: Next ball value = " + nextBalls[i]);
            }
            for (int i=0; i<rowCounts; i++) {
                for (int j=0; j<colCounts; j++) {
                    gameCells[i][j] = fiStream.read();
                    Log.i(TAG, "FileInputStream Read: Value of ball at (" + i + ", " + j + ") = " + gameCells[i][j]);
                }
            }
            // reading current score
            byte[] scoreByte = new byte[4];
            fiStream.read(scoreByte);
            cScore = ByteBuffer.wrap(scoreByte).getInt();
            Log.i(TAG, "FileInputStream Read: Current score = " + cScore);
            // reading undoEnable
            bValue = fiStream.read();
            if (bValue == 1) {
                // has undo data
                Log.i(TAG, "FileInputStream Read: Game has undo data");
                undoYn = true;
                // undoNumOneTime = fiStream.read();
                fiStream.read();
                for (int i=0; i<ColorBallsApp.NumOfColorsUsedByDifficult; i++) {
                    undoNextBalls[i] = fiStream.read();
                }
                // save backupCells
                for (int i=0; i<rowCounts; i++) {
                    for (int j=0; j<colCounts; j++) {
                        backupCells[i][j] = fiStream.read();
                    }
                }
                byte[] undoScoreByte = new byte[4];
                fiStream.read(undoScoreByte);
                unScore = ByteBuffer.wrap(undoScoreByte).getInt();
                Log.i(TAG, "FileInputStream Read: undoScore = " + unScore);
            } else {
                // does not has undo data
                Log.i(TAG, "FileInputStream Read: Game does not has undo data");
                undoYn = false;
            }
            fiStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            succeeded = false;
        }

        dismissShowMessageOnScreen();
        ColorBallsApp.isShowingLoadingGameMessage = false;
        ColorBallsApp.isProcessingJob = false;

        String textContent;
        if (succeeded) {
            // reflesh Main UI with loaded data
            textContent = succeededLoadGameString;
            setHasSound(soundYn);
            setIsEasyLevel(easyYn);
            gridData.setNextBalls(nextBalls);
            gridData.setCellValues(gameCells);
            currentScore = cScore;
            undoEnable = undoYn;
            gridData.setUndoNextBalls(undoNextBalls);
            gridData.setBackupCells(backupCells);
            undoScore = unScore;
            // start update UI
            currentScoreView.setText(String.format(Locale.getDefault(), "%8d", currentScore));
            displayGameView();
        } else {
            textContent = failedLoadGameString;
        }
        AlertDialogFragment gameLoadedDialog = new AlertDialogFragment(new AlertDialogFragment.DialogButtonListener() {
            @Override
            public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                dialogFragment.dismissAllowingStateLoss();
            }
        });
        Bundle args = new Bundle();
        args.putString("TextContent", textContent);
        args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
        args.putFloat("TextFontSize", textFontSize);
        args.putInt("Color", Color.BLUE);
        args.putInt("Width", 0);    // wrap_content
        args.putInt("Height", 0);   // wrap_content
        args.putInt("NumButtons", 1);
        args.putBoolean("IsAnimation", false);
        gameLoadedDialog.setArguments(args);
        gameLoadedDialog.show(getSupportFragmentManager(), "GameLoadedDialogTag");
    }

    // public methods
    public void undoTheLast() {

        if (!undoEnable) {
            return;
        }

        ColorBallsApp.isProcessingJob = true; // started undoing

        gridData.undoTheLast();

        // restore the screen
        displayGameView();

        bouncingStatus = 0;
        bouncyBallIndexI = -1;
        bouncyBallIndexJ = -1;

        currentScore = undoScore;
        currentScoreView.setText( String.format(Locale.getDefault(), "%8d",currentScore));

        // completedPath = true;
        undoEnable = false;

        ColorBallsApp.isProcessingJob = false;    // finished
    }

    public void showTop10ScoreHistory() {
        ColorBallsApp.isProcessingJob = true;
        ColorBallsApp.isShowingLoadingMessage = true;
        showMessageOnScreen(loadingString);
        Intent myService = new Intent(this, MyTop10ScoresService.class);
        startService(myService);
    }
    public void showGlobalTop10History() {
        ColorBallsApp.isProcessingJob = true;
        ColorBallsApp.isShowingLoadingMessage = true;
        showMessageOnScreen(loadingString);
        Intent myService = new Intent(this, MyGlobalTop10Service.class);
        String webUrl = ColorBallsApp.REST_Website + "/GetTop10PlayerscoresREST";  // ASP.NET Core
        webUrl += "?gameId=" + ColorBallsApp.GameId;   // parameters
        myService.putExtra("WebUrl", webUrl);
        startService(myService);
    }
    public void newGame() {
        recordScore(1);   //   START A NEW GAME
    }
    public void saveGame() {
        AlertDialogFragment sureSaveDialog = new AlertDialogFragment(new AlertDialogFragment.DialogButtonListener() {
            @Override
            public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                // cancel the action of saving game
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                // start saving game to internal storage
                dialogFragment.dismissAllowingStateLoss();
                int numOfSaved = 0;
                try {
                    File inputFile = new File(ColorBallsApp.AppContext.getFilesDir(), ColorBallsApp.NumOfSavedGameFileName);
                    FileInputStream fiStream = new FileInputStream(inputFile);
                    numOfSaved = fiStream.read();
                    fiStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (numOfSaved < ColorBallsApp.Max_Saved_Games) {
                    boolean succeeded = startSavingGame(numOfSaved, false);
                } else {
                    // display warning to users
                    final int finalNumOfSaved = numOfSaved;
                    AlertDialogFragment warningSaveGameDialog = new AlertDialogFragment(new AlertDialogFragment.DialogButtonListener() {
                        @Override
                        public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                            dialogFragment.dismissAllowingStateLoss();
                        }

                        @Override
                        public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                            dialogFragment.dismissAllowingStateLoss();
                            boolean succeeded = startSavingGame(finalNumOfSaved, true);
                        }
                    });
                    Bundle args = new Bundle();
                    args.putString("TextContent", warningSaveGameString); // excessive the number (5)
                    args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
                    args.putFloat("TextFontSize", textFontSize);
                    args.putInt("Color", Color.BLUE);
                    args.putInt("Width", 0);    // wrap_content
                    args.putInt("Height", 0);   // wrap_content
                    args.putInt("NumButtons", 2);
                    args.putBoolean("IsAnimation", false);
                    warningSaveGameDialog.setArguments(args);
                    warningSaveGameDialog.show(getSupportFragmentManager(), "SaveGameWarningDialogTag");
                }
            }
        });
        Bundle args = new Bundle();
        args.putString("TextContent", sureToSaveGameString);
        args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
        args.putFloat("TextFontSize", textFontSize);
        args.putInt("Color", Color.BLUE);
        args.putInt("Width", 0);    // wrap_content
        args.putInt("Height", 0);   // wrap_content
        args.putInt("NumButtons", 2);
        args.putBoolean("IsAnimation", false);
        sureSaveDialog.setArguments(args);
        sureSaveDialog.show(getSupportFragmentManager(), "SureSaveDialogTag");
    }
    public void loadGame() {
        AlertDialogFragment sureLoadDialog = new AlertDialogFragment(new AlertDialogFragment.DialogButtonListener() {
            @Override
            public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                // cancel the action of loading game
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                // start loading game to internal storage
                dialogFragment.dismissAllowingStateLoss();
                startLoadingGame();
            }
        });
        Bundle args = new Bundle();
        args.putString("TextContent", sureToLoadGameString);
        args.putInt("FontSize_Scale_Type", ColorBallsApp.FontSize_Scale_Type);
        args.putFloat("TextFontSize", textFontSize);
        args.putInt("Color", Color.BLUE);
        args.putInt("Width", 0);    // wrap_content
        args.putInt("Height", 0);   // wrap_content
        args.putInt("NumButtons", 2);
        args.putBoolean("IsAnimation", false);
        sureLoadDialog.setArguments(args);
        sureLoadDialog.show(getSupportFragmentManager(), "SureLoadDialogTag");
    }

    public void recordScore(final int entryPoint) {

        ColorBallsApp.isProcessingJob = true;

        final EditText et = new EditText(this);
        et.setTextColor(Color.BLUE);
        // et.setBackground(new ColorDrawable(Color.TRANSPARENT));
        // et.setBackgroundColor(Color.TRANSPARENT);
        et.setHint(nameStr);
        ScreenUtil.resizeTextSize(et, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        et.setGravity(Gravity.CENTER);
        AlertDialog alertD = new AlertDialog.Builder(this).create();
        alertD.setTitle(null);
        alertD.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertD.setCancelable(false);
        alertD.setView(et);
        alertD.setButton(DialogInterface.BUTTON_NEGATIVE, cancelStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showInterstitialAdAndNewGameOrQuit(entryPoint);
            }
        });
        alertD.setButton(DialogInterface.BUTTON_POSITIVE, submitStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // removed on 2019-02-20 no global ranking any more
                // use thread to add a record to database (remote database on AWS-EC2)
                Thread restThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String webUrl = ColorBallsApp.REST_Website + "/AddOneRecordREST";   // ASP.NET Cor
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("PlayerName", et.getText().toString());
                            jsonObject.put("Score", currentScore);
                            jsonObject.put("GameId", ColorBallsApp.GameId);
                            PlayerRecordRest.addOneRecord(webUrl, jsonObject);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Log.d(TAG, "Failed to add one record to Playerscore table.");
                        }
                    }
                };
                restThread.start();

                // modified on 2018-11-07
                boolean isInTop10 = ColorBallsApp.ScoreSQLiteDB.isInTop10(currentScore);
                if (isInTop10) {
                    // inside top 10
                    // record the current score
                    ColorBallsApp.ScoreSQLiteDB.addScore(et.getText().toString(),currentScore);
                    ColorBallsApp.ScoreSQLiteDB.deleteAllAfterTop10();  // only keep the top 10
                }
                //

                showInterstitialAdAndNewGameOrQuit(entryPoint);
            }
        });

        alertD.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                setDialogStyle(dialog);
            }
        });

        alertD.show();

        ColorBallsApp.isProcessingJob = false;
    }

    public boolean getIsEasyLevel() {
        return this.isEasyLevel;
    }

    public void setIsEasyLevel(boolean yn) {
        this.isEasyLevel = yn;
        if (this.isEasyLevel) {
            // easy level
            getGridData().setNumOfColorsUsed(ColorBallsApp.NumOfColorsUsedByEasy);
        } else {
            // difficult
            getGridData().setNumOfColorsUsed(ColorBallsApp.NumOfColorsUsedByDifficult);
        }
        // displayNextColorBalls(); // removed on 2019-01-22
    }

    public boolean getHasSound() {
        return hasSound;
    }
    public void setHasSound(boolean hasSound) {
        this.hasSound = hasSound;
    }
    public ImageView getScoreImageView() {
        return scoreImageView;
    }
    public void showMessageOnScreen(String messageString) {
        Bitmap dialog_board_image = BitmapFactory.decodeResource(ColorBallsApp.AppResources, R.drawable.dialog_board_image);
        Bitmap showBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(dialog_board_image, messageString, Color.RED);
        scoreImageView.setVisibility(View.VISIBLE);
        scoreImageView.setImageBitmap(showBitmap);
    }
    public void dismissShowMessageOnScreen() {
        scoreImageView.setImageBitmap(null);
        scoreImageView.setVisibility(View.GONE);
    }

    private class CalculateScoreThread extends Thread {

        // private final String ScoreDialogTag = new String("ScoreDialogFragment");
        private int numBalls = 0;
        private int color = 0;
        private int score = 0;
        private HashSet<Point> hasPoint = null;
        private boolean isNextBalls;
        private Bitmap scoreBitmap;

        private final Handler calculateScoreHandler = new Handler(Looper.getMainLooper());
        private boolean isSynchronizeFinished = false;

        public CalculateScoreThread(HashSet<Point> linkedPoint, boolean isNextBalls) {
            this.isNextBalls = isNextBalls;
            if (linkedPoint != null) {
                hasPoint = new HashSet<>(linkedPoint);
                Point point = hasPoint.iterator().next();
                color = gridData.getCellValue(point.x, point.y);

                numBalls = hasPoint.size();
                score = scoreCalculate(numBalls);
                String scoreString = String.valueOf(score);
                Bitmap dialog_board_image = BitmapFactory.decodeResource(ColorBallsApp.AppResources, R.drawable.dialog_board_image);
                scoreBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(dialog_board_image, scoreString, Color.BLACK);
            }
        }

        private synchronized void onPreExecute() {
            threadCompleted[1] = false;

            isSynchronizeFinished = false;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (calculateScoreHandler) {
                        scoreImageView.setVisibility(View.VISIBLE);
                        isSynchronizeFinished = true;
                        calculateScoreHandler.notifyAll();
                        Log.d(TAG, "CalculateScoreThread-->onPreExecute() --> notifyAll()");
                    }
                }
            });
            synchronized (calculateScoreHandler) {
                while (!isSynchronizeFinished) {
                    try {
                        Log.d(TAG, "CalculateScoreThread-->onPreExecute() --> wait()");
                        calculateScoreHandler.wait();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "CalculateScoreThread-->onPreExecute() wait exception");
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "CalculateScoreThread-->onPreExecute() is finished.");
        }

        private synchronized void doInBackground() {
            if (hasPoint != null) {
                int twinkleCountDown = 5;
                for (int i = 1; i <= twinkleCountDown; i++) {
                    int md = i % 2; // modulus
                    onProgressUpdate(md);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                onProgressUpdate(2);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                onProgressUpdate(3);
            } else {
                Log.d(TAG, "CalculateScoreThread-->doInBackground()-->hasPoint is null.");
            }
            Log.d(TAG, "CalculateScoreThread-->doInBackground() is finished.");
        }

        private synchronized void onProgressUpdate(int status) {
            if (hasPoint != null) {
                isSynchronizeFinished = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (calculateScoreHandler) {
                            switch (status) {
                                case 0:
                                    for (Point item : hasPoint) {
                                        ImageView v = findViewById(item.x * rowCounts + item.y);
                                        drawBall(v, color);
                                    }
                                    break;
                                case 1:
                                    for (Point item : hasPoint) {
                                        ImageView v = findViewById(item.x * rowCounts + item.y);
                                        drawOval(v, color);
                                    }
                                    break;
                                case 2:
                                case 3:
                                    scoreImageView.setImageBitmap(scoreBitmap);
                                    break;
                            }
                            isSynchronizeFinished = true;
                            calculateScoreHandler.notifyAll();
                            Log.d(TAG, "CalculateScoreThread-->onProgressUpdate() --> notifyAll()");
                        }
                    }
                });
                synchronized (calculateScoreHandler) {
                    while (!isSynchronizeFinished) {
                        try {
                            Log.d(TAG, "CalculateScoreThread-->onProgressUpdate() --> wait()");
                            calculateScoreHandler.wait();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "CalculateScoreThread-->onProgressUpdate() wait exception");
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                Log.d(TAG, "CalculateScoreThread-->onProgressUpdate()-->hasPoint is null.");
            }
            Log.d(TAG, "CalculateScoreThread-->onProgressUpdate() is finished.");
        }

        private synchronized void onPostExecute() {
            if (hasPoint != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scoreImageView.setImageBitmap(scoreBitmap);
                        // clear values of cells
                        for (Point item : hasPoint) {
                            clearCell(item.x, item.y);
                        }
                        // update the UI
                        undoScore = currentScore;
                        currentScore = currentScore + score;
                        currentScoreView.setText(String.format(Locale.getDefault(), "%8d", currentScore));
                        // hide score ImageView
                        scoreImageView.setImageBitmap(null);
                        scoreImageView.setVisibility(View.GONE);
                        // added on 2019-03-30
                        if (isNextBalls) {
                            displayNextColorBalls();
                        }
                    }
                });
            } else {
                Log.d(TAG, "CalculateScoreThread-->onPostExecute()-->hasPoint is null.");
            }

            threadCompleted[1] = true;  // user can start input command

            Log.d(TAG, "CalculateScoreThread-->onPostExecute() is finished.");
        }

        @Override
        public synchronized void run() {
            super.run();
            onPreExecute();
            doInBackground();
            onPostExecute();
        }

        public void startCalculate() {
            start();
        }
    }

    // end of original from MainFragmentUI.java


    private void setBannerAndNativeAdUI() {
        bannerLinearLayout = findViewById(R.id.linearlayout_for_ads_in_myActivity);
        String testString = "";
        // for debug mode
        if (com.smile.colorballs.BuildConfig.DEBUG) {
            testString = "IMG_16_9_APP_INSTALL#";
        }
        String facebookBannerID = testString + ColorBallsApp.facebookBannerID;
        //
        myBannerAdView = new SetBannerAdViewForAdMobOrFacebook(this, null, bannerLinearLayout
                , ColorBallsApp.googleAdMobBannerID, facebookBannerID);
        myBannerAdView.showBannerAdViewFromAdMobOrFacebook(ColorBallsApp.AdProvider);

        // show AdMob native ad if the device is tablet
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            String nativeAdvancedId0 = ColorBallsApp.googleAdMobNativeID;     // real native ad unit id
            FrameLayout nativeAdsFrameLayout = findViewById(R.id.nativeAdsFrameLayout);
            com.google.android.ads.nativetemplates.TemplateView nativeAdTemplateView = findViewById(R.id.nativeAdTemplateView);
            nativeTemplate = new GoogleAdMobNativeTemplate(this, nativeAdsFrameLayout
                    , nativeAdvancedId0, nativeAdTemplateView);
            nativeTemplate.showNativeAd();
        }
    }

    private void setBroadcastReceiver() {
        myReceiver = new MyBroadcastReceiver();
        myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(MyTop10ScoresService.Action_Name);
        myIntentFilter.addAction(MyGlobalTop10Service.Action_Name);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(myReceiver, myIntentFilter);
    }

    public void exitApplication() {
        final Handler handlerClose = new Handler(Looper.getMainLooper());
        final int timeDelay = 200;
        handlerClose.postDelayed( ()-> {
            // exit application
            finish();
        },timeDelay);
    }

    public void showAdUntilDismissed(Activity activity) {
        if (ColorBallsApp.InterstitialAd == null) {
            return;
        }

        showInterstitialAdThread = ColorBallsApp.InterstitialAd.new ShowInterstitialAdThread(0, ColorBallsApp.AdProvider);
        showInterstitialAdThread.startShowAd();
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private final String SUCCEEDED = "0";
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
                                        showAdUntilDismissed(activity);
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
                        startActivityForResult(top10Intent, Top10ScoreActivityRequestCode);
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
                                        showAdUntilDismissed(activity);
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
                        startActivityForResult(globalTop10Intent, GlobalTop10ActivityRequestCode);
                    }
                    dismissShowMessageOnScreen();
                    ColorBallsApp.isShowingLoadingMessage = false;
                    ColorBallsApp.isProcessingJob = false;
                    break;
            }
        }
    }
}
