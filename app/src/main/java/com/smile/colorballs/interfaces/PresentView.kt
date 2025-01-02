package com.smile.colorballs.interfaces

import android.widget.ImageView

interface PresentView {
    fun getImageViewById(id: Int): ImageView
    fun updateHighestScoreOnUi(highestScore: Int)
    fun updateCurrentScoreOnUi(score: Int)
    fun showMessageOnScreen(message: String)
    fun dismissShowMessageOnScreen()
    fun showSaveScoreAlertDialog(entryPoint: Int, score: Int)
    fun showSaveGameDialog()
    fun showLoadGameDialog()
    fun showGameOverDialog()
}