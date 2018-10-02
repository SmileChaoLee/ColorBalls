package com.smile.colorballs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class SettingActivity extends AppCompatActivity {

    private float fontSizeForText;
    private ToggleButton soundSwitch;
    private boolean hasSound;
    private ToggleButton easyLevelSwitch;
    private boolean isEasyLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        fontSizeForText = 30;
        hasSound = true;
        isEasyLevel = true;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fontSizeForText = extras.getFloat("FontSizeForText");
            hasSound = extras.getBoolean("HasSound");
            isEasyLevel = extras.getBoolean("IsEasyLevel");
        }

        if (fontSizeForText == 50) {
            // not a cell phone, it is a tablet
            setTheme(R.style.ThemeTextSize50Transparent);
        } else {
            setTheme(R.style.ThemeTextSize30Transparent);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        soundSwitch = findViewById(R.id.soundSwitch);
        soundSwitch.setTextSize(fontSizeForText);
        soundSwitch.setChecked(hasSound);
        soundSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hasSound = ((ToggleButton)view).isChecked();
            }
        });

        easyLevelSwitch = findViewById(R.id.easyLevelSwitch);
        easyLevelSwitch.setTextSize(fontSizeForText);
        easyLevelSwitch.setChecked(isEasyLevel);
        easyLevelSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEasyLevel = ((ToggleButton)view).isChecked();
            }
        });

        Button confirmButton = findViewById(R.id.confirmSettingButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(true);
            }
        });

        Button cancelButton = findViewById(R.id.cancelSettingButton);
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
