package com.smile.colorballs.viewmodel

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.BundleCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.constants.WhichBall
import com.smile.colorballs.models.GameProp
import com.smile.colorballs.models.GridData
import com.smile.colorballs.models.ColorBallInfo
import com.smile.colorballs.presenters.Presenter
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.roomdatabase.Score
import com.smile.smilelibraries.utilities.SoundPoolUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.nio.ByteBuffer

class ColorBallViewModel: ViewModel() {

    private interface ShowScoreCallback {
        fun sCallback()
    }

    private lateinit var mPresenter: Presenter
    private val bouncyBallHandler = Handler(Looper.getMainLooper())
    private val movingBallHandler = Handler(Looper.getMainLooper())
    private val showingScoreHandler = Handler(Looper.getMainLooper())
    private var mGameProp = GameProp()
    private var mGridData = GridData()

    private var loadingStr = ""
    private var savingGameStr = ""
    private var loadingGameStr = ""
    private var sureToSaveGameStr = ""
    private var sureToLoadGameStr = ""
    private var gameOverStr = ""
    private var saveScoreStr = ""
    private lateinit var soundPool: SoundPoolUtil
    private lateinit var medalImageIds: List<Int>

    var mGameAction = Constants.IS_QUITING_GAME

    private val currentScore = mutableIntStateOf(0)
    fun getCurrentScore() = currentScore.intValue
    private fun setCurrentScore(score: Int) {
        currentScore.intValue = score
    }

    private val highestScore = mutableIntStateOf(0)
    fun getHighestScore() = highestScore.intValue
    private fun setHighestScore(score: Int) {
        highestScore.intValue = score
    }

    private val screenMessage = mutableStateOf("")
    fun getScreenMessage() = screenMessage.value
    private fun setScreenMessage(msg: String) {
        screenMessage.value = msg
    }

    private val saveGameText = mutableStateOf("")
    fun getSaveGameText() = saveGameText.value
    fun setSaveGameText(text: String) {
        saveGameText.value = text
    }

    private val loadGameText = mutableStateOf("")
    fun getLoadGameText() = loadGameText.value
    fun setLoadGameText(text: String) {
        loadGameText.value = text
    }

    private val saveScoreTitle = mutableStateOf("")
    fun getSaveScoreTitle() = saveScoreTitle.value
    fun setSaveScoreTitle(title: String) {
        saveScoreTitle.value = title
    }

    val gridDataArray = Array(Constants.ROW_COUNTS) {
        Array(Constants.ROW_COUNTS) {
            mutableStateOf(ColorBallInfo())
        }
    }

    init {
        Log.d(TAG, "MainComposeViewModel.init")
    }

    fun setPresenter(presenter: Presenter) {
        mPresenter = presenter
        medalImageIds = mPresenter.medalImageIds
        loadingStr = mPresenter.loadingStr
        savingGameStr = mPresenter.savingGameStr
        loadingGameStr = mPresenter.loadingGameStr
        sureToSaveGameStr = mPresenter.sureToSaveGameStr
        sureToLoadGameStr = mPresenter.sureToLoadGameStr
        gameOverStr = mPresenter.gameOverStr
        saveScoreStr = mPresenter.saveScoreStr
        soundPool = mPresenter.soundPool
    }

    fun cellClickListener(i: Int, j: Int) {
        Log.d(TAG, "cellClickListener.($i, $j)")
        Log.d(TAG, "cellClickListener.isBallBouncing = " +
                "${mGameProp.isBallBouncing}")
        if (ColorBallsApp.isProcessingJob) return

        val ballColor = mGridData.getCellValue(i, j)
        if (!mGameProp.isBallBouncing) {
            if (ballColor != 0) {
                if ((mGameProp.bouncyBallIndexI == -1) &&
                    (mGameProp.bouncyBallIndexJ == -1)) {
                    mGameProp.isBallBouncing = true
                    drawBouncyBall(i, j)
                    mGameProp.bouncyBallIndexI = i
                    mGameProp.bouncyBallIndexJ = j
                }
            }
        } else {
            val bouncyI = mGameProp.bouncyBallIndexI
            val bouncyJ = mGameProp.bouncyBallIndexJ
            if (ballColor == 0) {
                if ((bouncyI >= 0) && (bouncyJ >= 0)) {
                    if (mGridData.canMoveCellToCell(Point(bouncyI, bouncyJ), Point(i, j))) {
                        mGameProp.isBallBouncing = false
                        // cancel the bouncy timer
                        stopBouncyAnimation()
                        mGameProp.bouncyBallIndexI = -1
                        mGameProp.bouncyBallIndexJ = -1
                        drawBallAlongPath()
                        mGameProp.undoEnable = true
                    } else {
                        //  make a sound
                        if (mGameProp.hasSound) {
                            soundPool.playSound()
                        }
                    }
                }
            } else {
                //  cell is not blank
                if ((bouncyI >= 0) && (bouncyJ >= 0)) {
                    stopBouncyAnimation()
                    drawBall(bouncyI, bouncyJ, mGridData.getCellValue(bouncyI, bouncyJ))
                    drawBouncyBall(i, j)
                    mGameProp.bouncyBallIndexI = i
                    mGameProp.bouncyBallIndexJ = j
                }
            }
        }
    }

    private fun setData(prop: GameProp, gData: GridData) {
        Log.d(TAG, "setData")
        mGameProp = prop
        mGridData = gData
    }

    private fun initData() {
        Log.d(TAG, "initData")
        mGameProp.initialize()
        mGridData.initialize()
    }

    fun initGame(state: Bundle?) {
        Log.d(TAG, "initGame = $state")
        // ColorBallsApp.isProcessingJob = mGameProp.isProcessingJob
        ColorBallsApp.isProcessingJob = true
        val isNewGame = restoreState(state)
        ColorBallsApp.isShowingLoadingMessage = mGameProp.isShowingLoadingMessage
        setCurrentScore(mGameProp.currentScore)
        // displayGameView()
        if (isNewGame) {    // new game
            Log.d(TAG, "initGame.isNewGame")
            displayGameGridView()
            displayGridDataNextCells()
        } else {
            displayGameView()
            // display the original state before changing configuration
            // need to be tested
            if (ColorBallsApp.isShowingLoadingMessage) {
                setScreenMessage(loadingStr)
            }
            if (mGameProp.isBallMoving) {
                Log.d(TAG, "initGame.mGameProp.isBallMoving() is true")
                drawBallAlongPath()
            }
            if (mGameProp.isShowingScoreMessage) {
                Log.d(TAG, "initGame.mGameProp.isShowingScoreMessage() is true")
                val showScore = ShowScore(
                    mGridData.getLightLine(), mGameProp.lastGotScore,
                    mGameProp.isShowNextBallsAfterBlinking,
                    object : ShowScoreCallback {
                        override fun sCallback() {
                            lastPartOfInitialGame()
                        }
                    })
                Log.d(TAG, "initGame.showingScoreHandler.post().")
                showingScoreHandler.post(showScore)
            } else {
                lastPartOfInitialGame()
            }
        }
        getAndSetHighestScore() // a coroutine operation
        ColorBallsApp.isProcessingJob = false
    }

    private fun getAndSetHighestScore() {
        Log.d(TAG, "getAndSetHighestScore")
        viewModelScope.launch(Dispatchers.IO) {
            val db = mPresenter.scoreDatabase()
            val score = db.getHighestScore()
            Log.d(TAG, "getAndSetHighestScore.score = $score")
            db.close()
            setHighestScore(score)
        }
    }

    private fun addScoreInLocalTop10(playerName : String, score : Int) {
        Log.d(TAG, "addScoreInLocalTop10")
        viewModelScope.launch(Dispatchers.IO) {
            val db = mPresenter.scoreDatabase()
            if (db.isInTop10(score)) {
                val scoreModel = Score(playerName = playerName, playerScore = score)
                val rowId = db.addScore(scoreModel)
                Log.d(TAG, "addScoreInLocalTop10.rowId = $rowId")
                db.deleteAllAfterTop10()
            }
            db.close()
            // get the highest score after adding
            getAndSetHighestScore()
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
                    BundleCompat.getParcelable(it, Constants.GAME_PROP_TAG,
                        GameProp::class.java)
                else it.getParcelable(Constants.GAME_PROP_TAG)
            gridData =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GRID_DATA_TAG,
                        GridData::class.java)
                else it.getParcelable(Constants.GRID_DATA_TAG)
        }
        isNewGame = true
        if (gameProp != null && gridData != null) {
            Log.d(TAG, "restoreState.gridData!! = $gridData")
            gridData.apply {
                for (i in 0 until Constants.ROW_COUNTS) {
                    for (j in 0 until Constants.ROW_COUNTS) {
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

    private fun lastPartOfInitialGame() {
        if (mGameProp.isBallBouncing) {
            drawBouncyBall(mGameProp.bouncyBallIndexI, mGameProp.bouncyBallIndexJ)
        }
        if (mGameProp.isShowingNewGameDialog) {
            Log.d(TAG, "lastPartOfInitialGame.newGame()")
            newGame()
        }
        if (mGameProp.isShowingQuitGameDialog) {
            Log.d(TAG, "lastPartOfInitialGame.show quitGame()")
            quitGame()
        }
        if (mGameProp.isShowingSureSaveDialog) {
            Log.d(TAG, "lastPartOfInitialGame.saveGame()")
            saveGame()
        }
        if (mGameProp.isShowingSureLoadDialog) {
            Log.d(TAG, "lastPartOfInitialGame.loadGame()")
            loadGame()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        mGameProp.isShowingLoadingMessage = ColorBallsApp.isShowingLoadingMessage
        // mGameProp.isProcessingJob = ColorBallsApp.isProcessingJob
        Log.d(TAG, "onSaveInstanceState.mGridData = $mGridData")
        outState.putParcelable(Constants.GAME_PROP_TAG, mGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, mGridData)
    }

    fun hasSound(): Boolean {
        println("Presenter.hasSound.mGameProp = $mGameProp")
        println("Presenter.hasSound.hasSound = ${mGameProp.hasSound}")
        return mGameProp.hasSound
    }

    fun setHasSound(hasSound: Boolean) {
        println("Presenter.setHasSound.mGameProp = $mGameProp")
        println("Presenter.setHasSound.hasSound = $hasSound")
        mGameProp.hasSound = hasSound
    }

    fun isEasyLevel(): Boolean {
        return mGameProp.isEasyLevel
    }

    fun setEasyLevel(yn: Boolean) {
        mGameProp.isEasyLevel = yn
        mGridData.setNumOfColorsUsed(if (yn) Constants.NUM_EASY else Constants.NUM_DIFFICULT)
    }

    fun hasNextBall(): Boolean {
        return mGameProp.hasNextBall
    }

    fun setHasNextBall(hasNextBall: Boolean, isNextBalls: Boolean) {
        mGameProp.hasNextBall = hasNextBall
        if (isNextBalls) {
            displayNextBallsView()
        }
    }

    private fun setShowingNewGameDialog(showingNewGameDialog: Boolean) {
        mGameProp.isShowingNewGameDialog = showingNewGameDialog
    }

    private fun setShowingQuitGameDialog(showingQuitGameDialog: Boolean) {
        mGameProp.isShowingQuitGameDialog = showingQuitGameDialog
    }

    fun undoTheLast() {
        if (!mGameProp.undoEnable) {
            return
        }
        ColorBallsApp.isProcessingJob = true // started undoing
        mGridData.undoTheLast()
        stopBouncyAnimation()
        mGameProp.isBallBouncing = false
        mGameProp.bouncyBallIndexI = -1
        mGameProp.bouncyBallIndexJ = -1
        // restore the screen
        displayGameView()
        mGameProp.currentScore = mGameProp.undoScore
        setCurrentScore(mGameProp.currentScore)
        mGameProp.undoEnable = false
        ColorBallsApp.isProcessingJob = false // finished
    }

    fun setSaveScoreAlertDialogState(state: Boolean) {
        ColorBallsApp.isProcessingJob = state
        if (mGameAction == Constants.IS_CREATING_GAME) {
            // new game
            setShowingNewGameDialog(state)
        } else {
            // quit game
            setShowingQuitGameDialog(state)
        }
    }

    fun setShowingSureSaveDialog(isShowingSureSaveDialog: Boolean) {
        mGameProp.isShowingSureSaveDialog = isShowingSureSaveDialog
    }

    fun setShowingSureLoadDialog(isShowingSureLoadDialog: Boolean) {
        mGameProp.isShowingSureLoadDialog = isShowingSureLoadDialog
    }

    fun saveScore(playerName: String) {
        Log.d(TAG, "saveScore")
        // use thread to add a record to remote database
        val restThread: Thread = object : Thread() {
            override fun run() {
                try {
                    // ASP.NET Core
                    val jsonObject = JSONObject()
                    jsonObject.put("PlayerName", playerName)
                    jsonObject.put("Score", mGameProp.currentScore)
                    jsonObject.put("GameId", Constants.GAME_ID)
                    PlayerRecordRest.addOneRecord(jsonObject)
                    Log.d(TAG, "saveScore.Succeeded to add one record to remote.")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Log.d(TAG, "saveScore.Failed to add one record to remote.")
                }
            }
        }
        restThread.start()

        // save to local storage
        addScoreInLocalTop10(playerName, mGameProp.currentScore)
    }

    fun newGame() {
        // creating a new game
        stopBouncyAnimation()
        mGameAction = Constants.IS_CREATING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    fun quitGame() {
        // quiting the game
        stopBouncyAnimation()
        mGameAction = Constants.IS_QUITING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    fun saveGame() {
        Log.d(TAG, "saveGame")
        setSaveGameText(sureToSaveGameStr)
    }

    fun loadGame() {
        Log.d(TAG, "loadGame")
        setLoadGameText(sureToLoadGameStr)
    }

    fun readNumberOfSaved(): Int {
        Log.d(TAG, "readNumberOfSaved")
        var numOfSaved = 0
        try {
            val fiStream = mPresenter.fileInputStream(NUM_SAVE_FILENAME)
            numOfSaved = fiStream.read()
            fiStream.close()
        } catch (ex: IOException) {
            Log.d(TAG, "readNumberOfSaved.IOException")
            ex.printStackTrace()
        }
        return numOfSaved
    }

    fun startSavingGame(num: Int): Boolean {
        Log.d(TAG, "startSavingGame")
        ColorBallsApp.isProcessingJob = true
        setScreenMessage(savingGameStr)

        var numOfSaved = num
        var succeeded = true
        try {
            var foStream = mPresenter.fileOutputStream(SAVE_FILENAME)
            // save settings
            Log.d(TAG, "startSavingGame.hasSound = " + mGameProp.hasSound)
            if (mGameProp.hasSound) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.isEasyLevel = " + mGameProp.isEasyLevel)
            if (mGameProp.isEasyLevel) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.hasNextBall = " + mGameProp.hasNextBall)
            if (mGameProp.hasNextBall) foStream.write(1) else foStream.write(0)
            // save next balls
            // foStream.write(gridData.ballNumOneTime);
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
            foStream.write(Constants.BALL_NUM_ONE_TIME)
            for ((_, value) in mGridData.getNextCellIndices()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = $value")
                foStream.write(value)
            }
            var sz = mGridData.getNextCellIndices().size
            for (i in sz until Constants.NUM_DIFFICULT) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = " + 0)
                foStream.write(0)
            }
            Log.d(TAG, "startSavingGame.getNextCellIndices.size() = $sz")
            foStream.write(sz)
            for ((key) in mGridData.getNextCellIndices()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().x = " + key.x)
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().y = " + key.y)
                foStream.write(key.x)
                foStream.write(key.y)
            }
            Log.d(
                TAG,"startSavingGame.getUndoNextCellIndices().size() = "
                    + mGridData.getUndoNextCellIndices().size)
            foStream.write(mGridData.getUndoNextCellIndices().size)
            for ((key) in mGridData.getUndoNextCellIndices()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().x = " + key.x)
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().y = " + key.y)
                foStream.write(key.x)
                foStream.write(key.y)
            }
            // save values on 9x9 grid
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.ROW_COUNTS) {
                    Log.d(
                        TAG,"startSavingGame.gridData.getCellValue(i, j) = "
                            + mGridData.getCellValue(i, j))
                    foStream.write(mGridData.getCellValue(i, j))
                }
            }
            // save current score
            val scoreByte = ByteBuffer.allocate(4).putInt(mGameProp.currentScore).array()
            Log.d(TAG, "startSavingGame.scoreByte = $scoreByte")
            foStream.write(scoreByte)
            // save undoEnable
            Log.d(TAG, "startSavingGame.isUndoEnable = " + mGameProp.undoEnable)
            // can undo or no undo
            if (mGameProp.undoEnable) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
            foStream.write(Constants.BALL_NUM_ONE_TIME)
            // save undoNextBalls
            for ((_, value) in mGridData.getUndoNextCellIndices()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = $value")
                foStream.write(value)
            }
            sz = mGridData.getUndoNextCellIndices().size
            for (i in sz until Constants.NUM_DIFFICULT) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = " + 0)
                foStream.write(0)
            }
            // save backupCells
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.ROW_COUNTS) {
                    Log.d(
                        TAG,"startSavingGame.gridData.getBackupCells()[i][j] = "
                            + mGridData.getBackupCells()[i][j])
                    foStream.write(mGridData.getBackupCells()[i][j])
                }
            }
            val undoScoreByte = ByteBuffer.allocate(4).putInt(mGameProp.undoScore).array()
            Log.d(TAG, "startSavingGame.undoScoreByte = $undoScoreByte")
            foStream.write(undoScoreByte)
            foStream.close()
            // end of writing
            numOfSaved++
            // save numOfSaved back to file (ColorBallsApp.NumOfSavedGameFileName)
            Log.d(TAG, "startSavingGame.creating fileOutputStream.")
            foStream = mPresenter.fileOutputStream(NUM_SAVE_FILENAME)
            foStream.write(numOfSaved)
            foStream.close()
            Log.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
            Log.d(TAG, "startSavingGame.Failed.")
        }

        ColorBallsApp.isProcessingJob = false
        // presentView.dismissShowMessageOnScreen()
        setScreenMessage("")
        Log.d(TAG, "startSavingGame.Finished")
        return succeeded
    }

    fun startLoadingGame(): Boolean {
        Log.d(TAG, "startLoadingGame")
        ColorBallsApp.isProcessingJob = true
        setScreenMessage(loadingGameStr)

        var succeeded = true
        val hasSound: Boolean
        val isEasyLevel: Boolean
        val hasNextBall: Boolean
        var ballNumOneTime: Int
        val nextBalls = IntArray(Constants.NUM_DIFFICULT)
        val gameCells = Array(Constants.ROW_COUNTS) {
            IntArray(Constants.ROW_COUNTS) }
        val cScore: Int
        val isUndoEnable: Boolean
        val undoNextBalls = IntArray(Constants.NUM_DIFFICULT)
        val backupCells = Array(Constants.ROW_COUNTS) {
            IntArray(Constants.ROW_COUNTS) }
        var unScore = mGameProp.undoScore
        try {
            // clear nextCellIndices and undoNextCellIndices
            mGridData.setNextCellIndices(HashMap())
            mGridData.setUndoNextCellIndices(HashMap())
            Log.d(TAG, "startLoadingGame.Creating inputFile")
            // File inputFile = new File(mContext.getFilesDir(), savedGameFileName);
            // long fileSizeInByte = inputFile.length();
            // Log.d(TAG, "startLoadingGame.File size = " + fileSizeInByte);
            // FileInputStream fiStream = new FileInputStream(inputFile);
            val fiStream = mPresenter.fileInputStream(SAVE_FILENAME)
            Log.d(TAG, "startLoadingGame.available() = " + fiStream.available())
            Log.d(TAG, "startLoadingGame.getChannel().size() = " + fiStream.channel.size())
            // game sound
            var bValue = fiStream.read()
            hasSound = bValue == 1
            Log.d(TAG, "startLoadingGame.hasSound = $hasSound")
            // game level
            bValue = fiStream.read()
            isEasyLevel = bValue == 1
            Log.d(TAG, "startLoadingGame.isEasyLevel = $isEasyLevel")
            // next balls
            bValue = fiStream.read()
            hasNextBall = bValue == 1
            Log.d(TAG, "startLoadingGame.hasNextBall = $hasNextBall")
            ballNumOneTime = fiStream.read()
            Log.i(TAG, "startLoadingGame.ballNumOneTime = $ballNumOneTime")
            for (i in 0 until Constants.NUM_DIFFICULT) {
                nextBalls[i] = fiStream.read()
                Log.d(TAG, "startLoadingGame.nextCellIndices.cell.getColor() = " + nextBalls[i])
            }
            val nextCellIndicesSize = fiStream.read()
            Log.d(TAG, "startLoadingGame.getNextCellIndices.size() = $nextCellIndicesSize")
            for (i in 0 until nextCellIndicesSize) {
                val x = fiStream.read()
                val y = fiStream.read()
                Log.d(TAG, "startLoadingGame.nextCellIndices.getKey().x = $x")
                Log.d(TAG, "startLoadingGame.nextCellIndices.getKey().y = $y")
                mGridData.addNextCellIndices(Point(x, y))
            }
            val undoNextCellIndicesSize = fiStream.read()
            Log.d(
                TAG,"startLoadingGame.getUndoNextCellIndices.size() = " +
                    "$undoNextCellIndicesSize")
            for (i in 0 until undoNextCellIndicesSize) {
                val x = fiStream.read()
                val y = fiStream.read()
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.getKey().x = $x")
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.geyKey().y = $y")
                mGridData.addUndoNextCellIndices(Point(x, y))
            }
            // load values on 9x9 grid
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.ROW_COUNTS) {
                    gameCells[i][j] = fiStream.read()
                    Log.d(TAG, "startLoadingGame.gridData.getCellValue(i, j) = " + gameCells[i][j])
                }
            }
            // reading current score
            val scoreByte = ByteArray(4)
            fiStream.read(scoreByte)
            Log.d(TAG, "startLoadingGame.scoreByte = $scoreByte")
            cScore = ByteBuffer.wrap(scoreByte).getInt()
            // reading undoEnable
            bValue = fiStream.read()
            isUndoEnable = bValue == 1
            Log.d(TAG, "startLoadingGame.isUndoEnable = $isUndoEnable")
            if (isUndoEnable) {
                ballNumOneTime = fiStream.read()
                Log.d(TAG, "startLoadingGame.ballNumOneTime = $ballNumOneTime")
                for (i in 0 until Constants.NUM_DIFFICULT) {
                    undoNextBalls[i] = fiStream.read()
                    Log.d(
                        TAG,"startLoadingGame.undoNextCellIndices.getValue() = "
                            + undoNextBalls[i])
                }
                // save backupCells
                for (i in 0 until Constants.ROW_COUNTS) {
                    for (j in 0 until Constants.ROW_COUNTS) {
                        backupCells[i][j] = fiStream.read()
                        Log.d(
                            TAG,"startLoadingGame.gridData.getBackupCells()[i][j] = "
                                + backupCells[i][j])
                    }
                }
                val undoScoreByte = ByteArray(4)
                fiStream.read(undoScoreByte)
                Log.d(TAG, "startLoadingGame.undoScoreByte = $undoScoreByte")
                unScore = ByteBuffer.wrap(undoScoreByte).getInt()
            }
            fiStream.close()
            // refresh Main UI with loaded data
            setHasSound(hasSound)
            setEasyLevel(isEasyLevel)
            setHasNextBall(hasNextBall, false)
            var kk = 0
            for (entry in mGridData.getNextCellIndices().entries) {
                entry.setValue(nextBalls[kk++])
            }
            mGridData.setCellValues(gameCells)
            mGameProp.currentScore = cScore
            mGameProp.undoEnable = isUndoEnable
            kk = 0
            for (entry in mGridData.getUndoNextCellIndices().entries) {
                entry.setValue(undoNextBalls[kk++])
            }
            mGridData.setBackupCells(backupCells)
            mGameProp.undoScore = unScore
            // start update UI
            setCurrentScore(mGameProp.currentScore)
            Log.d(TAG, "startLoadingGame.starting displayGameView().")
            displayGameView()
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        ColorBallsApp.isProcessingJob = false
        // presentView.dismissShowMessageOnScreen()
        setScreenMessage("")

        return succeeded
    }

    fun release() {
        stopBouncyAnimation()
        showingScoreHandler.removeCallbacksAndMessages(null)
        movingBallHandler.removeCallbacksAndMessages(null)
        soundPool.release()
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
        val numBalls = intArrayOf(0, 0, 0, 0, 0, 0)
        for (point in linkedLine) {
            when (mGridData.getCellValue(point.x, point.y)) {
                Constants.COLOR_RED -> numBalls[0]++
                Constants.COLOR_GREEN -> numBalls[1]++
                Constants.COLOR_BLUE -> numBalls[2]++
                Constants.COLOR_MAGENTA -> numBalls[3]++
                Constants.COLOR_YELLOW -> numBalls[4]++
                Constants.COLOR_CYAN -> numBalls[5]++
                else -> {}
            }
        }
        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*2 + ... + (n-5)*2
        val minScore = 5
        var totalScore = 0
        for (numBall in numBalls) {
            if (numBall >= 5) {
                var score = minScore
                val extraBalls = numBall - minScore
                if (extraBalls > 0) {
                    // greater than 5 balls
                    val rate = 2
                    for (i in 1..extraBalls) {
                        // rate = 2;   // added on 2018-10-02
                        score += i * rate
                    }
                }
                totalScore += score
            }
        }
        if (!mGameProp.isEasyLevel) {
            // difficult level
            totalScore *= 2 // double of easy level
        }

        return totalScore
    }

    private fun drawBouncyBall(i: Int, j: Int) {
        val color = mGridData.getCellValue(i, j)
        Log.d(TAG, "drawBouncyBall.($i, $j), color = $color")
        var whichBall= WhichBall.BALL
        object : Runnable {
            override fun run() {
                if (whichBall == WhichBall.BALL) drawBall(i, j, color)
                else drawOval(i, j, color)
                whichBall = if (whichBall == WhichBall.BALL) WhichBall.OVAL_BALL
                else WhichBall.BALL
                bouncyBallHandler.postDelayed(this, 200)
            }
        }.also { bouncyBallHandler.post(it) }
    }

    private fun stopBouncyAnimation() {
        bouncyBallHandler.removeCallbacksAndMessages(null)
    }

    private fun drawBall(i: Int, j: Int, color: Int) {
        Log.d(TAG, "drawBall.($i, $j), color = $color")
        gridDataArray[i][j].value = ColorBallInfo(color, WhichBall.BALL)
    }

    private fun drawOval(i: Int, j: Int, color: Int) {
        Log.d(TAG, "drawOval.($i, $j), color = $color")
        gridDataArray[i][j].value = ColorBallInfo(color, WhichBall.OVAL_BALL)
    }

    private fun drawNextBall(i: Int, j: Int, color: Int) {
        Log.d(TAG, "drawNextBall.($i, $j), color = $color")
        val trueColor = if (mGameProp.hasNextBall) color else 0
        Log.d(TAG, "drawNextBall.($i, $j), trueColor = $trueColor")
        gridDataArray[i][j].value = ColorBallInfo(trueColor, WhichBall.NEXT_BALL)
    }

    private fun displayNextBallsView() {
        // display the view of next balls
        Log.d(TAG, "displayNextBallsView")
        try {
            for ((key, value) in mGridData.getNextCellIndices()) {
                Log.d(TAG, "displayNextBallsView.color = $value")
                drawNextBall(key.x, key.y, value)
            }
        } catch (ex: Exception) {
            Log.d(TAG, "displayNextBallsView.Exception: ")
            ex.printStackTrace()
        }
    }

    private fun displayNextColorBalls() {
        if (mGridData.randCells() == 0) {
            // no vacant, so game over
            gameOver()
            return
        }
        //   display the balls on the nextBallsView
        displayNextBallsView()
    }

    private fun clearCell(i: Int, j: Int) {
        mGridData.setCellValue(i, j, 0)
    }

    private fun displayGridDataNextCells() {
        Log.d(TAG, "displayGridDataNextCells")
        var n1: Int
        var n2: Int
        var hasMoreFive = false
        val linkedPoint = HashSet<Point>()
        for ((key, value) in mGridData.getNextCellIndices()) {
            n1 = key.x
            n2 = key.y
            Log.d(TAG, "displayGridDataNextCells.($n1, $n2), color = $value")
            mGridData.setCellValue(n1, n2, value)
            drawBall(n1, n2, mGridData.getCellValue(n1, n2))
            if (mGridData.checkMoreThanFive(n1, n2)) {
                hasMoreFive = true
                for (point in mGridData.getLightLine()) {
                    if (!linkedPoint.contains(point)) {
                        linkedPoint.add(Point(point))
                    }
                }
            }
        }

        if (hasMoreFive) {
            mGridData.setLightLine(linkedPoint) // added on 2020-07-13
            mGameProp.lastGotScore = calculateScore(mGridData.getLightLine())
            mGameProp.undoScore = mGameProp.currentScore
            mGameProp.currentScore += mGameProp.lastGotScore
            setCurrentScore(mGameProp.currentScore)
            val showScore = ShowScore(
                mGridData.getLightLine(), mGameProp.lastGotScore,
                true, object : ShowScoreCallback {
                    override fun sCallback() {
                        Log.d(TAG, "ShowScoreCallback.sCallback.Do nothing.")
                    }
                })
            Log.d(TAG, "displayGridDataNextCells.post(showScore)")
            showingScoreHandler.post(showScore)
        } else {
            displayNextColorBalls()
        }
    }

    private fun displayGameGridView() {
        // display the 9 x 9 game view
        Log.d(TAG, "displayGameGridView")
        try {
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.ROW_COUNTS) {
                    val color = mGridData.getCellValue(i, j)
                    Log.d(TAG, "displayGameGridView.($i, $j), color = $color")
                    drawBall(i, j, color)
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.d(TAG, "displayGameGridView.Exception: ")
            ex.printStackTrace()
        }
    }

    private fun displayGameView() {
        // display the 9 x 9 game view
        Log.d(TAG, "displayGameView")
        displayGameGridView()
        // display the view of next balls
        displayNextBallsView()
    }

    private fun drawBallAlongPath() {
        mGameProp.isBallMoving = true
        ColorBallsApp.isProcessingJob = true
        val sizeOfPath = mGridData.getPathPoint().size
        if (sizeOfPath == 0) {
            Log.w(TAG, "drawBallAlongPath.sizeOfPathPoint == 0")
            return
        }
        val beginI = mGridData.getPathPoint()[sizeOfPath - 1].x
        val beginJ = mGridData.getPathPoint()[sizeOfPath - 1].y
        Log.d(TAG, "drawBallAlongPath.beginI = $beginI, beginJ = $beginJ")
        val targetI = mGridData.getPathPoint()[0].x // the target point
        val targetJ = mGridData.getPathPoint()[0].y // the target point
        Log.d(TAG, "drawBallAlongPath.targetI = $targetI, targetJ = $targetJ")
        val color = mGridData.getCellValue(beginI, beginJ)
        Log.d(TAG, "drawBallAlongPath.color = $color")
        mGridData.lastBall = color  // last ball clicked

        val tempList = ArrayList(mGridData.getPathPoint())
        val runnablePath: Runnable = object : Runnable {
            var ballYN: Boolean = true
            var countDown: Int = tempList.size * 2 - 1
            @Synchronized
            override fun run() {
                movingBallHandler.removeCallbacksAndMessages(null)
                if (countDown >= 2) {   // eliminate start point
                    val i = countDown / 2
                    drawBall(tempList[i].x, tempList[i].y, if (ballYN) color else 0)
                    ballYN = !ballYN
                    countDown--
                    movingBallHandler.postDelayed(this, 20)
                } else {
                    clearCell(beginI, beginJ) // blank the original cell. Added on 2020-09-16
                    mGridData.setCellValue(targetI, targetJ, color)
                    drawBall(targetI, targetJ, color)
                    //  check if there are more than five balls
                    //  with same color connected together
                    if (mGridData.checkMoreThanFive(targetI, targetJ)) {
                        mGameProp.lastGotScore = calculateScore(mGridData.getLightLine())
                        mGameProp.undoScore = mGameProp.currentScore
                        mGameProp.currentScore += mGameProp.lastGotScore
                        setCurrentScore(mGameProp.currentScore)
                        Log.d(TAG, "drawBallAlongPath.showScore")
                        val showScore = ShowScore(
                            mGridData.getLightLine(), mGameProp.lastGotScore,
                            false, object : ShowScoreCallback {
                                override fun sCallback() {
                                    Log.d(TAG, "drawBallAlongPath.ShowScoreCallback.sCallback")
                                    mGameProp.isBallMoving = false
                                    Log.d(TAG, "drawBallAlongPath.run().sCallback finished.")
                                }
                            })
                        Log.d(TAG, "drawBallAlongPath.showingScoreHandler.post")
                        showingScoreHandler.post(showScore)
                    } else {
                        mGridData.regenerateNextCellIndices(Point(targetI, targetJ))
                        Log.d(TAG, "drawBallAlongPath.run().displayGridDataNextCells")
                        displayGridDataNextCells() // has a problem
                        Log.d(TAG, "drawBallAlongPath.run() finished.")
                        mGameProp.isBallMoving = false
                        ColorBallsApp.isProcessingJob = false
                    }
                }
            }
        }
        Log.d(TAG, "drawBallAlongPath.movingBallHandler.post")
        movingBallHandler.post(runnablePath)
    }

    private inner class ShowScore(
        linkedPoint: HashSet<Point>,
        val lastGotScore: Int, val isNextBalls: Boolean,
        val callback: ShowScoreCallback
    ): Runnable {
        private var pointSet: HashSet<Point>
        private var mCounter = 0
        init {
            Log.d(TAG, "ShowScore")
            pointSet = HashSet(linkedPoint)
            mGameProp.isShowNextBallsAfterBlinking = isNextBalls
            mGameProp.isShowingScoreMessage = true
            ColorBallsApp.isProcessingJob = true
        }

        @Synchronized
        private fun onProgressUpdate(status: Int) {
            when (status) {
                0 -> for (item in pointSet) {
                    drawBall(item.x, item.y, mGridData.getCellValue(item.x, item.y))
                }
                1 -> for (item in pointSet) {
                    drawOval(item.x, item.y, mGridData.getCellValue(item.x, item.y))
                }
                2 -> {}
                3 -> {
                    setScreenMessage(lastGotScore.toString())
                    for (item in pointSet) {
                        clearCell(item.x, item.y)
                        drawBall(item.x, item.y, mGridData.getCellValue(item.x, item.y))
                    }
                    if (isNextBalls) {
                        Log.d(TAG, "ShowScore.onProgressUpdate.displayNextColorBalls")
                        displayNextColorBalls()
                    } else {
                        Log.d(TAG, "ShowScore.onProgressUpdate.displayNextBallsView")
                        displayNextBallsView()
                    }
                }
                4 -> {
                    Log.d(TAG, "ShowScore.onProgressUpdate.dismissShowMessageOnScreen.")
                    setScreenMessage("")
                    mGameProp.isShowingScoreMessage = false
                    ColorBallsApp.isProcessingJob = false
                }
                else -> {}
            }
        }

        @Synchronized
        override fun run() {
            val twinkleCountDown = 5
            mCounter++
            Log.d(TAG, "ShowScore.run().mCounter = $mCounter")
            showingScoreHandler.removeCallbacksAndMessages(null)
            if (mCounter <= twinkleCountDown) {
                val md = mCounter % 2 // modulus
                onProgressUpdate(md)
                showingScoreHandler.postDelayed(this, 100)
            } else {
                if (mCounter == twinkleCountDown + 1) {
                    onProgressUpdate(3) // show score
                    showingScoreHandler.postDelayed(this, 500)
                } else {
                    onProgressUpdate(4) // dismiss showing message
                    callback.sCallback()
                }
            }
        }
    }

    companion object {
        private const val TAG = "ColorBallViewModel"
        private const val NUM_SAVE_FILENAME = "NumSavedGame"
        private const val SAVE_FILENAME = "SavedGame"
    }
}