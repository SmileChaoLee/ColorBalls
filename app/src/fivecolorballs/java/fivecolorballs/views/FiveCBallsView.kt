package fivecolorballs.views

import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.smile.colorballs_main.R
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichBall
import com.smile.colorballs_main.models.ColorBallInfo
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.BaseView
import com.smile.colorballs_main.views.CbComposable
import fivecolorballs.constants.FiveBallsConstants
import fivecolorballs.interfaces.FiveBallsPresentView
import fivecolorballs.presenters.FiveBallsPresenter
import fivecolorballs.viewmodels.FiveBallsViewModel

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
        mPresenter = FiveBallsPresenter(this,
            resources.configuration.orientation)
        viewModel = FiveBallsViewModel(mPresenter)

        gameWidthRation = if (mOrientation.intValue
            == Configuration.ORIENTATION_PORTRAIT) 0.8f else 0.8f
        menuBarWeight = 0.0f
        gameGridWeight = 8.0f

        super.onCreate(savedInstanceState)
    }

    @Composable
    fun ShowNext4Balls(modifier: Modifier = Modifier) {
        LogUtil.i(TAG, "ShowNext4Balls.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val next = mutableStateListOf<ColorBallInfo>()
        next.add(ColorBallInfo(ballColor = Constants.COLOR_RED, whichBall = WhichBall.BALL,
            isResize = true))
        next.add(ColorBallInfo(ballColor = Constants.COLOR_BLUE, whichBall = WhichBall.BALL,
            isResize = true))
        next.add(ColorBallInfo(ballColor = Constants.COLOR_MAGENTA, whichBall = WhichBall.BALL,
            isResize = true))
        next.add(ColorBallInfo(ballColor = Constants.COLOR_GREEN, whichBall = WhichBall.BALL,
            isResize = true))
        LazyColumn(modifier = modifier) {
            items(items = next) {item ->
                val bitmap = colorBallMap.getValue(item.ballColor)
                var modifier = Modifier
                    .background(color = Color.Transparent)
                var scale: ContentScale = ContentScale.Inside
                if (item.isResize) {
                    modifier = modifier
                        .size(mImageSizeDp.dp)
                        .padding(all = 0.dp)
                    scale = ContentScale.Fit
                }
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "",
                    contentScale = scale,
                    modifier = modifier
                )
            }
        }
    }

    // implementing FiveBallsPresentView
    override fun getCreateNewGameStr() = getString(R.string.createNewGameStr)
    // end of implementing

    // implement abstract fun of BaseView
    @Composable
    override fun ToolBarMenu(modifier: Modifier) {
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
                    ShowGameGrid()
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
                            verticalArrangement = Arrangement.Bottom) {
                            ShowCurrentScore(modifier = Modifier.weight(1f),
                                pFontSize = scoreFontSize)
                            SHowHighestScore(modifier = Modifier.weight(1f),
                                pFontSize = scoreFontSize)
                        }
                    }
                }
                Column(modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    ShowMessageOnScreen()
                }
            }
        }
    }

    @Composable
    override fun CreateNewGameDialog() {
        LogUtil.i(TAG, "CreateNewGameDialog")
        val dialogText = viewModel.getCreateNewGameText()
        if (dialogText.isNotEmpty()) {
            viewModel.setShowingCreateGameDialog(true)
            val buttonListener = object: CbComposable.ButtonClickListener {
                override fun buttonOkClick() {
                    viewModel.setCreateNewGameText("")
                    viewModel.setShowingCreateGameDialog(false)
                    quitOrNewGame()
                }
                override fun buttonCancelClick() {
                    viewModel.setCreateNewGameText("")
                    viewModel.setShowingCreateGameDialog(false)
                }
            }
            CbComposable.DialogWithText(
                this@FiveCBallsView,
                buttonListener, "", dialogText
            )
        }
    }

    override fun getBasePresenter(): FiveBallsPresenter {
        return mPresenter
    }

    override fun getBaseViewModel(): FiveBallsViewModel {
        return viewModel
    }

    override fun ifInterstitialWhenSaveScore() {
        LogUtil.i(TAG, "ifShowInterstitialAd")
        if (viewModel.timesPlayed >= FiveBallsConstants.SHOW_ADS_AFTER_TIMES) {
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
    // end of implementing abstract fun of BaseView

    override fun ifInterstitialWhenNewGame() {
        LogUtil.i(TAG, "ifInterstitialWhenNewGame")
        viewModel.initGame(bundle = null)
    }
    // end of implementing abstract fun of MyView
}