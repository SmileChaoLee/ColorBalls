package com.smile.dropcolorballs.views

import android.content.res.Configuration
import android.os.Bundle
import com.smile.colorballs_main.R
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichBall
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.models.ColorBallInfo
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.BaseView
import com.smile.colorballs_main.views.CbComposable
import com.smile.colorballs_main.views.CbSettingActivity
import com.smile.dropcolorballs.interfaces.DropBallsPresentView
import com.smile.dropcolorballs.presenters.DropBallsPresenter
import com.smile.dropcolorballs.viewmodels.DropBallsViewModel
import kotlin.math.abs

class DropCBallsActivity: BaseView(),
    DropBallsPresentView {

    companion object {
        private const val TAG = "DropCBallsActivity"
    }

    private lateinit var viewModel: DropBallsViewModel
    private lateinit var mPresenter: DropBallsPresenter
    private var settingOrMenuClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "$TAG.onCreate")
        // Must be before super.onCreate(savedInstanceState)
        menuBarWeight = 0.0f
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            LogUtil.d(TAG, "$TAG.onCreate.PORTRAIT")
            gameGridWeight = 8.0f
            gameWidthRation = 0.8f
        } else {
            LogUtil.d(TAG, "$TAG.onCreate.LANDSCAPE")
            gameGridWeight = 10.0f
            gameWidthRation = 0.9f
        }
        LogUtil.d(TAG, "$TAG.onCreate.menuBarWeight = $menuBarWeight")
        LogUtil.d(TAG, "$TAG.onCreate.gameGridWeight = $gameGridWeight")

        mPresenter = DropBallsPresenter(this)
        viewModel = DropBallsViewModel(mPresenter)

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
        stopActionOnClick()
    }
    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause")
        actionOnClick()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy")
        viewModel.release()
    }

    @Composable
    fun ShowNext4Balls(modifier: Modifier = Modifier) {
        LogUtil.d(TAG, "ShowNext4Balls.getOrientation()" +
                " = ${getOrientation()}")
        val next4 = viewModel.next4Balls
        if (next4.isEmpty()) {
            LogUtil.d(TAG, "ShowNext4Balls.viewModel.next4Balls is empty")
            return
        }
        LogUtil.d(TAG, "ShowNext4Balls.viewModel.next4Balls.size = ${next4.size}")
        LazyColumn(modifier = modifier) {
            items(items = viewModel.next4Balls) {item ->
                ShowBall(ColorBallInfo(item, WhichBall.BALL))
            }
        }
    }

    // implementing DropBallsPresentView
    // override fun getCreateNewGameStr() = getString(R.string.createNewGameStr)
    // end of implementing

    // implement interface, GameOptions
    override fun setWhichGame() {
        LogUtil.d(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.DROP_BALLS)
    }
    // end of implementing interface, GameOptions

    // implement abstract fun of BaseView
    @Composable
    override fun ToolBarMenu(modifier: Modifier) {
        LogUtil.d(TAG, "ToolBarMenu")
        // do nothing
    }

    @Composable
    override fun GameViewGrid() {
        LogUtil.d(TAG, "GameViewGrid.getOrientation()" +
                " = ${getOrientation()}")
        val scoreFontSize = CbComposable.mFontSize * 1.0f
        val screenWidth = LocalWindowInfo.current.containerSize.width
        LogUtil.d(TAG, "GameViewGrid.screenWidth = $screenWidth")
        val pixelPerCol = (screenWidth*gameWidthRation) / viewModel.colCounts
        LogUtil.d(TAG, "GameViewGrid.pixelPerCol = $pixelPerCol")
        val diffStandard = 0.85
        val backColor = Color(0xFF00BCD4)
        Column(modifier = Modifier.fillMaxSize()
            .background(backColor)
            .pointerInput(Unit) {
                // Use the Initial pass to receive the event before the child's Main pass
                // awaitPointerEventScope {
                awaitEachGesture {
                    LogUtil.d(TAG, "GameViewGrid.awaitEachGesture")
                    // Detects the ACTION_DOWN equivalent
                    // awaitFirstDown(pass = PointerEventPass.Initial) // starting from parent
                    // regardless if event is consumed by children
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var downX = down.position.x
                    var isMoved = false
                    var upY = 0f
                    var tapX = 0f
                    LogUtil.d(TAG, "GameViewGrid.awaitFirstDown")
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
                                    LogUtil.d(TAG, "GameViewGrid.moveX = $moveX")
                                    if (abs(moveX) >= diffStandard) {
                                        downX = it.position.x
                                        if (moveX > 0) {
                                            viewModel.shiftRunningCol(1)
                                        } else {
                                            viewModel.shiftRunningCol(-1)
                                        }
                                        isMoved = true
                                    }
                                } else {
                                    LogUtil.d(TAG, "GameViewGrid.UP detected")
                                    upY = it.position.y - down.position.y
                                    tapX = it.position.x - down.position.x
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed }) // loop while pressed and moving
                    if (upY >= pixelPerCol * 1.5f) {
                        viewModel.toDropToEnd()
                    } else if (!isMoved && (abs(upY) <= 50f && abs(tapX) <= 50f)) {
                        // tap
                        if (settingOrMenuClicked) return@awaitEachGesture
                        viewModel.rotateRunningBalls()
                    }
                }
            },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top) {
            Box(contentAlignment = Alignment.TopStart) {
                Row {
                    ShowGameGrid(isClickable = false)
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
                        ShowGameLevel(modifier = Modifier.weight(1f),
                            scoreFontSize)
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
    fun ShowGameLevel(modifier: Modifier, scoreFontSize: TextUnit) {
        LogUtil.d(TAG, "ShowGameLevel.getOrientation()" +
                " = ${getOrientation()}")
        val levelStr = when(viewModel.mGameLevel) {
            Constants.GAME_LEVEL_1 -> getString(R.string.level1Str)
            Constants.GAME_LEVEL_2 -> getString(R.string.level2Str)
            Constants.GAME_LEVEL_3 -> getString(R.string.level3Str)
            Constants.GAME_LEVEL_4 -> getString(R.string.level4Str)
            Constants.GAME_LEVEL_5 -> getString(R.string.level5Str)
            else -> {""}
        }
        Column(modifier = modifier.padding(all = 0.dp)) {
            Text(text = levelStr,
                color = Color.Black, fontSize = scoreFontSize)
        }
    }

    @Composable
    override fun CreateNewGameDialog() {
        LogUtil.d(TAG, "CreateNewGameDialog")
        // do nothing
    }

    override fun getCurrentPresenter(): DropBallsPresenter {
        return mPresenter
    }

    override fun getCurrentViewModel(): DropBallsViewModel {
        return viewModel
    }

    override fun ifInterstitialWhenSaveScore() {
        LogUtil.d(TAG, "ifShowInterstitialAd")
        // do nothing
    }

    override fun ifCreatingNewGame(newGameLevel: Int, originalLevel: Int) {
        LogUtil.d(TAG, "ifCreatingNewGame")
        if (newGameLevel != originalLevel) {
            // create new game
            LogUtil.d(TAG, "ifCreatingNewGame.create a new game")
            actionOnClick()
            viewModel.newGame()
        }
    }

    override fun putOtherToBundle(extras: Bundle) {
        extras.putString(CbSettingActivity.HAS_SOUND_TITLE,
            getString(R.string.soundTitle))
        extras.putString(CbSettingActivity.GAME_LEVEL_TITLE,
            getString(R.string.gameLevelTitle))
        extras.putString(CbSettingActivity.NEXT_BALL_TITLE, "")
    }

    override fun setHasNextForView(hasNext: Boolean) {
        viewModel.setHasNext(hasNext)
    }

    override fun ifInterstitialWhenNewGame() {
        LogUtil.d(TAG, "ifInterstitialWhenNewGame")
        viewModel.initGame(bundle = null)
    }

    override fun isDropBalls() = true

    override fun actionOnClick() {
        LogUtil.d(TAG, "actionOnClick")
        settingOrMenuClicked = true
        viewModel.stopRunningHandler()
    }

    override fun stopActionOnClick() {
        LogUtil.d(TAG, "stopActionOnClick")
        settingOrMenuClicked = false
        viewModel.startRunningHandler()
    }

    override fun setTheGameLevel(gameLevel: Int) {
        viewModel.setDropGameLevel(gameLevel)
    }
    // end of implementing abstract fun of BaseView
}