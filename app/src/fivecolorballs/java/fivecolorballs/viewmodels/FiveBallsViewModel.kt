package fivecolorballs.viewmodels

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import fivecolorballs.constants.FiveBallsConstants
import fivecolorballs.models.FiveCbGridData
import fivecolorballs.presenters.FiveBallsPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FiveBallsViewModel(private val fivePresenter: FiveBallsPresenter)
    : BaseViewModel(fivePresenter) {

    companion object {
        private const val TAG = "FiveBallsViewModel"
    }

    private val runningBallsHandler = Handler(Looper.getMainLooper())
    private var fiveGameProp: GameProp
    private var fiveGridData: FiveCbGridData
    private val rbLastIndex = FiveBallsConstants.NUM_NEXT_BALLS - 1
    private val runningBalls = ArrayList<Int>()
    private var nextRunningRow = 0
    // one second
    private var droppingSpeed = FiveBallsConstants.NORMAL_DROPPING_SPEED
    private var isToEnd = false
    var runningCol = FiveBallsConstants.COLUMN_COUNTS / 2
    private var isGameJustStarted = true
    private var isFinishRunning = false
    private var isGameOver = false
    private var gameStartTime = System.currentTimeMillis()

    private val _next4Balls = mutableStateListOf<Int>()
    val next4Balls: List<Int>
        get() = _next4Balls

    // runBallsRunnable will be running in the UI (main) thread
    // if it is handled by runningBallsHandler
    private val runBallsRunnable = object : Runnable {
        override fun run() {
            LogUtil.d(TAG, "runBallsRunnable.run()")
            runningBallsHandler.removeCallbacksAndMessages(null)
            if (fiveGameProp.isProcessingJob) {
                runningBallsHandler.postDelayed(this, droppingSpeed)
                return
            }
            showRunningBalls(curRow = nextRunningRow)
            // erase after index is rbLastIndex
            if (nextRunningRow > rbLastIndex) {
                drawBall(nextRunningRow - (rbLastIndex + 1), runningCol, 0)
            }
            nextRunningRow++
            reachBottomOrBlock()
            LogUtil.d(TAG, "runBallsRunnable.isFinishRunning = $isFinishRunning")
            if (!isFinishRunning) {
                runningBallsHandler.postDelayed(this, droppingSpeed)
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
        isToEnd = false
        isFinishRunning = false
        isGameOver = false
        setDroppingSpeed(FiveBallsConstants.NORMAL_DROPPING_SPEED)
        fiveGameProp.isProcessingJob = false
        gameStartTime = System.currentTimeMillis()
        isGameJustStarted = true
        startRunBalls()
    }

    fun setDroppingSpeed(seconds: Long) {
        runningBallsHandler.removeCallbacksAndMessages(null)
        droppingSpeed = if (seconds < FiveBallsConstants.MIN_DROPPING_SPEED)
            FiveBallsConstants.MIN_DROPPING_SPEED
        else seconds
        runningBallsHandler.post(runBallsRunnable)
    }

    fun toDropToEnd() {
        if (fiveGameProp.isProcessingJob) return
        if (isToEnd) return
        isToEnd = true
        runningBallsHandler.removeCallbacksAndMessages(null)
        droppingSpeed = FiveBallsConstants.END_DROPPING_SPEED
        runningBallsHandler.post(runBallsRunnable)
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
        isToEnd = false
        isFinishRunning = false
        nextRunningRow = row
        runningCol = FiveBallsConstants.COLUMN_COUNTS / 2
        // one minute speed up 1 ms
        val passTime = (System.currentTimeMillis() - gameStartTime) / 5000L
        setDroppingSpeed(FiveBallsConstants.NORMAL_DROPPING_SPEED - passTime)
        LogUtil.i(TAG,"startRunBalls.droppingSpeed = $droppingSpeed")
        isGameJustStarted = false
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
            runningBallsHandler.removeCallbacksAndMessages(null)
            val threeSet = HashSet<Point>()
            val curRow = if (nextRunningRow == 0) 0 else nextRunningRow - 1
            val minIndex = minOf(curRow, rbLastIndex)
            for (k in 0..minOf(curRow, minIndex)) {
                // update fiveGridData.mCellValues[][]
                fiveGridData.mCellValues[curRow - k][runningCol] = runningBalls[rbLastIndex - k]
                threeSet.add(Point(curRow-k,runningCol))
            }
            if (!fiveGridData.moreThan3NABOR(threeSet)) {
                fiveGridData.setNextRunning()
                if (nextRunningRow < FiveBallsConstants.NUM_NEXT_BALLS) {
                    // Game over
                    isGameOver = true
                    gameOver()
                } else {
                    startRunBalls()
                }
                return
            }
            startCrashBalls()
        }
    }

    private fun startCrashBalls() {
        LogUtil.d(TAG, "startCrashBalls")
        fiveGameProp.isProcessingJob = true
        val tempLine = HashSet(fiveGridData.addUpLightLine)
        LogUtil.d(TAG, "startCrashBalls.tempLine.size = ${tempLine.size}")
        fiveGameProp.lastGotScore = calculateScore(tempLine)
        fiveGameProp.currentScore += fiveGameProp.lastGotScore
        setCurrentScore(fiveGameProp.currentScore)
        val showScore = ShowScore(
            fiveGridData,
            tempLine,
            fiveGameProp.lastGotScore,
            false /* no used*/,
            object : ShowScoreCallback {
                override fun sCallback() {
                    LogUtil.d(TAG, "startCrashBalls.sCallback")
                    viewModelScope.launch(Dispatchers.Default) {
                        // Refresh the game view
                        val canCrashAgain = fiveGridData.crashColorBalls()
                        LogUtil.d(TAG, "startCrashBalls.sCallback.canCrashAgain = $canCrashAgain")
                        displayGameGridView()
                        if (canCrashAgain) {
                            startCrashBalls()    // recursion
                        } else {
                            // check if game over
                            fiveGridData.setNextRunning()
                            fiveGameProp.isProcessingJob = false
                            startRunBalls()
                        }
                    }
                }
            })
        showingScoreHandler.post(showScore)
    }

    fun shiftRunningCol(addValue: Int) {
        LogUtil.d(TAG,"shiftRunningCol")
        if (fiveGameProp.isProcessingJob) return
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
            // check if it meet the bottom or having a ball under
            reachBottomOrBlock()
        }
        // runningBallsHandler.post(runBallsRunnable)
        // startRunBalls(row = runningRow)
    }

    fun rotateRunningBalls() {
        LogUtil.i(TAG,"rotateRunningBalls")
        if (fiveGameProp.isProcessingJob) return
        val first = runningBalls[0]
        for (i in 0 until rbLastIndex) {
            runningBalls[i] = runningBalls[i+1]
        }
        runningBalls[rbLastIndex] = first
        val curRow = if (nextRunningRow == 0) 0 else nextRunningRow - 1
        showRunningBalls(curRow = curRow )
    }

    fun startRunningHandler() {
        LogUtil.i(TAG,"startRunningHandler")
        if (isGameJustStarted) return
        runningBallsHandler.post(runBallsRunnable)
    }

    fun stopRunningHandler() {
        LogUtil.i(TAG,"stopRunningHandler")
        runningBallsHandler.removeCallbacksAndMessages(null)
    }

    private fun restoreState(state: Bundle?): Boolean {
        LogUtil.i(TAG,"restoreState.state = $state")
        var isNewGame = true
        var gameProp: GameProp? = null
        var gridData: FiveCbGridData? = null
        state?.let {
            LogUtil.d(TAG,"restoreState.state not null")
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
        LogUtil.i(TAG, "dealWithIsNextBalls")
        // do nothing
    }

    private fun calculateScore(linkedLine: HashSet<Point>?): Int {
        if (linkedLine == null) {
            return 0
        }
        // easy level
        // one point for each ball
        return linkedLine.size
    }
}