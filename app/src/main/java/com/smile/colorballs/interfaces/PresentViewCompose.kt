package com.smile.colorballs.interfaces

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.runtime.MutableState
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.FileInputStream
import java.io.FileOutputStream

interface PresentViewCompose {
    fun contextResources() : Resources
    fun soundPool() : SoundPoolUtil
    fun highestScore() : Int
    fun addScoreInLocalTop10(playerName : String, score : Int)
    fun fileInputStream(fileName : String) : FileInputStream
    fun fileOutputStream(fileName : String) : FileOutputStream
    fun compatDrawable(id : Int) : Drawable?
    fun bitmapToDrawable(bm : Bitmap, width : Int, height : Int) : Drawable?
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
    fun setArrayDrawable(i: Int, j: Int, drawable: Drawable?, isReSize: Boolean)
}