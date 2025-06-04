package com.smile.colorballs.interfaces

import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.FileInputStream
import java.io.FileOutputStream

interface PresentViewCompose {
    fun soundPool() : SoundPoolUtil
    fun getHighestScore() : Int
    fun addScoreInLocalTop10(playerName : String, score : Int)
    fun fileInputStream(fileName : String) : FileInputStream
    fun fileOutputStream(fileName : String) : FileOutputStream
    fun showLoadingStrOnScreen()
    fun showSavingGameStrOnScreen()
    fun showLoadingGameStrOnScreen()
    fun showSaveScoreAlertDialog()
    fun showSaveGameDialog()
    fun showLoadGameDialog()
    fun showGameOverDialog()
}