package fivecolorballs.viewmodels

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smile.colorballs_main.models.GridData
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import fivecolorballs.presenters.FiveBallsPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.ByteBuffer

class FiveBallsViewModel(private val fivePresenter: FiveBallsPresenter)
    : BaseViewModel(fivePresenter) {

    companion object {
        private const val TAG = "FiveBallsViewModel"
    }

    private var brGameProp: GameProp
    private var brGridData: GridData
    private val showingScoreHandler = Handler(Looper.getMainLooper())
    private var createNewGameStr = ""
    var timesPlayed = 0

    private val createNewGameText = mutableStateOf("")
    fun getCreateNewGameText() = createNewGameText.value
    fun setCreateNewGameText(text: String) {
        createNewGameText.value = text
    }

    init {
        LogUtil.i(TAG, "FiveBallsViewModel.init")
        brGameProp = GameProp()
        brGridData = GridData(fivePresenter.rowCounts,
            fivePresenter.colCounts)
        mGameProp = brGameProp
        mGridData = brGridData
        super.setProperties()
        createNewGameStr = fivePresenter.createNewGameStr
    }

    override fun cellClickListener(i: Int, j: Int) {
        LogUtil.i(TAG, "cellClickListener.($i, $j)")
        if (brGridData.getCellValue(i, j) == 0) return  // no ball
        if (brGameProp.isProcessingJob) return
        if (brGridData.checkMoreThanTwo(i, j)) {
            brGridData.backupCells()
            brGameProp.undoScore = brGameProp.currentScore
            brGameProp.undoEnable = true
            brGameProp.isProcessingJob = true
            val tempLine = HashSet(brGridData.getLightLine())
            LogUtil.d(TAG, "cellClickListener.tempLine.size = ${tempLine.size}")
            brGameProp.lastGotScore = calculateScore(tempLine)
            brGameProp.currentScore += brGameProp.lastGotScore
            setCurrentScore(brGameProp.currentScore)
            val showScore = ShowScore(
                brGridData,
                brGridData.getLightLine(),
                brGameProp.lastGotScore,
                false /* no used*/,
                object : ShowScoreCallback {
                    override fun sCallback() {
                        LogUtil.d(TAG, "cellClickListener.sCallback")
                        viewModelScope.launch(Dispatchers.Default) {
                            // Refresh the game view
                            brGridData.refreshColorBalls(hasNext())
                            delay(200)
                            displayGameGridView()
                            if (brGridData.isGameOver()) {
                                LogUtil.d(TAG, "cellClickListener.sCallback.gameOver()")
                                gameOver()
                            }
                            brGameProp.isProcessingJob = false
                        }
                    }
                })
            showingScoreHandler.post(showScore)
        }
    }

    private fun setData(prop: GameProp, gData: GridData) {
        LogUtil.i(TAG, "setData")
        brGameProp = prop
        brGridData = gData
        // update mGameProp and mGridData in BaseViewModel
        mGameProp = prop
        mGridData = gData
    }

    private fun initData() {
        LogUtil.i(TAG, "initData")
        brGameProp.initializeKeepSetting(getWhichGame())
        brGridData.initialize()
    }

    override fun initGame(bundle: Bundle?) {
        LogUtil.i(TAG, "initGame = $bundle")
        brGameProp.isProcessingJob = true
        val isNewGame = restoreState(bundle)
        setCurrentScore(brGameProp.currentScore)
        if (isNewGame) {
            // generate
            LogUtil.i(TAG, "initGame.isNewGame")
            brGridData.generateColorBalls()
        }
        displayGameGridView()
        getAndSetHighestScore() // a coroutine operation
        if (!isNewGame) {
            if (isShowingCreateGameDialog()) {
                isCreatingNewGame()
            } else {
                lastPartOfInitialGame()
            }
        }
        brGameProp.isProcessingJob = false
    }

    private fun restoreState(state: Bundle?): Boolean {
        LogUtil.i(TAG,"restoreState.state")
        var isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: GridData? = null
        state?.let {
            LogUtil.d(TAG,"restoreState.state not null then restore the state")
            gameProp =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.getParcelable(Constants.GAME_PROP_TAG,
                        GameProp::class.java)
                else it.getParcelable(Constants.GAME_PROP_TAG)
            gridData =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.getParcelable(Constants.GRID_DATA_TAG,
                        GridData::class.java)
                else it.getParcelable(Constants.GRID_DATA_TAG)
        }
        isNewGame = true
        if (gameProp != null && gridData != null) {
            LogUtil.d(TAG, "restoreState.gridData = $gridData")
            gridData.apply {
                for (i in 0 until rowCounts) {
                    for (j in 0 until colCounts) {
                        if (getCellValue(i, j) != 0) {
                            // has value, so not a new game
                            isNewGame = false
                            break
                        }
                    }
                }
                if (isNewGame) {
                    LogUtil.i(TAG, "restoreState.CellValues are all 0")
                }
            }
        }
        LogUtil.i(TAG, "restoreState.isNewGame = $isNewGame")
        if (isNewGame) {
            initData()
        } else {
            setData(gameProp!!, gridData!!)
        }

        return isNewGame
    }

    override fun saveInstanceState(outState: Bundle) {
        outState.putParcelable(Constants.GAME_PROP_TAG, brGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, brGridData)
    }

    override fun undoTheLast() {
        LogUtil.i(TAG, "undoTheLast.undoEnable = ${brGameProp.undoEnable}")
        if (!brGameProp.undoEnable) {
            return
        }
        brGridData.undoTheLast()
        // restore the screen
        displayGameGridView()
        brGameProp.currentScore = brGameProp.undoScore
        setCurrentScore(brGameProp.currentScore)
        brGameProp.undoEnable = false
    }

    fun isCreatingNewGame() {
        LogUtil.i(TAG, "isCreatingNewGame")
        mGameAction = Constants.IS_CREATING_GAME
        setCreateNewGameText(createNewGameStr)
    }

    override fun newGame() {
        // creating a new game
        LogUtil.i(TAG, "newGame")
        timesPlayed++
        LogUtil.d(TAG, "newGame.timesPlayed = $timesPlayed")
        mGameAction = Constants.IS_CREATING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    override fun startSavingGame(): Boolean {
        LogUtil.i(TAG, "startSavingGame")
        brGameProp.isProcessingJob = true
        setScreenMessage(savingGameStr)
        var succeeded = true
        try {
            val foStream = fivePresenter.fileOutputStream(Constants.SAVE_FIVE_COLORS)
            // save settings
            if (hasSound()) foStream.write(1) else foStream.write(0)
            if (isEasyLevel()) foStream.write(1) else foStream.write(0)
            if (hasNext()) foStream.write(1) else foStream.write(0)
            // save values on game grid
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    foStream.write(brGridData.getCellValue(i, j))
                }
            }
            // save backupCells
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    foStream.write(brGridData.getBackupCells()[i][j])
                }
            }
            // save current score
            val scoreByte = ByteBuffer.allocate(4).putInt(brGameProp.currentScore).array()
            foStream.write(scoreByte)
            // save undo score
            val undoScoreByte = ByteBuffer.allocate(4).putInt(brGameProp.undoScore).array()
            foStream.write(undoScoreByte)
            // save undoEnable
            if (brGameProp.undoEnable) foStream.write(1) else foStream.write(0)
            foStream.close()
            // end of writing
            LogUtil.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            succeeded = false
            LogUtil.e(TAG, "startSavingGame.Failed.", ex)
        }
        setScreenMessage("")
        LogUtil.d(TAG, "startSavingGame.Finished")
        brGameProp.isProcessingJob = false

        return succeeded
    }

    override fun startLoadingGame(): Boolean {
        LogUtil.i(TAG, "startLoadingGame")
        brGameProp.isProcessingJob = true
        setScreenMessage(loadingGameStr)
        var succeeded = true
        val hasSound: Boolean
        val isEasyLevel: Boolean
        val hasNext: Boolean
        val gameCells = Array(rowCounts) {
            IntArray(colCounts) }
        val cScore: Int
        val isUndoEnable: Boolean
        val backupCells = Array(rowCounts) {
            IntArray(colCounts) }
        val unScore: Int
        try {
            val fiStream = fivePresenter.fileInputStream(Constants.SAVE_BALLS_REMOVER)
            LogUtil.d(TAG, "startLoadingGame.available() = " + fiStream.available())
            LogUtil.d(TAG, "startLoadingGame.getChannel().size() = " + fiStream.channel.size())
            // read game settings
            var bValue = fiStream.read()
            hasSound = bValue == 1
            bValue = fiStream.read()
            isEasyLevel = bValue == 1
            bValue = fiStream.read()
            hasNext = bValue == 1
            setHasSound(hasSound)
            setEasyLevel(isEasyLevel)
            setHasNext(hasNext)
            // load values on game grid
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    gameCells[i][j] = fiStream.read()
                }
            }
            // reading backupCells
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    backupCells[i][j] = fiStream.read()
                }
            }
            // reading current score
            val scoreByte = ByteArray(4)
            fiStream.read(scoreByte)
            cScore = ByteBuffer.wrap(scoreByte).getInt()
            // reading undo score
            val undoScoreByte = ByteArray(4)
            fiStream.read(undoScoreByte)
            unScore = ByteBuffer.wrap(undoScoreByte).getInt()
            // reading undoEnable
            bValue = fiStream.read()
            isUndoEnable = bValue == 1
            fiStream.close()

            // refresh Main UI with loaded data
            brGridData.setCellValues(gameCells)
            brGridData.setBackupCells(backupCells)
            brGameProp.currentScore = cScore
            brGameProp.undoScore = unScore
            brGameProp.undoEnable = isUndoEnable
            // start update UI
            setCurrentScore(brGameProp.currentScore)
            displayGameGridView()
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        setScreenMessage("")
        brGameProp.isProcessingJob = false

        return succeeded
    }

    override fun dealWithIsNextBalls(isNextBalls: Boolean) {
        // do nothing
    }

    private fun calculateScore(linkedLine: HashSet<Point>?): Int {
        if (linkedLine == null) {
            return 0
        }
        // easy level
        // 5 points for each ball if only 2 balls
        // 6 points for each ball if it is 3 balls
        // 7 points for each ball if it is 4 balls
        // 8 points for each ball if it is 5 balls
        // difficult level
        // 6 points for each ball if only 2 balls
        // 8 points for each ball if it is 3 balls
        // 10 points for each ball if it is 4 balls
        // 12 points for each ball if it is 5 balls
        val minBalls = 2
        val minScoreEach = if (isEasyLevel()) 5 else 6
        val plusScore = if (isEasyLevel()) 1 else 2
        val numBalls = linkedLine.size
        val totalScore = (minScoreEach + (numBalls - minBalls) * plusScore) * numBalls
        return totalScore
    }

    private inner class ShowScore_old(
        linkedPoint: HashSet<Point>,
        val lastGotScore: Int,
        val callback: ShowScoreCallback
    ): Runnable {
        private var pointSet: HashSet<Point>
        private var mCounter = 0
        init {
            LogUtil.i(TAG, "ShowScore")
            pointSet = HashSet(linkedPoint)
        }

        @Synchronized
        private fun onProgressUpdate(status: Int) {
            when (status) {
                0 -> for (item in pointSet) {
                    drawBall(item.x, item.y, brGridData.getCellValue(item.x, item.y))
                }
                1 -> for (item in pointSet) {
                    drawOval(item.x, item.y, brGridData.getCellValue(item.x, item.y))
                }
                2 -> for (item in pointSet) {
                    drawFirework(item.x, item.y)
                }
                3 -> {
                    setScreenMessage(lastGotScore.toString())
                    for (item in pointSet) {
                        clearCell(item.x, item.y)
                        drawBall(item.x, item.y, brGridData.getCellValue(item.x, item.y))
                    }
                }
                4 -> {
                    LogUtil.d(TAG, "ShowScore.onProgressUpdate.dismissShowMessageOnScreen.")
                    setScreenMessage("")
                }
                else -> {}
            }
        }

        @Synchronized
        override fun run() {
            val twinkleCountDown = 5
            mCounter++
            LogUtil.d(TAG, "ShowScore.run().mCounter = $mCounter")
            showingScoreHandler.removeCallbacksAndMessages(null)
            if (mCounter <= twinkleCountDown) {
                val md = mCounter % 2 // modulus
                onProgressUpdate(md)
                showingScoreHandler.postDelayed(this, 100)
            } else {
                when (mCounter) {
                    twinkleCountDown + 1 -> {
                        onProgressUpdate(2) // show the flash
                        showingScoreHandler.postDelayed(this, 100)
                    }
                    twinkleCountDown + 2 -> {
                        onProgressUpdate(3) // show score
                        showingScoreHandler.postDelayed(this, 500)
                    }
                    else -> {
                        onProgressUpdate(4) // dismiss showing message
                        callback.sCallback()
                    }
                }
            }
        }
    }
}