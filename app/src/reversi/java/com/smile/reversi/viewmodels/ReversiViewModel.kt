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

    }

    override fun startSavingGame(): Boolean {
        LogUtil.i(TAG, "startSavingGame")
        rGameProp.isProcessingJob = true
        setScreenMessage(savingGameStr)
        var succeeded = true
        try {
            val foStream = rPresenter.fileOutputStream(Constants.SAVE_BALLS_REMOVER)
            // save settings
            if (hasSound()) foStream.write(1) else foStream.write(0)
            if (getGameLevel() == Constants.GAME_LEVEL_1) foStream.write(1) else foStream.write(0)
            if (hasNext()) foStream.write(1) else foStream.write(0)
            // save current player
            foStream.write(currentPlayer.value)
            // save values on game grid
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    foStream.write(rGridData.getCellValue(i, j))
                }
            }
            // save backupCells
            val backup = rGridData.getBackupCells()
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    foStream.write(backup[i][j])
                }
            }
            foStream.close()
            LogUtil.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: java.io.IOException) {
            succeeded = false
            LogUtil.e(TAG, "startSavingGame.Failed.", ex)
        }
        setScreenMessage("")
        rGameProp.isProcessingJob = false
        return succeeded
    }

    override fun startLoadingGame(): Boolean {
        LogUtil.i(TAG, "startLoadingGame")
        rGameProp.isProcessingJob = true
        setScreenMessage(loadingGameStr)
        var succeeded = true
        try {
            val fiStream = rPresenter.fileInputStream(Constants.SAVE_BALLS_REMOVER)
            // read game settings
            var bValue = fiStream.read()
            val hasSound = bValue == 1
            bValue = fiStream.read()
            val gameLevel = bValue
            bValue = fiStream.read()
            val hasNext = bValue == 1
            setHasSound(hasSound)
            setGameLevel(gameLevel)
            setHasNext(hasNext)
            // read current player
            val cp = fiStream.read()
            currentPlayer.value = cp
            // load values on game grid
            val gameCells = Array(rowCounts) { IntArray(colCounts) }
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    gameCells[i][j] = fiStream.read()
                }
            }
            // reading backupCells
            val backupCells = Array(rowCounts) { IntArray(colCounts) }
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    backupCells[i][j] = fiStream.read()
                }
            }
            fiStream.close()

            // refresh UI with loaded data
            rGridData.setCellValues(gameCells)
            rGridData.setBackupCells(backupCells)
                displayGameGridView()
        } catch (ex: java.io.IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        setScreenMessage("")
        rGameProp.isProcessingJob = false
        return succeeded
    }

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