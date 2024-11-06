package com.smile.colorballs.coroutines

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.colorballs.Constants
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalTop10Coroutine {
    companion object {
        const val ACTION_NAME = "com.smile.Service.LocalTop10Service"
        private const val TAG = "LocalTop10Coroutine"

        fun getLocalTop10(context : Context) {
            Log.d(TAG, "getLocalTop10")
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    Log.d(TAG, "getLocalTop10.getDataAndSendBack")
                    getDataAndSendBack(context)
                } catch (ex : Exception) {
                    Log.d(TAG, "getLocalTop10.CoroutineScope Exception")
                }
                Log.d(TAG, "getLocalTop10.finished CoroutineScope")
            }
        }

        private fun getDataAndSendBack(context : Context) {
            val playerNames = ArrayList<String>()
            val playerScores = ArrayList<Int>()
            ScoreSQLite(context).let {
                PlayerRecordRest.GetLocalTop10(it, playerNames, playerScores)
                it.close()
            }
            Intent(ACTION_NAME).let {
                Bundle().apply {
                    putStringArrayList(Constants.PlayerNamesKey, playerNames)
                    putIntegerArrayList(Constants.PlayerScoresKey, playerScores)
                    it.putExtras(this)
                }
                LocalBroadcastManager.getInstance(context).apply {
                    Log.d(TAG, "getDataAndSendBack.sent result.")
                    sendBroadcast(it)
                }
            }
        }
    }
}