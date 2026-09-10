package com.smile.reversi.views

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import com.smile.colorballs_main.R
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.CbRmBaseView
import com.smile.colorballs_main.views.CbSettingActivity
import com.smile.reversi.constants.ReversiConstants
import com.smile.reversi.interfaces.ReversiPresentView
import com.smile.reversi.presenters.ReversiPresenter
import com.smile.reversi.viewmodels.ReversiViewModel
import kotlin.math.abs

class ReversiActivity: CbRmBaseView(), ReversiPresentView {

    companion object {
        private const val TAG = "ReversiActivity"
    }

    private var playMode: Int = ReversiConstants.PLAY_WIth_AI
    private lateinit var viewModel: ReversiViewModel
    private lateinit var mPresenter: ReversiPresenter

    override fun hasTop10Menu() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "$TAG.onCreate.savedInstanceState = $savedInstanceState")
        if (savedInstanceState == null) {
            intent?.let {
                val extras = it.extras
                extras?.let { bundle ->
                    playMode = bundle.getInt(ReversiConstants.PLAY_MODE,
                        ReversiConstants.PLAY_WIth_AI)
                }
            }
        } else {
            playMode = savedInstanceState.getInt(ReversiConstants.PLAY_MODE,
                ReversiConstants.PLAY_WIth_AI)
        }
        LogUtil.d(TAG, "onCreate.playMode = $playMode")
        mPresenter = ReversiPresenter(this)
        viewModel = ReversiViewModel(mPresenter, playMode)
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ReversiConstants.PLAY_MODE, playMode)
        super.onSaveInstanceState(outState)
    }

    // implement ReversiPresentView
    override fun getBluePassStr() = getString(R.string.bluePass)
    override fun getRedPassStr() = getString(R.string.redPass)
    override fun getImageSizeDp(): Float {
        return mImageSizeDp
    }
    override fun getWhoWinsMessage(
        redCount: Int,
        blueCount: Int
    ): String {
        val diff = abs(redCount - blueCount)
        val cellStr = getString(R.string.cells)
        val red = getString(R.string.red)
        val blue = getString(R.string.blue)
        val message = when {
            redCount > blueCount -> {
                val winStr = getString(R.string.redWinBy)
                "$winStr $diff $cellStr ($red: $redCount, $blue: $blueCount)"
            }
            blueCount > redCount -> {
                val winStr = getString(R.string.blueWinBy)
                "$winStr $diff $cellStr ($red: $redCount, $blue: $blueCount)"
            }
            else -> {
                val tie = getString(R.string.tie)
                "$tie: $red: $redCount, $blue: $blueCount"
            }
        }
        return message
    }
    //

    // implement GameOptions
    override fun setWhichGame() {
        LogUtil.d(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.REVERSI)
    }

    // implement parent class, BaseView.kt
    @Composable
    override fun CreateNewGameDialog() {
        LogUtil.d(TAG, "CreateNewGameDialog")
    }

    override fun getFieldStrings(): Array<String> {
        return arrayOf(
            "",
            getString(R.string.okStr),      // okStr
            ""
        )
    }

    override fun getCurrentPresenter(): ReversiPresenter {
        return mPresenter
    }

    override fun getCurrentViewModel(): ReversiViewModel {
        return viewModel
    }

    override fun ifInterstitialWhenSaveScore() {
        // otherwise do nothing
    }

    override fun ifInterstitialWhenNewGame() {
        LogUtil.d(TAG, "ifInterstitialWhenNewGame")
        viewModel.initGame(bundle = null)
    }

    override fun ifCreatingNewGame(newGameLevel: Int, originalLevel: Int) {
        // not supported
    }

    override fun setHasNextForView(hasNext: Boolean) {
        viewModel.setHasNext(hasNext, true)
    }

    @Composable
    override fun ToolBarMenu(modifier: Modifier) {
        // Match BallsRemover toolbar layout: left area (2f), center area (2f), then three buttons (1f each)
        Row(modifier = modifier.background(colorPrimary)) {
            // Left placeholder
            Box(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)) {
                // intentionally empty to match layout
            }
            // current player indicator
            Box(modifier = Modifier
                .weight(2f)
                .padding(start = 10.dp)
                .align(Alignment.CenterVertically)) {
                val bmp = colorBallMap[viewModel.getCurrentPlayer()]
                bmp?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "",
                        modifier = Modifier.size((mImageSizeDp * 0.7f).dp).align(Alignment.CenterStart))
                }
            }
            // Right: Setting, Menu with equal weights
            SettingButton(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
            ShowMenu(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
        }
    }

    override fun putOtherToBundle(extras: Bundle) {
        extras.putString(CbSettingActivity.HAS_SOUND_TITLE,
            getString(R.string.soundTitle))
        extras.putString(CbSettingActivity.GAME_LEVEL_TITLE,
            getString(R.string.gameLevelTitle))
        extras.putString(CbSettingActivity.NEXT_BALL_TITLE,
            getString(R.string.nextMoveTitle))
    }
    // end of implementing parent class, BaseView.kt
}
