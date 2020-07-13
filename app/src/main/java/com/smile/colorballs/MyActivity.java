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
import com.smile.smilelibraries.Models.ExitAppTimer;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil;
import com.smile.smilelibraries.showing_banner_ads_utility.SetBannerAdViewForAdMobOrFacebook;
import com.smile.smilelibraries.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.Locale;

public class MyActivity extends AppCompatActivity implements MyActivityPresenter.PresentView {

    // private properties
    private static final String TAG = "MyActivity";
    private static final String GlobalTop10FragmentTag = "GlobalTop10FragmentTag";
    private static final String LocalTop10FragmentTag = "LocalTop10FragmentTag";
    private final int SettingActivityRequestCode = 1;
    private final int Top10ScoreActivityRequestCode = 2;
    private final int GlobalTop10ActivityRequestCode = 3;
    private final int PrivacyPolicyActivityRequestCode = 10;

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

    private LinearLayout bannerLinearLayout;
    private SetBannerAdViewForAdMobOrFacebook myBannerAdView;
    private GoogleAdMobNativeTemplate nativeTemplate;

    private ShowingInterstitialAdsUtil.ShowInterstitialAdThread showInterstitialAdThread = null;

    private final static String GameOverDialogTag = "GameOverDialogFragmentTag";

    private ImageView scoreImageView = null;

    private TextView toolbarTitleTextView;
    private TextView currentScoreView;

    private int rowCounts = 9;
    private int colCounts = 9;
    // private int cellWidth = 0;
    // private int cellHeight = 0;

    // private GameProperties gameProperties;
    // private GridData gridData;
    private AlertDialog saveScoreAlertDialog;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() is called");

        mPresenter = new MyActivityPresenter(this, this);

        super.onCreate(savedInstanceState);

        ColorBallsApp.InterstitialAd = new ShowingInterstitialAdsUtil(this, ColorBallsApp.facebookAds, ColorBallsApp.googleInterstitialAd);

        if (ScreenUtil.isTablet(this)) {
            // Table then change orientation to Landscape
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            // phone then change orientation to Portrait
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_my);

        createActivityUI();

        createGameView(savedInstanceState);

        setBroadcastReceiver();

        Log.d(TAG, "onCreate() is finished.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SettingActivityRequestCode:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        boolean hasSound = extras.getBoolean("HasSound");
                        mPresenter.setHasSound(hasSound);
                        boolean isEasyLevel = extras.getBoolean("IsEasyLevel");
                        mPresenter.setIsEasyLevel(isEasyLevel);
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
            for (int i = 0; i < menuSize; i++) {
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
                extras.putBoolean("HasSound", mPresenter.getHasSound());
                extras.putBoolean("IsEasyLevel", mPresenter.getIsEasyLevel());
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

        if (saveScoreAlertDialog != null) {
            saveScoreAlertDialog.dismiss();
        }

        mPresenter.onSaveInstanceState(outState);

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
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MyActivity.onStop() is called");
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

        if (mPresenter != null) {
            mPresenter.release();
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
            mPresenter.quitGame();   //   from   END PROGRAM
        } else {
            exitAppTimer.start();
            float toastFontSize = textFontSize * 0.7f;
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

    private void createGameView(Bundle savedInstanceState) {
        // find Out Width and Height of GameView
        LinearLayout linearLayout_myActivity = findViewById(R.id.linearLayout_myActivity);
        float main_WeightSum = linearLayout_myActivity.getWeightSum();

        LinearLayout gameViewLinearLayout = findViewById(R.id.gameViewLinearLayout);
        LinearLayout.LayoutParams gameViewLp = (LinearLayout.LayoutParams) gameViewLinearLayout.getLayoutParams();
        float gameView_Weight = gameViewLp.weight;
        float mainGameViewHeight = screenHeight * gameView_Weight / main_WeightSum;
        Log.d(TAG, "mainGameViewHeight = " + mainGameViewHeight);

        float gameViewWeightSum = gameViewLinearLayout.getWeightSum();
        LinearLayout mainGameViewUiLayout = findViewById(R.id.gameViewLayout);
        LinearLayout.LayoutParams mainGameViewtUiLayoutParams = (LinearLayout.LayoutParams) mainGameViewUiLayout.getLayoutParams();
        float mainGameViewUi_weight = mainGameViewtUiLayoutParams.weight;
        float mainGameViewWidth = screenWidth * (mainGameViewUi_weight / gameViewWeightSum);
        Log.d(TAG, "mainGameViewWidth = " + mainGameViewWidth);
        //

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
        LinearLayout.LayoutParams nextBallsLayoutParams = (LinearLayout.LayoutParams) nextBallsLayout.getLayoutParams();
        float width_weight_nextBalls = nextBallsLayoutParams.weight;

        int nextBallsViewWidth = (int) (mainGameViewWidth * width_weight_nextBalls / width_weightSum_scoreNextBallsLayout);   // 3/5 of screen width

        LinearLayout.LayoutParams oneNextBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneNextBallLp.width = nextBallsViewWidth / nextBallsColumn;
        // the layout_weight for height is 1
        oneNextBallLp.height = (int) (mainGameViewHeight * height_weight_scoreNextBallsLayout / height_weightSum_GameViewUi);
        oneNextBallLp.gravity = Gravity.CENTER;

        ImageView imageView;
        for (int i = 0; i < nextBallsRow; i++) {
            for (int j = 0; j < nextBallsColumn; j++) {
                imageView = new ImageView(this);
                imageView.setId(MyActivityPresenter.nextBallsViewIdStart + (nextBallsColumn * i + j));
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
        Log.d(TAG, "createGameView()-->rowCounts = " + rowCounts);
        colCounts = gridCellsLayout.getColumnCount();
        Log.d(TAG, "createGameView()-->colCounts = " + colCounts);
        // LinearLayout.LayoutParams gridLp = (LinearLayout.LayoutParams) gridCellsLayout.getLayoutParams();
        // float height_weight_gridCellsLayout = gridLp.weight;

        int cellWidth = (int) (mainGameViewWidth / colCounts);
        int eight10thOfHeight = (int) (mainGameViewHeight / height_weightSum_GameViewUi * height_weight_gridCellsLayout);
        if (mainGameViewWidth > eight10thOfHeight) {
            // if screen width greater than 8-10th of screen height
            cellWidth = eight10thOfHeight / rowCounts;
        }
        int cellHeight = cellWidth;

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
                        if ((mPresenter.completedAll()) && (!ColorBallsApp.isProcessingJob)) {
                            mPresenter.doDrawBallsAndCheckListener(v);
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
        createColorBallsGame(savedInstanceState);
    }

    private void createColorBallsGame(Bundle savedInstanceState) {
        saveScoreAlertDialog = null;
        boolean isNewGame = mPresenter.initializeColorBallsGame(rowCounts, colCounts, savedInstanceState);
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
        if (ColorBallsApp.InterstitialAd != null) {
            showInterstitialAdThread = ColorBallsApp.InterstitialAd.new
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

    private void saveGame() {
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
                int numOfSaved = mPresenter.readNumberOfSaved();
                if (numOfSaved < ColorBallsApp.Max_Saved_Games) {
                    boolean succeeded = mPresenter.startSavingGame(numOfSaved);
                    if (succeeded) {
                        ScreenUtil.showToast(getApplicationContext(), getString(R.string.succeededSaveGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                    } else {
                        ScreenUtil.showToast(getApplicationContext(), getString(R.string.failedSaveGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                    }
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
                            boolean succeeded = mPresenter.startSavingGame(finalNumOfSaved);
                            if (succeeded) {
                                ScreenUtil.showToast(getApplicationContext(), getString(R.string.succeededSaveGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                            } else {
                                ScreenUtil.showToast(getApplicationContext(), getString(R.string.failedSaveGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                            }
                            showAdUntilDismissed(MyActivity.this);
                        }
                    });
                    Bundle args = new Bundle();
                    String warningSaveGameString0 = getString(R.string.warningSaveGameString) + " ("
                            + ColorBallsApp.Max_Saved_Games + " "
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
                    warningSaveGameDialog.setArguments(args);
                    warningSaveGameDialog.show(getSupportFragmentManager(), "SaveGameWarningDialogTag");
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
        sureSaveDialog.setArguments(args);
        sureSaveDialog.show(getSupportFragmentManager(), "SureSaveDialogTag");
    }

    private void loadGame() {
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
                boolean succeeded = mPresenter.startLoadingGame();
                if (succeeded) {
                    ScreenUtil.showToast(getApplicationContext(), getString(R.string.succeededLoadGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                } else {
                    ScreenUtil.showToast(getApplicationContext(), getString(R.string.failedLoadGameString), textFontSize, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
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
        sureLoadDialog.setArguments(args);
        sureLoadDialog.show(getSupportFragmentManager(), "SureLoadDialogTag");
    }

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

    private void exitApplication() {
        final Handler handlerClose = new Handler(Looper.getMainLooper());
        final int timeDelay = 200;
        handlerClose.postDelayed( ()-> {
            // exit application
            finish();
        },timeDelay);
    }

    private void showAdUntilDismissed(Activity activity) {
        if (ColorBallsApp.InterstitialAd == null) {
            return;
        }

        showInterstitialAdThread = ColorBallsApp.InterstitialAd.new ShowInterstitialAdThread(0, ColorBallsApp.AdProvider);
        showInterstitialAdThread.startShowAd();
    }

    // implementing MyActivity.PresentView

    @Override
    public ImageView getImageViewById(int id) {
        return findViewById(id);
    }

    public void updateHighestScoreOnUi(int highestScore) {
        toolbarTitleTextView.setText(String.format(Locale.getDefault(), "%8d", highestScore));
    }

    @Override
    public void updateCurrentScoreOnUi(int score) {
        currentScoreView.setText(String.format(Locale.getDefault(), "%8d", score));
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

    public void showGameOverDialog() {
        AlertDialogFragment gameOverDialog = new AlertDialogFragment(new AlertDialogFragment.DialogButtonListener() {
            @Override
            public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss();
                mPresenter.quitGame();   //   Ending the game
            }

            @Override
            public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss();
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
