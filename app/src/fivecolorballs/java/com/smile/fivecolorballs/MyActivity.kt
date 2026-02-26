package com.smile.fivecolorballs

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.ump.ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
import com.smile.colorballs_main.R
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.CbSettingActivity
import com.smile.colorballs_main.views.Top10Activity
import com.smile.fivecolorballs.constants.FiveBallsConstants
import com.smile.fivecolorballs.presenters.MyPresenter
import com.smile.fivecolorballs.presenters.MyPresenter.MyPresentView
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment.DialogButtonListener
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView
import com.smile.smilelibraries.utilities.FontAndBitmapUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import com.smile.smilelibraries.utilities.UmpUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.Locale

class MyActivity : AppCompatActivity(), MyPresentView {

    companion object {
        // private properties
        private const val TAG = "MyActivity"
        private const val GAME_OVER_DIALOG_TAG = "GameOverDialogTag"
    }

    private lateinit var mPresenter: MyPresenter
    private var mColorBallMap: HashMap<Int, Bitmap> = HashMap()
    private var mColorOvalBallMap: HashMap<Int, Bitmap> = HashMap()
    private lateinit var supportToolbar: Toolbar
    private var textFontSize = 0f
    private var fontScale = 0f
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var myBannerAdView: SetBannerAdView? = null
    private var nativeTemplate: GoogleAdMobNativeTemplate? = null
    private var scoreImageView: ImageView? = null
    private var toolbarTitleTextView: TextView? = null
    private var currentScoreView: TextView? = null
    private var mRowCounts = 9
    private var mColCounts = 9
    private var saveScoreAlertDialog: AlertDialog? = null
    private var sureSaveDialog: AlertDialogFragment? = null
    private var sureLoadDialog: AlertDialogFragment? = null
    private var gameOverDialog: AlertDialogFragment? = null
    private lateinit var settingLauncher: ActivityResultLauncher<Intent>
    private lateinit var top10Launcher: ActivityResultLauncher<Intent>
    private var touchDisabled = true

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        touchDisabled = true
        var bm = BitmapFactory.decodeResource(resources, R.drawable.redball)
        mColorBallMap.put(Constants.COLOR_RED, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.redball_o)
        mColorOvalBallMap.put(Constants.COLOR_RED, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.greenball)
        mColorBallMap.put(Constants.COLOR_GREEN, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.greenball_o)
        mColorOvalBallMap.put(Constants.COLOR_GREEN, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.blueball)
        mColorBallMap.put(Constants.COLOR_BLUE, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.blueball_o)
        mColorOvalBallMap.put(Constants.COLOR_BLUE, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.magentaball)
        mColorBallMap.put(Constants.COLOR_MAGENTA, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.magentaball_o)
        mColorOvalBallMap.put(Constants.COLOR_MAGENTA, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.yellowball)
        mColorBallMap.put(Constants.COLOR_YELLOW, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.yellowball_o)
        mColorOvalBallMap.put(Constants.COLOR_YELLOW, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.cyanball)
        mColorBallMap.put(Constants.COLOR_CYAN, bm)
        bm = BitmapFactory.decodeResource(resources, R.drawable.cyanball_o)
        mColorOvalBallMap.put(Constants.COLOR_CYAN, bm)

        mPresenter = MyPresenter(this)

        super.onCreate(savedInstanceState)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        setContentView(R.layout.activity_my)

        createActivityUI()
        createGameView(savedInstanceState)
        setBannerAndNativeAdUI()

        settingLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            LogUtil.i(TAG, TAG + "onCreate.settingLauncher.result")
            if (result.resultCode != RESULT_OK) return@registerForActivityResult
            val data = result.data
            if (data == null) return@registerForActivityResult
            val extras = data.extras
            if (extras == null) return@registerForActivityResult
            val hasSound = extras.getBoolean(Constants.HAS_SOUND, true)
            mPresenter.setHasSound(hasSound)
            val hasNext = extras.getBoolean(Constants.HAS_NEXT, true)
            mPresenter.setHasNext(hasNext, true)
        }
        top10Launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            // Handle the result here
            LogUtil.i(TAG, "top10Launcher.result = $result")
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                // Handle the fragment's back press (null check for playerFragment)
                onBackKeyPressed()
            }
        })

        // String deviceHashedId = "0FFD34B018082E4BCF218FE6299B48A2"; // for debug test
        val deviceHashedId = "" // for release
        UmpUtil.initConsentInformation(
            this@MyActivity,
            DEBUG_GEOGRAPHY_EEA, deviceHashedId,
            object : UmpUtil.UmpInterface {
                override fun callback() {
                    LogUtil.d(TAG, "dataConsentRequest.finished")
                    // enabling receiving touch events
                    touchDisabled = false
                }
            })

        LogUtil.d(TAG, "onCreate() is finished.")
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (touchDisabled) {
            // Consume the touch event, effectively disabling touch
            return true
        }
        // Allow touch events to proceed
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.my, menu)
        val popupThemeId = supportToolbar.popupTheme
        // final Context wrapper = new ContextThemeWrapper(this, R.style.menu_text_style);
        val wrapper: Context = ContextThemeWrapper(this, popupThemeId)
        ScreenUtil.resizeMenuTextIconSize(wrapper, menu, fontScale)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (mPresenter.isProcessingJob) return super.onOptionsItemSelected(item)
        val id = item.itemId
        if (id == R.id.quitGame) {
            mPresenter.quitGame() //  exit game
            return true
        }
        if (id == R.id.newGame) {
            mPresenter.newGame()
            return true
        }
        if (id == R.id.undoGame) {
            mPresenter.undoTheLast()
            return super.onOptionsItemSelected(item)
        }
        if (id == R.id.top10) {
            showTop10Players(true)
            return super.onOptionsItemSelected(item)
        }
        if (id == R.id.globalTop10) {
            showTop10Players(false)
            return super.onOptionsItemSelected(item)
        }
        if (id == R.id.saveGame) {
            mPresenter.saveGame()
            return super.onOptionsItemSelected(item)
        }
        if (id == R.id.loadGame) {
            mPresenter.loadGame()
            return true
        }
        if (id == R.id.setting) {
            val setIntent = Intent(this, CbSettingActivity::class.java)
            val extras = Bundle()
            extras.putString(Constants.GAME_ID, Constants.FIVE_COLOR_BALLS_ID)
            extras.putBoolean(Constants.HAS_SOUND, mPresenter.hasSound())
            extras.putInt(Constants.GAME_LEVEL, Constants.GAME_LEVEL_1)
            extras.putBoolean(Constants.HAS_NEXT, mPresenter.hasNext())
            setIntent.putExtras(extras)
            settingLauncher.launch(setIntent)
            return true
        }
        if (id == R.id.privacyPolicy) {
            val requestCode = 10
            PrivacyPolicyUtil.startPrivacyPolicyActivity(this, requestCode)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtil.d(TAG, "onSaveInstanceState() is called")
        saveScoreAlertDialog?.dismiss()
        mPresenter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume() is called")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause() is called")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.d(TAG, "onStop() is called")
    }

    public override fun onDestroy() {
        LogUtil.d(TAG, "onDestroy() is called")
        mPresenter.release()
        myBannerAdView?.destroy()
        nativeTemplate?.release()
        sureSaveDialog?.dismissAllowingStateLoss()
        sureLoadDialog?.dismissAllowingStateLoss()
        gameOverDialog?.dismissAllowingStateLoss()
        super.onDestroy()
    }

    private fun onBackKeyPressed() {
        LogUtil.d(TAG, "onBackKeyPressed")
        // singleton
        val exitAppTimer = ExitAppTimer.getInstance(1000)
        if (exitAppTimer.canExit()) {
            mPresenter.quitGame()
        } else {
            exitAppTimer.start()
            ScreenUtil.showToast(
                this, getString(R.string.backKeyToExitApp),
                textFontSize * 0.7f, Toast.LENGTH_SHORT
            )
        }
    }

    private fun createActivityUI() {
        findOutTextFontSize()
        findOutScreenSize()
        setUpSupportActionBar()
    }

    private fun findOutTextFontSize() {
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)
        fontScale = ScreenUtil.getPxFontScale(this)
    }

    private fun findOutScreenSize() {
        val size = ScreenUtil.getScreenSize(this)
        screenWidth = size.x.toFloat()
        screenHeight = size.y.toFloat()
        val statusBarHeight = ScreenUtil.getStatusBarHeight(this).toFloat()
        val actionBarHeight = ScreenUtil.getActionBarHeight(this).toFloat()
        // keep navigation bar
        screenHeight = screenHeight - statusBarHeight - actionBarHeight
    }

    private fun setUpSupportActionBar() {
        supportToolbar = findViewById(R.id.colorballs_toolbar)
        setSupportActionBar(supportToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun createGameView(savedInstanceState: Bundle?) {
        // find Out Width and Height of GameView
        val linearLayoutMyActivity = findViewById<LinearLayout>(R.id.linearLayout_myActivity)
        val mainWeightSum = linearLayoutMyActivity.weightSum

        val gameViewLinearLayout = findViewById<LinearLayout>(R.id.gameViewLinearLayout)
        val gameViewLp = gameViewLinearLayout.layoutParams as LinearLayout.LayoutParams
        val gameViewWeight = gameViewLp.weight
        val mainGameViewHeight = screenHeight * gameViewWeight / mainWeightSum
        LogUtil.d(TAG, "createGameView.mainGameViewHeight = $mainGameViewHeight")

        val gameViewWeightSum = gameViewLinearLayout.weightSum
        val mainGameViewUiLayout = findViewById<LinearLayout>(R.id.gameViewLayout)
        val mainGameViewUiLayoutParams =
            mainGameViewUiLayout.layoutParams as LinearLayout.LayoutParams
        val mainGameViewUiWeight = mainGameViewUiLayoutParams.weight
        val mainGameViewWidth = screenWidth * (mainGameViewUiWeight / gameViewWeightSum)
        LogUtil.d(TAG, "createGameView.mainGameViewWidth = $mainGameViewWidth")

        // layout_for_game_view.xml
        var heightWeightSumGameViewUi = 100f // default
        try {
            val gameViewUiLayout = findViewById<LinearLayout>(R.id.linearlayout_for_game_view_ui)
            val temp = gameViewUiLayout.weightSum
            if (temp != 0f) {
                heightWeightSumGameViewUi = temp
            }
        } catch (ex: Exception) {
            LogUtil.e(TAG, "createGameView.Exception: ", ex)
        }

        val scoreNextBallsLayout = findViewById<LinearLayout>(R.id.score_next_balls_layout)
        val widthWeightSumScoreNextBallsLayout = scoreNextBallsLayout.weightSum
        val scoreNextBallsLayoutParams =
            scoreNextBallsLayout.layoutParams as LinearLayout.LayoutParams
        val heightWeightScoreNextBallsLayout = scoreNextBallsLayoutParams.weight

        // display the highest score and current score
        toolbarTitleTextView = findViewById(R.id.toolbarTitleTextView)
        ScreenUtil.resizeTextSize(toolbarTitleTextView, textFontSize)

        currentScoreView = findViewById(R.id.currentScoreTextView)
        ScreenUtil.resizeTextSize(currentScoreView, textFontSize)

        // display the view of next balls
        val nextBallsLayout = findViewById<GridLayout>(R.id.nextBallsLayout)
        val nextBallsRow = nextBallsLayout.rowCount
        val nextBallsColumn = nextBallsLayout.columnCount
        val nextBallsLayoutParams = nextBallsLayout.layoutParams as LinearLayout.LayoutParams
        val widthWeightNextBalls = nextBallsLayoutParams.weight

        val nextBallsViewWidth =
            (mainGameViewWidth * widthWeightNextBalls / widthWeightSumScoreNextBallsLayout).toInt() // 3/5 of screen width

        val oneNextBallLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        oneNextBallLp.width = nextBallsViewWidth / nextBallsColumn
        // the layout_weight for height is 1
        oneNextBallLp.height =
            (mainGameViewHeight * heightWeightScoreNextBallsLayout / heightWeightSumGameViewUi).toInt()
        oneNextBallLp.gravity = Gravity.CENTER

        var imageView: ImageView?
        for (i in 0..<nextBallsRow) {
            for (j in 0..<nextBallsColumn) {
                imageView = ImageView(this)
                imageView.setId(MyPresenter.NB_IMAGEVIEW_START_ID + (nextBallsColumn * i + j))
                imageView.isClickable = false
                imageView.setAdjustViewBounds(true)
                imageView.setScaleType(ImageView.ScaleType.FIT_XY)
                imageView.setBackgroundResource(R.drawable.next_ball_background_image)
                nextBallsLayout.addView(imageView, oneNextBallLp)
            }
        }

        val gridPartFrameLayout = findViewById<FrameLayout>(R.id.gridPartFrameLayout)
        val frameLp = gridPartFrameLayout.layoutParams as LinearLayout.LayoutParams
        val heightWeightGridCellsLayout = frameLp.weight

        // for 9 x 9 grid: main part of this game
        val gridCellsLayout = findViewById<GridLayout>(R.id.gridCellsLayout)
        mRowCounts = gridCellsLayout.rowCount
        mColCounts = gridCellsLayout.columnCount
        var cellWidth = (mainGameViewWidth / mColCounts).toInt()
        val eight10thOfHeight =
            (mainGameViewHeight / heightWeightSumGameViewUi * heightWeightGridCellsLayout).toInt()
        if (mainGameViewWidth > eight10thOfHeight) {
            // if screen width greater than 8-10th of screen height
            cellWidth = eight10thOfHeight / mRowCounts
        }
        val cellH = cellWidth

        // added on 2018-10-02 to test and it works
        // setting the width and the height of GridLayout by using the FrameLayout that is on top of it
        frameLp.width = cellWidth * mColCounts
        frameLp.topMargin = 20
        frameLp.gravity = Gravity.CENTER
        val oneBallLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        oneBallLp.width = cellWidth
        oneBallLp.height = cellH
        oneBallLp.gravity = Gravity.CENTER

        // set listener for each ImageView
        // ImageView imageView;
        var imId: Int
        for (i in 0..<mRowCounts) {
            for (j in 0..<mColCounts) {
                // imId = i * colCounts + j;
                imId = i * mRowCounts + j
                imageView = ImageView(this)
                imageView.setId(imId)
                imageView.setAdjustViewBounds(true)
                imageView.setScaleType(ImageView.ScaleType.FIT_XY)
                imageView.setBackgroundResource(R.drawable.box_image)
                imageView.isClickable = true
                imageView.setOnClickListener { v: View ->
                    if (!mPresenter.isProcessingJob) {
                        mPresenter.doDrawBallsAndCheckListener(v)
                    }
                }
                gridCellsLayout.addView(imageView, imId, oneBallLp)
            }
        }
        scoreImageView = findViewById(R.id.scoreImageView)
        scoreImageView?.setVisibility(View.GONE)
        createColorBallsGame(savedInstanceState)
    }

    private fun createColorBallsGame(savedInstanceState: Bundle?) {
        saveScoreAlertDialog = null
        val isNewGame =
            mPresenter.initializeColorBallsGame(mRowCounts, mColCounts, savedInstanceState)
        LogUtil.d(TAG, "createColorBallsGame.isNewGame = $isNewGame")
    }

    private fun setDialogStyle(dialog: DialogInterface?) {
        val dlg = dialog as AlertDialog
        val win = dlg.window
        if (win == null) return

        win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        win.setDimAmount(0.0f) // no dim for background screen
        win.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        win.setBackgroundDrawableResource(R.drawable.dialog_board_image)

        val nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE)
        ScreenUtil.resizeTextSize(nBtn, textFontSize)
        nBtn.setTypeface(Typeface.DEFAULT_BOLD)
        nBtn.setTextColor(Color.RED)

        val layoutParams = nBtn.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        nBtn.setLayoutParams(layoutParams)

        val pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE)
        ScreenUtil.resizeTextSize(pBtn, textFontSize)
        pBtn.setTypeface(Typeface.DEFAULT_BOLD)
        pBtn.setTextColor(Color.rgb(0x00, 0x64, 0x00))
        pBtn.setLayoutParams(layoutParams)
    }

    private fun quitOrNewGame(entryPoint: Int) {
        if (entryPoint == 0) {
            //  END PROGRAM
            exitApplication()
        } else if (entryPoint == 1) {
            //  NEW GAME
            createColorBallsGame(null)
        }
    }

    private fun showTop10Players(isLocal: Boolean) {
        val topIntent = Intent(this, Top10Activity::class.java)
        val extras = Bundle()
        extras.putString(Constants.GAME_ID, Constants.FIVE_COLOR_BALLS_ID)
        extras.putString(Constants.DATABASE_NAME, FiveBallsConstants.FIVE_COLOR_BALLS_DATABASE)
        extras.putBoolean(Constants.IS_LOCAL_TOP10, isLocal)
        topIntent.putExtras(extras)
        top10Launcher.launch(topIntent)
    }

    private fun setBannerAndNativeAdUI() {
        val bannerLinearLayout =
            findViewById<LinearLayout?>(R.id.linearlayout_for_ads_in_myActivity)
        val bannerId = FiveCBallsApp.ADMOB_BANNER_ID // real Banner ID
        // use test Banner ID, Google’s universal test IDs
        // bannerId = "ca-app-pub-3940256099942544/6300978111";   // test Banner ID
        myBannerAdView = SetBannerAdView(
            this, null,
            bannerLinearLayout, bannerId, ""
        )
        myBannerAdView?.showBannerAdView(0) // AdMob first

        // show AdMob native ad if the device is tablet
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val nativeAdvancedId0 = FiveCBallsApp.ADMOB_NATIVE_ID // real native ad unit id
            // nativeAdvancedId0 = "ca-app-pub-3940256099942544/2247696110";   // test native ad unit id
            val nativeAdsFrameLayout = findViewById<FrameLayout>(R.id.nativeAdsFrameLayout)
            nativeAdsFrameLayout.visibility = View.VISIBLE
            val nativeAdTemplateView = findViewById<TemplateView>(R.id.nativeAdTemplateView)
            nativeAdTemplateView.visibility = View.VISIBLE
            nativeTemplate = GoogleAdMobNativeTemplate(
                this, nativeAdsFrameLayout,
                nativeAdvancedId0, nativeAdTemplateView
            )
            nativeTemplate?.showNativeAd()
        }
    }

    private fun exitApplication() {
        val handlerClose = Handler(Looper.getMainLooper())
        val timeDelay = 200
        // exit application
        handlerClose.postDelayed({ this.finish() }, timeDelay.toLong())
    }

    // implementing MyActivity.PresentView
    override fun getLoadingStr(): String {
        return getString(R.string.loadingString)
    }

    override fun geSavingGameStr(): String {
        return getString(R.string.savingGameString)
    }

    override fun getLoadingGameStr(): String {
        return getString(R.string.loadingGameString)
    }

    override fun getSureToSaveGameStr(): String {
        return getString(R.string.sureToSaveGameStr)
    }

    override fun getSureToLoadGameStr(): String {
        return getString(R.string.sureToLoadGameStr)
    }

    override fun getSaveScoreStr(): String {
        return getString(R.string.saveScoreStr)
    }

    override fun soundPool(): SoundPoolUtil {
        LogUtil.d(TAG, "soundPool")
        val sPool = SoundPoolUtil(this, R.raw.uhoh)
        LogUtil.d(TAG, "soundPool.sPool = $sPool")
        return sPool
    }

    override fun getScoreDatabase(): ScoreSQLite {
        return ScoreSQLite(this, FiveBallsConstants.FIVE_COLOR_BALLS_DATABASE)
    }

    override fun fileInputStream(fileName: String): FileInputStream? {
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(File(filesDir, fileName))
        } catch (ex: FileNotFoundException) {
            LogUtil.e(TAG, "FileInputStream.FileNotFoundException: ", ex)
        }
        return fis
    }

    override fun fileOutputStream(fileName: String): FileOutputStream? {
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(File(filesDir, fileName))
        } catch (ex: FileNotFoundException) {
            LogUtil.e(TAG, "fileOutputStream.FileNotFoundException: ", ex)
        }
        return fos
    }

    override fun getColorBallMap(): HashMap<Int, Bitmap> {
        return mColorBallMap
    }

    override fun getColorOvalBallMap(): HashMap<Int, Bitmap> {
        return mColorOvalBallMap
    }

    override fun getImageViewById(id: Int): ImageView {
        return findViewById(id)
    }

    override fun updateHighestScoreOnUi(highestScore: Int) {
        toolbarTitleTextView?.text = String.format(Locale.ENGLISH, "%8d", highestScore)
    }

    override fun updateCurrentScoreOnUi(score: Int) {
        currentScoreView?.text = String.format(Locale.ENGLISH, "%8d", score)
    }

    override fun showMessageOnScreen(message: String) {
        val dialogBoardImage = BitmapFactory.decodeResource(
            resources,R.drawable.dialog_board_image)
        val showBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(
            dialogBoardImage, message, Color.RED)
        scoreImageView?.setVisibility(View.VISIBLE)
        scoreImageView?.setImageBitmap(showBitmap)
    }

    override fun dismissShowMessageOnScreen() {
        scoreImageView?.setImageBitmap(null)
        scoreImageView?.setVisibility(View.GONE)
    }

    override fun showSaveGameDialog() {
        sureSaveDialog = AlertDialogFragment.newInstance(object : DialogButtonListener {
            override fun noButtonOnClick(dialogFragment: AlertDialogFragment) {
                // cancel the action of saving game
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingSureSaveDialog(false)
            }

            override fun okButtonOnClick(dialogFragment: AlertDialogFragment) {
                // start saving game to internal storage
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingSureSaveDialog(false)
                val succeeded = mPresenter.startSavingGame()
                val msg = if (succeeded) {
                    getString(R.string.succeededSaveGameString)
                } else {
                    getString(R.string.failedSaveGameString)
                }
                LogUtil.d(TAG, "showSaveGameDialog.okButtonOnClick.msg = $msg")
                ScreenUtil.showToast(this@MyActivity, msg, textFontSize, Toast.LENGTH_LONG)
            }
        })
        val args = Bundle()
        args.putString("TextContent", getString(R.string.sureToSaveGameString))
        args.putInt("FontSize_Scale_Type", ScreenUtil.FontSize_Pixel_Type)
        args.putFloat("TextFontSize", textFontSize)
        args.putInt("Color", Color.BLUE)
        args.putInt("Width", 0) // wrap_content
        args.putInt("Height", 0) // wrap_content
        args.putInt("NumButtons", 2)
        args.putBoolean("IsAnimation", false)

        mPresenter.setShowingSureSaveDialog(true)
        sureSaveDialog?.arguments = args
        sureSaveDialog?.show(supportFragmentManager, "SureSaveDialogTag")
    }

    override fun showLoadGameDialog() {
        sureLoadDialog = AlertDialogFragment.newInstance(object : DialogButtonListener {
            override fun noButtonOnClick(dialogFragment: AlertDialogFragment) {
                // cancel the action of loading game
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingSureLoadDialog(false)
            }

            override fun okButtonOnClick(dialogFragment: AlertDialogFragment) {
                // start loading game to internal storage
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingSureLoadDialog(false)
                val succeeded = mPresenter.startLoadingGame()
                val msg = if (succeeded) {
                    getString(R.string.succeededLoadGameString)
                } else {
                    getString(R.string.failedLoadGameString)
                }
                ScreenUtil.showToast(this@MyActivity, msg, textFontSize, Toast.LENGTH_LONG)
            }
        })
        val args = Bundle()
        args.putString("TextContent", getString(R.string.sureToLoadGameString))
        args.putInt("FontSize_Scale_Type", ScreenUtil.FontSize_Pixel_Type)
        args.putFloat("TextFontSize", textFontSize)
        args.putInt("Color", Color.BLUE)
        args.putInt("Width", 0) // wrap_content
        args.putInt("Height", 0) // wrap_content
        args.putInt("NumButtons", 2)
        args.putBoolean("IsAnimation", false)

        mPresenter.setShowingSureLoadDialog(true)
        sureLoadDialog?.arguments = args
        sureLoadDialog?.show(supportFragmentManager, "SureLoadDialogTag")
    }

    override fun showGameOverDialog() {
        gameOverDialog = AlertDialogFragment.newInstance(object : DialogButtonListener {
            override fun noButtonOnClick(dialogFragment: AlertDialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.quitGame() //   Ending the game
            }
            override fun okButtonOnClick(dialogFragment: AlertDialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.newGame()
            }
        })
        val args = Bundle()
        args.putString("TextContent", getString(R.string.gameOverStr))
        args.putInt("FontSize_Scale_Type", ScreenUtil.FontSize_Pixel_Type)
        args.putFloat("TextFontSize", textFontSize)
        args.putInt("Color", Color.BLUE)
        args.putInt("Width", 0) // wrap_content
        args.putInt("Height", 0) // wrap_content
        args.putInt("NumButtons", 2)
        args.putBoolean("IsAnimation", false)

        gameOverDialog?.arguments = args
        gameOverDialog?.show(supportFragmentManager, GAME_OVER_DIALOG_TAG)

        LogUtil.d(TAG, "gameOverDialog.show() has been called.")
    }

    override fun showSaveScoreAlertDialog(entryPoint: Int) {
        mPresenter.setSaveScoreAlertDialogState(entryPoint, true)
        val et = EditText(this)
        et.setTextColor(Color.BLUE)
        et.setHint(getString(R.string.nameStr))
        ScreenUtil.resizeTextSize(et, textFontSize)
        et.setGravity(Gravity.CENTER)
        saveScoreAlertDialog = AlertDialog.Builder(this).create()
        saveScoreAlertDialog?.setTitle(null)
        saveScoreAlertDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        saveScoreAlertDialog?.setCancelable(false)
        saveScoreAlertDialog?.setView(et)
        saveScoreAlertDialog?.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.cancelStr)
        ) { dialog: DialogInterface?, which: Int ->
            dialog!!.dismiss()
            quitOrNewGame(entryPoint)
            mPresenter.setSaveScoreAlertDialogState(entryPoint, false)
            saveScoreAlertDialog = null
        }
        saveScoreAlertDialog?.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(R.string.submitStr)
        ) { dialog: DialogInterface?, which: Int ->
            mPresenter.saveScore(et.getText().toString())
            dialog!!.dismiss()
            quitOrNewGame(entryPoint)
            mPresenter.setSaveScoreAlertDialogState(entryPoint, false)
            saveScoreAlertDialog = null
        }

        saveScoreAlertDialog?.setOnShowListener { dialog: DialogInterface? ->
            this.setDialogStyle(
                dialog
            )
        }

        saveScoreAlertDialog?.show()
    } // end of implementing
}
