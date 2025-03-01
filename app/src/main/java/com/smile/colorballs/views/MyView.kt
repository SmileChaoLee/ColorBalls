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
import com.smile.colorballs.constants.Constants
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
    }

    abstract fun showInterstitialAd()
    abstract fun quitOrNewGame(entryPoint: Int)
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

    override fun showMessageOnScreen(message: String) {
        BitmapFactory.decodeResource(resources, R.drawable.dialog_board_image).let {
            FontAndBitmapUtil.getBitmapFromBitmapWithText(
                it, message, Color.RED).apply {
                scoreImageView.visibility = View.VISIBLE
                scoreImageView.setImageBitmap(this)
            }
        }
    }

    override fun dismissShowMessageOnScreen() {
        scoreImageView.setImageBitmap(null)
        scoreImageView.visibility = View.GONE
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
                ScreenUtil.showToast(this@MyView, msg, textFontSize,
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
            sureSaveDialog.arguments = this
            sureSaveDialog.show(supportFragmentManager, Constants.SURE_SAVE_DIALOG_TAG)
        }
        val fragment = supportFragmentManager.findFragmentByTag(Constants.SURE_SAVE_DIALOG_TAG)
        Log.d(TAG,"MyView.showSaveGameDialog.fragment = $fragment")
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
                if (mPresenter.startLoadingGame()) {
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
            sureLoadDialog.arguments = this
            sureLoadDialog.show(supportFragmentManager, Constants.SURE_LOAD_DIALOG_TAG)
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
            gameOverDialog.arguments = this
            gameOverDialog.show(supportFragmentManager, Constants.GAME_OVER_DIALOG_TAG)
            Log.d(TAG, "gameOverDialog.show() has been called.")
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
        saveScoreAlertDialog.setTitle(null)
        saveScoreAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        saveScoreAlertDialog.setCancelable(false)
        saveScoreAlertDialog.setView(et)
        saveScoreAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.cancelStr)
        ) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            quitOrNewGame(entryPoint)
            mPresenter.setSaveScoreAlertDialogState(entryPoint, false)
            // saveScoreAlertDialog = null
        }
        saveScoreAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
            getString(R.string.submitStr)
        ) { dialog: DialogInterface, _: Int ->
            mPresenter.saveScore(et.text.toString(), score)
            dialog.dismiss()
            quitOrNewGame(entryPoint)
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