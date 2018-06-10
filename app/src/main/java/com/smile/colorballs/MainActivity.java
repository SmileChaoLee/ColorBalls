package com.smile.colorballs;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.smile.scoresqlite.ScoreSQLite;

public class MainActivity extends AppCompatActivity {

    // public properties
    public static final String MainUiFragmentTag = "MainUiFragmentForMainActivity";

    // private properties
    private String TAG = "com.smile.colorballs.MainActivity";
    private ScoreSQLite scoreSQLite = null;
    private int mainUiLayoutId;
    private MenuItem registerMenuItemEasy = null;
    private MenuItem registerMenuItemDifficult = null;
    private MainUiFragment mainUiFragment = null;

    public MainActivity() {
        System.out.println("MainActivity ---> Constructor");
        scoreSQLite = new ScoreSQLite(MainActivity.this);
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

        setContentView(R.layout.activity_main);
        mainUiLayoutId = R.id.mainUiLayout;

        FragmentManager fmManager = getSupportFragmentManager();
        mainUiFragment = (MainUiFragment) fmManager.findFragmentByTag(MainUiFragmentTag);
        if (mainUiFragment == null) {
            mainUiFragment = MainUiFragment.newInstance();
            FragmentTransaction ft = fmManager.beginTransaction();
            ft.add(mainUiLayoutId, mainUiFragment, MainUiFragmentTag);
            ft.commit();
        }

        // for AdBuddiz ads
        AdBuddiz.setPublisherKey("57c7153c-35dd-488a-beaa-3cae8b3ab668");
        AdBuddiz.cacheAds(this); // this = current Activity
        // AdBuddiz.RewardedVideo.fetch(this); // this = current Activity
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

            AdBuddiz.showAd(this);
            // AdBuddiz.RewardedVideo.show(this); // this = current Activity

            return true;
        }
        if (id == R.id.newGame) {
            mainUiFragment.newGame();
            AdBuddiz.showAd(this);
            return true;
        }
        if (id == R.id.easyLevel) {
            item.setChecked(true);
            registerMenuItemDifficult.setChecked(false);
            mainUiFragment.getGridData().setMinBallsOneTime(MainUiFragment.MINB);
            mainUiFragment.getGridData().setMaxBallsOneTime(MainUiFragment.MINB);
            mainUiFragment.displayNextColorBalls();

            AdBuddiz.showAd(this);
            // AdBuddiz.showAd(this);
            // AdBuddiz.RewardedVideo.show(this); // this = current Activity

            return true;
        }
        if (id == R.id.difficultLevel) {
            item.setChecked(true);
            registerMenuItemEasy.setChecked(false);
            mainUiFragment.getGridData().setMinBallsOneTime(MainUiFragment.MINB);
            mainUiFragment.getGridData().setMaxBallsOneTime(MainUiFragment.MAXB);
            mainUiFragment.displayNextColorBalls();

            AdBuddiz.showAd(this);
            // AdBuddiz.showAd(this);
            // AdBuddiz.RewardedVideo.show(this); // this = current Activity

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
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

    public int getMainUiLayoutId() {
        return this.mainUiLayoutId;
    }
}
