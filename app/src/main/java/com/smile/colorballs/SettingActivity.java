package com.smile.colorballs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smile.smilelibraries.utilities.ScreenUtil;

public class SettingActivity extends AppCompatActivity {

    private boolean hasSound;
    private boolean isEasyLevel;
    private boolean hasNextBall;

    /**
     if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
     // Oreo on API 26. Under Full Screen and Translucent, the orientation cannot be changed
     }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, ScreenUtil.FontSize_Pixel_Type, null);
        float textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, ScreenUtil.FontSize_Pixel_Type, 0.0f);

        hasSound = true;
        isEasyLevel = true;
        hasNextBall = true;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hasSound = extras.getBoolean(Constants.HasSoundKey);
            isEasyLevel = extras.getBoolean(Constants.IsEasyLevelKey);
            hasNextBall = extras.getBoolean(Constants.HasNextBallKey);
        }

        setContentView(R.layout.activity_setting);

        TextView settingTitle = findViewById(R.id.settingTitle);
        ScreenUtil.resizeTextSize(settingTitle, textFontSize, ScreenUtil.FontSize_Pixel_Type);

        TextView soundSettingTitle = findViewById(R.id.soundSettingTitle);
        ScreenUtil.resizeTextSize(soundSettingTitle, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        ToggleButton soundSwitch = findViewById(R.id.soundSwitch);
        ScreenUtil.resizeTextSize(soundSwitch, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        soundSwitch.setChecked(hasSound);
        soundSwitch.setOnClickListener( (View view)-> hasSound = ((ToggleButton)view).isChecked());

        TextView levelSettingTitle = findViewById(R.id.levelSettingTitle);
        ScreenUtil.resizeTextSize(levelSettingTitle, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        ToggleButton easyLevelSwitch = findViewById(R.id.easyLevelSwitch);
        ScreenUtil.resizeTextSize(easyLevelSwitch, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        easyLevelSwitch.setChecked(isEasyLevel);
        easyLevelSwitch.setOnClickListener( (View view)-> isEasyLevel = ((ToggleButton)view).isChecked());

        TextView nextBallSettingTitle = findViewById(R.id.nextBallSettingTitle);
        ScreenUtil.resizeTextSize(nextBallSettingTitle, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        ToggleButton nextBallSettingSwitch = findViewById(R.id.nextBallSettingSwitch);
        ScreenUtil.resizeTextSize(nextBallSettingSwitch, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        nextBallSettingSwitch.setChecked(hasNextBall);
        nextBallSettingSwitch.setOnClickListener( (View view)-> hasNextBall = ((ToggleButton)view).isChecked());

        Button confirmButton = findViewById(R.id.confirmSettingButton);
        ScreenUtil.resizeTextSize(confirmButton, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        confirmButton.setOnClickListener( (View view)-> returnToPrevious(true) );

        Button cancelButton = findViewById(R.id.cancelSettingButton);
        ScreenUtil.resizeTextSize(cancelButton, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        cancelButton.setOnClickListener( (View view)-> returnToPrevious(false) );
    }

    @Override
    public void onBackPressed() {
        returnToPrevious(false);
    }

    private void returnToPrevious(boolean confirmed) {

        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putBoolean(Constants.HasSoundKey, hasSound);
        extras.putBoolean(Constants.IsEasyLevelKey, isEasyLevel);
        extras.putBoolean(Constants.HasNextBallKey, hasNextBall);
        returnIntent.putExtras(extras);

        int resultYn = confirmed? Activity.RESULT_OK : Activity.RESULT_CANCELED;

        setResult(resultYn, returnIntent);    // can bundle some data to previous activity
        finish();
    }
}
