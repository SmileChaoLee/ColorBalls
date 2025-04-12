package com.smile.colorballs.interfaces

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.FileInputStream
import java.io.FileOutputStream

interface PresentView {
    fun contextResources() : Resources
    fun soundPool() : SoundPoolUtil
    fun highestScore() : Int
    fun addScoreInLocalTop10(playerName : String, score : Int)
    fun fileInputStream(fileName : String) : FileInputStream
    fun fileOutputStream(fileName : String) : FileOutputStream
    fun compatDrawable(id : Int) : Drawable?
    fun bitmapToDrawable(bm : Bitmap, with : Int, height : Int) : Drawable?
    fun getImageViewById(id : Int) : ImageView
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