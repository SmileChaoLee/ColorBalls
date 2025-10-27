package com.smile.colorballs_main.models

import android.os.Parcelable
import com.smile.colorballs_main.constants.WhichGame
import kotlinx.parcelize.Parcelize

@Parcelize
class GameProp(
    var whichGame: WhichGame = WhichGame.NO_BARRIER,
    var undoEnable : Boolean = false,
    var currentScore : Int = 0,
    var undoScore : Int = 0,
    var lastGotScore : Int = 0,
    var isEasyLevel : Boolean = true,
    var hasSound : Boolean = true,
    var hasNext : Boolean = true,
    var isProcessingJob : Boolean = false,
    var isShowingMessage : Boolean = false,
    var isShowingScoreMessage : Boolean = false,
    var isShowNextBallsAfterBlinking : Boolean = false,
    var isShowingNewGameDialog : Boolean = false,
    var isShowingQuitGameDialog : Boolean = false,
    var isShowingSureSaveDialog : Boolean = false,
    var isShowingSureLoadDialog : Boolean = false,
    var isShowingCreateGameDialog : Boolean = false,
    var bouncyBallIndexI : Int = -1,
    var bouncyBallIndexJ : Int = -1,
    var isBallBouncing : Boolean = false,
    var isBallMoving : Boolean = false): Parcelable {

    fun initializeKeepSetting(whGame: WhichGame) {
        whichGame = whGame
        undoEnable = false
        currentScore = 0
        undoScore = 0
        lastGotScore = 0
        // isEasyLevel = true
        // hasSound = true
        // hasNext = true
        isProcessingJob = false
        isShowingMessage = false
        isShowingScoreMessage = false
        isShowNextBallsAfterBlinking = false
        isShowingNewGameDialog = false
        isShowingQuitGameDialog = false
        isShowingSureSaveDialog = false
        isShowingSureLoadDialog = false
        isShowingCreateGameDialog = false
        bouncyBallIndexI = -1
        bouncyBallIndexJ = -1
        isBallBouncing = false
        isBallMoving = false
    }
}