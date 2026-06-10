package com.smile.reversi.viewmodels

import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import com.smile.reversi.models.ReversiGridData
import com.smile.reversi.presenters.ReversiPresenter

class ReversiViewModel(private val rPresenter: ReversiPresenter)
    : BaseViewModel(rPresenter) {

    companion object {
        private const val TAG = "ReversiViewModel"
    }

    private var rGameProp: GameProp = GameProp()
    private var rGridData: ReversiGridData = ReversiGridData()
    private var createNewGameStr = ""
    private val currentPlayer = mutableStateOf(Constants.COLOR_RED)

    private val createNewGameText = mutableStateOf("")
    fun getCreateNewGameText() = createNewGameText.value
    fun setCreateNewGameText(text: String) {
        createNewGameText.value = text
    }

    init {
        LogUtil.i(TAG, "ReversiViewModel.init")
        mGameProp = rGameProp
        mGridData = rGridData
        super.setProperties()
    }

    fun getCurrentPlayer() = currentPlayer.value

    override fun initGame(bundle: Bundle?) {
        LogUtil.i(TAG, "initGame")
        rGameProp.initializeKeepSetting(WhichGame.REMOVE_BALLS)
        rGridData.initialize()
        setCurrentScore(rGridData.countColor(Constants.COLOR_RED))
        displayGameGridView()
    }

    override fun cellClickListener(i: Int, j: Int) {
        LogUtil.i(TAG, "cellClickListener.($i,$j)")
        if (rGridData.getCellValue(i, j) != 0) return
        val color = currentPlayer.value
        val flips = rGridData.flipsForMove(i, j, color)
        if (flips.isEmpty()) return
        rGridData.backupCells()
        rGridData.placePiece(i, j, color)
        displayGameGridView()

        // switch player
        val nextColor = if (color == Constants.COLOR_RED) Constants.COLOR_BLUE else Constants.COLOR_RED
        currentPlayer.value = nextColor
        val opponentMoves = rGridData.getValidMoves(currentPlayer.value)
        if (opponentMoves.isEmpty()) {
            val myMoves = rGridData.getValidMoves(color)
            if (myMoves.isEmpty()) {
                // game over
                gameOver()
            } else {
                // skip opponent
                currentPlayer.value = color
            }
        }
        setCurrentScore(rGridData.countColor(Constants.COLOR_RED))
    }

    override fun startSavingGame(): Boolean { return false }
    override fun startLoadingGame(): Boolean { return false }

    override fun saveInstanceState(outState: Bundle) {
        outState.putParcelable(Constants.GAME_PROP_TAG, rGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, rGridData)
    }

    fun isCreatingNewGame() {
        LogUtil.i(TAG, "isCreatingNewGame")
        mGameAction = Constants.IS_CREATING_GAME
        setCreateNewGameText(createNewGameStr)
    }

    override fun newGame() {
        rGridData.initialize()
        displayGameGridView()
    }

    override fun undoTheLast() {
        rGridData.undoTheLast()
        displayGameGridView()
    }

    override fun dealWithIsNextBalls(isNextBalls: Boolean) { /* no-op */ }

    override fun release() {
        super.release()
    }
}