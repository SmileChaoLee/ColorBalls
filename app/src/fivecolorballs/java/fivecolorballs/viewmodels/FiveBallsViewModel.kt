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

class FiveBallsViewModel(private val fivePresenter: FiveBallsPresenter)
    : BaseViewModel(fivePresenter) {

    companion object {
        private const val TAG = "FiveBallsViewModel"
    }

    private val runningBallsHandler = Handler(Looper.getMainLooper())
    private var fiveGameProp: GameProp
    private var fiveGridData: FiveCbGridData
    var timesPlayed = 0
    private val rbLastIndex = FiveBallsConstants.NUM_NEXT_BALLS - 1
    private val runningBalls = ArrayList<Int>()
    private var nextRunningRow = 0
    var runningCol = FiveBallsConstants.COLUMN_COUNTS / 2
    private var isFinishRunning = false
    private var isGameOver = false

    private val _next4Balls = mutableStateListOf<Int>()
    val next4Balls: List<Int>
        get() = _next4Balls

    // runBallsRunnable will be running in the UI (main) thread
    // if it is handled by runningBallsHandler
    private val runBallsRunnable = object : Runnable {
        override fun run() {
            runningBallsHandler.removeCallbacksAndMessages(null)
            showRunningBalls(curRow = nextRunningRow)
            // erase after index is rbLastIndex
            if (nextRunningRow > rbLastIndex) {
                drawBall(nextRunningRow - (rbLastIndex + 1), runningCol, 0)
            }
            nextRunningRow++
            reachBottomOrBlock()
            LogUtil.d(TAG, "runBallsRunnable.isGameOver = $isGameOver")
            LogUtil.d(TAG, "runBallsRunnable.isFinishRunning = $isFinishRunning")
            if (!isGameOver) {
                if (isFinishRunning) {
                    startRunBalls()
                } else {
                    runningBallsHandler.postDelayed(this, 1000)
                }
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
        LogUtil.i(TAG, "initGame() = $bundle")
        fiveGameProp.isProcessingJob = true
        val isNewGame = restoreState(bundle)
        setCurrentScore(fiveGameProp.currentScore)
        if (isNewGame) {
            // generate
            LogUtil.i(TAG, "initGame.isNewGame")
        }
        displayGameGridView()
        getAndSetHighestScore() // a coroutine operation
        isFinishRunning = false
        isGameOver = false
        startRunBalls()
        fiveGameProp.isProcessingJob = false
    }

    fun startRunBalls(row: Int = 0) {
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
        isFinishRunning = false
        nextRunningRow = row
        runningCol = FiveBallsConstants.COLUMN_COUNTS / 2
        runningBallsHandler.post(runBallsRunnable)
    }

    private fun eraseRunningBalls(curRow: Int) {
        // erase previous Running Balls
        for (k in 0..minOf(curRow, rbLastIndex)) {
            drawBall(curRow - k, runningCol, 0)
        }
    }

    private fun showRunningBalls(curRow: Int) {
        for (k in 0..minOf(curRow, rbLastIndex)) {
            drawBall(
                curRow - k, runningCol,
                runningBalls[rbLastIndex - k]
            )
        }
    }

    private fun reachBottomOrBlock() {
        LogUtil.d(TAG,"reachBottomOrBlock.nextRunningRow = $nextRunningRow")
        if (nextRunningRow < FiveBallsConstants.ROW_COUNTS) {
            val ballColor = fiveGridData.mCellValues[nextRunningRow][runningCol]
            LogUtil.d(TAG,"reachBottomOrBlock.ballColor = $ballColor")
            if (ballColor != 0) isFinishRunning = true
        } else {
            isFinishRunning = true
        }
        LogUtil.d(TAG,"reachBottomOrBlock.isFinishRunning = $isFinishRunning")
        if (isFinishRunning) {
            // update fiveGridData.mCellValues[][]
            val curRow = if (nextRunningRow == 0) 0 else nextRunningRow - 1
            for (k in 0..minOf(curRow, rbLastIndex)) {
                fiveGridData.mCellValues[curRow - k][runningCol] = runningBalls[rbLastIndex - k]
            }
            if (nextRunningRow < FiveBallsConstants.NUM_NEXT_BALLS) {
                // Game over
                isGameOver = true
                gameOver()
            }
            fiveGridData.setNextRunning()
        }
    }

    fun shiftRunningCol(addValue: Int) {
        LogUtil.d(TAG,"shiftRunningCol")
        if (isFinishRunning) return
        if (runningCol + addValue < 0) return
        if (runningCol + addValue >= FiveBallsConstants.COLUMN_COUNTS) return

        // no need because this method is running in the same thread as
        // runBallsRunnable is running
        // runningBallsHandler.removeCallbacksAndMessages(null)
        val curRow = if (nextRunningRow == 0) 0 else nextRunningRow - 1
        var isBlocked = false
        for (k in 0 .. minOf(curRow, rbLastIndex)) {
            if (fiveGridData.mCellValues[curRow - k][runningCol+addValue] != 0) {
                isBlocked = true
                break
            }
        }
        if (!isBlocked) {
            eraseRunningBalls(curRow = curRow)
            // change column
            runningCol += addValue
            showRunningBalls(curRow = curRow)
        }
        // runningBallsHandler.post(runBallsRunnable)
        // startRunBalls(row = runningRow)
    }

    private fun restoreState(state: Bundle?): Boolean {
        LogUtil.i(TAG,"restoreState.state = $state")
        var isNewGame = true
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
            isNewGame = false
        }
        LogUtil.i(TAG, "restoreState.isNewGame = $isNewGame")
        if (isNewGame) {
            initData()
        } else {
            setData(gameProp!!, gridData!!)
        }

        return isNewGame
    }

    override fun release() {
        super.release()
        runningBallsHandler.removeCallbacksAndMessages(null)
    }

    override fun saveInstanceState(outState: Bundle) {
        runningBallsHandler.removeCallbacksAndMessages(null)
        outState.putParcelable(Constants.GAME_PROP_TAG, fiveGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, fiveGridData)
    }

    override fun undoTheLast() {
        LogUtil.i(TAG, "undoTheLast.undoEnable")
        // do nothing
    }

    override fun newGame() {
        // creating a new game
        LogUtil.i(TAG, "newGame")
        runningBallsHandler.removeCallbacksAndMessages(null)
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