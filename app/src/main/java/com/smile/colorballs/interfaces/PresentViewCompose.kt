package com.smile.colorballs.interfaces

import com.smile.colorballs.R
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.FileInputStream
import java.io.FileOutputStream

interface PresentViewCompose {
    fun getMedalImageIds(): List<Int>
    fun getLoadingStr(): String
    fun geSavingGameStr(): String
    fun getLoadingGameStr(): String
    fun getSureToSaveGameStr(): String
    fun getSureToLoadGameStr(): String
    fun getGameOverStr(): String
    fun getSaveScoreStr(): String
    fun soundPool() : SoundPoolUtil
    fun getHighestScore() : Int
    fun addScoreInLocalTop10(playerName : String, score : Int)
    fun fileInputStream(fileName : String) : FileInputStream
    fun fileOutputStream(fileName : String) : FileOutputStream
}