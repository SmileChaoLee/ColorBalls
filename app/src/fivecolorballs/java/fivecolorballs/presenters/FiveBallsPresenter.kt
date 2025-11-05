package fivecolorballs.presenters

import android.content.res.Configuration
import com.smile.colorballs_main.presenters.BasePresenter
import fivecolorballs.interfaces.FiveBallsPresentView

class FiveBallsPresenter(
    presentView: FiveBallsPresentView,
    orientation: Int)
    : BasePresenter(presentView) {
    val createNewGameStr = presentView.getCreateNewGameStr()
    val rowCounts = if (orientation == Configuration.ORIENTATION_PORTRAIT)
        15 else 12
    val colCounts = 8
}