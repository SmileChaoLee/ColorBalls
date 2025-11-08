package fivecolorballs.views

import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.smile.colorballs_main.constants.WhichBall
import com.smile.colorballs_main.models.ColorBallInfo
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.BaseView
import com.smile.colorballs_main.views.CbComposable
import fivecolorballs.constants.FiveBallsConstants
import fivecolorballs.interfaces.FiveBallsPresentView
import fivecolorballs.presenters.FiveBallsPresenter
import fivecolorballs.viewmodels.FiveBallsViewModel
import kotlin.math.abs

abstract class FiveCBallsView: BaseView(),
    FiveBallsPresentView {

    companion object {
        private const val TAG = "FiveCBallsView"
    }

    protected lateinit var viewModel: FiveBallsViewModel
    private lateinit var mPresenter: FiveBallsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "$TAG.onCreate")
        // Must be before super.onCreate(savedInstanceState)
        menuBarWeight = 0.0f
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            LogUtil.i(TAG, "$TAG.onCreate.PORTRAIT")
            gameGridWeight = 8.0f
            gameWidthRation = 0.8f
        } else {
            LogUtil.i(TAG, "$TAG.onCreate.LANDSCAPE")
            gameGridWeight = 10.0f
            gameWidthRation = 0.9f
        }
        LogUtil.i(TAG, "$TAG.onCreate.menuBarWeight = $menuBarWeight")
        LogUtil.i(TAG, "$TAG.onCreate.gameGridWeight = $gameGridWeight")

        mPresenter = FiveBallsPresenter(this)
        viewModel = FiveBallsViewModel(mPresenter)

        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        viewModel.release()
    }

    @Composable
    fun ShowNext4Balls(modifier: Modifier = Modifier) {
        LogUtil.i(TAG, "ShowNext4Balls.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val next4 = viewModel.next4Balls
        if (next4.isEmpty()) {
            LogUtil.i(TAG, "ShowNext4Balls.viewModel.next4Balls is empty")
            return
        }
        LogUtil.i(TAG, "ShowNext4Balls.viewModel.next4Balls.size = ${next4.size}")
        LazyColumn(modifier = modifier) {
            items(items = viewModel.next4Balls) {item ->
                ShowBall(ColorBallInfo(item, WhichBall.BALL))
            }
        }
    }

    // implementing FiveBallsPresentView
    // override fun getCreateNewGameStr() = getString(R.string.createNewGameStr)
    // end of implementing

    // implement abstract fun of BaseView
    @Composable
    override fun ToolBarMenu(modifier: Modifier) {
        LogUtil.i(TAG, "ToolBarMenu")
        // do nothing
    }

    @Composable
    override fun GameViewGrid() {
        LogUtil.i(TAG, "GameViewGrid.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val scoreFontSize = CbComposable.mFontSize * 1.0f
        Column(modifier = Modifier.fillMaxSize()
            .background(Color.DarkGray),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top) {
            Box(contentAlignment = Alignment.TopStart) {
                Row {
                    StartShowGameGrid()
                    Column(modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Column(modifier = Modifier.weight(2f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top) {
                            ShowMenu(modifier = Modifier)
                            Spacer(modifier = Modifier
                                .fillMaxWidth()
                                .height(height = 20.dp))
                            SettingButton(modifier = Modifier)
                            Spacer(modifier = Modifier
                                .fillMaxWidth()
                                .height(height = 20.dp))
                            ShowNext4Balls(modifier = Modifier)
                        }
                        Column(modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom) {
                            ShowCurrentScore(modifier = Modifier.weight(1f),
                                pFontSize = scoreFontSize)
                            SHowHighestScore(modifier = Modifier.weight(1f),
                                pFontSize = scoreFontSize)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun StartShowGameGrid() {
        // measured in pixel
        val screenWidth = LocalWindowInfo.current.containerSize.width
        LogUtil.i(TAG, "StartShowGameGrid.screenWidth = $screenWidth")
        val pixelPerCol = (screenWidth*gameWidthRation) / FiveBallsConstants.COLUMN_COUNTS
        LogUtil.i(TAG, "StartShowGameGrid.pixelPerCol = $pixelPerCol")
        val diffStandard = 0.85
        Column(modifier = Modifier
            .pointerInput(Unit) {
                // Use the Initial pass to receive the event before the child's Main pass
                // awaitPointerEventScope {
                awaitEachGesture {
                    LogUtil.i(TAG, "StartShowGameGrid.awaitEachGesture")
                    // Detects the ACTION_DOWN equivalent
                    // awaitFirstDown(pass = PointerEventPass.Initial) // starting from parent
                    // regardless if event is consumed by children
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var downX = down.position.x
                    LogUtil.i(TAG, "StartShowGameGrid.awaitFirstDown")
                    do {
                        // val event = awaitPointerEvent(PointerEventPass.Initial)  // starting from parent
                        val event = awaitPointerEvent()
                        // Log or handle the tap event in the parent
                        // Do NOT consume the event here
                        // if you want the child to receive it
                        event.changes.apply {
                            all { it.isConsumed }   // must be here
                            forEach {
                                if (it.pressed) {
                                    val moveX = (it.position.x - downX)/pixelPerCol
                                    LogUtil.d(TAG, "StartShowGameGrid.moveX = $moveX")
                                    if (abs(moveX) >= diffStandard) {
                                        downX = it.position.x
                                        if (moveX > 0) {
                                            viewModel.shiftRunningCol(1)
                                        } else {
                                            viewModel.shiftRunningCol(-1)
                                        }
                                    }
                                } else {
                                    LogUtil.d(TAG, "StartShowGameGrid.UP detected")
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed }) // loop while pressed and moving
                }
            }
        ) {
            ShowGameGrid(isClickable = false)
        }
    }

    @Composable
    override fun CreateNewGameDialog() {
        LogUtil.i(TAG, "CreateNewGameDialog")
        // do nothing
    }

    override fun getCurrentPresenter(): FiveBallsPresenter {
        return mPresenter
    }

    override fun getCurrentViewModel(): FiveBallsViewModel {
        return viewModel
    }

    override fun ifInterstitialWhenSaveScore() {
        LogUtil.i(TAG, "ifShowInterstitialAd")
        if (viewModel.timesPlayed >=
            FiveBallsConstants.SHOW_ADS_AFTER_TIMES) {
            LogUtil.d(TAG, "ifShowInterstitialAd.showInterstitialAd")
            showInterstitialAd()
            viewModel.timesPlayed = 0
        }
    }

    override fun ifCreatingNewGame(newEasyLevel: Boolean, originalLevel: Boolean) {
        // do nothing
    }

    override fun setHasNextForView(hasNext: Boolean) {
        viewModel.setHasNext(hasNext)
    }

    override fun ifInterstitialWhenNewGame() {
        LogUtil.i(TAG, "ifInterstitialWhenNewGame")
        viewModel.initGame(bundle = null)
    }
    // end of implementing abstract fun of BaseView
}