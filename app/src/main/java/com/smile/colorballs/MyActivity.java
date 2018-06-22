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

import com.smile.facebookadsutil.FacebookAds;
import com.smile.scoresqlite.ScoreSQLite;
import com.smile.utility.ScreenUtl;
import java.util.ArrayList;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;

public class MyActivity extends AppCompatActivity {

    // private properties
    private final String TAG = new String("com.smile.colorballs.MyActivity");
    private ScoreSQLite scoreSQLite = null;
    private int mainUiLayoutId = -1;
    private int scoreHistoryLayoutId = -1;
    private MenuItem registerMenuItemEasy = null;
    private MenuItem registerMenuItemDifficult = null;

    private MainUiFragment mainUiFragment = null;
    private Top10ScoreFragment top10ScoreFragment = null;

    private int fontSizeForText = 24;
    private float dialog_widthFactor = 1.0f;
    private float dialog_heightFactor = 1.0f;
    private float dialogFragment_widthFactor = dialog_widthFactor;
    private float dialogFragment_heightFactor = dialog_heightFactor;

    // private properties facebook ads
    private FacebookAds facebookAds = null;

    public MyActivity() {
        System.out.println("MyActivity ---> Constructor");
        scoreSQLite = new ScoreSQLite(MyActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception ex) {
            Log.d(TAG, "Unable to start this Activity.");
            ex.printStackTrace();
            finish();
        }

        Point size = new Point();
        ScreenUtl.getScreenSize(this, size);
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
        setTitle(String.format("%9d", highestScore));

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
        scoreHistoryLayoutId = R.id.scoreHistoryLayout;

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

        facebookAds = new FacebookAds(this);

        // for AdBuddiz ads
        AdBuddiz.setPublisherKey("57c7153c-35dd-488a-beaa-3cae8b3ab668");
        AdBuddiz.cacheAds(this); // this = current Activity
        // AdBuddiz.RewardedVideo.fetch(this); // this = current Activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Top10ScoreActivity.activityRequestCode) {
                // show ads
                facebookAds.showAd(TAG);
                AdBuddiz.showAd(MyActivity.this);   // added on 2017-10-24
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        MenuItem registerMenuItemEndGame = menu.findItem(R.id.quitGame);
        registerMenuItemEndGame.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem registerMenuItemNewGame = menu.findItem(R.id.newGame);
        registerMenuItemNewGame.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem registerMenuItemOption = menu.findItem(R.id.option);
        registerMenuItemOption.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        registerMenuItemEasy = menu.findItem(R.id.easyLevel);
        registerMenuItemDifficult = menu.findItem(R.id.difficultLevel);

        if (mainUiFragment.getEasyLevel()) {
            // easy level
            registerMenuItemDifficult.setChecked(false);
            registerMenuItemEasy.setChecked(true);
        }
        else {
            registerMenuItemDifficult.setChecked(true);
            registerMenuItemEasy.setChecked(false);
        }

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
        if (id == R.id.easyLevel) {
            item.setChecked(true);
            registerMenuItemDifficult.setChecked(false);
            mainUiFragment.getGridData().setMinBallsOneTime(MainUiFragment.MINB);
            mainUiFragment.getGridData().setMaxBallsOneTime(MainUiFragment.MINB);
            mainUiFragment.displayNextColorBalls();
            facebookAds.showAd(TAG);
            AdBuddiz.showAd(this);

            return true;
        }
        if (id == R.id.difficultLevel) {
            item.setChecked(true);
            registerMenuItemEasy.setChecked(false);
            mainUiFragment.getGridData().setMinBallsOneTime(MainUiFragment.MINB);
            mainUiFragment.getGridData().setMaxBallsOneTime(MainUiFragment.MAXB);
            mainUiFragment.displayNextColorBalls();
            facebookAds.showAd(TAG);
            AdBuddiz.showAd(this);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (isChangingConfigurations()) {
            // configuration is changing then remove the top10ScoreFragment
            if (top10ScoreFragment != null) {
                // remove historyFragment
                FragmentManager fmManager = getSupportFragmentManager();
                FragmentTransaction ft = fmManager.beginTransaction();
                ft.remove(top10ScoreFragment);
                ft.commit();
                System.out.println("MyActivity.onSaveInstanceState() is called. ---> removed top10ScoreFragment");
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
        if (facebookAds != null) {
            facebookAds.close();
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

    public FacebookAds getFacebookAds() {
        return this.facebookAds;
    }

    public void showScoreHistory() {
        new ShowTop10Scores().execute();
    }

    private class ShowTop10Scores extends AsyncTask<Void,Integer,ArrayList<Pair<String, Integer>>> {

        public static final String Top10LoadingDialog = "Top10LoadingDialogFragment";
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
                ArrayList<String> playerNames = new ArrayList<String>();
                ArrayList<Integer> playerScores = new ArrayList<Integer>();
                for (Pair pair : resultList) {
                    playerNames.add((String)pair.first);
                    playerScores.add((Integer)pair.second);
                }

                View historyView = findViewById(scoreHistoryLayoutId);
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

                                    facebookAds.showAd(TAG);
                                    AdBuddiz.showAd(MyActivity.this);   // added on 2017-10-24
                                }
                            }
                        });
                        FragmentManager fmManager = getSupportFragmentManager();
                        FragmentTransaction ft = fmManager.beginTransaction();
                        ft = fmManager.beginTransaction();
                        Fragment currentTop10SocreFragment = (Top10ScoreFragment) fmManager.findFragmentByTag(Top10ScoreFragment.Top10ScoreFragmentTag);
                        if (currentTop10SocreFragment == null) {
                            ft.add(scoreHistoryLayoutId, top10ScoreFragment, Top10ScoreFragment.Top10ScoreFragmentTag);
                        } else {
                            ft.replace(scoreHistoryLayoutId, top10ScoreFragment, Top10ScoreFragment.Top10ScoreFragmentTag);
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
                    startActivityForResult(intent, Top10ScoreActivity.activityRequestCode);
                }
            }
        }
    }
}
