package com.smile.reversi.views

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import com.smile.colorballs_main.R
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.BaseView
import com.smile.colorballs_main.views.CbComposable
import com.smile.colorballs_main.views.CbRmBaseView
import com.smile.reversi.interfaces.ReversiPresentView
import com.smile.reversi.presenters.ReversiPresenter
import com.smile.reversi.viewmodels.ReversiViewModel
import com.smile.smilelibraries.models.ExitAppTimer

class ReversiActivity: CbRmBaseView(), ReversiPresentView {

    companion object {
        private const val TAG = "ReversiActivity"
    }

    private lateinit var viewModel: ReversiViewModel
    private lateinit var mPresenter: ReversiPresenter

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
        viewModel.setWhichGame(WhichGame.REMOVE_BALLS)
    }

    @Composable
    override fun CreateNewGameDialog() {
        val dialogText = viewModel.getCreateNewGameText()
        if (dialogText.isNotEmpty()) {
            viewModel.setShowingCreateGameDialog(true)
            val buttonListener = object: CbComposable.ButtonClickListener {
                override fun buttonOkClick() {
                    viewModel.setCreateNewGameText("")
                    viewModel.setShowingCreateGameDialog(false)
                    quitOrNewGame()
                }
                override fun buttonCancelClick() {
                    viewModel.setCreateNewGameText("")
                    viewModel.setShowingCreateGameDialog(false)
                }
            }
            CbComposable.DialogWithText(
                this@ReversiActivity,
                buttonListener, "", dialogText
            )
        }
    }

    override fun getCurrentPresenter(): ReversiPresenter {
        return mPresenter
    }

    override fun getCurrentViewModel(): ReversiViewModel {
        return viewModel
    }

    @Composable
    override fun SaveScoreDialog() {
        val dialogTitle = baseViewModel.getSaveScoreTitle()
        LogUtil.d(TAG, "SaveScoreDialog.dialogTitle = $dialogTitle")
        // if (viewModel.mGameAction == Constants.IS_APP_JUST_STARTED) return
        if (dialogTitle.isEmpty()) return
        quitOrNewGame()
    }

    override fun ifInterstitialWhenSaveScore() {
        // otherwise do nothing
    }

    override fun ifInterstitialWhenNewGame() {
        viewModel.initGame(bundle = null)
    }

    override fun ifCreatingNewGame(newGameLevel: Int, originalLevel: Int) {
        if (newGameLevel != originalLevel) {
            viewModel.isCreatingNewGame()
        }
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

            // Right: Undo, Setting, Menu with equal weights
            UndoButton(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
            SettingButton(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
            ShowMenu(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
        }
    }
}
