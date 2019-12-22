package com.smile.colorballs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.smile.Service.MyGlobalTop10IntentService;
import com.smile.Service.MyTop10ScoresIntentService;
import com.smile.smilelibraries.Models.ExitAppTimer;
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil;
import com.smile.smilelibraries.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;

public class MyActivity extends AppCompatActivity {

    // private properties
    private final String TAG = new String("com.smile.colorballs.MyActivity");
    private final String GlobalTop10FragmentTag = "GlobalTop10FragmentTag";
    private final String LocalTop10FragmentTag = "LocalTop10FragmentTag";
    private final int SettingActivityRequestCode = 1;
    private final int Top10ScoreActivityRequestCode = 2;
    private final int GlobalTop10ActivityRequestCode = 3;
    private final int PrivacyPolicyActivityRequestCode = 10;
    private final int QuitGame = 0;
    private final int NewGame = 1;

    private float textFontSize;
    private float fontScale;

    private Toolbar supportToolbar;
    private int mainUiFragmentLayoutId = -1;
    private int top10LayoutId = -1;
    private AdView bannerAdView = null;

    private MainUiFragment mainUiFragment = null;
    private Top10ScoreFragment top10ScoreFragment = null;
    private Top10ScoreFragment globalTop10Fragment = null;
    private float mainFragmentHeight;
    private float mainFragmentWidth;

    private MyBroadcastReceiver myReceiver;
    private IntentFilter myIntentFilter;
    // private int isQuitOrNewGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate() is called");
        /*
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        */

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, ColorBallsApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, ColorBallsApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, ColorBallsApp.FontSize_Scale_Type, 0.0f);

        float textSizeCompInfo;
        if (ScreenUtil.isTablet(this)) {
            // Table then change orientation to Landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            textSizeCompInfo = textFontSize * 0.6f;
        } else {
            // phone then change orientation to Portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            textSizeCompInfo = textFontSize;
        }

        Point size = ScreenUtil.getScreenSize(this);
        float screenWidth = size.x;
        float screenHeight = size.y;

        float statusBarHeight = ScreenUtil.getStatusBarHeight(this);
        float actionBarHeight = ScreenUtil.getActionBarHeight(this);
        // keep navigation bar
        screenHeight = screenHeight - statusBarHeight - actionBarHeight;

        setContentView(R.layout.activity_my);

        TextView companyDescriptionTextView = findViewById(R.id.companyDescriptionTextView);
        if (companyDescriptionTextView != null) {
            ScreenUtil.resizeTextSize(companyDescriptionTextView, textSizeCompInfo, ColorBallsApp.FontSize_Scale_Type);
        }

        TextView companyNameTextView = findViewById(R.id.companyNameTextView);
        if (companyNameTextView != null) {
            ScreenUtil.resizeTextSize(companyNameTextView, textSizeCompInfo, ColorBallsApp.FontSize_Scale_Type);
        }

        TextView companyContactEmailTextView = findViewById(R.id.companyContactEmailTextView);
        if (companyContactEmailTextView != null) {
            ScreenUtil.resizeTextSize(companyContactEmailTextView, textSizeCompInfo, ColorBallsApp.FontSize_Scale_Type);
        }

        supportToolbar = findViewById(R.id.colorballs_toolbar);
        setSupportActionBar(supportToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        LinearLayout linearLayout_myActivity = findViewById(R.id.linearLayout_myActivity);
        float main_WeightSum = linearLayout_myActivity.getWeightSum();

        mainUiFragmentLayoutId = R.id.mainUiFragmentLayout;
        top10LayoutId = R.id.top10Layout;

        LinearLayout gameViewLinearLayout = findViewById(R.id.gameViewLinearLayout);
        float gameViewWeightSum = gameViewLinearLayout.getWeightSum();
        LinearLayout.LayoutParams gameViewLp = (LinearLayout.LayoutParams) gameViewLinearLayout.getLayoutParams();
        float mainFragment_Weight = gameViewLp.weight;
        mainFragmentHeight = screenHeight * mainFragment_Weight / main_WeightSum;

        LinearLayout mainUiFragmentLayout = findViewById(mainUiFragmentLayoutId);
        LinearLayout.LayoutParams mainUiFragmentLayoutParams = (LinearLayout.LayoutParams)mainUiFragmentLayout.getLayoutParams();
        float mainUiFragment_weight = mainUiFragmentLayoutParams.weight;
        mainFragmentWidth = screenWidth * (mainUiFragment_weight / gameViewWeightSum);

        FragmentManager fmManager = getSupportFragmentManager();
        mainUiFragment = (MainUiFragment) fmManager.findFragmentByTag(MainUiFragment.MainUiFragmentTag);

        if (mainUiFragment == null) {
            mainUiFragment = MainUiFragment.newInstance();
            FragmentTransaction ft = fmManager.beginTransaction();
            ft.add(mainUiFragmentLayoutId, mainUiFragment, MainUiFragment.MainUiFragmentTag);
            if (mainUiFragment.isStateSaved()) {
                ft.commitAllowingStateLoss();
            } else {
                ft.commit();
            }
        }

        LinearLayout bannerLinearLayout = findViewById(R.id.linearlayout_for_ads_in_myActivity);
        LinearLayout companyInfoLayout = findViewById(R.id.linearlayout_for_company_information);
        if (!ColorBallsApp.googleAdMobBannerID.isEmpty()) {
            if (companyInfoLayout != null) {
                companyInfoLayout.setVisibility(View.GONE);
            }
            Log.d(TAG, "Starting the initialization for Banner Ad.");
            bannerAdView = new AdView(this);
            bannerAdView.setAdSize(AdSize.BANNER);
            bannerAdView.setAdUnitId(ColorBallsApp.googleAdMobBannerID);
            bannerLinearLayout.addView(bannerAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            bannerAdView.loadAd(adRequest);
        } else {
            // show company information
            if (bannerLinearLayout != null) {
                bannerLinearLayout.setVisibility(View.GONE);
            }
            if (companyInfoLayout != null) {
                companyInfoLayout.setVisibility(View.VISIBLE);
            }
        }

        myReceiver = new MyBroadcastReceiver();
        myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(MyTop10ScoresIntentService.Action_Name);
        myIntentFilter.addAction(MyGlobalTop10IntentService.Action_Name);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(myReceiver, myIntentFilter);

        // isQuitOrNewGame = QuitGame; // default is quiting game when destroy activity

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
                        mainUiFragment.setHasSound(hasSound);
                        boolean isEasyLevel = extras.getBoolean("IsEasyLevel");
                        mainUiFragment.setIsEasyLevel(isEasyLevel);
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
            // removed on 2017-10-24
            // Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate);
            mainUiFragment.recordScore(0);   //   from   END PROGRAM
            return true;
        }
        if (id == R.id.newGame) {
            mainUiFragment.newGame();
            return true;
        }

        if (!isProcessingJob) {
            if (id == R.id.undoGame) {
                mainUiFragment.undoTheLast();
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.top10) {
                mainUiFragment.showTop10ScoreHistory();
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.globalTop10) {
                mainUiFragment.showGlobalTop10History();
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.saveGame) {
                mainUiFragment.saveGame();
                return super.onOptionsItemSelected(item);
            }
            if (id == R.id.loadGame) {
                mainUiFragment.loadGame();
                return true;
            }
            if (id == R.id.setting) {
                ColorBallsApp.isProcessingJob = true;    // started procession job
                Intent intent = new Intent(this, SettingActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean("HasSound", mainUiFragment.getHasSound());
                extras.putBoolean("IsEasyLevel", mainUiFragment.getIsEasyLevel());
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
    protected void onSaveInstanceState(Bundle outState) {

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
        } else {
            // if configuration is not changing (still landscape because portrait does not have this fragment)
            // keep top10ScoreFragment on screen (on right side)
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
        super.onDestroy();
        Log.d(TAG, "MyActivity.onDestroy() is called");

        /*
        if (ColorBallsApp.ScoreSQLiteDB != null) {
            ColorBallsApp.ScoreSQLiteDB.close();
        }
        */

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(myReceiver);

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

    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        ExitAppTimer exitAppTimer = ExitAppTimer.getInstance(1000); // singleton class
        if (exitAppTimer.canExit()) {
            mainUiFragment.recordScore(0);   //   from   END PROGRAM
        } else {
            exitAppTimer.start();
            ScreenUtil.showToast(this, getString(R.string.backKeyToExitApp), textFontSize*0.7f, ColorBallsApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
        }
    }

    public void exitApplication() {
        // isQuitOrNewGame = QuitGame;
        final Handler handlerClose = new Handler();
        final int timeDelay = 200;
        handlerClose.postDelayed(new Runnable() {
            public void run() {
                // exit application
                finish();
            }
        },timeDelay);
    }

    public void showAdUntilDismissed(Activity activity) {
        if (ColorBallsApp.InterstitialAd == null) {
            return;
        }

        ShowingInterstitialAdsUtil.ShowAdAsyncTask showAdAsyncTask =
                ColorBallsApp.InterstitialAd.new ShowAdAsyncTask(0);
        showAdAsyncTask.execute();
    }
    public float getMainFragmentWidth() {
        return this.mainFragmentWidth;
    }
    public float getMainFragmentHeight() {
        return this.mainFragmentHeight;
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private final String SUCCEEDED = "0";
        private final String FAILED = "1";

        @Override
        public void onReceive(Context context, Intent intent) {

            System.out.println("MyActivity.MyBroadcastReceiver.onReceive() is called.");

            String actionName = intent.getAction();
            Bundle extras;
            ArrayList<String> playerNames = new ArrayList<>();
            ArrayList<Integer> playerScores = new ArrayList<>();
            View historyView;
            switch (actionName) {
                case MyTop10ScoresIntentService.Action_Name:

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
                            ft = fmManager.beginTransaction();
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
                    mainUiFragment.dismissShowMessageOnScreen();
                    ColorBallsApp.isShowingLoadingMessage = false;
                    ColorBallsApp.isProcessingJob = false;
                    break;

                case MyGlobalTop10IntentService.Action_Name:

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
                            ft = fmManager.beginTransaction();
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
                    mainUiFragment.dismissShowMessageOnScreen();
                    ColorBallsApp.isShowingLoadingMessage = false;
                    ColorBallsApp.isProcessingJob = false;
                    break;
            }
        }
    }
}
