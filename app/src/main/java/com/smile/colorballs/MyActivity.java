package com.smile.colorballs;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.smile.alertdialogfragment.AlertDialogFragment;
import com.smile.dao.PlayerRecordRest;
import com.smile.facebookadsutil.*;
import com.smile.scoresqlite.ScoreSQLite;
import com.smile.utility.ScreenUtil;

import java.util.ArrayList;
import java.util.Locale;

// import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyActivity extends AppCompatActivity {

    // private properties
    private final String TAG = new String("com.smile.colorballs.MyActivity");
    private ScoreSQLite scoreSQLite = null;
    private int mainUiLayoutId = -1;
    private int top10LayoutId = -1;

    private MainUiFragment mainUiFragment = null;
    private Top10ScoreFragment top10ScoreFragment = null;
    private GlobalTop10Fragment globalTop10Fragment = null;

    private int fontSizeForText = 24;
    private float dialog_widthFactor = 1.0f;
    private float dialog_heightFactor = 1.0f;
    private float dialogFragment_widthFactor = dialog_widthFactor;
    private float dialogFragment_heightFactor = dialog_heightFactor;

    // private properties facebook ads
    private FacebookInterstitialAds facebookInterstitialAds = null;

    // public properties
    public static final int SettingActivityRequestCode = 1;
    public static final int Top10ScoreActivityRequestCode = 2;
    public static final int GlobalTop10ActivityRequestCode = 3;

    // public static final String REST_Website = new String("http://192.168.0.11:5000/Playerscore");
    public static final String REST_Website = new String("    http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore");
    public MyActivity() {
        System.out.println("MyActivity ---> Constructor");
        scoreSQLite = new ScoreSQLite(MyActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        try {
            // requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception ex) {
            Log.d(TAG, "Unable to start this Activity.");
            ex.printStackTrace();
            finish();
        }
        */

        Point size = new Point();
        ScreenUtil.getScreenSize(this, size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        float baseWidth = 1080.0f;
        float baseHeight = 1776.0f;
        fontSizeForText = 24;   // default for portrait of cell phone
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            if (screenWidth >= 2000) {
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
            if (screenWidth >= 1300) {
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

        int highestScore = scoreSQLite.readHighestScore();
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
                System.out.println("MyActivity -----> mainUiFragment is created.");
            }
        }

        System.out.println("Package Name = " + getPackageName());

        String facebookPlacementID = new String("200699663911258_200701030577788"); // for colorballs
        if (BuildConfig.APPLICATION_ID == "com.smile.colorballs") {
            facebookPlacementID = new String("200699663911258_200701030577788"); // for colorballs
        } else if (BuildConfig.APPLICATION_ID == "com.smile.fivecolorballs") {
            facebookPlacementID = new String("241884113266033_241884616599316"); // for fivecolorballs
        } else {
            // default
        }
        facebookInterstitialAds = new FacebookInterstitialAds(this, facebookPlacementID);

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
                            mainUiFragment.setEasyLevel(isEasyLevel);
                        }
                    }
                    break;
                case Top10ScoreActivityRequestCode:
                    break;
                case GlobalTop10ActivityRequestCode:
                    break;
            }

        // show ads
        Log.i(TAG, "Facebook showing ads");
        facebookInterstitialAds.showAd(TAG);
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
            Intent intent = new Intent(this, SettingActivity.class);
            Bundle extras = new Bundle();
            extras.putInt("FontSizeForText", fontSizeForText);
            extras.putBoolean("HasSound", mainUiFragment.getHasSound());
            extras.putBoolean("IsEasyLevel", mainUiFragment.getEasyLevel());
            intent.putExtras(extras);
            startActivityForResult(intent, SettingActivityRequestCode);
            return true;
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
                System.out.println("MyActivity.onSaveInstanceState() is called. ---> removed top10ScoreFragment");
            }
            if (globalTop10Fragment != null) {
                // remove globalTop10Fragment
                FragmentManager fmManager = getSupportFragmentManager();
                FragmentTransaction ft = fmManager.beginTransaction();
                ft.remove(globalTop10Fragment);
                ft.commit();
                System.out.println("MyActivity.onSaveInstanceState() is called. ---> removed globalTop10Fragment");
            }
        } else {
            // if configuration is not changing (still landscape because portrait does not have this fragment)
            // keep top10ScoreFragment on screen (on right side)
        }
        System.out.println("MyActivity.onSaveInstanceState() is called");

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (isFinishing()) {
            if (facebookInterstitialAds != null) {
                facebookInterstitialAds.close();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        finish();
    }

    public ScoreSQLite getScoreSQLite() {
        return this.scoreSQLite;
    }

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

    public FacebookInterstitialAds getFacebookInterstitialAds() {
        return this.facebookInterstitialAds;
    }

    public void showTop10ScoreHistory() {
        new ShowTop10Scores().execute();
    }

    public void showGlobalTop10History() {
        new ShowGlobalTop10().execute();
    }

    private class ShowTop10Scores extends AsyncTask<Void,Integer,ArrayList<Pair<String, Integer>>> {

        private static final String Top10LoadingDialog = "Top10LoadingDialogFragment";
        private Animation animationText = null;
        private AlertDialogFragment loadingDialog = null;

        public ShowTop10Scores() {
            System.out.println("ShowTop10Scores()->fontSizeForText = " + fontSizeForText);
            System.out.println("ShowTop10Scores()->dialogFragment_widthFactor = " + dialogFragment_widthFactor);
            loadingDialog = new AlertDialogFragment();
            Bundle args = new Bundle();
            args.putString("textContent", getResources().getString(R.string.loadScore));
            args.putFloat("textSize", fontSizeForText * dialogFragment_widthFactor);
            args.putInt("color", Color.RED);
            args.putInt("width", 0);    // wrap_content
            args.putInt("height", 0);   // wrap_content
            args.putInt("numButtons", 0);
            loadingDialog.setArguments(args);
        }

        @Override
        protected void onPreExecute() {

            animationText = new AlphaAnimation(0.0f,1.0f);
            animationText.setDuration(300);
            animationText.setStartOffset(0);
            animationText.setRepeatMode(Animation.REVERSE);
            animationText.setRepeatCount(Animation.INFINITE);
            loadingDialog.show(getSupportFragmentManager(), Top10LoadingDialog);
        }

        @Override
        protected ArrayList<Pair<String, Integer>> doInBackground(Void... params) {
            int i = 0;
            publishProgress(i);
            // String[] result = scoreSQLite.read10HighestScore();
            ArrayList<Pair<String, Integer>> resultList = scoreSQLite.readTop10ScoreList();
            // wait for one second
            try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }

            i = 1;
            TextView textLoad = loadingDialog.getText_shown();
            while (textLoad == null) {
                textLoad = loadingDialog.getText_shown();
                SystemClock.sleep(20);
            }

            publishProgress(i);

            // return result;
            return resultList;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (!isCancelled()) {
                TextView textLoad = loadingDialog.getText_shown();
                try {
                    if (progress[0] == 0) {
                        if (animationText != null) {
                            textLoad.startAnimation(animationText);
                        }
                    } else {
                        if (animationText != null) {
                            textLoad.clearAnimation();
                            animationText = null;
                        }
                        textLoad.setText("");
                    }
                } catch (Exception ex) {
                    System.out.println("MyActivity.ShowTop10Scores.onProgressUpdate() failed --> textLoad animation");
                    ex.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Pair<String, Integer>> resultList) {

            if (!isCancelled()) {
                System.out.println("MyActivity.ShowTop10Scores() ---> calling loadingDialog.dismissAllowingStateLoss()");
                // loadingDialog.dismiss(); // removed on 2018-06-18
                loadingDialog.dismissAllowingStateLoss();   // added on 2018-06-18
                ArrayList<String> playerNames = new ArrayList<>();
                ArrayList<Integer> playerScores = new ArrayList<>();
                for (Pair pair : resultList) {
                    playerNames.add((String)pair.first);
                    playerScores.add((Integer)pair.second);
                }

                View historyView = findViewById(top10LayoutId);
                if (historyView != null) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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

                                    facebookInterstitialAds.showAd(TAG);
                                    // AdBuddiz.showAd(MyActivity.this);   // added on 2017-10-24
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

                        System.out.println("MyActivity.ShowTop10Scores() -----> top10ScoreFragment is created.");
                    }
                } else {
                    // for Portrait
                    top10ScoreFragment = null;
                    Intent intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                    Bundle extras = new Bundle();
                    extras.putStringArrayList("Top10Players", playerNames);
                    extras.putIntegerArrayList("Top10Scores", playerScores);
                    extras.putInt("FontSizeForText", fontSizeForText);
                    intent.putExtras(extras);
                    startActivityForResult(intent, Top10ScoreActivityRequestCode);
                }
            }
        }
    }

    private class ShowGlobalTop10 extends AsyncTask<Void,Integer,String[]> {

        private static final String GlobalTop10LoadingDialog = "GlobalTop10LoadingDialogFragment";
        private Animation animationText = null;
        private AlertDialogFragment loadingDialog = null;
        private final String SUCCEEDED = "0";
        private final String FAILED = "1";
        private final String EXCEPTION = "2";

        public ShowGlobalTop10() {
            System.out.println("ShowGlobalTop10()->fontSizeForText = " + fontSizeForText);
            System.out.println("ShowGlobalTop10()->dialogFragment_widthFactor = " + dialogFragment_widthFactor);
            loadingDialog = new AlertDialogFragment();
            Bundle args = new Bundle();
            args.putString("textContent", getResources().getString(R.string.loadScore));
            args.putFloat("textSize", fontSizeForText * dialogFragment_widthFactor);
            args.putInt("color", Color.RED);
            args.putInt("width", 0);    // wrap_content
            args.putInt("height", 0);   // wrap_content
            args.putInt("numButtons", 0);
            loadingDialog.setArguments(args);
        }

        @Override
        protected void onPreExecute() {

            animationText = new AlphaAnimation(0.0f,1.0f);
            animationText.setDuration(300);
            animationText.setStartOffset(0);
            animationText.setRepeatMode(Animation.REVERSE);
            animationText.setRepeatCount(Animation.INFINITE);
            loadingDialog.show(getSupportFragmentManager(), GlobalTop10LoadingDialog);
        }

        @Override
        protected String[] doInBackground(Void... params) {
            int i = 0;
            String[] result = {"",""};

            publishProgress(i);

            String webUrl = new String(REST_Website + "/GetTop10PlayerscoresREST");   // ASP.NET Core
            result = PlayerRecordRest.getTop10Scores(webUrl);

            // wait for one second
            try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }

            i = 1;
            TextView textLoad = loadingDialog.getText_shown();
            while (textLoad == null) {
                textLoad = loadingDialog.getText_shown();
                SystemClock.sleep(20);
            }

            publishProgress(i);

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (!isCancelled()) {
                TextView textLoad = loadingDialog.getText_shown();
                try {
                    if (progress[0] == 0) {
                        if (animationText != null) {
                            textLoad.startAnimation(animationText);
                        }
                    } else {
                        if (animationText != null) {
                            textLoad.clearAnimation();
                            animationText = null;
                        }
                        textLoad.setText("");
                    }
                } catch (Exception ex) {
                    System.out.println("MyActivity.ShowGlobalTop10.onProgressUpdate() failed --> textLoad animation");
                    ex.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String[] result) {

            if (!isCancelled()) {
                System.out.println("MyActivity.ShowGlobalTop10() ---> calling loadingDialog.dismissAllowingStateLoss()");
                // loadingDialog.dismiss(); // removed on 2018-06-18
                loadingDialog.dismissAllowingStateLoss();   // added on 2018-06-18

                ArrayList<String> playerNames = new ArrayList<>();
                ArrayList<Integer> playerScores = new ArrayList<>();

                String status = result[0].toUpperCase();
                if (status.equals(SUCCEEDED)) {
                    // Succeeded
                    try {
                        JSONArray jArray = new JSONArray(result[1]);

                        System.out.println("JSONArray = " + jArray);

                        for (int i=0; i<jArray.length(); i++) {
                            JSONObject jo = jArray.getJSONObject(i);
                            playerNames.add(jo.getString("PlayerName"));
                            playerScores.add(jo.getInt("Score"));
                        }

                    } catch(JSONException ex) {
                        String errorMsg = ex.toString();
                        Log.d(TAG, "Failed to parse JSONObject from the result." + "\n" + errorMsg);
                        ex.printStackTrace();
                        playerNames.add("JSONException->JSONArray");
                        playerScores.add(0);
                    }

                } else if (status.equals(FAILED)) {
                    // Failed
                    Log.d(TAG, "Failed to get global top 10.");
                    playerNames.add("Web Connection Failed.");
                    playerScores.add(0);
                } else {
                    // Exception
                    Log.d(TAG, "Failed to get global top 10 because of exception.");
                    playerNames.add("Exception on Web read.");
                    playerScores.add(0);
                }

                View historyView = findViewById(top10LayoutId);
                if (historyView != null) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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

                                    facebookInterstitialAds.showAd(TAG);
                                    // AdBuddiz.showAd(MyActivity.this);   // added on 2017-10-24
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

                        System.out.println("MyActivity.ShowGlobalTop10() -----> globalTop10Fragment is created.");
                    }
                } else {
                    // for Portrait
                    globalTop10Fragment = null;
                    Intent intent = new Intent(getApplicationContext(), GlobalTop10Activity.class);
                    Bundle extras = new Bundle();
                    extras.putStringArrayList("Top10Players", playerNames);
                    extras.putIntegerArrayList("Top10Scores", playerScores);
                    extras.putInt("FontSizeForText", fontSizeForText);
                    intent.putExtras(extras);
                    startActivityForResult(intent, GlobalTop10ActivityRequestCode);
                }
            }
        }
    }
}
