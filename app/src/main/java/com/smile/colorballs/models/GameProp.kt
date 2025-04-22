package com.smile.colorballs.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class GameProp private constructor(
    var gridData: GridData,
    var isShowingLoadingMessage : Boolean,
    var isShowingScoreMessage : Boolean,
    var isShowNextBallsAfterBlinking : Boolean,
    var isProcessingJob : Boolean,
    var isShowingNewGameDialog : Boolean,
    var isShowingQuitGameDialog : Boolean,
    var isShowingSureSaveDialog : Boolean,
    var isShowingWarningSaveGameDialog : Boolean,
    var isShowingSureLoadDialog : Boolean,
    var isShowingGameOverDialog : Boolean,
    var threadCompleted : BooleanArray,
    var bouncyBallIndexI : Int,
    var bouncyBallIndexJ : Int,
    var isBallBouncing : Boolean,
    var isBallMoving : Boolean,
    var undoEnable : Boolean,
    var currentScore : Int,
    var undoScore : Int,
    var lastGotScore : Int,
    var isEasyLevel : Boolean,
    var hasSound : Boolean,
    var hasNextBall : Boolean) : Parcelable {
        constructor(gridData : GridData) : this(gridData,
            false, false,
            false, false,
            false, false,
            false, false,
            false, false,
            booleanArrayOf(true,true,true,true,true,true,true,true,true,true),
            -1, -1,
            false,false, false,
            0, 0, 0,
            true, true, true)
}