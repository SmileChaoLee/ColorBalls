package com.smile.colorballs.presenters
import com.smile.colorballs.interfaces.PresentView
import com.smile.smilelibraries.utilities.SoundPoolUtil

class Presenter(private val presentView: PresentView) {
    val medalImageIds = presentView.getMedalImageIds()
    val loadingStr = presentView.getLoadingStr()
    val savingGameStr = presentView.geSavingGameStr()
    val loadingGameStr =  presentView.getLoadingGameStr()
    val sureToSaveGameStr = presentView.getSureToSaveGameStr()
    val sureToLoadGameStr = presentView.getSureToLoadGameStr()
    val gameOverStr = presentView.getGameOverStr()
    val saveScoreStr = presentView. getSaveScoreStr()
    val soundPool: SoundPoolUtil = presentView.soundPool()
    fun scoreDatabase() = presentView.getRoomDatabase()
    fun fileInputStream(filename: String) = presentView.fileInputStream(filename)
    fun fileOutputStream(filename: String) = presentView.fileOutputStream(filename)
}