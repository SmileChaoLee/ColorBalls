package com.smile.colorballs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ToggleButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.databinding.ActivitySettingBinding
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.colorballs.models.Settings
import com.smile.colorballs.viewmodel.SettingViewModel

class SettingActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySettingBinding
    private val settingViewModel : SettingViewModel by viewModels()

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

        val settings = Settings()
        intent.extras?.let {
            settings.hasSound = it.getBoolean(Constants.HAS_SOUND, true)
            settings.easyLevel = it.getBoolean(Constants.IS_EASY_LEVEL, true)
            settings.hasNextBall = it.getBoolean(Constants.HAS_NEXT_BALL, true)
        }

        settingViewModel.settings = settings

        // setContentView(R.layout.activity_setting)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            lifecycleOwner = this@SettingActivity
            viewModel = settingViewModel
        }

        binding.settingTitle.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }
        binding.soundSettingTitle.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }
        binding.soundSwitch.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            it.apply {
                setOnClickListener { view: View ->
                    settingViewModel.setHasSound((view as ToggleButton).isChecked)
                }
            }
        }
        binding.soundSetting.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }

        binding.levelSettingTitle.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }
        binding.levelSwitch.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            it.apply {
                setOnClickListener { view: View ->
                    settingViewModel.setEasyLevel((view as ToggleButton).isChecked)
                }
            }
        }
        binding.levelSetting.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }

        binding.nextBallSettingTitle.let{
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }
        binding.nextBallSettingSwitch.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            it.apply {
                setOnClickListener { view: View ->
                    settingViewModel.setHasNextBall((view as ToggleButton).isChecked)
                }
            }
        }
        binding.nextBallSetting.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }

        binding.confirmSettingButton.let {
            ScreenUtil.resizeTextSize(it, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            it.setOnClickListener { returnToPrevious(true) }
        }
        binding.cancelSettingButton.let {
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
                putBoolean(Constants.HAS_SOUND, settingViewModel.settings.hasSound)
                putBoolean(Constants.IS_EASY_LEVEL, settingViewModel.settings.easyLevel)
                putBoolean(Constants.HAS_NEXT_BALL, settingViewModel.settings.hasNextBall)
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