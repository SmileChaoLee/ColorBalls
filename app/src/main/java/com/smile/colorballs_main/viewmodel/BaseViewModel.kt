package com.smile.colorballs_main.viewmodel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smile.colorballs_main.models.GridData
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichBall
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.models.ColorBallInfo
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.presenters.BasePresenter
import com.smile.colorballs_main.roomdatabase.Score
import com.smile.colorballs_main.tools.GameUtil
import com.smile.colorballs_main.tools.LogUtil
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.utilities.SoundPoolUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

abstract class BaseViewModel(
    private var basePresenter: BasePresenter): ViewModel() {

    abstract fun initGame(bundle: Bundle?)
    abstract fun cellClickListener(i: Int, j: Int)
    abstract fun startSavingGame(): Boolean
    abstract fun startLoadingGame(): Boolean
    abstract fun saveInstanceState(outState: Bundle)
    abstract fun newGame()
    abstract fun undoTheLast()

    interface ShowScoreCallback {
        fun sCallback()
    }

    private val showingScoreHandler = Handler(Looper.getMainLooper())
    var loadingStr = ""
    var savingGameStr = ""
    var loadingGameStr = ""
    private var sureToSaveGameStr = ""
    private var sureToLoadGameStr = ""
    var saveScoreStr = ""
    lateinit var soundPool: SoundPoolUtil

    protected var mGameProp = GameProp()
    protected var mGridData = GridData()
    var mGameAction = Constants.IS_QUITING_GAME
    var rowCounts: Int = 0
        private set(value) {
            if (value != field) field = value
        }
    var colCounts: Int = 0
        private set(value) {
            if (value != field) field = value
        }

    private val currentScore = mutableIntStateOf(0)
    fun getCurrentScore() = currentScore.intValue
    fun setCurrentScore(score: Int) {
        currentScore.intValue = score
    }

    private val highestScore = mutableIntStateOf(0)
    fun getHighestScore() = highestScore.intValue
    fun setHighestScore(score: Int) {
        highestScore.intValue = score
    }

    private val screenMessage = mutableStateOf("")
    fun getScreenMessage() = screenMessage.value
    fun setScreenMessage(msg: String) {
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

    lateinit var gridDataArray: Array<Array<MutableState<ColorBallInfo>>>

    init {
        LogUtil.i(TAG, "BaseViewModel.init")
    }

    fun setPresenter(presenter: BasePresenter) {
        basePresenter = presenter
        setProperties()
    }

    fun setProperties() {
        rowCounts = mGridData.rowCounts
        colCounts = mGridData.colCounts
        loadingStr = basePresenter.loadingStr
        savingGameStr = basePresenter.savingGameStr
        loadingGameStr = basePresenter.loadingGameStr
        sureToSaveGameStr = basePresenter.sureToSaveGameStr
        sureToLoadGameStr = basePresenter.sureToLoadGameStr
        saveScoreStr = basePresenter.saveScoreStr
        soundPool = basePresenter.soundPool
        gridDataArray = Array(rowCounts) {
            Array(colCounts) {
                mutableStateOf(ColorBallInfo())
            }
        }
    }

    fun setWhichGame(whichGame: WhichGame) {
        mGameProp.whichGame = whichGame
    }

    fun getWhichGame(): WhichGame {
        return mGameProp.whichGame
    }

    fun isProcessingJob() = mGameProp.isProcessingJob

    fun getAndSetHighestScore() {
        LogUtil.i(TAG, "getAndSetHighestScore")
        viewModelScope.launch(Dispatchers.IO) {
            val db = basePresenter.scoreDatabase()
            val score = db.getHighestScore()
            LogUtil.d(TAG, "getAndSetHighestScore.score = $score")
            db.close()
            setHighestScore(score)
        }
    }

    private fun addScoreInLocalTop10(playerName : String, score : Int) {
        LogUtil.i(TAG, "addScoreInLocalTop10")
        viewModelScope.launch(Dispatchers.IO) {
            val db = basePresenter.scoreDatabase()
            if (db.isInTop10(score)) {
                val scoreModel = Score(playerName = playerName, playerScore = score)
                val rowId = db.addScore(scoreModel)
                LogUtil.d(TAG, "addScoreInLocalTop10.rowId = $rowId")
                db.deleteAllAfterTop10()
            }
            db.close()
            // get the highest score after adding
            getAndSetHighestScore()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        saveInstanceState(outState)
    }

    fun hasSound(): Boolean {
        return mGameProp.hasSound
    }
    fun setHasSound(hasSound: Boolean) {
        mGameProp.hasSound = hasSound
    }

    fun isEasyLevel(): Boolean {
        LogUtil.d(TAG, "isEasyLevel.easyLevel = ${mGameProp.isEasyLevel}")
        return mGameProp.isEasyLevel
    }
    fun setEasyLevel(easyLevel: Boolean) {
        mGameProp.isEasyLevel = easyLevel
        val num = if (easyLevel) Constants.NUM_BALLS_USED_EASY
        else Constants.NUM_BALLS_USED_DIFF
        mGridData.setNumOfColorsUsed(num)
        LogUtil.d(TAG, "setEasyLevel.easyLevel = ${mGameProp.isEasyLevel}")
    }

    fun hasNext(): Boolean {
        return mGameProp.hasNext
    }

    fun setHasNext(hasNext: Boolean) {
        mGameProp.hasNext = hasNext
    }

    fun saveScore(playerName: String) {
        LogUtil.i(TAG, "saveScore")
        // use thread to add a record to remote database
        val restThread: Thread = object : Thread() {
            override fun run() {
                try {
                    // ASP.NET Core
                    val jsonObject = JSONObject()
                    jsonObject.put("PlayerName", playerName)
                    jsonObject.put("Score", mGameProp.currentScore)
                    jsonObject.put("GameId", GameUtil.getGameId(getWhichGame()))
                    PlayerRecordRest.addOneRecord(jsonObject)
                    LogUtil.d(TAG, "saveScore.Succeeded to add one record to remote.")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    LogUtil.e(TAG, "saveScore.Failed to add one record to remote", ex)
                }
            }
        }
        restThread.start()

        // save to local storage
        addScoreInLocalTop10(playerName, mGameProp.currentScore)
    }

    fun setShowingMessageDialog(isShowingMessage: Boolean) {
        mGameProp.isShowingMessage = isShowingMessage
    }
    fun isShowingMessageDialog() = mGameProp.isShowingMessage

    fun setShowingScoreDialog(isShowingScoreMessage: Boolean) {
        mGameProp.isShowingScoreMessage = isShowingScoreMessage
    }
    fun isShowingScoreDialog() = mGameProp.isShowingScoreMessage

    fun setShowingSureSaveDialog(isShowingSureSaveDialog: Boolean) {
        mGameProp.isShowingSureSaveDialog = isShowingSureSaveDialog
    }
    fun isShowingSureSaveDialog() =  mGameProp.isShowingSureSaveDialog

    fun setShowingSureLoadDialog(isShowingSureLoadDialog: Boolean) {
        mGameProp.isShowingSureLoadDialog = isShowingSureLoadDialog
    }
    fun isShowingSureLoadDialog() = mGameProp.isShowingSureLoadDialog

    fun setShowingNewGameDialog(showingNewGameDialog: Boolean) {
        mGameProp.isShowingNewGameDialog = showingNewGameDialog
    }
    fun isShowingNewGameDialog() = mGameProp.isShowingNewGameDialog

    fun setShowingQuitGameDialog(showingQuitGameDialog: Boolean) {
        mGameProp.isShowingQuitGameDialog = showingQuitGameDialog
    }
    fun isShowingQuitGameDialog() = mGameProp.isShowingQuitGameDialog

    fun setShowingCreateGameDialog(isShowingCreateGameDialog: Boolean) {
        mGameProp.isShowingCreateGameDialog = isShowingCreateGameDialog
    }
    fun isShowingCreateGameDialog() = mGameProp.isShowingCreateGameDialog

    fun lastPartOfInitialGame() {
        if (isShowingNewGameDialog()) {
            LogUtil.i(TAG, "lastPartOfInitialGame.newGame()")
            newGame()
        }
        if (isShowingQuitGameDialog()) {
            LogUtil.i(TAG, "lastPartOfInitialGame.show quitGame()")
            quitGame()
        }
        if (isShowingSureSaveDialog()) {
            LogUtil.i(TAG, "lastPartOfInitialGame.saveGame()")
            saveGame()
        }
        if (isShowingSureLoadDialog()) {
            LogUtil.i(TAG, "lastPartOfInitialGame.loadGame()")
            loadGame()
        }
    }

    fun setSaveScoreAlertDialogState(state: Boolean) {
        // mGameProp.isProcessingJob = state
        if (mGameAction == Constants.IS_CREATING_GAME) {
            // new game
            setShowingNewGameDialog(state)
        } else {
            // quit game
            setShowingQuitGameDialog(state)
        }
        mGameProp.isProcessingJob = false
    }

    fun quitGame() {
        // quiting the game
        LogUtil.i(TAG, "quitGame")
        mGameAction = Constants.IS_QUITING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    fun saveGame() {
        LogUtil.i(TAG, "saveGame")
        setSaveGameText(sureToSaveGameStr)
    }

    fun loadGame() {
        LogUtil.i(TAG, "loadGame")
        setLoadGameText(sureToLoadGameStr)
    }

    open fun release() {
        showingScoreHandler.removeCallbacksAndMessages(null)
        soundPool.release()
    }

    fun gameOver() {
        LogUtil.i(TAG, "gameOver")
        if (hasSound()) {
            soundPool.playSound()
        }
        newGame()
    }

    fun drawBall(i: Int, j: Int, color: Int) {
        LogUtil.d(TAG, "drawBall.($i, $j), color = $color")
        gridDataArray[i][j].value = ColorBallInfo(color, WhichBall.BALL)
    }

    fun drawOval(i: Int, j: Int, color: Int) {
        LogUtil.d(TAG, "drawOval.($i, $j), color = $color")
        gridDataArray[i][j].value = ColorBallInfo(color, WhichBall.OVAL_BALL)
    }

    fun clearCell(i: Int, j: Int) {
        mGridData.setCellValue(i, j, 0)
    }

    fun displayGameGridView() {
        LogUtil.i(TAG, "displayGameGridView")
        try {
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    val color = mGridData.getCellValue(i, j)
                    drawBall(i, j, color)
                }
            }
        } catch (ex: java.lang.Exception) {
            LogUtil.e(TAG, "displayGameGridView.Exception: ", ex)
            ex.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}