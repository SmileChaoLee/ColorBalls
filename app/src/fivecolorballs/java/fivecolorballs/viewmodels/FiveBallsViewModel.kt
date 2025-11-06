package fivecolorballs.viewmodels

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import fivecolorballs.constants.FiveBallsConstants
import fivecolorballs.models.FiveCbGridData
import fivecolorballs.presenters.FiveBallsPresenter
import java.io.IOException
import java.nio.ByteBuffer

class FiveBallsViewModel(private val fivePresenter: FiveBallsPresenter)
    : BaseViewModel(fivePresenter) {

    companion object {
        private const val TAG = "FiveBallsViewModel"
    }

    private val runningBallsHandler = Handler(Looper.getMainLooper())
    private var fiveGameProp: GameProp
    private var fiveGridData: FiveCbGridData
    var timesPlayed = 0
    var runningCol = FiveBallsConstants.COLUMN_COUNTS / 2

    private val _next4Balls = mutableStateListOf<Int>()
    val next4Balls: List<Int>
        get() = _next4Balls

    init {
        LogUtil.i(TAG, "FiveBallsViewModel.init")
        fiveGameProp = GameProp()
        fiveGridData = FiveCbGridData()
        mGameProp = fiveGameProp
        mGridData = fiveGridData
        super.setProperties()
    }

    override fun cellClickListener(i: Int, j: Int) {
        LogUtil.i(TAG, "cellClickListener.($i, $j)")
        // do nothing
    }

    private fun setData(prop: GameProp, gData: FiveCbGridData) {
        LogUtil.i(TAG, "setData")
        fiveGameProp = prop
        fiveGridData = gData
        // update mGameProp and mGridData in BaseViewModel
        mGameProp = prop
        mGridData = gData
    }

    private fun initData() {
        LogUtil.i(TAG, "initData")
        fiveGameProp.initializeKeepSetting(getWhichGame())
        fiveGridData.initialize()
    }

    override fun initGame(bundle: Bundle?) {
        LogUtil.i(TAG, "initGame = $bundle")
        fiveGameProp.isProcessingJob = true
        val isNewGame = restoreState(bundle)
        setCurrentScore(fiveGameProp.currentScore)
        if (isNewGame) {
            // generate
            LogUtil.i(TAG, "initGame.isNewGame")
        }
        displayGameGridView()
        getAndSetHighestScore() // a coroutine operation
        startRunBalls()
        fiveGameProp.isProcessingJob = false
    }

    fun startRunBalls() {
        LogUtil.i(TAG,"startRunBalls")
        val runningBalls = fiveGridData.runningBalls
        if (runningBalls.isEmpty()) {
            LogUtil.i(TAG,"startRunBalls.runningBalls.size = 0")
            return
        }
        _next4Balls.clear()
        _next4Balls.addAll(fiveGridData.next4Balls)
        if (next4Balls.isEmpty()) {
            LogUtil.i(TAG,"startRunBalls.next4Balls.size = 0")
            return
        }
        var i = 0
        val lastIndex = FiveBallsConstants.NUM_NEXT_BALLS - 1
        val runBallsRunnable = object : Runnable {
            override fun run() {
                runningBallsHandler.removeCallbacksAndMessages(null)
                for (k in 0 .. minOf(i, lastIndex)) {
                    drawBall(i - k, runningCol, runningBalls[lastIndex - k])
                }
                // erase after index is lastIndex
                if (i > lastIndex) {
                    drawBall(i - (lastIndex+1), runningCol, 0)
                }
                i++
                if (i < FiveBallsConstants.ROW_COUNTS) {
                    runningBallsHandler.postDelayed(this, 1000)
                } else {
                    reStartRunBalls()
                }
            }
        }
        runningBallsHandler.post(runBallsRunnable)
    }

    private fun reStartRunBalls() {
        LogUtil.i(TAG,"reStartRunBalls")
        runningCol--
        if (runningCol >= 0) {
            fiveGridData.setNextRunning()
            startRunBalls()
        }
    }

    private fun restoreState(state: Bundle?): Boolean {
        LogUtil.i(TAG,"restoreState.state")
        var isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: FiveCbGridData? = null
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
                        FiveCbGridData::class.java)
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
        outState.putParcelable(Constants.GAME_PROP_TAG, fiveGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, fiveGridData)
    }

    override fun undoTheLast() {
        LogUtil.i(TAG, "undoTheLast.undoEnable = ${fiveGameProp.undoEnable}")
        if (!fiveGameProp.undoEnable) {
            return
        }
        fiveGridData.undoTheLast()
        // restore the screen
        displayGameGridView()
        fiveGameProp.currentScore = fiveGameProp.undoScore
        setCurrentScore(fiveGameProp.currentScore)
        fiveGameProp.undoEnable = false
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
        fiveGameProp.isProcessingJob = true
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
                    foStream.write(fiveGridData.getCellValue(i, j))
                }
            }
            // save backupCells
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    foStream.write(fiveGridData.getBackupCells()[i][j])
                }
            }
            // save current score
            val scoreByte = ByteBuffer.allocate(4).putInt(fiveGameProp.currentScore).array()
            foStream.write(scoreByte)
            // save undo score
            val undoScoreByte = ByteBuffer.allocate(4).putInt(fiveGameProp.undoScore).array()
            foStream.write(undoScoreByte)
            // save undoEnable
            if (fiveGameProp.undoEnable) foStream.write(1) else foStream.write(0)
            foStream.close()
            // end of writing
            LogUtil.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            succeeded = false
            LogUtil.e(TAG, "startSavingGame.Failed.", ex)
        }
        setScreenMessage("")
        LogUtil.d(TAG, "startSavingGame.Finished")
        fiveGameProp.isProcessingJob = false

        return succeeded
    }

    override fun startLoadingGame(): Boolean {
        LogUtil.i(TAG, "startLoadingGame")
        fiveGameProp.isProcessingJob = true
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
            fiveGridData.setCellValues(gameCells)
            fiveGridData.setBackupCells(backupCells)
            fiveGameProp.currentScore = cScore
            fiveGameProp.undoScore = unScore
            fiveGameProp.undoEnable = isUndoEnable
            // start update UI
            setCurrentScore(fiveGameProp.currentScore)
            displayGameGridView()
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        setScreenMessage("")
        fiveGameProp.isProcessingJob = false

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
                    drawBall(item.x, item.y, fiveGridData.getCellValue(item.x, item.y))
                }
                1 -> for (item in pointSet) {
                    drawOval(item.x, item.y, fiveGridData.getCellValue(item.x, item.y))
                }
                2 -> for (item in pointSet) {
                    drawFirework(item.x, item.y)
                }
                3 -> {
                    setScreenMessage(lastGotScore.toString())
                    for (item in pointSet) {
                        clearCell(item.x, item.y)
                        drawBall(item.x, item.y, fiveGridData.getCellValue(item.x, item.y))
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