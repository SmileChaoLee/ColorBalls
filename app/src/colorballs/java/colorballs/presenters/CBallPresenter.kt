package colorballs.presenters

import colorballs.interfaces.CBallPresentView
import com.smile.colorballs_main.presenters.BasePresenter

class CBallPresenter(private val presentView: CBallPresentView)
    : BasePresenter(presentView) {
    val gameOverStr = presentView.getGameOverStr()
}