package com.smile.colorballs.presenters

import com.smile.colorballs.interfaces.CBallPresentView
import com.smile.colorballs_main.presenters.BasePresenter

class CBallPresenter(private val presentView: CBallPresentView)
    : BasePresenter(presentView) {
    val gameOverStr = presentView.getGameOverStr()
}