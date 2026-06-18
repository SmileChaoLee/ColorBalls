package com.smile.colorballs_main.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.smile.colorballs_main.R
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.models.Settings
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.ui.theme.ColorBallsTheme
import com.smile.colorballs_main.viewmodel.SettingViewModel
import com.smile.smilelibraries.utilities.ScreenUtil

class CbSettingActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CbSettingActivity"
        const val HAS_SOUND_TITLE = "soundTitle"
        const val GAME_LEVEL_TITLE = "gameLevelTitle"
        const val NEXT_BALL_TITLE = "hasNextTitle"
    }

    private val settingViewModel : SettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")

        val textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this@CbSettingActivity)
        val toastTextSize = textFontSize * 0.7f
        CbComposable.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp
        CbComposable.toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp

        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            // new creation of this activity
            val setting = Settings()
            LogUtil.d(TAG, "onCreate.savedInstanceState is null")
            intent.extras?.let {
                settingViewModel.gameId = it.getString(Constants.GAME_ID, "")
                setting.hasSound = it.getBoolean(Constants.HAS_SOUND, true)
                setting.gameLevel = it.getInt(Constants.GAME_LEVEL, 1)
                setting.hasNext = it.getBoolean(Constants.HAS_NEXT, true)
                settingViewModel.hasSoundTitle = it.getString(HAS_SOUND_TITLE, "")
                settingViewModel.gameLevelTitle = it.getString(GAME_LEVEL_TITLE, "")
                settingViewModel.hasNextTitle = it.getString(NEXT_BALL_TITLE, "")
            }
            settingViewModel.setSettings(setting)
        } else {
            // re-creation of this activity
            LogUtil.d(TAG, "onCreate.savedInstanceState not null")
            if (settingViewModel.settings.value == null) {
                settingViewModel.setSettings(Settings())
            }
        }
        val gameId = settingViewModel.gameId
        val hasSoundTitle = settingViewModel.hasSoundTitle
        val gameLevelTitle = settingViewModel.gameLevelTitle
        val hasNextTitle = settingViewModel.hasNextTitle

        val textClick = object : CbComposable.SettingClickListener {
            override fun hasSoundClick(hasSound: Boolean) {
                LogUtil.d(TAG, "textClick.hasSoundClick.hasSound = $hasSound")
                settingViewModel.setHasSound(hasSound)
            }
            override fun gameLevelClick(gameLevel: Int) {
                LogUtil.d(TAG, "textClick.easyLevelClick.easyLevel = $gameLevel")
                settingViewModel.setGameLevel(gameLevel)
            }
            override fun hasNextClick(hasNext: Boolean) {
                LogUtil.d(TAG, "textClick.hasNextClick.hasNext = $hasNext")
                settingViewModel.setHasNext(hasNext)
            }
        }

        val buttonClick = object : CbComposable.ButtonClickListener  {
            override fun buttonOkClick() {
                returnToPrevious(confirmed = true)
            }
            override fun buttonCancelClick() {
                returnToPrevious(confirmed = false)
            }
        }

        setContent {
            LogUtil.d(TAG, "onCreate.setContent.gameId = $gameId")
            val gameLevel = ArrayList<Int>()
            val gameLevelStr = ArrayList<String>()
            gameLevel.add(Constants.GAME_LEVEL_1)
            gameLevel.add(Constants.GAME_LEVEL_2)
            gameLevelStr.add(getString(R.string.level1Str))
            gameLevelStr.add(getString(R.string.level2Str))
            ColorBallsTheme {
                settingViewModel.settings.value?.let {
                    CbComposable.SettingCompose(buttonClick,
                        textClick,
                        backgroundColor = Color(0xbb0000ff), it,
                        getString(R.string.settingStr),
                        hasSoundTitle,
                        gameLevelTitle,
                        hasNextTitle,
                        getString(R.string.onStr),
                        getString(R.string.offStr),
                        getString(R.string.yesStr),
                        getString(R.string.noStr),
                        gameLevel,
                        gameLevelStr,
                        getString(R.string.okStr),
                        getString(R.string.cancelStr)
                    )
                }
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                returnToPrevious(confirmed = false)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        LogUtil.d(TAG, "onStart()")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume()")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtil.d(TAG, "onSaveInstanceState()")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        LogUtil.d(TAG, "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy()")
    }

    private fun returnToPrevious(confirmed: Boolean) {
        Intent().let { intent ->
            Bundle().let { bundle ->
                settingViewModel.settings.value?.also {
                    bundle.putBoolean(Constants.HAS_SOUND, it.hasSound)
                    bundle.putInt(Constants.GAME_LEVEL, it.gameLevel)
                    bundle.putBoolean(Constants.HAS_NEXT, it.hasNext)
                    intent.putExtras(bundle)
                }
            }
            setResult(if (confirmed) RESULT_OK else RESULT_CANCELED,
                intent) // can bundle some data to previous activity
        }
        finish()
    }
}