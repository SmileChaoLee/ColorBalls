package com.smile.colorballs.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.models.TopPlayer
import com.smile.colorballs.views.ui.theme.ColorBallsTheme
import com.smile.smilelibraries.models.Player
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.colorballs.roomdatabase.ScoreDatabase
import com.smile.colorballs.tools.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Top10Activity : ComponentActivity() {

    private val top10Players = mutableStateOf(listOf<TopPlayer>())
    private var top10TitleName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.i(TAG, "onCreate")

        var gameId = Constants.GAME_NO_BARRIER_ID
        var databaseName = Constants.NO_BARRIER_DATABASE_NAME
        var isLocal = false // default is Global top10
        intent.extras?.let {
            gameId = it.getString(Constants.GAME_ID, gameId)
            databaseName = it.getString(Constants.DATABASE_NAME,databaseName)
            isLocal = it.getBoolean(Constants.IS_LOCAL_TOP10, isLocal)
        }
        top10TitleName =
            if (isLocal) getString(R.string.localTop10Score) else
                getString(R.string.globalTop10Str)
        val medalImageIds = listOf(
            R.drawable.gold_medal,
            R.drawable.silver_medal,
            R.drawable.bronze_medal,
            R.drawable.copper_medal,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image)

        setContent {
            LogUtil.d(TAG, "onCreate.setContent.Composables.Top10Compose")
            ColorBallsTheme {
                LaunchedEffect(Unit) {
                    LogUtil.d(TAG, "onCreate.setContent.LaunchedEffect")
                    launch(Dispatchers.IO) {
                        val players: ArrayList<Player>
                        if (isLocal) {
                            val db = ScoreDatabase.getDatabase(this@Top10Activity,
                                databaseName)
                            players = db.getLocalTop10()
                            db.close()
                        } else {
                            players = PlayerRecordRest.GetGlobalTop10(gameId)
                        }
                        val top10 = ArrayList<TopPlayer>()
                        for (i in 0 until players.size) {
                            players[i].playerName?.let { name ->
                                if (name.trim().isEmpty()) players[i].playerName = "No Name"
                            } ?: run {
                                LogUtil.d(TAG, "onCreate.players[i].playerName = null")
                                players[i].playerName = "No Name"
                            }
                            top10.add(TopPlayer(players[i], medalImageIds[i]))
                        }
                        top10Players.value = top10
                    }
                }
                LogUtil.d(TAG, "onCreate.setContent.Composables.Top10Compose")
                CbComposable.Top10Composable(
                    title = top10TitleName,
                    topPlayers = top10Players.value, buttonListener =
                    object : CbComposable.ButtonClickListener {
                        override fun buttonOkClick() {
                            LogUtil.d(TAG, "Composables.OkButtonListener.buttonOkClick")
                            setResult(RESULT_OK)
                            finish()
                        }
                    },
                    getString(R.string.okStr)
                )
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                setResult(RESULT_OK)
                finish()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i(TAG, "onStart()")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume()")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtil.i(TAG, "onSaveInstanceState()")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy()")
    }

    companion object {
        private const val TAG = "Top10Activity"
    }
}