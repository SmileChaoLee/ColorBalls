package com.smile.ballsremover.presenters

import com.smile.ballsremover.interfaces.BallsRmPresentView
import com.smile.colorballs_main.presenters.BasePresenter

class BallsRmPresenter(private val presentView: BallsRmPresentView)
    : BasePresenter(presentView) {
    val createNewGameStr = presentView.getCreateNewGameStr()
}