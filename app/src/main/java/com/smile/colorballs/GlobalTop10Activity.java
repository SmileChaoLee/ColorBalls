package com.smile.colorballs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import java.util.ArrayList;

public class GlobalTop10Activity extends AppCompatActivity {

    private static final String TAG = new String("GlobalTop10Activity");
    public static final int activityRequestCode = 3;

    private ArrayList<String> top10Players = new ArrayList<String>();
    private ArrayList<Integer> top10Scores = new ArrayList<Integer>();

    private FragmentManager fmManager = null;
    private Fragment globalTop10Fragment = null;
    private int globalTop10LayoutId;

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
        setContentView(R.layout.activity_global_top10);
        globalTop10LayoutId = R.id.global_top10_linear_layout;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            top10Players = extras.getStringArrayList("Top10Players");
            top10Scores = extras.getIntegerArrayList("Top10Scores");
            fontSizeForText = extras.getInt("FontSizeForText");
        }

        globalTop10Fragment = GlobalTop10Fragment.newInstance(top10Players, top10Scores, fontSizeForText, new GlobalTop10Fragment.GlobalTop10OkButtonListener() {
            @Override
            public void buttonOkClick(Activity activity) {
                // Intent returnIntent = new Intent();
                // setResult(Activity.RESULT_OK, returnIntent);
                setResult(Activity.RESULT_OK);
                activity.finish();
            }
        });

        fmManager = getSupportFragmentManager();
        FragmentTransaction ft = fmManager.beginTransaction();
        Fragment currentGlobalTop10Fragment = fmManager.findFragmentByTag(GlobalTop10Fragment.GlobalTop10FragmentTag);
        if (currentGlobalTop10Fragment == null) {
            ft.add(globalTop10LayoutId, globalTop10Fragment, GlobalTop10Fragment.GlobalTop10FragmentTag);
        } else {
            ft.replace(globalTop10LayoutId, globalTop10Fragment, GlobalTop10Fragment.GlobalTop10FragmentTag);
        }
        ft.commit();
        System.out.println("GlobalTop10Activity.onCreate() -----> globalTop10Fragment is created.");

        System.out.println("GlobalTop10Activity.onCreate().");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("GlobalTop10Activity.onResume().");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
