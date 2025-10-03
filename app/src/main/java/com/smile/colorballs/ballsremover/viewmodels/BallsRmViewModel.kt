package com.smile.colorballs.ballsremover.viewmodels

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smile.colorballs.models.GridData
import com.smile.colorballs.ballsremover.presenters.BallsRmPresenter
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.models.GameProp
import com.smile.colorballs.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.ByteBuffer

class BallsRmViewModel(private val bRmPresenter: BallsRmPresenter)
    : BaseViewModel(bRmPresenter) {

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
        Log.d(TAG, "BallsRmViewModel.init")
        brGameProp = GameProp()
        brGridData = GridData()
        mGameProp = brGameProp
        mGridData = brGridData
        super.setProperties()
        createNewGameStr = bRmPresenter.createNewGameStr
    }

    override fun cellClickListener(i: Int, j: Int) {
        Log.d(TAG, "cellClickListener.($i, $j)")
        if (brGridData.getCellValue(i, j) == 0) return  // no ball
        if (brGameProp.isProcessingJob) return
        if (brGridData.checkMoreThanTwo(i, j)) {
            brGridData.backupCells()
            brGameProp.undoScore = brGameProp.currentScore
            brGameProp.undoEnable = true
            brGameProp.isProcessingJob = true
            val tempLine = HashSet(brGridData.getLightLine())
            Log.d(TAG, "cellClickListener.tempLine.size = ${tempLine.size}")
            brGameProp.lastGotScore = calculateScore(tempLine)
            brGameProp.currentScore += brGameProp.lastGotScore
            setCurrentScore(brGameProp.currentScore)
            val showScore = ShowScore(
                brGridData.getLightLine(), brGameProp.lastGotScore,
                object : ShowScoreCallback {
                    override fun sCallback() {
                        Log.d(TAG, "cellClickListener.sCallback")
                        viewModelScope.launch(Dispatchers.Default) {
                            // Refresh the game view
                            brGridData.refreshColorBalls(hasNext())
                            delay(200)
                            displayGameGridView()
                            if (brGridData.isGameOver()) {
                                Log.d(TAG, "cellClickListener.sCallback.gameOver()")
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
        Log.d(TAG, "setData")
        brGameProp = prop
        brGridData = gData
        // update mGameProp and mGridData in BaseViewModel
        mGameProp = prop
        mGridData = gData
    }

    private fun initData() {
        Log.d(TAG, "initData")
        brGameProp.initializeKeepSetting(getWhichGame())
        brGridData.initialize()
    }

    override fun initGame(bundle: Bundle?) {
        Log.d(TAG, "initGame = $bundle")
        brGameProp.isProcessingJob = true
        val isNewGame = restoreState(bundle)
        setCurrentScore(brGameProp.currentScore)
        if (isNewGame) {
            // generate
            Log.d(TAG, "initGame.isNewGame")
            brGridData.generateColorBalls()
        }
        displayGameGridView()
        getAndSetHighestScore() // a coroutine operation
        brGameProp.isProcessingJob = false
    }

    private fun restoreState(state: Bundle?): Boolean {
        var isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: GridData? = null
        state?.let {
            Log.d(TAG,"restoreState.state not null then restore the state")
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
            Log.d(TAG, "restoreState.gridData = $gridData")
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
                    Log.d(TAG, "restoreState.CellValues are all 0")
                }
            }
        }
        Log.d(TAG, "restoreState.isNewGame = $isNewGame")
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
        Log.d(TAG, "undoTheLast.undoEnable = ${brGameProp.undoEnable}")
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
        Log.d(TAG, "isCreatingNewGame")
        mGameAction = Constants.IS_CREATING_GAME
        setCreateNewGameText(createNewGameStr)
    }

    override fun newGame() {
        // creating a new game
        Log.d(TAG, "newGame")
        timesPlayed++
        Log.d(TAG, "newGame.timesPlayed = $timesPlayed")
        mGameAction = Constants.IS_CREATING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    override fun startSavingGame(): Boolean {
        Log.d(TAG, "startSavingGame")
        brGameProp.isProcessingJob = true
        setScreenMessage(savingGameStr)
        var succeeded = true
        try {
            val foStream = bRmPresenter.fileOutputStream(Constants.SAVE_BALLS_REMOVER)
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
            Log.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
            Log.d(TAG, "startSavingGame.Failed.")
        }
        setScreenMessage("")
        Log.d(TAG, "startSavingGame.Finished")
        brGameProp.isProcessingJob = false

        return succeeded
    }

    override fun startLoadingGame(): Boolean {
        Log.d(TAG, "startLoadingGame")
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
            val fiStream = bRmPresenter.fileInputStream(Constants.SAVE_BALLS_REMOVER)
            Log.d(TAG, "startLoadingGame.available() = " + fiStream.available())
            Log.d(TAG, "startLoadingGame.getChannel().size() = " + fiStream.channel.size())
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

    private inner class ShowScore(
        linkedPoint: HashSet<Point>,
        val lastGotScore: Int,
        val callback: ShowScoreCallback
    ): Runnable {
        private var pointSet: HashSet<Point>
        private var mCounter = 0
        init {
            Log.d(TAG, "ShowScore")
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
                2 -> {}
                3 -> {
                    setScreenMessage(lastGotScore.toString())
                    for (item in pointSet) {
                        clearCell(item.x, item.y)
                        drawBall(item.x, item.y, brGridData.getCellValue(item.x, item.y))
                    }
                }
                4 -> {
                    Log.d(TAG, "ShowScore.onProgressUpdate.dismissShowMessageOnScreen.")
                    setScreenMessage("")
                }
                else -> {}
            }
        }

        @Synchronized
        override fun run() {
            val twinkleCountDown = 5
            mCounter++
            Log.d(TAG, "ShowScore.run().mCounter = $mCounter")
            if (mCounter <= twinkleCountDown) {
                val md = mCounter % 2 // modulus
                onProgressUpdate(md)
                showingScoreHandler.postDelayed(this, 150)
            } else {
                if (mCounter == twinkleCountDown + 1) {
                    onProgressUpdate(3) // show score
                    showingScoreHandler.postDelayed(this, 500)
                } else {
                    showingScoreHandler.removeCallbacksAndMessages(null)
                    onProgressUpdate(4) // dismiss showing message
                    callback.sCallback()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BallsRmViewModel"
    }
}