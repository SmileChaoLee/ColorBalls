package com.smile.colorballs.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class GameProp(
    var isShowingLoadingMessage : Boolean = false,
    var isShowingScoreMessage : Boolean = false,
    var isShowNextBallsAfterBlinking : Boolean = false,
    var isProcessingJob : Boolean = false,
    var isShowingNewGameDialog : Boolean = false,
    var isShowingQuitGameDialog : Boolean = false,
    var isShowingSureSaveDialog : Boolean = false,
    var isShowingSureLoadDialog : Boolean = false,
    var isShowingGameOverDialog : Boolean = false,
    var threadCompleted : BooleanArray =
        booleanArrayOf(true,true,true,true,true,true,true,true,true,true),
    var bouncyBallIndexI : Int = -1,
    var bouncyBallIndexJ : Int = -1,
    var isBallBouncing : Boolean = false,
    var isBallMoving : Boolean = false,
    var undoEnable : Boolean = false,
    var currentScore : Int = 0,
    var undoScore : Int = 0,
    var lastGotScore : Int = 0,
    var isEasyLevel : Boolean = true,
    var hasSound : Boolean = true,
    var hasNextBall : Boolean = true) : Parcelable {
        /*
        constructor() : this(
            false, false,
            false, false,
            false, false,
            false,
            false, false,
            booleanArrayOf(true,true,true,true,true,true,true,true,true,true),
            -1, -1,
            false,false, false,
            0, 0, 0,
            true, true, true)
         */
}