package com.smile.colorballs.presenters
import com.smile.colorballs.interfaces.CBallPresentView

class CBallPresenter(private val presentView: CBallPresentView)
    : BasePresenter(presentView) {
    val gameOverStr = presentView.getGameOverStr()
}