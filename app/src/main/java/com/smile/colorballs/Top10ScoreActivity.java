package com.smile.colorballs;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class Top10ScoreActivity extends AppCompatActivity {

    private static final String TAG = "Top10ScoreActivity";
    private ArrayList<String> top10Players = new ArrayList<String>();
    private ArrayList<Integer> top10Scores = new ArrayList<Integer>();

    private FragmentManager fmManager = null;
    private Fragment top10ScoreFragment = null;
    private int top10LayoutId;

    private int fontSizeForText = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setContentView(R.layout.activity_top10_score);
        top10LayoutId = R.id.top10_score_linear_layout;
        System.out.println("Top10ScoreActivity.onCreate().");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            top10Players = extras.getStringArrayList("Top10Players");
            top10Scores = extras.getIntegerArrayList("Top10Scores");
            fontSizeForText = extras.getInt("FontSizeForText");
        }

        top10ScoreFragment = Top10ScoreFragment.newInstance(top10Players, top10Scores, fontSizeForText, new Top10ScoreFragment.Top10OkButtonListener() {
            @Override
            public void buttonOkClick(Activity activity) {
                activity.finish();
            }
        });

        fmManager = getSupportFragmentManager();
        FragmentTransaction ft = fmManager.beginTransaction();
        Fragment currentTop10ScoreFragment = fmManager.findFragmentByTag(Top10ScoreFragment.Top10ScoreFragmentTag);
        if (currentTop10ScoreFragment == null) {
            ft.add(top10LayoutId, top10ScoreFragment, Top10ScoreFragment.Top10ScoreFragmentTag);
        } else {
            ft.replace(top10LayoutId, top10ScoreFragment, Top10ScoreFragment.Top10ScoreFragmentTag);
        }
        ft.commit();
        System.out.println("Top10ScoreActivity.onResume() -----> top10ScoreFragment is created.");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (top10ScoreFragment != null) {
            // remove top10ScoreFragment
            FragmentTransaction ft = fmManager.beginTransaction();
            ft.remove(top10ScoreFragment);
            ft.commit();
            System.out.println("Top10ScoreActivity.onSaveInstanceState() ---> removed top10ScoreFragment");
        }
        super.onSaveInstanceState(outState);
    }
}
