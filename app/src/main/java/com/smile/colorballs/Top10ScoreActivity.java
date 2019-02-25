package com.smile.colorballs;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

import java.util.ArrayList;

public class Top10ScoreActivity extends AppCompatActivity {

    private static final String TAG = new String("Top10ScoreActivity");
    private final String Top10ScoreFragmentTag = "Top10ScoreFragmentTag";

    private ArrayList<String> top10Players = new ArrayList<>();
    private ArrayList<Integer> top10Scores = new ArrayList<>();

    private FragmentManager fmManager = null;
    private Fragment top10ScoreFragment = null;
    private int top10LayoutId;
    private String top10TitleName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // hide action bar
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();

        } catch (Exception ex) {
            Log.d(TAG, "Unable to start this Activity.");
            ex.printStackTrace();
        }
        */

        if (ScreenUtil.isTablet(this)) {
            // Table then change orientation to Landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            // phone then change orientation to Portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            top10TitleName = extras.getString("Top10TitleName");
            top10Players = extras.getStringArrayList("Top10Players");
            top10Scores = extras.getIntegerArrayList("Top10Scores");
        }

        setContentView(R.layout.activity_top10_score);

        top10LayoutId = R.id.top10_score_linear_layout;

        top10ScoreFragment = Top10ScoreFragment.newInstance(top10TitleName, top10Players, top10Scores, new Top10ScoreFragment.Top10OkButtonListener() {
            @Override
            public void buttonOkClick(Activity activity) {
                // Intent returnIntent = new Intent();
                // setResult(Activity.RESULT_OK, returnIntent);
                // activity.finish();
                // or
                setResult(Activity.RESULT_OK);
                activity.finish();
            }
        });

        fmManager = getSupportFragmentManager();
        FragmentTransaction ft = fmManager.beginTransaction();
        Fragment currentTop10ScoreFragment = fmManager.findFragmentByTag(Top10ScoreFragmentTag);
        if (currentTop10ScoreFragment == null) {
            ft.add(top10LayoutId, top10ScoreFragment, Top10ScoreFragmentTag);
        } else {
            ft.replace(top10LayoutId, top10ScoreFragment, Top10ScoreFragmentTag);
        }
        ft.commit();

        System.out.println("Top10ScoreActivity.onCreate() -----> top10ScoreFragment is created.");
        System.out.println("Top10ScoreActivity.onCreate().");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Top10ScoreActivity.onResume().");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
