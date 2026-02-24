package com.smile.fivecolorballs;

import static com.google.android.ump.ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Handler;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Locale;

import com.smile.colorballs_main.R;
import com.smile.colorballs_main.constants.Constants;
import com.smile.colorballs_main.tools.LogUtil;
import com.smile.colorballs_main.views.CbSettingActivity;
import com.smile.colorballs_main.views.Top10Activity;
import com.smile.fivecolorballs.constants.FiveBallsConstants;
import com.smile.fivecolorballs.presenters.MyPresenter;
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.models.ExitAppTimer;
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil;
import com.smile.smilelibraries.scoresqlite.ScoreSQLite;
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;
import com.smile.smilelibraries.utilities.SoundPoolUtil;
import com.smile.smilelibraries.utilities.UmpUtil;

import org.jetbrains.annotations.NotNull;

public class MyActivity extends AppCompatActivity implements MyPresenter.MyPresentView {

    // private properties
    private static final String TAG = "MyActivity";
    private MyPresenter mPresenter;
    public HashMap<Integer, Bitmap> colorBallMap = new HashMap<>();
    public HashMap<Integer, Bitmap> colorOvalBallMap = new HashMap<>();
    private Toolbar supportToolbar;
    private float textFontSize;
    private float fontScale;
    private float screenWidth;
    private float screenHeight;
    private SetBannerAdView myBannerAdView;
    private GoogleAdMobNativeTemplate nativeTemplate;
    private final static String GameOverDialogTag = "GameOverDialogFragmentTag";
    private ImageView scoreImageView = null;
    private TextView toolbarTitleTextView;
    private TextView currentScoreView;
    private int rowCounts = 9;
    private int colCounts = 9;
    private AlertDialog saveScoreAlertDialog;
    private AlertDialogFragment sureSaveDialog;
    private AlertDialogFragment sureLoadDialog;
    private AlertDialogFragment gameOverDialog;
    private ActivityResultLauncher<Intent> settingLauncher;
    private ActivityResultLauncher<Intent> top10Launcher;
    private boolean touchDisabled = true;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        touchDisabled = true;
        LogUtil.d(TAG, "onCreate.touchDisabled = " + touchDisabled);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.redball);
        colorBallMap.put(Constants.COLOR_RED, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.redball_o);
        colorOvalBallMap.put(Constants.COLOR_RED, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.greenball);
        colorBallMap.put(Constants.COLOR_GREEN, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.greenball_o);
        colorOvalBallMap.put(Constants.COLOR_GREEN, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.blueball);
        colorBallMap.put(Constants.COLOR_BLUE, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.blueball_o);
        colorOvalBallMap.put(Constants.COLOR_BLUE, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.magentaball);
        colorBallMap.put(Constants.COLOR_MAGENTA, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.magentaball_o);
        colorOvalBallMap.put(Constants.COLOR_MAGENTA, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.yellowball);
        colorBallMap.put(Constants.COLOR_YELLOW, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.yellowball_o);
        colorOvalBallMap.put(Constants.COLOR_YELLOW, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.cyanball);
        colorBallMap.put(Constants.COLOR_CYAN, bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.cyanball_o);
        colorOvalBallMap.put(Constants.COLOR_CYAN, bm);

        mPresenter = new MyPresenter( this);

        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_my);

        createActivityUI();
        createGameView(savedInstanceState);
        setBannerAndNativeAdUI();

        settingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    LogUtil.i(TAG, TAG + "onCreate.settingLauncher.result received");
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    Intent data = result.getData();
                    if (data == null) return;
                    Bundle extras = data.getExtras();
                    if (extras == null) return;
                    boolean hasSound = extras.getBoolean(Constants.HAS_SOUND, true);
                    mPresenter.setHasSound(hasSound);
                    boolean hasNext = extras.getBoolean(Constants.HAS_NEXT,true);
                    mPresenter.setHasNext(hasNext, true);
                }
        );

        top10Launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the result here
                    LogUtil.i(TAG, "top10Launcher.result = " + result.toString());
                }
        );

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed");
                // Handle the fragment's back press (null check for playerFragment)
                onBackKeyPressed();
            }
        });

        // String deviceHashedId = "0FFD34B018082E4BCF218FE6299B48A2"; // for debug test
        String deviceHashedId = ""; // for release
        UmpUtil.INSTANCE.initConsentInformation(
                this,
                DEBUG_GEOGRAPHY_EEA,
                deviceHashedId,
                () -> {
                    LogUtil.d(TAG, "dataConsentRequest.finished");
                    // enabling receiving touch events
                    touchDisabled = false;
                }
        );


        LogUtil.d(TAG, "onCreate() is finished.");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (touchDisabled) {
            // Consume the touch event, effectively disabling touch
            return true;
        }
        // Allow touch events to proceed
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        if (supportToolbar != null) {
            final int popupThemeId = supportToolbar.getPopupTheme();
            // final Context wrapper = new ContextThemeWrapper(this, R.style.menu_text_style);
            final Context wrapper = new ContextThemeWrapper(this, popupThemeId);
            ScreenUtil.resizeMenuTextIconSize(wrapper, menu, fontScale);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        boolean isProcessingJob = mPresenter.isProcessingJob();

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
                showTop10Players(true);
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.globalTop10) {
                showTop10Players(false);
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
                Intent setIntent = new Intent(this, CbSettingActivity.class);
                Bundle extras = new Bundle();
                extras.putString(Constants.GAME_ID, Constants.FIVE_COLOR_BALLS_ID);
                extras.putBoolean(Constants.HAS_SOUND, mPresenter.getHasSound());
                extras.putInt(Constants.GAME_LEVEL, Constants.GAME_LEVEL_1);
                extras.putBoolean(Constants.HAS_NEXT, mPresenter.isHasNext());
                setIntent.putExtras(extras);
                settingLauncher.launch(setIntent);
                return true;
            }
            if (id == R.id.privacyPolicy) {
                int requestCode = 10;
                PrivacyPolicyUtil.startPrivacyPolicyActivity(this, requestCode);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        LogUtil.d(TAG, "onSaveInstanceState() is called");
        if (saveScoreAlertDialog != null) {
            saveScoreAlertDialog.dismiss();
        }
        mPresenter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume() is called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause() is called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.d(TAG, "onStop() is called");
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy() is called");

        if (mPresenter != null) {
            mPresenter.release();
        }

        if (myBannerAdView != null) {
            myBannerAdView.destroy();
            myBannerAdView = null;
        }
        if (nativeTemplate != null) {
            nativeTemplate.release();
        }

        if (sureSaveDialog != null) {
            sureSaveDialog.dismissAllowingStateLoss();
        }

        if (sureLoadDialog != null) {
            sureLoadDialog.dismissAllowingStateLoss();
        }

        if (gameOverDialog != null) {
            gameOverDialog.dismissAllowingStateLoss();
        }

        super.onDestroy();
    }

    private void onBackKeyPressed() {
        LogUtil.d(TAG, "onBackKeyPressed");
        // singleton
        ExitAppTimer exitAppTimer = ExitAppTimer.getInstance(1000);
        if (exitAppTimer.canExit()) {
            mPresenter.quitGame();
        } else {
            exitAppTimer.start();
            ScreenUtil.showToast(this, getString(R.string.backKeyToExitApp),
                    textFontSize * 0.7f, Toast.LENGTH_SHORT);
        }
    }

    private void createActivityUI() {
        findOutTextFontSize();
        findOutScreenSize();
        setUpSupportActionBar();
    }

    private void findOutTextFontSize() {
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        fontScale = ScreenUtil.getPxFontScale(this);
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
        LogUtil.d(TAG, "createGameView.mainGameViewHeight = " + mainGameViewHeight);

        float gameViewWeightSum = gameViewLinearLayout.getWeightSum();
        LinearLayout mainGameViewUiLayout = findViewById(R.id.gameViewLayout);
        LinearLayout.LayoutParams mainGameViewtUiLayoutParams = (LinearLayout.LayoutParams) mainGameViewUiLayout.getLayoutParams();
        float mainGameViewUi_weight = mainGameViewtUiLayoutParams.weight;
        float mainGameViewWidth = screenWidth * (mainGameViewUi_weight / gameViewWeightSum);
        LogUtil.d(TAG, "createGameView.mainGameViewWidth = " + mainGameViewWidth);

        // layout_for_game_view.xml
        float height_weightSum_GameViewUi = 100;    // default
        try {
            LinearLayout gameViewUiLayout = findViewById(R.id.linearlayout_for_game_view_ui);
            float temp = gameViewUiLayout.getWeightSum();
            if (temp != 0) {
                height_weightSum_GameViewUi = temp;
            }
        } catch (Exception ex) {
            LogUtil.e(TAG, "createGameView.Exception: ", ex);
        }

        LinearLayout scoreNextBallsLayout = findViewById(R.id.score_next_balls_layout);
        float width_weightSum_scoreNextBallsLayout = scoreNextBallsLayout.getWeightSum();
        LinearLayout.LayoutParams scoreNextBallsLayoutParams = (LinearLayout.LayoutParams) scoreNextBallsLayout.getLayoutParams();
        float height_weight_scoreNextBallsLayout = scoreNextBallsLayoutParams.weight;

        // display the highest score and current score
        toolbarTitleTextView = findViewById(R.id.toolbarTitleTextView);
        ScreenUtil.resizeTextSize(toolbarTitleTextView, textFontSize);

        currentScoreView = findViewById(R.id.currentScoreTextView);
        ScreenUtil.resizeTextSize(currentScoreView, textFontSize);

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
                imageView.setId(MyPresenter.nextBallsViewIdStart + (nextBallsColumn * i + j));
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
        colCounts = gridCellsLayout.getColumnCount();
        int cellWidth = (int) (mainGameViewWidth / colCounts);
        int eight10thOfHeight = (int) (mainGameViewHeight / height_weightSum_GameViewUi * height_weight_gridCellsLayout);
        if (mainGameViewWidth > eight10thOfHeight) {
            // if screen width greater than 8-10th of screen height
            cellWidth = eight10thOfHeight / rowCounts;
        }
        int cellH = cellWidth;

        // added on 2018-10-02 to test and it works
        // setting the width and the height of GridLayout by using the FrameLayout that is on top of it
        frameLp.width = cellWidth * colCounts;
        frameLp.topMargin = 20;
        frameLp.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams oneBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneBallLp.width = cellWidth;
        oneBallLp.height = cellH;
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
                imageView.setBackgroundResource(R.drawable.box_image);
                imageView.setClickable(true);
                imageView.setOnClickListener(v -> {
                    if (!mPresenter.isProcessingJob()) {
                        mPresenter.doDrawBallsAndCheckListener(v);
                    }
                });
                gridCellsLayout.addView(imageView, imId, oneBallLp);
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
        LogUtil.d(TAG, "createColorBallsGame.isNewGame = " + isNewGame);
    }

    private void setDialogStyle(DialogInterface dialog) {
        AlertDialog dlg = (AlertDialog)dialog;
        Window win = dlg.getWindow();
        if (win == null) return ;

        win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        win.setDimAmount(0.0f); // no dim for background screen
        win.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        win.setBackgroundDrawableResource(R.drawable.dialog_board_image);

        Button nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
        ScreenUtil.resizeTextSize(nBtn, textFontSize);
        nBtn.setTypeface(Typeface.DEFAULT_BOLD);
        nBtn.setTextColor(Color.RED);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)nBtn.getLayoutParams();
        layoutParams.weight = 10;
        nBtn.setLayoutParams(layoutParams);

        Button pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        ScreenUtil.resizeTextSize(pBtn, textFontSize);
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
    }

    private void showTop10Players(boolean isLocal) {
        Intent topIntent = new Intent(this, Top10Activity.class);
        Bundle extras = new Bundle();
        extras.putString(Constants.GAME_ID, Constants.FIVE_COLOR_BALLS_ID);
        extras.putString(Constants.DATABASE_NAME, FiveBallsConstants.FIVE_COLOR_BALLS_DATABASE);
        extras.putBoolean(Constants.IS_LOCAL_TOP10, isLocal);
        topIntent.putExtras(extras);
        top10Launcher.launch(topIntent);
    }

    private void setBannerAndNativeAdUI() {
        LinearLayout bannerLinearLayout = findViewById(R.id.linearlayout_for_ads_in_myActivity);
        String bannerId = FiveCBallsApp.ADMOB_BANNER_ID;     // real Banner ID
        // use test Banner ID, Google’s universal test IDs
        // bannerId = "ca-app-pub-3940256099942544/6300978111";   // test Banner ID
        myBannerAdView = new SetBannerAdView(this, null,
                bannerLinearLayout, bannerId, "");
        myBannerAdView.showBannerAdView(0); // AdMob first

        // show AdMob native ad if the device is tablet
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            String nativeAdvancedId0 = FiveCBallsApp.ADMOB_NATIVE_ID;     // real native ad unit id
            // nativeAdvancedId0 = "ca-app-pub-3940256099942544/2247696110";   // test native ad unit id
            FrameLayout nativeAdsFrameLayout = findViewById(R.id.nativeAdsFrameLayout);
            nativeAdsFrameLayout.setVisibility(View.VISIBLE);
            com.google.android.ads.nativetemplates.TemplateView nativeAdTemplateView
                    = findViewById(R.id.nativeAdTemplateView);
            nativeAdTemplateView.setVisibility(View.VISIBLE);
            nativeTemplate = new GoogleAdMobNativeTemplate(this, nativeAdsFrameLayout
                    , nativeAdvancedId0, nativeAdTemplateView);
            nativeTemplate.showNativeAd();
        }
    }

    private void exitApplication() {
        final Handler handlerClose = new Handler(Looper.getMainLooper());
        final int timeDelay = 200;
        // exit application
        handlerClose.postDelayed(this::finish,timeDelay);
    }

    // implementing MyActivity.PresentView
    @Override
    public @NotNull String getLoadingStr() {
        return getString(R.string.loadingString);
    }

    @Override
    public @NotNull String geSavingGameStr() {
        return getString(R.string.savingGameString);
    }

    @Override
    public @NotNull String getLoadingGameStr() {
        return getString(R.string.loadingGameString);
    }

    @Override
    public @NotNull String getSureToSaveGameStr() {
        return getString(R.string.sureToSaveGameStr);
    }

    @Override
    public @NotNull String getSureToLoadGameStr() {
        return getString(R.string.sureToLoadGameStr);
    }

    @Override
    public @NotNull String getSaveScoreStr() {
        return getString(R.string.saveScoreStr);
    }

    @Override
    public @NotNull SoundPoolUtil soundPool() {
        LogUtil.d(TAG, "soundPool");
        SoundPoolUtil sPool = new SoundPoolUtil(this, R.raw.uhoh);
        LogUtil.d(TAG, "soundPool.sPool = " + sPool);
        return sPool;
    }

    @Override
    public @NotNull ScoreSQLite getScoreDatabase() {
        return new ScoreSQLite(this, FiveBallsConstants.FIVE_COLOR_BALLS_DATABASE);
    }

    @Override
    public FileInputStream fileInputStream(@NotNull String fileName) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(getFilesDir(), fileName));
        } catch (FileNotFoundException ex) {
            LogUtil.e(TAG, "FileInputStream.FileNotFoundException: ", ex);
        }
        return fis;
    }

    @Override
    public FileOutputStream fileOutputStream(@NotNull String fileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(getFilesDir(), fileName));
        } catch (FileNotFoundException ex) {
            LogUtil.e(TAG, "fileOutputStream.FileNotFoundException: ", ex);
        }
        return fos;
    }

    @Override
    public HashMap<Integer, Bitmap> getColorBallMap() {
        return colorBallMap;
    }

    @Override
    public HashMap<Integer, Bitmap> getColorOvalBallMap() {
        return colorOvalBallMap;
    }

    @Override
    public ImageView getImageViewById(int id) {
        return findViewById(id);
    }

    @Override
    public void updateHighestScoreOnUi(int highestScore) {
        toolbarTitleTextView.setText(String.format(Locale.getDefault(), "%8d", highestScore));
    }

    @Override
    public void updateCurrentScoreOnUi(int score) {
        currentScoreView.setText(String.format(Locale.getDefault(), "%8d", score));
    }

    @Override
    public void showMessageOnScreen(String messageString) {
        Bitmap dialog_board_image = BitmapFactory.decodeResource(getResources(),
                R.drawable.dialog_board_image);
        Bitmap showBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(dialog_board_image,
                messageString, Color.RED);
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
                boolean succeeded = mPresenter.startSavingGame();
                String msg;
                if (succeeded) {
                    msg = getString(R.string.succeededSaveGameString);
                } else {
                    msg = getString(R.string.failedSaveGameString);
                }
                LogUtil.d(TAG, "showSaveGameDialog.okButtonOnClick.msg = " + msg);
                ScreenUtil.showToast(MyActivity.this, msg, textFontSize, Toast.LENGTH_LONG);
            }
        });
        Bundle args = new Bundle();
        args.putString("TextContent", getString(R.string.sureToSaveGameString));
        args.putInt("FontSize_Scale_Type", ScreenUtil.FontSize_Pixel_Type);
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
                String msg;
                if (succeeded) {
                    msg = getString(R.string.succeededLoadGameString);
                } else {
                    msg = getString(R.string.failedLoadGameString);
                }
                ScreenUtil.showToast(MyActivity.this, msg, textFontSize, Toast.LENGTH_LONG);
            }
        });
        Bundle args = new Bundle();
        args.putString("TextContent", getString(R.string.sureToLoadGameString));
        args.putInt("FontSize_Scale_Type", ScreenUtil.FontSize_Pixel_Type);
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
        args.putInt("FontSize_Scale_Type", ScreenUtil.FontSize_Pixel_Type);
        args.putFloat("TextFontSize", textFontSize);
        args.putInt("Color", Color.BLUE);
        args.putInt("Width", 0);    // wrap_content
        args.putInt("Height", 0);   // wrap_content
        args.putInt("NumButtons", 2);
        args.putBoolean("IsAnimation", false);

        gameOverDialog.setArguments(args);
        gameOverDialog.show(getSupportFragmentManager(), GameOverDialogTag);

        LogUtil.d(TAG, "gameOverDialog.show() has been called.");
    }

    @Override
    public void showSaveScoreAlertDialog(final int entryPoint) {
        mPresenter.setSaveScoreAlertDialogState(entryPoint, true);
        final EditText et = new EditText(this);
        et.setTextColor(Color.BLUE);
        et.setHint(getString(R.string.nameStr));
        ScreenUtil.resizeTextSize(et, textFontSize);
        et.setGravity(Gravity.CENTER);
        saveScoreAlertDialog = new AlertDialog.Builder(this).create();
        saveScoreAlertDialog.setTitle(null);
        saveScoreAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveScoreAlertDialog.setCancelable(false);
        saveScoreAlertDialog.setView(et);
        saveScoreAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancelStr), (dialog, which) -> {
            dialog.dismiss();
            quitOrNewGame(entryPoint);
            mPresenter.setSaveScoreAlertDialogState(entryPoint, false);
            saveScoreAlertDialog = null;
        });
        saveScoreAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.submitStr), (dialog, which) -> {
            mPresenter.saveScore(et.getText().toString());
            dialog.dismiss();
            quitOrNewGame(entryPoint);
            mPresenter.setSaveScoreAlertDialogState(entryPoint, false);
            saveScoreAlertDialog = null;
        });

        saveScoreAlertDialog.setOnShowListener(this::setDialogStyle);

        saveScoreAlertDialog.show();
    }
    // end of implementing
}
