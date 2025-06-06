package com.smile.colorballs.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smile.colorballs.models.TopPlayer
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainComposeViewModel : ViewModel() {

    lateinit var medalImageIds: List<Int>

    private val _top10Players = mutableStateOf(listOf<TopPlayer>())
    val top10Players: MutableState<List<TopPlayer>>
        get() = _top10Players

    private val _top10TitleName = mutableStateOf("")
    val top10TitleName: MutableState<String>
        get() = _top10TitleName
    fun setTop10TitleName(titleName: String) {
        _top10TitleName.value = titleName
    }

    private val _settingTitle = mutableStateOf("")
    val settingTitle: MutableState<String>
        get() = _settingTitle
    fun setSettingTitle(titleName: String) {
        _settingTitle.value = titleName
    }

    fun getTop10Players(context: Context, isLocal: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "showTop10Players.lifecycleScope")
            val players = if (isLocal) {
                PlayerRecordRest.GetLocalTop10(ScoreSQLite(context))
            } else {
                PlayerRecordRest.GetGlobalTop10("1")
            }
            val top10 = ArrayList<TopPlayer>()
            for (i in 0 until players.size) {
                players[i].playerName?.let { name ->
                    if (name.trim().isEmpty()) players[i].playerName = "No Name"
                } ?: run {
                    Log.d(TAG, "showTop10Players.players[i].playerName = null")
                    players[i].playerName = "No Name"
                }
                top10.add(TopPlayer(players[i], medalImageIds[i]))
            }
            _top10Players.value = top10
        }
    }

    companion object {
        private const val TAG = "MainComposeViewModel"
    }
}