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
import com.smile.reversi.interfaces.ReversiPresentView
import com.smile.reversi.presenters.ReversiPresenter
import com.smile.reversi.viewmodels.ReversiViewModel

class ReversiActivity: CbRmBaseView(), ReversiPresentView {

    companion object {
        private const val TAG = "ReversiActivity"
    }

    private lateinit var viewModel: ReversiViewModel
    private lateinit var mPresenter: ReversiPresenter

    override fun hasTop10Menu() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "$TAG.onCreate.savedInstanceState = $savedInstanceState")
        mPresenter = ReversiPresenter(this)
        viewModel = ReversiViewModel(mPresenter)
        super.onCreate(savedInstanceState)
    }

    // implement ReversiPresentView
    override fun getCreateNewGameStr() = getString(R.string.createNewGameStr)

    // implement GameOptions
    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
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
        LogUtil.i(TAG, "ifInterstitialWhenNewGame")
        viewModel.initGame(bundle = null)
    }

    override fun ifCreatingNewGame(newGameLevel: Int, originalLevel: Int) {
        // not supported
    }

    override fun setHasNextForView(hasNext: Boolean) {
        viewModel.setHasNext(hasNext)
    }

    @Composable
    override fun ToolBarMenu(modifier: Modifier) {
        // Match BallsRemover toolbar layout: left area (2f), center area (2f), then three buttons (1f each)
        Row(modifier = modifier.background(colorPrimary)) {
            // Left: current player indicator
            Box(modifier = Modifier
                .weight(2f)
                .padding(start = 10.dp)
                .align(Alignment.CenterVertically)) {
                val bmp = colorBallMap[viewModel.getCurrentPlayer()]
                bmp?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "",
                        modifier = Modifier.size(36.dp).align(Alignment.CenterStart))
                }
            }

            // Middle placeholder (keeps spacing consistent with BallsRemover)
            Box(modifier = Modifier
                .weight(2f)
                .align(Alignment.CenterVertically)) {
                // intentionally empty to match layout
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
    // end of implementing parent class, BaseView.kt
}
