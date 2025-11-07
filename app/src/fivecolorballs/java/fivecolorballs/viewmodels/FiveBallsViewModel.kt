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

class FiveBallsViewModel(fivePresenter: FiveBallsPresenter)
    : BaseViewModel(fivePresenter) {

    companion object {
        private const val TAG = "FiveBallsViewModel"
    }

    private val runningBallsHandler = Handler(Looper.getMainLooper())
    private var fiveGameProp: GameProp
    private var fiveGridData: FiveCbGridData
    var timesPlayed = 0
    private val runningBalls = ArrayList<Int>()
    private var runningRow = 0
    var runningCol = FiveBallsConstants.COLUMN_COUNTS / 2

    private val _next4Balls = mutableStateListOf<Int>()
    val next4Balls: List<Int>
        get() = _next4Balls

    private val runBallsRunnable = object : Runnable {
        val lastIndex = FiveBallsConstants.NUM_NEXT_BALLS - 1
        override fun run() {
            runningBallsHandler.removeCallbacksAndMessages(null)
            for (k in 0 .. minOf(runningRow, lastIndex)) {
                drawBall(runningRow - k, runningCol,
                    runningBalls[lastIndex - k])
            }
            // erase after index is lastIndex
            if (runningRow > lastIndex) {
                drawBall(runningRow - (lastIndex+1), runningCol, 0)
            }
            runningRow++
            if (runningRow < FiveBallsConstants.ROW_COUNTS) {
                runningBallsHandler.postDelayed(this, 1000)
            } else {
                reStartRunBalls()
            }
        }
    }

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
        startRunBalls(row = 0)
        fiveGameProp.isProcessingJob = false
    }

    fun startRunBalls(row: Int) {
        LogUtil.i(TAG,"startRunBalls")
        runningBalls.clear()
        runningBalls.addAll(fiveGridData.runningBalls)
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
        runningRow = row
        /*
        val lastIndex = FiveBallsConstants.NUM_NEXT_BALLS - 1
        val runBallsRunnable = object : Runnable {
            override fun run() {
                runningBallsHandler.removeCallbacksAndMessages(null)
                for (k in 0 .. minOf(runningRow, lastIndex)) {
                    drawBall(runningRow - k, runningCol,
                        runningBalls[lastIndex - k])
                }
                // erase after index is lastIndex
                if (runningRow > lastIndex) {
                    drawBall(runningRow - (lastIndex+1), runningCol, 0)
                }
                runningRow++
                if (runningRow < FiveBallsConstants.ROW_COUNTS) {
                    runningBallsHandler.postDelayed(this, 1000)
                } else {
                    reStartRunBalls()
                }
            }
        }
        */
        runningBallsHandler.post(runBallsRunnable)
    }

    private fun reStartRunBalls() {
        LogUtil.i(TAG,"reStartRunBalls")
        runningCol--
        if (runningCol >= 0) {
            fiveGridData.setNextRunning()
            startRunBalls(row = 0)
        }
    }

    fun addRunningCol(addValue: Int) {
        runningCol += addValue
        if (runningCol < 0) runningCol = 0
        if (runningCol > FiveBallsConstants.COLUMN_COUNTS - 1)
            runningCol = FiveBallsConstants.COLUMN_COUNTS - 1
    }

    fun eraseRunningBalls() {
        LogUtil.i(TAG,"eraseRunningBalls")
        runningBallsHandler.removeCallbacksAndMessages(null)
        runningRow = if (runningRow == 0) 0 else runningRow - 1
        val lastIndex = FiveBallsConstants.NUM_NEXT_BALLS - 1
        for (k in 0 .. minOf(runningRow, lastIndex)) {
            drawBall(runningRow - k, runningCol, 0)
        }
        runningBallsHandler.post(runBallsRunnable)
        // startRunBalls(row = runningRow)
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
        return true
    }

    override fun startLoadingGame(): Boolean {
        LogUtil.i(TAG, "startLoadingGame")
        return true
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
}