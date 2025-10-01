package com.smile.colorballs.interfaces

import com.smile.colorballs.roomdatabase.ScoreDatabase
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.FileInputStream
import java.io.FileOutputStream

interface BasePresentView {
    fun getLoadingStr(): String
    fun geSavingGameStr(): String
    fun getLoadingGameStr(): String
    fun getSureToSaveGameStr(): String
    fun getSureToLoadGameStr(): String
    fun getSaveScoreStr(): String
    fun soundPool() : SoundPoolUtil
    fun getRoomDatabase(): ScoreDatabase
    fun fileInputStream(fileName : String) : FileInputStream
    fun fileOutputStream(fileName : String) : FileOutputStream
}