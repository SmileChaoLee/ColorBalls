package com.smile.colorballs.views.compose

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.interfaces.PresentViewCompose
import com.smile.colorballs.presenters.PresenterCompose
import com.smile.colorballs.views.compose.MainActivity.Companion
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment.DialogButtonListener
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.utilities.FontAndBitmapUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

abstract class MyViewCompose: ComponentActivity(), PresentViewCompose {

    companion object {
        private const val TAG = "MyViewCompose"
    }

    abstract fun showInterstitialAd()
    abstract fun quitOrNewGame(entryPoint: Int)
    abstract fun setDialogStyle(dialog: DialogInterface)

    protected var textFontSize = 0f
    protected lateinit var mPresenter: PresenterCompose

    protected var mImageSizeDp = 0f
    protected var boxImage: Bitmap? = null
    protected val colorBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorOvalBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorNextBallMap: HashMap<Int, Bitmap> = HashMap()

    private var sureSaveDialog: AlertDialogFragment? = null
    private var sureLoadDialog: AlertDialogFragment? = null
    private var gameOverDialog: AlertDialogFragment? = null
    protected var saveScoreAlertDialog: AlertDialog? = null

    protected fun bitmapDrawableResources() {
        val imageSizePx = ScreenUtil.dpToPixel(mImageSizeDp)
        Log.d(TAG, "bitmapDrawableResources.mImageSizeDp = $mImageSizeDp")
        Log.w(TAG, "bitmapDrawableResources.imageSizePx = $imageSizePx")
        val ballWidth = imageSizePx.toInt()
        val ballHeight = imageSizePx.toInt()
        val nextBallWidth = (imageSizePx * 0.5f).toInt()
        val nextBallHeight = (imageSizePx * 0.5f).toInt()
        val ovalBallWidth = (imageSizePx * 0.9f).toInt()
        val ovalBallHeight = (imageSizePx * 0.7f).toInt()

        BitmapFactory.decodeResource(resources, R.drawable.box_image).let { bm ->
            boxImage = createScaledBitmap(bm, ballWidth, ballHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.redball)?.let { bm ->
            colorBallMap[Constants.COLOR_RED] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_RED] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_RED] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.greenball)?.let { bm ->
            colorBallMap[Constants.COLOR_GREEN] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_GREEN] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_GREEN] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.blueball)?.let { bm ->
            colorBallMap[Constants.COLOR_BLUE] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_BLUE] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_BLUE] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.magentaball)?.let { bm ->
            colorBallMap[Constants.COLOR_MAGENTA] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_MAGENTA] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_MAGENTA] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.yellowball)?.let { bm ->
            colorBallMap[Constants.COLOR_YELLOW] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_YELLOW] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_YELLOW] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.cyanball)?.let { bm ->
            colorBallMap[Constants.COLOR_CYAN] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_CYAN] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_CYAN] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }
    }

    // implementing PresentView
    override fun soundPool(): SoundPoolUtil {
        return SoundPoolUtil(this, R.raw.uhoh)
    }

    override fun getHighestScore() : Int {
        Log.d(TAG, "getHighestScore")
        val scoreSQLiteDB = ScoreSQLite(this)
        val score = scoreSQLiteDB.readHighestScore()
        scoreSQLiteDB.close()
        return score
    }

    override fun addScoreInLocalTop10(playerName : String, score : Int) {
        val scoreSQLiteDB = ScoreSQLite(this)
        if (scoreSQLiteDB.isInTop10(score)) {
            // inside top 10, then record the current score
            scoreSQLiteDB.addScore(playerName, score)
            scoreSQLiteDB.deleteAllAfterTop10() // only keep the top 10
        }
        scoreSQLiteDB.close()
    }

    override fun fileInputStream(fileName : String): FileInputStream {
        return FileInputStream(File(filesDir, fileName))
    }

    override fun fileOutputStream(fileName : String): FileOutputStream {
        return FileOutputStream(File(filesDir, fileName))
    }

    private fun bitmapToDrawable(bm : Bitmap, width : Int, height : Int) : Drawable? {
        return FontAndBitmapUtil.convertBitmapToDrawable(this, bm,
            width, height)
    }

    override fun showLoadingStrOnScreen() {
        mPresenter.screenMessage.value = getString(R.string.loadingStr)
    }

    override fun showSavingGameStrOnScreen() {
        mPresenter.screenMessage.value = getString(R.string.savingGameStr)
    }

    override fun showLoadingGameStrOnScreen() {
        mPresenter.screenMessage.value = getString(R.string.loadingGameStr)
    }

    override fun showSaveGameDialog() {
        Log.d(TAG, "showSaveGameDialog")
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
                val numOfSaved: Int = mPresenter.readNumberOfSaved()
                val msg =
                    if (mPresenter.startSavingGame(numOfSaved)) {
                        getString(R.string.succeededSaveGameStr)
                    } else {
                        getString(R.string.failedSaveGameStr)
                    }
                ScreenUtil.showToast(this@MyViewCompose, msg, textFontSize,
                    ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                showInterstitialAd()
            }
        })
        Bundle().apply {
            putString(AlertDialogFragment.TextContentKey, getString(R.string.sureToSaveGameStr))
            putInt(AlertDialogFragment.FontSizeScaleTypeKey, ScreenUtil.FontSize_Pixel_Type)
            putFloat(AlertDialogFragment.TextFontSizeKey, textFontSize)
            putInt(AlertDialogFragment.ColorKey, Color.BLUE)
            putInt(AlertDialogFragment.WidthKey, 0) // wrap_content
            putInt(AlertDialogFragment.HeightKey, 0) // wrap_content
            putInt(AlertDialogFragment.NumButtonsKey, 2)
            putBoolean(AlertDialogFragment.IsAnimationKey, false)
            mPresenter.setShowingSureSaveDialog(true)
            sureSaveDialog?.let {
                it.arguments = this
                // Need to implement this
                // it.show(supportFragmentManager, Constants.SURE_SAVE_DIALOG_TAG)
            }
        }
        // Need to implement this
        // val fragment = supportFragmentManager.findFragmentByTag(Constants.SURE_SAVE_DIALOG_TAG)
        // Log.d(TAG,"MyView.showSaveGameDialog.fragment = $fragment")
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
                /*
                if (mPresenter.startLoadingGame()) {
                    ScreenUtil.showToast(
                        this@MyViewCompose,
                        getString(R.string.succeededLoadGameStr),
                        textFontSize,
                        ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_LONG
                    )
                } else {
                    ScreenUtil.showToast(
                        this@MyViewCompose,
                        getString(R.string.failedLoadGameStr),
                        textFontSize,
                        ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_LONG
                    )
                }
                */
            }
        })
        Bundle().apply {
            putString(AlertDialogFragment.TextContentKey, getString(R.string.sureToLoadGameStr))
            putInt(AlertDialogFragment.FontSizeScaleTypeKey, ScreenUtil.FontSize_Pixel_Type)
            putFloat(AlertDialogFragment.TextFontSizeKey, textFontSize)
            putInt(AlertDialogFragment.ColorKey, Color.BLUE)
            putInt(AlertDialogFragment.WidthKey, 0) // wrap_content
            putInt(AlertDialogFragment.HeightKey, 0) // wrap_content
            putInt(AlertDialogFragment.NumButtonsKey, 2)
            putBoolean(AlertDialogFragment.IsAnimationKey, false)
            mPresenter.setShowingSureLoadDialog(true)
            sureLoadDialog?.let {
                it.arguments = this
                // Need to implement this
                // it.show(supportFragmentManager, Constants.SURE_LOAD_DIALOG_TAG)
            }
        }
    }

    override fun showGameOverDialog() {
        gameOverDialog = AlertDialogFragment.newInstance(object : DialogButtonListener {
            override fun noButtonOnClick(dialogFragment: AlertDialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingGameOverDialog(false)
                mPresenter.quitGame()
            }

            override fun okButtonOnClick(dialogFragment: AlertDialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingGameOverDialog(false)
                mPresenter.newGame()
            }
        })
        Bundle().apply {
            putString(AlertDialogFragment.TextContentKey, getString(R.string.gameOverStr))
            putInt(AlertDialogFragment.FontSizeScaleTypeKey, ScreenUtil.FontSize_Pixel_Type)
            putFloat(AlertDialogFragment.TextFontSizeKey, textFontSize)
            putInt(AlertDialogFragment.ColorKey, Color.BLUE)
            putInt(AlertDialogFragment.WidthKey, 0) // wrap_content
            putInt(AlertDialogFragment.HeightKey, 0) // wrap_content
            putInt(AlertDialogFragment.NumButtonsKey, 2)
            putBoolean(AlertDialogFragment.IsAnimationKey, false)
            mPresenter.setShowingGameOverDialog(true)
            gameOverDialog?.let {
                it.arguments = this
                // Need to implement this
                // it.show(supportFragmentManager, Constants.GAME_OVER_DIALOG_TAG)
                Log.d(TAG, "gameOverDialog.show() has been called.")
            }
        }
    }

    override fun showSaveScoreAlertDialog(entryPoint: Int, score: Int) {
        mPresenter.setSaveScoreAlertDialogState(entryPoint, true)
        val et = EditText(this)
        et.setTextColor(Color.BLUE)
        et.hint = getString(R.string.nameStr)
        ScreenUtil.resizeTextSize(et, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        et.gravity = Gravity.CENTER
        saveScoreAlertDialog = AlertDialog.Builder(this).create()
        saveScoreAlertDialog?.let {
            it.setTitle(null)
            it.requestWindowFeature(Window.FEATURE_NO_TITLE)
            it.setCancelable(false)
            it.setView(et)
            it.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancelStr)
            ) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                quitOrNewGame(entryPoint)
                mPresenter.setSaveScoreAlertDialogState(entryPoint, false)
                // saveScoreAlertDialog = null
            }
            it.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.submitStr)
            ) { dialog: DialogInterface, _: Int ->
                mPresenter.saveScore(et.text.toString(), score)
                dialog.dismiss()
                quitOrNewGame(entryPoint)
                mPresenter.setSaveScoreAlertDialogState(entryPoint, false)
                // saveScoreAlertDialog = null
            }
            it.setOnShowListener { style ->
                setDialogStyle(style)
            }
            it.show()
        }
    }
    // end of implementing
}