package com.smile.colorballs.ballsremover.interfaces

import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.FileInputStream
import java.io.FileOutputStream

interface BallsRemoverPresentView {
    fun getMedalImageIds(): List<Int>
    fun getCreateNewGameStr(): String
    fun getLoadingStr(): String
    fun geSavingGameStr(): String
    fun getLoadingGameStr(): String
    fun getSureToSaveGameStr(): String
    fun getSureToLoadGameStr(): String
    fun getSaveScoreStr(): String
    fun soundPool() : SoundPoolUtil
    fun getHighestScore() : Int
    fun addScoreInLocalTop10(playerName : String, score : Int)
    fun fileInputStream(fileName : String) : FileInputStream
    fun fileOutputStream(fileName : String) : FileOutputStream
}