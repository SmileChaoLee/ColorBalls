package com.smile.colorballs.presenters
import com.smile.colorballs.interfaces.BasePresentView
import com.smile.smilelibraries.utilities.SoundPoolUtil

open class BasePresenter(private val presentView: BasePresentView) {
    val medalImageIds = presentView.getMedalImageIds()
    val loadingStr = presentView.getLoadingStr()
    val savingGameStr = presentView.geSavingGameStr()
    val loadingGameStr =  presentView.getLoadingGameStr()
    val sureToSaveGameStr = presentView.getSureToSaveGameStr()
    val sureToLoadGameStr = presentView.getSureToLoadGameStr()
    val saveScoreStr = presentView. getSaveScoreStr()
    val soundPool: SoundPoolUtil = presentView.soundPool()
    fun scoreDatabase() = presentView.getRoomDatabase()
    fun fileInputStream(filename: String) = presentView.fileInputStream(filename)
    fun fileOutputStream(filename: String) = presentView.fileOutputStream(filename)
}