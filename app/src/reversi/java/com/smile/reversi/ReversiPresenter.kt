package com.smile.reversi

import com.smile.colorballs_main.presenters.BasePresenter

class ReversiPresenter(private val presentView: ReversiPresentView)
    : BasePresenter(presentView) {
    val createNewGameStr = presentView.getCreateNewGameStr()
}