package com.smile.reversi.presenters

import com.smile.colorballs_main.presenters.BasePresenter
import com.smile.reversi.interfaces.ReversiPresentView

class ReversiPresenter(private val presentView: ReversiPresentView)
    : BasePresenter(presentView) {
    val bluePassStr = presentView.getBluePassStr()
    val redPassStr = presentView.getRedPassStr()
    fun whoWinsMessage(
        redCount: Int,
        blueCount: Int
    ): String {
          return presentView.getWhoWinsMessage(
              redCount = redCount,
              blueCount = blueCount,
          )
    }
}