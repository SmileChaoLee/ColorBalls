package com.smile.colorballs.views

import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.smile.colorballs.R
import com.smile.colorballs.interfaces.PresentView
import com.smile.colorballs.presenters.MyPresenter
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment.DialogButtonListener
import com.smile.smilelibraries.utilities.FontAndBitmapUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import java.util.Locale

abstract class MyView: AppCompatActivity(), PresentView {

    companion object {
        private const val TAG = "MyView"
        private const val GameOverDialogTag = "GameOverDialogFragmentTag"
        private const val MaxSavedGames = 5
    }

    abstract fun showAdUntilDismissed()
    abstract fun showInterstitialAdAndNewGameOrQuit(entryPoint: Int)
    abstract fun setDialogStyle(dialog: DialogInterface)

    protected var textFontSize = 0f
    protected lateinit var mPresenter: MyPresenter
    protected lateinit var highestScoreTextView: TextView
    protected lateinit var currentScoreTextView: TextView
    protected lateinit var scoreImageView: ImageView
    protected lateinit var sureSaveDialog: AlertDialogFragment
    protected lateinit var warningSaveGameDialog: AlertDialogFragment
    protected lateinit var sureLoadDialog: AlertDialogFragment
    protected lateinit var gameOverDialog: AlertDialogFragment
    protected lateinit var saveScoreAlertDialog: AlertDialog

    // implementing PresentView
    override fun getImageViewById(id: Int): ImageView {
        return findViewById(id)
    }

    override fun updateHighestScoreOnUi(highestScore: Int) {
        highestScoreTextView.text = String.format(Locale.getDefault(), "%8d", highestScore)
    }

    override fun updateCurrentScoreOnUi(score: Int) {
        currentScoreTextView.text = String.format(Locale.getDefault(), "%8d", score)
    }

    override fun showMessageOnScreen(messageString: String) {
        val dialogBoardImage =
            BitmapFactory.decodeResource(resources, R.drawable.dialog_board_image)
        val showBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(
            dialogBoardImage,
            messageString,
            Color.RED
        )
        scoreImageView.visibility = View.VISIBLE
        scoreImageView.setImageBitmap(showBitmap)
    }

    override fun dismissShowMessageOnScreen() {
        scoreImageView?.setImageBitmap(null)
        scoreImageView?.visibility = View.GONE
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
                val numOfSaved: Int = mPresenter.readNumberOfSaved()
                if (numOfSaved < MaxSavedGames) {
                    val succeeded: Boolean = mPresenter.startSavingGame(numOfSaved)
                    if (succeeded) {
                        ScreenUtil.showToast(
                            this@MyView,
                            getString(R.string.succeededSaveGameStr),
                            textFontSize,
                            ScreenUtil.FontSize_Pixel_Type,
                            Toast.LENGTH_LONG
                        )
                    } else {
                        ScreenUtil.showToast(
                            this@MyView,
                            getString(R.string.failedSaveGameStr),
                            textFontSize,
                            ScreenUtil.FontSize_Pixel_Type,
                            Toast.LENGTH_LONG
                        )
                    }
                } else {
                    // display warning to users
                    // final int finalNumOfSaved = numOfSaved;
                    showingWarningSaveGameDialog(numOfSaved)
                }
            }
        })
        val args = Bundle()
        args.putString(AlertDialogFragment.TextContentKey, getString(R.string.sureToSaveGameStr))
        args.putInt(AlertDialogFragment.FontSizeScaleTypeKey, ScreenUtil.FontSize_Pixel_Type)
        args.putFloat(AlertDialogFragment.TextFontSizeKey, textFontSize)
        args.putInt(AlertDialogFragment.ColorKey, Color.BLUE)
        args.putInt(AlertDialogFragment.WidthKey, 0) // wrap_content
        args.putInt(AlertDialogFragment.HeightKey, 0) // wrap_content
        args.putInt(AlertDialogFragment.NumButtonsKey, 2)
        args.putBoolean(AlertDialogFragment.IsAnimationKey, false)
        mPresenter.setShowingSureSaveDialog(true)
        sureSaveDialog.arguments = args
        sureSaveDialog.show(supportFragmentManager, "SureSaveDialogTag")
    }

    override fun showingWarningSaveGameDialog(finalNumOfSaved: Int) {
        warningSaveGameDialog = AlertDialogFragment.newInstance(object : DialogButtonListener {
            override fun noButtonOnClick(dialogFragment: AlertDialogFragment) {
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingWarningSaveGameDialog(false)
            }

            override fun okButtonOnClick(dialogFragment: AlertDialogFragment) {
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingWarningSaveGameDialog(false)
                val succeeded: Boolean = mPresenter.startSavingGame(finalNumOfSaved)
                if (succeeded) {
                    ScreenUtil.showToast(
                        this@MyView,
                        getString(R.string.succeededSaveGameStr),
                        textFontSize,
                        ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_LONG
                    )
                } else {
                    ScreenUtil.showToast(
                        this@MyView,
                        getString(R.string.failedSaveGameStr),
                        textFontSize,
                        ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_LONG
                    )
                }
                showAdUntilDismissed()
            }
        })
        val args = Bundle()
        val warningSaveGameString0 =
            """${getString(R.string.warningSaveGameStr)} ($MaxSavedGames ${
                getString(
                    R.string.howManyTimesStr
                )
            } )
${getString(R.string.continueStr)}?"""
        args.putString(
            AlertDialogFragment.TextContentKey,
            warningSaveGameString0
        ) // excessive the number (5)
        args.putInt(AlertDialogFragment.FontSizeScaleTypeKey, ScreenUtil.FontSize_Pixel_Type)
        args.putFloat(AlertDialogFragment.TextFontSizeKey, textFontSize)
        args.putInt(AlertDialogFragment.ColorKey, Color.BLUE)
        args.putInt(AlertDialogFragment.WidthKey, 0) // wrap_content
        args.putInt(AlertDialogFragment.HeightKey, 0) // wrap_content
        args.putInt(AlertDialogFragment.NumButtonsKey, 2)
        args.putBoolean(AlertDialogFragment.IsAnimationKey, false)
        mPresenter.setShowingWarningSaveGameDialog(true)
        warningSaveGameDialog.arguments = args
        warningSaveGameDialog.show(supportFragmentManager, "SaveGameWarningDialogTag")
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
                val succeeded: Boolean = mPresenter.startLoadingGame()
                if (succeeded) {
                    ScreenUtil.showToast(
                        this@MyView,
                        getString(R.string.succeededLoadGameStr),
                        textFontSize,
                        ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_LONG
                    )
                } else {
                    ScreenUtil.showToast(
                        this@MyView,
                        getString(R.string.failedLoadGameStr),
                        textFontSize,
                        ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_LONG
                    )
                }
            }
        })
        val args = Bundle()
        args.putString(AlertDialogFragment.TextContentKey, getString(R.string.sureToLoadGameStr))
        args.putInt(AlertDialogFragment.FontSizeScaleTypeKey, ScreenUtil.FontSize_Pixel_Type)
        args.putFloat(AlertDialogFragment.TextFontSizeKey, textFontSize)
        args.putInt(AlertDialogFragment.ColorKey, Color.BLUE)
        args.putInt(AlertDialogFragment.WidthKey, 0) // wrap_content
        args.putInt(AlertDialogFragment.HeightKey, 0) // wrap_content
        args.putInt(AlertDialogFragment.NumButtonsKey, 2)
        args.putBoolean(AlertDialogFragment.IsAnimationKey, false)
        mPresenter.setShowingSureLoadDialog(true)
        sureLoadDialog.arguments = args
        sureLoadDialog.show(supportFragmentManager, "SureLoadDialogTag")
    }

    override fun showGameOverDialog() {
        gameOverDialog = AlertDialogFragment.newInstance(object : DialogButtonListener {
            override fun noButtonOnClick(dialogFragment: AlertDialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingGameOverDialog(false)
                mPresenter.quitGame() //   Ending the game
            }

            override fun okButtonOnClick(dialogFragment: AlertDialogFragment) {
                // dialogFragment.dismiss();
                dialogFragment.dismissAllowingStateLoss()
                mPresenter.setShowingGameOverDialog(false)
                mPresenter.newGame()
            }
        })
        val args = Bundle()
        args.putString(AlertDialogFragment.TextContentKey, getString(R.string.gameOverStr))
        args.putInt(AlertDialogFragment.FontSizeScaleTypeKey, ScreenUtil.FontSize_Pixel_Type)
        args.putFloat(AlertDialogFragment.TextFontSizeKey, textFontSize)
        args.putInt(AlertDialogFragment.ColorKey, Color.BLUE)
        args.putInt(AlertDialogFragment.WidthKey, 0) // wrap_content
        args.putInt(AlertDialogFragment.HeightKey, 0) // wrap_content
        args.putInt(AlertDialogFragment.NumButtonsKey, 2)
        args.putBoolean(AlertDialogFragment.IsAnimationKey, false)
        mPresenter.setShowingGameOverDialog(true)
        gameOverDialog.arguments = args
        gameOverDialog.show(supportFragmentManager, GameOverDialogTag)
        Log.d(TAG, "gameOverDialog.show() has been called.")
    }

    override fun showSaveScoreAlertDialog(entryPoint: Int, score: Int) {
        mPresenter.setSaveScoreAlertDialogState(entryPoint, true)
        val et = EditText(this)
        et.setTextColor(Color.BLUE)
        et.hint = getString(R.string.nameStr)
        ScreenUtil.resizeTextSize(et, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        et.gravity = Gravity.CENTER
        saveScoreAlertDialog = AlertDialog.Builder(this).create()
        saveScoreAlertDialog.setTitle(null)
        saveScoreAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        saveScoreAlertDialog.setCancelable(false)
        saveScoreAlertDialog.setView(et)
        saveScoreAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.cancelStr)
        ) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            showInterstitialAdAndNewGameOrQuit(entryPoint)
            mPresenter.setSaveScoreAlertDialogState(entryPoint, false)
            // saveScoreAlertDialog = null
        }
        saveScoreAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
            getString(R.string.submitStr)
        ) { dialog: DialogInterface, _: Int ->
            mPresenter.saveScore(et.text.toString(), score)
            dialog.dismiss()
            showInterstitialAdAndNewGameOrQuit(entryPoint)
            mPresenter.setSaveScoreAlertDialogState(entryPoint, false)
            // saveScoreAlertDialog = null
        }
        saveScoreAlertDialog.setOnShowListener {
            setDialogStyle(it)
        }
        saveScoreAlertDialog.show()
    }

    // end of implementing
}