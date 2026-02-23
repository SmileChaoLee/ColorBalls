package com.smile.fivecolorballs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smile.colorballs_main.R;
import com.smile.colorballs_main.tools.LogUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";
    private boolean hasSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        float textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        hasSound = true;
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                hasSound = extras.getBoolean("HasSound");
            }
        }

        setContentView(R.layout.activity_setting);

        TextView settingTitle = findViewById(R.id.settingTitle);
        ScreenUtil.resizeTextSize(settingTitle, textFontSize);
        TextView soundSettingTitle = findViewById(R.id.soundSettingTitle);
        ScreenUtil.resizeTextSize(soundSettingTitle, textFontSize);
        ToggleButton soundSwitch = findViewById(R.id.soundSwitch);
        ScreenUtil.resizeTextSize(soundSwitch, textFontSize);
        soundSwitch.setChecked(hasSound);
        soundSwitch.setOnClickListener( (View view)-> hasSound = ((ToggleButton)view).isChecked());
        Button confirmButton = findViewById(R.id.confirmSettingButton);
        ScreenUtil.resizeTextSize(confirmButton, textFontSize);
        confirmButton.setOnClickListener( (View view)-> returnToPrevious(true) );
        Button cancelButton = findViewById(R.id.cancelSettingButton);
        ScreenUtil.resizeTextSize(cancelButton, textFontSize);
        cancelButton.setOnClickListener( (View view)-> returnToPrevious(false) );

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed");
                // Handle the fragment's back press (null check for playerFragment)
                returnToPrevious(false);
            }
        });
    }

    private void returnToPrevious(boolean confirmed) {

        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putBoolean("HasSound", hasSound);
        returnIntent.putExtras(extras);

        int resultYn = Activity.RESULT_OK;
        if (!confirmed) {
            // cancelled
            resultYn = Activity.RESULT_CANCELED;
        }

        setResult(resultYn, returnIntent);    // can bundle some data to previous activity
        finish();
    }
}
