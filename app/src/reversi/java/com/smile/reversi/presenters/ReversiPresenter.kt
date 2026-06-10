package com.smile.reversi.presenters

import com.smile.colorballs_main.presenters.BasePresenter
import com.smile.reversi.interfaces.ReversiPresentView

class ReversiPresenter(private val presentView: ReversiPresentView)
    : BasePresenter(presentView) {
    val createNewGameStr = presentView.getCreateNewGameStr()
}