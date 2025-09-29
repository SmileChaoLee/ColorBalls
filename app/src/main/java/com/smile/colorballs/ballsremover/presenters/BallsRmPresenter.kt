package com.smile.colorballs.ballsremover.presenters

import com.smile.colorballs.ballsremover.interfaces.BallsRmPresentView
import com.smile.colorballs.presenters.BasePresenter

class BallsRmPresenter(private val presentView: BallsRmPresentView)
    : BasePresenter(presentView) {
    val createNewGameStr = presentView.getCreateNewGameStr()
}