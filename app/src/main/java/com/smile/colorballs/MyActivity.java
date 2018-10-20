package com.smile.colorballs;

import com.smile.smilepublicclasseslibrary.facebookadsutil.*;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.smile.Service.MyGlobalTop10IntentService;
import com.smile.Service.MyTop10ScoresIntentService;
import com.smile.utility.ScreenUtil;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyActivity extends AppCompatActivity {

    // private properties
    private final String TAG = new String("com.smile.colorballs.MyActivity");
    private int mainUiLayoutId = -1;
    private int top10LayoutId = -1;

    private MainUiFragment mainUiFragment = null;
    private Top10ScoreFragment top10ScoreFragment = null;
    private GlobalTop10Fragment globalTop10Fragment = null;
    private MyBroadcastReceiver myReceiver;

    private int fontSizeForText = 24;
    private float dialog_widthFactor = 1.0f;
    private float dialog_heightFactor = 1.0f;
    private float dialogFragment_widthFactor = dialog_widthFactor;
    private float dialogFragment_heightFactor = dialog_heightFactor;
    private final int SettingActivityRequestCode = 1;
    private final int Top10ScoreActivityRequestCode = 2;
    private final int GlobalTop10ActivityRequestCode = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Point size = new Point();
        ScreenUtil.getScreenSize(this, size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        float baseWidth = 1080.0f;
        float baseHeight = 1776.0f;
        fontSizeForText = 24;   // default for portrait of cell phone
        // if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        if (ColorBallsApp.AppResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            // if (screenWidth >= 2000) {
            if (ScreenUtil.isTablet(this)) {
                // assume Tablet
                fontSizeForText = 32;
            } else {
                // cell phone
                fontSizeForText = 16;

            }
            dialog_widthFactor = screenWidth / baseHeight;
            if (dialog_widthFactor < 1.0f) {
                dialog_widthFactor = 1.0f;
            }
            dialogFragment_widthFactor = dialog_widthFactor * 2.0f;

            dialog_heightFactor = screenHeight / baseWidth;
            if (dialog_heightFactor < 1.0f) {
                dialog_heightFactor = 1.0f;
            }
            dialogFragment_heightFactor = dialog_heightFactor * 2.0f;
        } else {
            // portrait
            // if (screenWidth >= 1300) {
            if (ScreenUtil.isTablet(this)) {
                // assume Tablet
                fontSizeForText = 48;
            } else {
                // cell phone
                fontSizeForText = 24;
            }
            dialog_widthFactor = screenWidth / baseWidth;
            if (dialog_widthFactor < 1.0f) {
                dialog_widthFactor = 1.0f;
            }
            dialogFragment_widthFactor = dialog_widthFactor;

            dialog_heightFactor = screenHeight / baseHeight;
            if (dialog_heightFactor < 1.0f) {
                dialog_heightFactor = 1.0f;
            }
            dialogFragment_heightFactor = dialog_heightFactor;
        }

        setContentView(R.layout.activity_my);

        int highestScore = ColorBallsApp.ScoreSQLiteDB.readHighestScore();
        setTitle(String.format(Locale.getDefault(), "%9d", highestScore));

        // setting the font size for activity label
        ActionBar actionBar = getSupportActionBar();

        TextView titleView = new TextView(this);
        titleView.setText(actionBar.getTitle());
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(fontSizeForText);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(titleView);
        //

        mainUiLayoutId = R.id.mainUiLayout;
        top10LayoutId = R.id.top10Layout;

        FragmentManager fmManager = getSupportFragmentManager();
        mainUiFragment = (MainUiFragment) fmManager.findFragmentByTag(MainUiFragment.MainUiFragmentTag);
        View gameView = findViewById(mainUiLayoutId);
        if (gameView != null) {
            if (mainUiFragment == null) {
                mainUiFragment = MainUiFragment.newInstance();
                FragmentTransaction ft = fmManager.beginTransaction();
                ft.add(mainUiLayoutId, mainUiFragment, MainUiFragment.MainUiFragmentTag);
                ft.commit();
            }
        }

        myReceiver = new MyBroadcastReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyTop10ScoresIntentService.Action_Name);
        intentFilter.addAction(MyGlobalTop10IntentService.Action_Name);
        localBroadcastManager.registerReceiver(myReceiver, intentFilter);

        // for AdBuddiz ads removed on 2018-07-03
        // AdBuddiz.setPublisherKey("57c7153c-35dd-488a-beaa-3cae8b3ab668");
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
                        boolean isEasyLevel = extras.getBoolean("IsEasyLevel");
                        mainUiFragment.setHasSound(hasSound);
                        mainUiFragment.setIsEasyLevel(isEasyLevel);
                    }
                }
                break;
            case Top10ScoreActivityRequestCode:
                break;
            case GlobalTop10ActivityRequestCode:
                // show Facebook ads
                showFacebookAdsUntilDismissed(this);
                break;
        }

        mainUiFragment.setIsProcessingJob(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        MenuItem registerMenuItemEndGame = menu.findItem(R.id.quitGame);
        registerMenuItemEndGame.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem registerMenuItemNewGame = menu.findItem(R.id.newGame);
        registerMenuItemNewGame.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem registerMenuItemOption = menu.findItem(R.id.setting);
        registerMenuItemOption.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        boolean isProcessingJob = mainUiFragment.getIsProcessingJob();
        if (!isProcessingJob) {
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
            if (id == R.id.setting) {
                mainUiFragment.setIsProcessingJob(true);    // started procession job
                Intent intent = new Intent(this, SettingActivity.class);
                Bundle extras = new Bundle();
                extras.putInt("FontSizeForText", fontSizeForText);
                extras.putBoolean("HasSound", mainUiFragment.getHasSound());
                extras.putBoolean("IsEasyLevel", mainUiFragment.getIsEasyLevel());
                intent.putExtras(extras);
                startActivityForResult(intent, SettingActivityRequestCode);
                return true;
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
                ft.commit();
                mainUiFragment.setIsProcessingJob(false);
            }
            if (globalTop10Fragment != null) {
                // remove globalTop10Fragment
                FragmentManager fmManager = getSupportFragmentManager();
                FragmentTransaction ft = fmManager.beginTransaction();
                ft.remove(globalTop10Fragment);
                ft.commit();
                mainUiFragment.setIsProcessingJob(false);
            }
        } else {
            // if configuration is not changing (still landscape because portrait does not have this fragment)
            // keep top10ScoreFragment on screen (on right side)
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ColorBallsApp.ScoreSQLiteDB != null) {
            ColorBallsApp.ScoreSQLiteDB.close();
        }
        if (ColorBallsApp.FacebookAds != null) {
            ColorBallsApp.FacebookAds.close();
        }
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(myReceiver);
        System.out.println("MyActivity.onDestroy --> myReceiver was unregistered.");
    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        quitApplication();
    }

    public void quitApplication() {
        final Handler handlerClose = new Handler();
        final int timeDelay = 300;
        handlerClose.postDelayed(new Runnable() {
            public void run() {
                // quit game
                finish();
            }
        },timeDelay);
    }

    public void reStartApplication()
    {
        final Handler handlerClose = new Handler();
        final int timeDelay = 300;
        handlerClose.postDelayed(new Runnable() {
            public void run() {
                // restart this MyActivity, new game
                Intent myIntent = getIntent();
                finish();
                startActivity(myIntent);
            }
        },timeDelay);
    }

    // private methods
    private void showFacebookAdsUntilDismissed(Activity activity) {
        FacebookInterstitialAds.ShowFacebookAdsAsyncTask_AlertDialog showAdsAsyncTask =
                ColorBallsApp.FacebookAds.new ShowFacebookAdsAsyncTask_AlertDialog(this, ColorBallsApp.AppResources.getString(R.string.showingAdsString), fontSizeForText, 0);
        showAdsAsyncTask.execute();
    }

    // public methods
    public int getFontSizeForText() {
        return fontSizeForText;
    }
    public float getDialog_widthFactor() {
        return dialog_widthFactor;
    }
    public float getDialog_heightFactor() {
        return dialog_heightFactor;
    }
    public float getDialogFragment_widthFactor() {
        return dialogFragment_widthFactor;
    }
    public float getDialogFragment_heightFactor() {
        return dialogFragment_heightFactor;
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private final String SUCCEEDED = "0";
        private final String FAILED = "1";

        @Override
        public void onReceive(Context context, Intent intent) {

            System.out.println("MyActivity.MyBroadcastReceiver.onReceive() is called.");
            mainUiFragment.dismissShowingLoadingMessage();

            String actionName = intent.getAction();
            Bundle extras;
            ArrayList<String> playerNames = new ArrayList<>();
            ArrayList<Integer> playerScores = new ArrayList<>();
            View historyView;
            switch (actionName) {
                case MyTop10ScoresIntentService.Action_Name:
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
                            top10ScoreFragment = Top10ScoreFragment.newInstance(playerNames, playerScores, fontSizeForText, new Top10ScoreFragment.Top10OkButtonListener() {
                                @Override
                                public void buttonOkClick(Activity activity) {
                                    if (top10ScoreFragment != null) {
                                        // remove top10ScoreFragment to dismiss the top 10 score screen
                                        FragmentManager fmManager = getSupportFragmentManager();
                                        FragmentTransaction ft = fmManager.beginTransaction();
                                        ft.remove(top10ScoreFragment);
                                        // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                                        ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                                    }
                                }
                            });
                            FragmentManager fmManager = getSupportFragmentManager();
                            FragmentTransaction ft = fmManager.beginTransaction();
                            ft = fmManager.beginTransaction();
                            Fragment currentTop10ScoreFragment = fmManager.findFragmentByTag(Top10ScoreFragment.Top10ScoreFragmentTag);
                            if (currentTop10ScoreFragment == null) {
                                ft.add(top10LayoutId, top10ScoreFragment, Top10ScoreFragment.Top10ScoreFragmentTag);
                            } else {
                                ft.replace(top10LayoutId, top10ScoreFragment, Top10ScoreFragment.Top10ScoreFragmentTag);
                            }
                            // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                            ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                            mainUiFragment.setIsProcessingJob(false);
                        }
                    } else {
                        // for Portrait
                        top10ScoreFragment = null;
                        Intent top10Intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                        Bundle top10Extras = new Bundle();
                        top10Extras.putStringArrayList("Top10Players", playerNames);
                        top10Extras.putIntegerArrayList("Top10Scores", playerScores);
                        top10Extras.putInt("FontSizeForText", fontSizeForText);
                        top10Intent.putExtras(top10Extras);
                        startActivityForResult(top10Intent, Top10ScoreActivityRequestCode);
                    }
                    break;
                case MyGlobalTop10IntentService.Action_Name:
                    String[] result;
                    extras = intent.getExtras();
                    if (extras != null) {
                        result = extras.getStringArray("RESULT");
                    } else {
                        //failed
                        result = new String[] {FAILED};
                    }
                    String status = result[0].toUpperCase();
                    if (status.equals(SUCCEEDED)) {
                        // Succeeded
                        try {
                            JSONArray jArray = new JSONArray(result[1]);
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject jo = jArray.getJSONObject(i);
                                playerNames.add(jo.getString("PlayerName"));
                                playerScores.add(jo.getInt("Score"));
                            }

                        } catch (JSONException ex) {
                            String errorMsg = ex.toString();
                            ex.printStackTrace();
                            playerNames.add("JSONException->JSONArray");
                            playerScores.add(0);
                        }

                    } else if (status.equals(FAILED)) {
                        // Failed
                        playerNames.add("Web Connection Failed.");
                        playerScores.add(0);
                    } else {
                        // Exception
                        playerNames.add("Exception on Web read.");
                        playerScores.add(0);
                    }

                    historyView = findViewById(top10LayoutId);
                    if (historyView != null) {
                        // if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if (ColorBallsApp.AppResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            globalTop10Fragment = GlobalTop10Fragment.newInstance(playerNames, playerScores, fontSizeForText, new GlobalTop10Fragment.GlobalTop10OkButtonListener() {
                                @Override
                                public void buttonOkClick(Activity activity) {
                                    if (globalTop10Fragment != null) {
                                        // remove GlobalTop10Fragment to dismiss the top 10 score screen
                                        FragmentManager fmManager = getSupportFragmentManager();
                                        FragmentTransaction ft = fmManager.beginTransaction();
                                        ft.remove(globalTop10Fragment);
                                        // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                                        ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                                        showFacebookAdsUntilDismissed(activity);
                                    }
                                }
                            });
                            FragmentManager fmManager = getSupportFragmentManager();
                            FragmentTransaction ft = fmManager.beginTransaction();
                            ft = fmManager.beginTransaction();
                            Fragment currentGlobalTop10Fragment = fmManager.findFragmentByTag(GlobalTop10Fragment.GlobalTop10FragmentTag);
                            if (currentGlobalTop10Fragment == null) {
                                ft.add(top10LayoutId, globalTop10Fragment, GlobalTop10Fragment.GlobalTop10FragmentTag);
                            } else {
                                ft.replace(top10LayoutId, globalTop10Fragment, GlobalTop10Fragment.GlobalTop10FragmentTag);
                            }
                            // ft.commit(); // removed on 2018-06-22 12:01 am because it will crash app under some situation
                            ft.commitAllowingStateLoss();   // resolve the crash issue temporarily
                            mainUiFragment.setIsProcessingJob(false);
                        }
                    } else {
                        // for Portrait
                        globalTop10Fragment = null;
                        Intent globalTop10Intent = new Intent(getApplicationContext(), GlobalTop10Activity.class);
                        Bundle globalTop10Extras = new Bundle();
                        globalTop10Extras.putStringArrayList("Top10Players", playerNames);
                        globalTop10Extras.putIntegerArrayList("Top10Scores", playerScores);
                        globalTop10Extras.putInt("FontSizeForText", fontSizeForText);
                        globalTop10Intent.putExtras(globalTop10Extras);
                        startActivityForResult(globalTop10Intent, GlobalTop10ActivityRequestCode);
                    }
                    break;
            }
        }
    }
}
