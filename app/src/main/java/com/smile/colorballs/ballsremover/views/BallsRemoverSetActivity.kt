package com.smile.colorballs.ballsremover.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.models.Settings
import com.smile.colorballs.views.ui.theme.ColorBallsTheme
import com.smile.colorballs.viewmodel.SettingViewModel
import com.smile.colorballs.views.CbComposable

class BallsRemoverSetActivity : ComponentActivity() {

    private val ballsRemoverSetViewModel : SettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        if (savedInstanceState == null) {
            // new creation of this activity
            Log.d(TAG, "onCreate.savedInstanceState is null")
            val setting = Settings()
            intent.extras?.let {
                setting.hasSound = it.getBoolean(Constants.HAS_SOUND, true)
                setting.easyLevel = it.getBoolean(Constants.EASY_LEVEL, true)
                setting.hasNext = it.getBoolean(Constants.HAS_NEXT, true)
            }
            ballsRemoverSetViewModel.setSettings(setting)
        } else {
            // re-creation of this activity
            Log.d(TAG, "onCreate.savedInstanceState not null")
            if (ballsRemoverSetViewModel.settings.value == null) {
                ballsRemoverSetViewModel.setSettings(Settings())
            }
        }

        val textClick = object : CbComposable.SettingClickListener {
            override fun hasSoundClick(hasSound: Boolean) {
                Log.d(TAG, "textClick.hasSoundClick.hasSound = $hasSound")
                ballsRemoverSetViewModel.setHasSound(hasSound)
            }
            override fun easyLevelClick(easyLevel: Boolean) {
                Log.d(TAG, "textClick.easyLevelClick.easyLevel = $easyLevel")
                ballsRemoverSetViewModel.setEasyLevel(easyLevel)
            }
            override fun hasNextClick(hasNext: Boolean) {
                Log.d(TAG, "textClick.hasNextClick.hasNext = $hasNext")
                ballsRemoverSetViewModel.setHasNext(hasNext)
            }
        }

        val buttonClick = object : CbComposable.ButtonClickListener {
            override fun buttonOkClick() {
                returnToPrevious(confirmed = true)
            }
            override fun buttonCancelClick() {
                returnToPrevious(confirmed = false)
            }
        }

        setContent {
            Log.d(TAG, "onCreate.setContent")
            ColorBallsTheme {
                ballsRemoverSetViewModel.settings.value?.let {
                    CbComposable.SettingCompose(
                        buttonClick, textClick,
                        backgroundColor = Color(0xbb0000ff), it,
                        getString(R.string.settingStr),
                        getString(R.string.soundStr),
                        getString(R.string.playerLevelStr),
                        getString(R.string.fillColumnStr),
                        getString(R.string.onStr),
                        getString(R.string.offStr),
                        getString(R.string.yesStr),
                        getString(R.string.noStr),
                        getString(R.string.no1),
                        getString(R.string.no2),
                        getString(R.string.easyStr),
                        getString(R.string.difficultStr),
                        getString(R.string.okStr),
                        getString(R.string.cancelStr)
                    )
                }
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                returnToPrevious(confirmed = false)
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

    private fun returnToPrevious(confirmed: Boolean) {
        Intent().let { intent ->
            Bundle().let { bundle ->
                ballsRemoverSetViewModel.settings.value?.also {
                    bundle.putBoolean(Constants.HAS_SOUND, it.hasSound)
                    bundle.putBoolean(Constants.EASY_LEVEL, it.easyLevel)
                    bundle.putBoolean(Constants.HAS_NEXT, it.hasNext)
                    intent.putExtras(bundle)
                }
            }
            setResult(if (confirmed) RESULT_OK else RESULT_CANCELED,
                intent) // can bundle some data to previous activity
        }
        finish()
    }

    companion object {
        private const val TAG = "BallsRemSetActivity"
    }
}