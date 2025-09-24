package com.smile.colorballs.ballsremover.presenters

import com.smile.colorballs.ballsremover.interfaces.BallsRemoverPresentView
import com.smile.smilelibraries.utilities.SoundPoolUtil

class BallsRemoverPresenter(private val presentView: BallsRemoverPresentView) {
    val medalImageIds = presentView.getMedalImageIds()
    val createNewGameStr = presentView.getCreateNewGameStr()
    val loadingStr = presentView.getLoadingStr()
    val savingGameStr = presentView.geSavingGameStr()
    val loadingGameStr =  presentView.getLoadingGameStr()
    val sureToSaveGameStr = presentView.getSureToSaveGameStr()
    val sureToLoadGameStr = presentView.getSureToLoadGameStr()
    val saveScoreStr = presentView. getSaveScoreStr()
    val soundPool: SoundPoolUtil = presentView.soundPool()
    fun highestScore() = presentView.getHighestScore()
    fun fileInputStream(filename: String) = presentView.fileInputStream(filename)
    fun fileOutputStream(filename: String) = presentView.fileOutputStream(filename)
    fun addScoreInLocalTop10(playerName: String, score: Int) =
        presentView.addScoreInLocalTop10(playerName, score)
}