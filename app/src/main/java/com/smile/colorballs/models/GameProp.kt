package com.smile.colorballs.models

import android.os.Parcelable
import com.smile.colorballs.constants.WhichGame
import kotlinx.parcelize.Parcelize

@Parcelize
open class GameProp(
    open var whichGame: WhichGame = WhichGame.NO_BARRIER,
    open var undoEnable : Boolean = false,
    open var currentScore : Int = 0,
    open var undoScore : Int = 0,
    open var lastGotScore : Int = 0,
    open var isEasyLevel : Boolean = true,
    open var hasSound : Boolean = true,
    open var hasNext : Boolean = true) : Parcelable {

    open fun initialize(whGame: WhichGame) {
        whichGame = whGame
        undoEnable = false
        currentScore = 0
        undoScore = 0
        lastGotScore = 0
        isEasyLevel = true
        hasSound = true
        hasNext = true
    }
}