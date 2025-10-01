package com.smile.colorballs.ballsremover.viewmodels

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smile.colorballs.ColorBallsApp
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
        mGameProp = GameProp()
        mGridData = GridData()
        super.setProperties()
        createNewGameStr = bRmPresenter.createNewGameStr
    }

    override fun initProperties(): String {
        Log.d(TAG, "initProperties.createNewGameStr = $createNewGameStr")
        return createNewGameStr
    }

    override fun cellClickListener(i: Int, j: Int) {
        Log.d(TAG, "cellClickListener.($i, $j)")
        if (ColorBallsApp.isProcessingJob) return
        if (mGridData.checkMoreThanTwo(i, j)) {
            mGridData.backupCells()
            mGameProp.undoScore = mGameProp.currentScore
            mGameProp.undoEnable = true
            ColorBallsApp.isProcessingJob = true
            val tempLine = HashSet(mGridData.getLightLine())
            Log.d(TAG, "cellClickListener.tempLine.size = ${tempLine.size}")
            mGameProp.lastGotScore = calculateScore(tempLine)
            mGameProp.currentScore += mGameProp.lastGotScore
            setCurrentScore(mGameProp.currentScore)
            val showScore = ShowScore(
                mGridData.getLightLine(), mGameProp.lastGotScore,
                object : ShowScoreCallback {
                    override fun sCallback() {
                        Log.d(TAG, "cellClickListener.sCallback")
                        viewModelScope.launch(Dispatchers.Default) {
                            // Refresh the game view
                            mGridData.refreshColorBalls(hasNext())
                            delay(200)
                            displayGameGridView()
                            ColorBallsApp.isProcessingJob = false
                            if (mGridData.isGameOver()) {
                                Log.d(TAG, "cellClickListener.sCallback.gameOver()")
                                gameOver()
                            }
                        }
                    }
                })
            showingScoreHandler.post(showScore)
        }
    }

    private fun setData(prop: GameProp, gData: GridData) {
        Log.d(TAG, "setData")
        mGameProp = prop
        mGridData = gData
    }

    private fun initData() {
        Log.d(TAG, "initData")
        mGameProp.initialize(getWhichGame())
        mGridData.initialize()
    }

    override fun initGame(bundle: Bundle?) {
        Log.d(TAG, "initGame = $bundle")
        ColorBallsApp.isProcessingJob = true
        val isNewGame = restoreState(bundle)
        // setHighestScore(mPresenter.highestScore())
        // Log.d(TAG, "initGame.highestScore = ${getHighestScore()}")
        setCurrentScore(mGameProp.currentScore)
        if (isNewGame) {
            // generate
            Log.d(TAG, "initGame.isNewGame")
            mGridData.generateColorBalls()
        }
        displayGameGridView()
        getAndSetHighestScore() // a coroutine operation
        ColorBallsApp.isProcessingJob = false
    }

    private fun getAndSetHighestScore() {
        Log.d(TAG, "getAndSetHighestScore")
        viewModelScope.launch(Dispatchers.IO) {
            val db = bRmPresenter.scoreDatabase()
            val score = db.getHighestScore()
            Log.d(TAG, "getAndSetHighestScore.score = $score")
            db.close()
            setHighestScore(score)
        }
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

    fun undoTheLast() {
        Log.d(TAG, "undoTheLast.undoEnable = ${mGameProp.undoEnable}")
        if (!mGameProp.undoEnable) {
            return
        }
        mGridData.undoTheLast()
        // restore the screen
        displayGameGridView()
        mGameProp.currentScore = mGameProp.undoScore
        setCurrentScore(mGameProp.currentScore)
        mGameProp.undoEnable = false
    }

    fun isCreatingNewGame() {
        Log.d(TAG, "isCreatingNewGame")
        mGameAction = Constants.IS_CREATING_GAME
        setCreateNewGameText(createNewGameStr)
    }

    fun newGame() {
        // creating a new game
        Log.d(TAG, "newGame")
        timesPlayed++
        Log.d(TAG, "newGame.timesPlayed = $timesPlayed")
        mGameAction = Constants.IS_CREATING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    override fun startSavingGame(): Boolean {
        Log.d(TAG, "startSavingGame")
        ColorBallsApp.isProcessingJob = true
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
                    foStream.write(mGridData.getCellValue(i, j))
                }
            }
            // save backupCells
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    foStream.write(mGridData.getBackupCells()[i][j])
                }
            }
            // save current score
            val scoreByte = ByteBuffer.allocate(4).putInt(mGameProp.currentScore).array()
            foStream.write(scoreByte)
            // save undo score
            val undoScoreByte = ByteBuffer.allocate(4).putInt(mGameProp.undoScore).array()
            foStream.write(undoScoreByte)
            // save undoEnable
            if (mGameProp.undoEnable) foStream.write(1) else foStream.write(0)
            foStream.close()
            // end of writing
            Log.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
            Log.d(TAG, "startSavingGame.Failed.")
        }
        ColorBallsApp.isProcessingJob = false
        setScreenMessage("")
        Log.d(TAG, "startSavingGame.Finished")
        return succeeded
    }

    override fun startLoadingGame(): Boolean {
        Log.d(TAG, "startLoadingGame")
        ColorBallsApp.isProcessingJob = true
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
            mGameProp.hasSound = hasSound
            mGameProp.isEasyLevel = isEasyLevel
            mGameProp.hasNext = hasNext
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
            mGridData.setCellValues(gameCells)
            mGridData.setBackupCells(backupCells)
            mGameProp.currentScore = cScore
            mGameProp.undoScore = unScore
            mGameProp.undoEnable = isUndoEnable
            // start update UI
            setCurrentScore(mGameProp.currentScore)
            displayGameGridView()
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        ColorBallsApp.isProcessingJob = false
        setScreenMessage("")
        return succeeded
    }

    private fun gameOver() {
        Log.d(TAG, "gameOver")
        if (hasSound()) {
            soundPool.playSound()
        }
        newGame()
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

    companion object {
        private const val TAG = "BallsRmViewModel"
    }
}