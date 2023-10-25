package com.smile.colorballs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.smile.smilelibraries.utilities.ScreenUtil

class SettingActivity : AppCompatActivity() {

    private var hasSound = true
    private var isEasyLevel = true
    private var hasNextBall = true

    /**
     * if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
     * // Oreo on API 26. Under Full Screen and Translucent, the orientation cannot be changed
     * }
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textFontSize = ScreenUtil.suitableFontSize(
            this,
            ScreenUtil.getDefaultTextSizeFromTheme(this, ScreenUtil.FontSize_Pixel_Type, null),
            ScreenUtil.FontSize_Pixel_Type,
            0.0f
        )
        intent.extras?.let {
            hasSound = it.getBoolean(Constants.HasSoundKey, true)
            isEasyLevel = it.getBoolean(Constants.IsEasyLevelKey, true)
            hasNextBall = it.getBoolean(Constants.HasNextBallKey, true)
        }
        setContentView(R.layout.activity_setting)

        findViewById<TextView>(R.id.settingTitle)?.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }
        findViewById<TextView>(R.id.soundSettingTitle)?.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }
        findViewById<ToggleButton>(R.id.soundSwitch)?.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            it.apply {
                isChecked = hasSound
                setOnClickListener { view: View -> hasSound = (view as ToggleButton).isChecked }
            }
        }
        findViewById<TextView>(R.id.levelSettingTitle)?.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }
        findViewById<ToggleButton>(R.id.easyLevelSwitch)?.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            it.apply {
                isChecked = isEasyLevel
                setOnClickListener { view: View ->
                    isEasyLevel = (view as ToggleButton).isChecked
                }
            }
        }
        findViewById<TextView>(R.id.nextBallSettingTitle)?.let{
            ScreenUtil.resizeTextSize(
                it,
                textFontSize,
                ScreenUtil.FontSize_Pixel_Type
            )
        }
        findViewById<ToggleButton>(R.id.nextBallSettingSwitch)?.let {
            ScreenUtil.resizeTextSize(
                it,
                textFontSize,
                ScreenUtil.FontSize_Pixel_Type
            )
            it.apply {
                isChecked = hasNextBall
                setOnClickListener { view: View ->
                    hasNextBall = (view as ToggleButton).isChecked
                }
            }
        }
        findViewById<Button>(R.id.confirmSettingButton)?.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            it.setOnClickListener { returnToPrevious(true) }
        }
        findViewById<Button>(R.id.cancelSettingButton)?.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            it.setOnClickListener { returnToPrevious(false) }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                returnToPrevious(false)
            }
        })
    }

    private fun returnToPrevious(confirmed: Boolean) {
        Intent().let {
            Bundle().apply {
                putBoolean(Constants.HasSoundKey, hasSound)
                putBoolean(Constants.IsEasyLevelKey, isEasyLevel)
                putBoolean(Constants.HasNextBallKey, hasNextBall)
                it.putExtras(this)
            }
            setResult(if (confirmed) RESULT_OK else RESULT_CANCELED,
                it) // can bundle some data to previous activity
        }
        finish()
    }

    companion object {
        private const val TAG = "SettingActivity"
    }
}