package com.smile.colorballs.views.compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import com.smile.colorballs.R
import com.smile.colorballs.shared_composables.Composables
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.models.EnvSetting
import com.smile.colorballs.shared_composables.ui.theme.ColorBallsTheme
import com.smile.colorballs.viewmodel.EnvSettingViewModel

class SettingActivityCompose : ComponentActivity() {

    private lateinit var mSettings: EnvSetting
    private val settingViewModel : EnvSettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        if (savedInstanceState == null) {
            // new creation of this activity
            Log.d(TAG, "onCreate.savedInstanceState is null")
            mSettings = EnvSetting()
            intent.extras?.let {
                mSettings.hasSound = it.getBoolean(Constants.HAS_SOUND, true)
                mSettings.easyLevel = it.getBoolean(Constants.IS_EASY_LEVEL, true)
                mSettings.hasNextBall = it.getBoolean(Constants.HAS_NEXT_BALL, true)
            }
            settingViewModel.setSettings(mSettings)
        } else {
            // re-creation of this activity
            settingViewModel.settings.value?.let {
                Log.d(TAG, "onCreate.settingViewModel.settings has value")
                mSettings = it
            } ?: run {
                Log.d(TAG, "onCreate.settingViewModel.settings has no value")
                mSettings = EnvSetting()
                settingViewModel.setSettings(mSettings)
            }
        }

        setContent {
            Log.d(TAG, "onCreate.setContent")
            ColorBallsTheme {
                settingViewModel.settings.value?.let {
                    Composables.SettingCompose(this@SettingActivityCompose,
                        "${getString(R.string.settingStr)} - Activity",
                        it.hasSound, it.easyLevel, it.hasNextBall,
                        backgroundColor = Color(0xbb0000ff)
                    )
                }
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                setResult(RESULT_OK)
                finish()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState()")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }

    companion object {
        private const val TAG = "SettingActivityCompose"
    }
}