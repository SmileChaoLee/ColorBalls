package com.smile.colorballs.ballsremover.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameProp(
    var undoEnable : Boolean = false,
    var currentScore : Int = 0,
    var undoScore : Int = 0,
    var lastGotScore : Int = 0) : Parcelable {

    fun initialize() {
        undoEnable = false
        currentScore = 0
        undoScore = 0
        lastGotScore = 0
    }
}