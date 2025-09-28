package com.smile.colorballs.ballsremover.models

import android.os.Parcelable
import com.smile.colorballs.constants.WhichGame
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameProp(
    var whichGame: WhichGame = WhichGame.REMOVE_BALLS,
    var undoEnable : Boolean = false,
    var currentScore : Int = 0,
    var undoScore : Int = 0,
    var lastGotScore : Int = 0) : Parcelable {

    fun initialize() {
        whichGame = WhichGame.REMOVE_BALLS
        undoEnable = false
        currentScore = 0
        undoScore = 0
        lastGotScore = 0
    }
}