package com.smile.colorballs.views.xml_compose

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.os.BundleCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.ads.nativetemplates.TemplateView
import com.smile.colorballs.BuildConfig
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.R.drawable
import com.smile.colorballs.R.id
import com.smile.colorballs.R.layout
import com.smile.colorballs.R.string
import com.smile.colorballs.shared_composables.Composables
import com.smile.colorballs.views.xml_base.SettingActivity
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.coroutines.Top10Coroutine
import com.smile.colorballs.models.GameProp
import com.smile.colorballs.models.GridData
import com.smile.colorballs.presenters.Presenter
import com.smile.colorballs.views.xml_base.MyView
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.player_record_rest.models.Player
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyActivityTop10Compose : MyView() {
    private var fontScale = 0f
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var supportToolbar: Toolbar? = null
    private var top10Fragment: Top10ComposeFragment? = null
    private var mainGameViewWidth = 0f
    private var cellWidth = 0
    private var cellHeight = 0
    private var myReceiver: MyBroadcastReceiver? = null
    private var nativeTemplate: GoogleAdMobNativeTemplate? = null
    private var myBannerAdView: SetBannerAdView? = null
    private var myBannerAdView2: SetBannerAdView? = null
    private lateinit var settingLauncher: ActivityResultLauncher<Intent>
    private lateinit var top10Launcher: ActivityResultLauncher<Intent>
    private var interstitialAd: ShowInterstitial? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        (application as ColorBallsApp).let {
            interstitialAd = ShowInterstitial(this, it.facebookAds,
                it.googleInterstitialAd)
        }
        super.onCreate(savedInstanceState)

        if (!BuildConfig.DEBUG) {
            requestedOrientation = if (ScreenUtil.isTablet(this)) {
                // Table then change orientation to Landscape
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                // phone then change orientation to Portrait
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        setContentView(layout.activity_my)

        val isNewGame = initPresenter(savedInstanceState)
        createActivityUI()
        createGameView()
        createGame(isNewGame)

        bannerAndNativeAd()
        setBroadcastReceiver()
        settingLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.d(TAG, "settingLauncher.result received")
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                data.extras?.let { extras ->
                    mPresenter.setHasSound(extras.getBoolean(Constants.HAS_SOUND))
                    mPresenter.setEasyLevel(extras.getBoolean(Constants.IS_EASY_LEVEL))
                    mPresenter.setHasNextBall(extras.getBoolean(Constants.HAS_NEXT_BALL), true)
                }
            }
            ColorBallsApp.isProcessingJob = false // setting activity finished
        }
        top10Launcher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.d(TAG, "top10Launcher.result received")
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "top10Launcher.Showing interstitial ads")
                showInterstitialAd()
            }
            ColorBallsApp.isShowingLoadingMessage = false
            ColorBallsApp.isProcessingJob = false
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                onBackWasPressed()
            }
        })

        Log.d(TAG, "onCreate() is finished.")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.my, menu)
        // final Context wrapper = new ContextThemeWrapper(this, R.style.menu_text_style);
        // or
        val popupThemeId = supportToolbar!!.popupTheme
        val wrapper: Context = ContextThemeWrapper(this, popupThemeId)
        // ScreenUtil.buildActionViewClassMenu(this, wrapper, menu, fScale, ScreenUtil.FontSize_Pixel_Type);
        ScreenUtil.resizeMenuTextIconSize(wrapper, menu, fontScale)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (ColorBallsApp.isProcessingJob) {
            return false
        }
        when (item.itemId) {
            id.undoGame -> {
                mPresenter.undoTheLast()
            }
            id.globalTop10 -> {
                showTop10Scores(false)
            }
            id.localTop10 -> {
                showTop10Scores(true)
            }
            id.setting -> {
                Log.d(TAG, "onOptionsItemSelected.settingLauncher.launch(intent)")
                ColorBallsApp.isProcessingJob = true // started procession job
                Intent(this, SettingActivity::class.java).let {
                    Bundle().let { extra ->
                        extra.putBoolean(Constants.HAS_SOUND, mPresenter.hasSound())
                        extra.putBoolean(Constants.IS_EASY_LEVEL, mPresenter.isEasyLevel())
                        extra.putBoolean(Constants.HAS_NEXT_BALL, mPresenter.hasNextBall())
                        it.putExtras(extra)
                        settingLauncher.launch(it)
                    }
                }
            }
            id.saveGame -> {
                mPresenter.saveGame()
            }
            id.loadGame -> {
                mPresenter.loadGame()
            }
            id.newGame -> {
                mPresenter.newGame()
            }
            id.quitGame -> {
                mPresenter.quitGame() //  exit game
            }
            id.privacyPolicy -> {
                PrivacyPolicyUtil.startPrivacyPolicyActivity(this, 10)
            }
            else -> {
                // return super.onOptionsItemSelected(item);
                return false
            }
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState")
        if (isChangingConfigurations) {
            // configuration is changing then remove top10Fragment
            top10Fragment?.let {
                // remove top10Fragment
                supportFragmentManager.beginTransaction().apply {
                    remove(it)
                    Log.d(TAG, "onSaveInstanceState.isStateSaved() = ${it.isStateSaved}")
                    commitAllowingStateLoss() // added on 2021-01-24
                    ColorBallsApp.isShowingLoadingMessage = false
                    ColorBallsApp.isProcessingJob = false
                }
            }
        }
        saveScoreAlertDialog?.dismiss()
        mPresenter.onSaveInstanceState(outState)

        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    public override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        mPresenter.release()
        interstitialAd?.releaseInterstitial()
        LocalBroadcastManager.getInstance(this).let {
            myReceiver?.let { rec ->
                it.unregisterReceiver(rec)
            }
        }
        myBannerAdView?.let {
            it.pause()
            it.destroy()
        }
        myBannerAdView2?.let {
            it.pause()
            it.destroy()
        }
        nativeTemplate?.release()
        sureSaveDialog?.dismissAllowingStateLoss()
        warningSaveGameDialog?.dismissAllowingStateLoss()
        sureLoadDialog?.dismissAllowingStateLoss()
        gameOverDialog?.dismissAllowingStateLoss()

        super.onDestroy()
    }

    private fun onBackWasPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        val exitAppTimer = ExitAppTimer.getInstance(1000) // singleton class
        if (exitAppTimer.canExit()) {
            mPresenter.quitGame() //   from   END PROGRAM
        } else {
            exitAppTimer.start()
            val toastFontSize = textFontSize * 0.7f
            Log.d(TAG, "toastFontSize = $toastFontSize")
            ScreenUtil.showToast(
                this@MyActivityTop10Compose,
                getString(string.backKeyToExitApp),
                toastFontSize,
                ScreenUtil.FontSize_Pixel_Type,
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun findTextFontSize() {
        val defaultSize = ScreenUtil.getDefaultTextSizeFromTheme(this,
                ScreenUtil.FontSize_Pixel_Type, null)
        textFontSize = ScreenUtil.suitableFontSize(
            this,
            defaultSize,
            ScreenUtil.FontSize_Pixel_Type,
            0.0f)
        fontScale = ScreenUtil.suitableFontScale(this,
            ScreenUtil.FontSize_Pixel_Type, 0.0f)
    }

    private fun findScreenSize() {
        val size = ScreenUtil.getScreenSize(this)
        screenWidth = size.x.toFloat()
        Log.d(TAG, "screenWidth = $screenWidth")
        screenHeight = size.y.toFloat()
        Log.d(TAG, "screenHeight = $screenHeight")
        val statusBarHeight = ScreenUtil.getStatusBarHeight(this).toFloat()
        Log.d(TAG, "statusBarHeight = $statusBarHeight")
        val actionBarHeight = ScreenUtil.getActionBarHeight(this).toFloat()
        Log.d(TAG, "actionBarHeight = $actionBarHeight")
        val navigationBarHeight = ScreenUtil.getNavigationBarHeight(this).toFloat()
        Log.d(TAG, "navigationBarHeight = $navigationBarHeight")
        // keep navigation bar
        screenHeight -= (statusBarHeight + actionBarHeight)
    }

    private fun setUpSupportActionBar() {
        supportToolbar = findViewById(id.colorBallToolbar)
        setSupportActionBar(supportToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun createActivityUI() {
        findTextFontSize()
        findScreenSize()
        setUpSupportActionBar()
    }

    private fun createGameView() {
        // find Out Width and Height of GameView
        var linearLayout = findViewById<LinearLayout>(id.linearLayout_myActivity)
        val mainWeightSum = linearLayout.weightSum
        Log.d(TAG, "createGameView.mainWeightSum = $mainWeightSum")

        linearLayout = findViewById(id.gameViewLinearLayout)
        val gameViewLp = linearLayout.layoutParams as LinearLayout.LayoutParams
        val gameViewWeight = gameViewLp.weight
        Log.d(TAG, "createGameView.gameViewWeight = $gameViewWeight")
        val mainGameViewHeight = screenHeight * gameViewWeight / mainWeightSum
        Log.d(TAG, "createGameView.mainGameViewHeight = $mainGameViewHeight")

        val gameViewWeightSum = linearLayout.weightSum
        Log.d(TAG, "createGameView.gameViewWeightSum = $gameViewWeightSum")
        linearLayout = findViewById(id.gameViewLayout)
        val mainGameViewUiLayoutParams =
            linearLayout.layoutParams as LinearLayout.LayoutParams
        val mainGameViewUiWeight = mainGameViewUiLayoutParams.weight
        Log.d(TAG, "createGameView.mainGameViewUiWeight = $mainGameViewUiWeight")
        mainGameViewWidth = screenWidth * (mainGameViewUiWeight / gameViewWeightSum)
        Log.d(TAG, "createGameView.mainGameViewWidth = $mainGameViewWidth")

        // display the highest score and current score
        supportToolbar?.let {
            highestScoreTextView = it.findViewById(id.highestScoreTextView)
            ScreenUtil.resizeTextSize(
                highestScoreTextView,
                textFontSize,
                ScreenUtil.FontSize_Pixel_Type
            )
            currentScoreTextView = it.findViewById(id.currentScoreTextView)
            ScreenUtil.resizeTextSize(
                currentScoreTextView,
                textFontSize,
                ScreenUtil.FontSize_Pixel_Type
            )
        }

        val gridPartFrameLayout = findViewById<FrameLayout>(id.gridPartFrameLayout)
        val frameLp = gridPartFrameLayout.layoutParams as LinearLayout.LayoutParams

        // for 9 x 9 grid: main part of this game
        val gridCellsLayout = findViewById<GridLayout>(id.gridCellsLayout)
        val rowCounts = gridCellsLayout.rowCount
        val colCounts = gridCellsLayout.columnCount
        Log.d(TAG, "createGameView.rowCounts = $rowCounts")
        Log.d(TAG, "createGameView.colCounts = $colCounts")
        cellWidth = (mainGameViewWidth / colCounts).toInt()
        Log.d(TAG, "createGameView.cellWidth = $cellWidth")
        if (mainGameViewWidth > mainGameViewHeight) {
            // if screen width greater than 8-10th of screen height
            cellWidth = (mainGameViewHeight / rowCounts).toInt()
        }
        cellHeight = cellWidth
        Log.d(TAG, "createGameView.cellHeight = $cellHeight")

        // added on 2018-10-02 to test and it works
        // setting the width and the height of GridLayout by using the FrameLayout that is on top of it
        frameLp.width = cellWidth * colCounts
        frameLp.topMargin = 20
        frameLp.gravity = Gravity.CENTER

        val oneBallLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        oneBallLp.width = cellWidth
        oneBallLp.height = cellHeight
        oneBallLp.gravity = Gravity.CENTER

        // set listener for each ImageView
        var imageView: ImageView
        var imId: Int
        for (i in 0 until rowCounts) {
            for (j in 0 until colCounts) {
                // imId = i * rowCounts + j;
                imId = mPresenter.getImageId(i, j)
                imageView = ImageView(this)
                imageView.id = imId
                imageView.adjustViewBounds = true
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                imageView.setBackgroundResource(drawable.box_image)
                imageView.isClickable = true
                imageView.setOnClickListener { v: View? ->
                    if ((mPresenter.completedAll()) && (!ColorBallsApp.isProcessingJob)) {
                        Log.d(TAG, "createGameView.onClick")
                        mPresenter.drawBallsAndCheckListener(v!!)
                    }
                }
                gridCellsLayout.addView(imageView, imId, oneBallLp)
            }
        }

        scoreImageView = findViewById(id.scoreImageView)
        scoreImageView.visibility = View.GONE
    }

    private fun initPresenter(state: Bundle?): Boolean {
        // restore instance state
        val isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: GridData? = null
        state?.let {
            Log.d(TAG,"initPresenter.state not null then restore the state")
            gameProp =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GAME_PROP_TAG, GameProp::class.java)
                else it.getParcelable(Constants.GAME_PROP_TAG)
            gridData =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GRID_DATA_TAG, GridData::class.java)
                else it.getParcelable(Constants.GRID_DATA_TAG)
        }
        if (gameProp == null || gridData == null) {
            Log.d(TAG, "initPresenter.prop or grid is null, new game")
            gameProp = GameProp()
            gridData = GridData()
            isNewGame = true
        } else {
            isNewGame = false
        }
        //
        mPresenter = Presenter(this@MyActivityTop10Compose, gameProp!!, gridData!!)

        return isNewGame
    }

    private fun createGame(isNewGame: Boolean) {
        saveScoreAlertDialog = null
        mPresenter.initGame(cellWidth, cellHeight, isNewGame)
    }

    override fun setDialogStyle(dialog: DialogInterface) {
        val dlg = dialog as AlertDialog
        (dlg.window)?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setDimAmount(0.0f) // no dim for background screen
                setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawableResource(drawable.dialog_board_image)
        }

        val nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE)
        ScreenUtil.resizeTextSize(nBtn, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        nBtn.typeface = Typeface.DEFAULT_BOLD
        nBtn.setTextColor(Color.RED)

        val layoutParams = nBtn.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        nBtn.layoutParams = layoutParams

        val pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE)
        ScreenUtil.resizeTextSize(pBtn, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        pBtn.typeface = Typeface.DEFAULT_BOLD
        pBtn.setTextColor(Color.rgb(0x00, 0x64, 0x00))
        pBtn.layoutParams = layoutParams
    }

    override fun showInterstitialAd() {
        Log.d(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    override fun quitOrNewGame(entryPoint: Int) {
        if (entryPoint == 0) {
            //  END PROGRAM
            exitApplication()
        } else if (entryPoint == 1) {
            //  NEW GAME
            initPresenter(null)
        }
        ColorBallsApp.isProcessingJob = false
    }

    private fun showTop10Scores(isLocal: Boolean) {
        Log.d(TAG, "showTop10Scores")
        ColorBallsApp.isProcessingJob = true
        ColorBallsApp.isShowingLoadingMessage = true
        showMessageOnScreen(getString(string.loadingStr))
        Log.d(TAG, "showTop10Scores.launch coroutine")
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "showTop10Scores.launch.Top10Coroutine.getTop10()")
            Top10Coroutine.getTop10(applicationContext, isLocal)
        }
        /*
        if (isLocal) {
            // myIntent = new Intent(this, LocalTop10Service.class);
            CoroutineScope(Dispatchers.Main).launch {
                Top10Coroutine.getTop10(applicationContext, isLocal)
            }
        } else {
            Intent(this, GlobalTop10Service::class.java).let {
                it.putExtra(Constants.GAME_ID_STRING, "1")
                startService(it)
            }
        }
        */
    }

    private fun bannerAndNativeAd() {
        val bannerLayout = findViewById<LinearLayout>(id.linearlayout_banner_myActivity)
        val adaptiveBannerLayout =
            findViewById<LinearLayout>(id.linearlayout_adaptiveBanner_myActivity)
        val testString = if (BuildConfig.DEBUG) "IMG_16_9_APP_INSTALL#" else ""
        val facebookBannerID = testString + ColorBallsApp.facebookBannerID
        val facebookBannerID2 = testString + ColorBallsApp.facebookBannerID2
        //
        val adaptiveBannerWidth: Float
        val adaptiveBannerDpWidth: Float

        val configuration = resources.configuration
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // show AdMob native ad if the device is tablet
            val nativeAdvancedId0 = ColorBallsApp.googleAdMobNativeID // real native ad unit id
            val nativeAdsFrameLayout = findViewById<FrameLayout>(id.nativeAdsFrameLayout)
            val nativeAdTemplateView = findViewById<TemplateView>(id.nativeAdTemplateView)
            GoogleAdMobNativeTemplate(this, nativeAdsFrameLayout,
                nativeAdvancedId0, nativeAdTemplateView).also {
                nativeTemplate = it
                it.showNativeAd()
            }
            adaptiveBannerWidth = screenWidth - mainGameViewWidth
            adaptiveBannerDpWidth = ScreenUtil.pixelToDp(adaptiveBannerWidth)
        } else {
            // one more banner (adaptive banner) ad for orientation is portrait
            adaptiveBannerWidth = mainGameViewWidth
            adaptiveBannerDpWidth = ScreenUtil.pixelToDp(adaptiveBannerWidth)
            SetBannerAdView(this, null,
                adaptiveBannerLayout, ColorBallsApp.googleAdMobBannerID2,
                facebookBannerID2, adaptiveBannerDpWidth.toInt()).also {
                myBannerAdView2 = it
                it.showBannerAdView(0) // AdMob first
            }
        }
        // normal banner
        Log.d(TAG, "adaptiveBannerDpWidth = $adaptiveBannerDpWidth")
        SetBannerAdView(this, null,
            bannerLayout, ColorBallsApp.googleAdMobBannerID,
            facebookBannerID, adaptiveBannerDpWidth.toInt()).also {
            myBannerAdView = it
            it.showBannerAdView(0) // AdMob first
        }
    }

    private fun setBroadcastReceiver() {
        myReceiver = MyBroadcastReceiver()
        val myIntentFilter = IntentFilter()
        myIntentFilter.let {
            it.addAction(Constants.GLOBAL_TOP10_ACTION_NAME)
            it.addAction(Constants.LOCAL_TOP10_ACTION_NAME)
        }
        LocalBroadcastManager.getInstance(this).let {
            myReceiver?.let { rec ->
                it.registerReceiver(rec, myIntentFilter)
            }
        }
    }

    private fun exitApplication() {
        val handlerClose = Handler(Looper.getMainLooper())
        val timeDelay = 200
        // exit application
        handlerClose.postDelayed({ this.finish() }, timeDelay.toLong())
    }

    private inner class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val actionName = intent.action
            Log.d(TAG, "MyBroadcastReceiver.actionName = $actionName")
            if (actionName != null) {
                var players = ArrayList<Player>()
                val top10LayoutId = id.top10Layout
                val top10ScoreTitle = if (actionName == Constants.GLOBAL_TOP10_ACTION_NAME) {
                    getString(string.globalTop10Score)
                } else {
                    getString(string.localTop10Score)
                }
                intent.extras?.let { extras ->
                    players = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        BundleCompat.getParcelableArrayList(
                            extras, Constants.TOP10_PLAYERS, Player::class.java)!!
                    } else extras.getParcelableArrayList(Constants.TOP10_PLAYERS)!!
                }
                Log.d(TAG, "MyBroadcastReceiver.players.size = ${players.size}")
                val historyView = findViewById<View>(top10LayoutId)
                if (historyView != null) {
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Log.d(TAG, "MyBroadcastReceiver.ORIENTATION_LANDSCAPE")
                        top10Fragment = Top10ComposeFragment.newInstance(
                            top10ScoreTitle,
                            players,
                            object: Composables.OkButtonListener {
                                override fun buttonOkClick() {
                                    Log.d(TAG, "MyBroadcastReceiver.Top10OkButtonListener")
                                    // remove top10Fragment to dismiss the top 10 score screen
                                    top10Fragment?.let { top10 ->
                                        supportFragmentManager.beginTransaction().let { ft ->
                                            ft.remove(top10)
                                            ft.commitAllowingStateLoss() // resolve the crash issue
                                        }
                                    }
                                    showInterstitialAd()
                                }
                            })
                        top10Fragment?.let { top10 ->
                            supportFragmentManager.let { m ->
                                m.beginTransaction().let { ft ->
                                    if (m.findFragmentByTag(TOP10_FRAGMENT_TAG) != null) {
                                        ft.replace(top10LayoutId, top10, TOP10_FRAGMENT_TAG)
                                    } else {
                                        ft.add(top10LayoutId, top10, TOP10_FRAGMENT_TAG)
                                    }
                                    ft.commitAllowingStateLoss() // resolve the crash issue
                                }
                            }
                        }
                    }
                } else {
                    // for Portrait
                    Log.d(TAG, "MyBroadcastReceiver.ORIENTATION_PORTRAIT")
                    top10Fragment = null
                    Intent(applicationContext,
                        Top10ComposeActivity::class.java).let { int ->
                        Bundle().apply {
                            putString(Constants.TOP10_TITLE_NAME, top10ScoreTitle)
                            putParcelableArrayList(Constants.TOP10_PLAYERS, players)
                            int.putExtras(this)
                            top10Launcher.launch(int)
                        }
                    }
                }
                dismissShowMessageOnScreen()
            }
            ColorBallsApp.isShowingLoadingMessage = false
            ColorBallsApp.isProcessingJob = false
        }
    }

    companion object {
        // private properties
        private const val TAG = "MyActivityTop10Compose"
        private const val TOP10_FRAGMENT_TAG = "Top10Fragment"
    }
}
