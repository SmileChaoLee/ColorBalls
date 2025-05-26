package com.smile.colorballs.interfaces

import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.FileInputStream
import java.io.FileOutputStream

interface PresentViewCompose {
    fun soundPool() : SoundPoolUtil
    fun drawBall(i: Int, j: Int, color: Int)
    fun drawOval(i: Int, j: Int, color: Int)
    fun drawNextBall(i: Int, j: Int, color: Int)
    fun addScoreInLocalTop10(playerName : String, score : Int)
    fun fileInputStream(fileName : String) : FileInputStream
    fun fileOutputStream(fileName : String) : FileOutputStream
    fun updateHighestScoreOnUi(highestScore : Int)
    fun updateCurrentScoreOnUi(score : Int)
    fun showMessageOnScreen(message : String)
    fun showLoadingStrOnScreen()
    fun showSavingGameStrOnScreen()
    fun showLoadingGameStrOnScreen()
    fun dismissShowMessageOnScreen()
    fun showSaveScoreAlertDialog(entryPoint : Int, score : Int)
    fun showSaveGameDialog()
    fun showLoadGameDialog()
    fun showGameOverDialog()
}