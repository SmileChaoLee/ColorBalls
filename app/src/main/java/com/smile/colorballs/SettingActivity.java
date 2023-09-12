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

    public static final String HasSoundKey = "HasSound";
    public static final String IsEasyLevelKey = "IsEasyLevel";
    public static final String HasNextBallKey = "HasNextBall";

    private boolean hasSound;
    private boolean isEasyLevel;
    private boolean hasNextBall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // Oreo on API 26. Under Full Screen and Translucent, the orientation cannot be changed
        }
        */
        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, ColorBallsApp.FontSize_Scale_Type, null);
        float textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, ColorBallsApp.FontSize_Scale_Type, 0.0f);

        hasSound = true;
        isEasyLevel = true;
        hasNextBall = true;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hasSound = extras.getBoolean(HasSoundKey);
            isEasyLevel = extras.getBoolean(IsEasyLevelKey);
            hasNextBall = extras.getBoolean(HasNextBallKey);
        }

        setContentView(R.layout.activity_setting);

        TextView settingTitle = findViewById(R.id.settingTitle);
        ScreenUtil.resizeTextSize(settingTitle, textFontSize, ColorBallsApp.FontSize_Scale_Type);

        TextView soundSettingTitle = findViewById(R.id.soundSettingTitle);
        ScreenUtil.resizeTextSize(soundSettingTitle, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        ToggleButton soundSwitch = findViewById(R.id.soundSwitch);
        ScreenUtil.resizeTextSize(soundSwitch, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        soundSwitch.setChecked(hasSound);
        soundSwitch.setOnClickListener( (View view)-> hasSound = ((ToggleButton)view).isChecked());

        TextView levelSettingTitle = findViewById(R.id.levelSettingTitle);
        ScreenUtil.resizeTextSize(levelSettingTitle, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        ToggleButton easyLevelSwitch = findViewById(R.id.easyLevelSwitch);
        ScreenUtil.resizeTextSize(easyLevelSwitch, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        easyLevelSwitch.setChecked(isEasyLevel);
        easyLevelSwitch.setOnClickListener( (View view)-> isEasyLevel = ((ToggleButton)view).isChecked());

        TextView nextBallSettingTitle = findViewById(R.id.nextBallSettingTitle);
        ScreenUtil.resizeTextSize(nextBallSettingTitle, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        ToggleButton nextBallSettingSwitch = findViewById(R.id.nextBallSettingSwitch);
        ScreenUtil.resizeTextSize(nextBallSettingSwitch, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        nextBallSettingSwitch.setChecked(hasNextBall);
        nextBallSettingSwitch.setOnClickListener( (View view)-> hasNextBall = ((ToggleButton)view).isChecked());

        Button confirmButton = findViewById(R.id.confirmSettingButton);
        ScreenUtil.resizeTextSize(confirmButton, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        confirmButton.setOnClickListener( (View view)-> returnToPrevious(true) );

        Button cancelButton = findViewById(R.id.cancelSettingButton);
        ScreenUtil.resizeTextSize(cancelButton, textFontSize, ColorBallsApp.FontSize_Scale_Type);
        cancelButton.setOnClickListener( (View view)-> returnToPrevious(false) );
    }

    @Override
    public void onBackPressed() {
        returnToPrevious(false);
    }

    private void returnToPrevious(boolean confirmed) {

        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putBoolean(HasSoundKey, hasSound);
        extras.putBoolean(IsEasyLevelKey, isEasyLevel);
        extras.putBoolean(HasNextBallKey, hasNextBall);
        returnIntent.putExtras(extras);

        int resultYn = confirmed? Activity.RESULT_OK : Activity.RESULT_CANCELED;

        setResult(resultYn, returnIntent);    // can bundle some data to previous activity
        finish();
    }
}
