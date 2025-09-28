package com.smile.colorballs.ballsremover.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import com.smile.colorballs.ballsremover.BallsRemoverComposables
import com.smile.colorballs.R
import com.smile.colorballs.ballsremover.constants.BallsRemoverConstants
import com.smile.colorballs.ballsremover.models.Settings
import com.smile.colorballs.views.ui.theme.ColorBallsTheme
import com.smile.colorballs.ballsremover.viewmodels.BallsRemoverSetViewModel
import com.smile.colorballs.views.CbComposable

class BallsRemoverSetActivity : ComponentActivity() {

    private val ballsRemoverSetViewModel : BallsRemoverSetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        if (savedInstanceState == null) {
            // new creation of this activity
            Log.d(TAG, "onCreate.savedInstanceState is null")
            val setting = Settings()
            intent.extras?.let {
                setting.hasSound = it.getBoolean(BallsRemoverConstants.HAS_SOUND, true)
                setting.gameLevel = it.getInt(BallsRemoverConstants.GAME_LEVEL, BallsRemoverConstants.EASY_LEVEL)
                setting.fillColumn = it.getBoolean(BallsRemoverConstants.FILL_COLUMN, true)
            }
            ballsRemoverSetViewModel.setSettings(setting)
        } else {
            // re-creation of this activity
            Log.d(TAG, "onCreate.savedInstanceState not null")
            if (ballsRemoverSetViewModel.settings.value == null) {
                ballsRemoverSetViewModel.setSettings(Settings())
            }
        }

        val textClick = object : CbComposable.BRemSettingClickListener {
            override fun hasSoundClick(hasSound: Boolean) {
                Log.d(TAG, "textClick.hasSoundClick.hasSound = $hasSound")
                ballsRemoverSetViewModel.setHasSound(hasSound)
            }
            override fun gameLevelClick(gameLevel: Int) {
                Log.d(TAG, "textClick.gameLevelClick.gameLevel = $gameLevel")
                ballsRemoverSetViewModel.setGameLevel(gameLevel)
            }
            override fun isFillColumnClick(fillColumn: Boolean) {
                Log.d(TAG, "textClick.isFillColumnClick.hasNext = $fillColumn")
                ballsRemoverSetViewModel.setFillColumn(fillColumn)
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
                    BallsRemoverComposables.SettingCompose(
                        this@BallsRemoverSetActivity,
                        buttonClick, textClick,
                        getString(R.string.settingStr),
                        backgroundColor = Color(0xbb0000ff), it
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
                    bundle.putBoolean(BallsRemoverConstants.HAS_SOUND, it.hasSound)
                    bundle.putInt(BallsRemoverConstants.GAME_LEVEL, it.gameLevel)
                    bundle.putBoolean(BallsRemoverConstants.FILL_COLUMN, it.fillColumn)
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