package com.smile.colorballs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class SettingActivity extends AppCompatActivity {

    private float textFontSize;
    private ToggleButton soundSwitch;
    private boolean hasSound;
    private ToggleButton easyLevelSwitch;
    private boolean isEasyLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, 0.0f);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        hasSound = true;
        isEasyLevel = true;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hasSound = extras.getBoolean("HasSound");
            isEasyLevel = extras.getBoolean("IsEasyLevel");
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        TextView settingTitle = findViewById(R.id.settingTitle);
        settingTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);

        TextView soundSettingTitle = findViewById(R.id.soundSettingTitle);
        soundSettingTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);

        soundSwitch = findViewById(R.id.soundSwitch);
        soundSwitch.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        soundSwitch.setChecked(hasSound);
        soundSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hasSound = ((ToggleButton)view).isChecked();
            }
        });

        TextView levelSettingTitle = findViewById(R.id.levelSettingTitle);
        levelSettingTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);

        easyLevelSwitch = findViewById(R.id.easyLevelSwitch);
        easyLevelSwitch.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        easyLevelSwitch.setChecked(isEasyLevel);
        easyLevelSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEasyLevel = ((ToggleButton)view).isChecked();
            }
        });

        Button confirmButton = findViewById(R.id.confirmSettingButton);
        confirmButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(true);
            }
        });

        Button cancelButton = findViewById(R.id.cancelSettingButton);
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(false);
            }
        });

    }

    @Override
    public void onBackPressed() {
        returnToPrevious(false);
    }

    private void returnToPrevious(boolean confirmed) {

        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putBoolean("HasSound", hasSound);
        extras.putBoolean("IsEasyLevel", isEasyLevel);
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
