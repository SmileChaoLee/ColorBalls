package com.smile.colorballs.coroutines

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.colorballs.constants.Constants
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Top10Coroutine {
    const val ACTION_NAME = "com.smile.Service.LocalTop10Service"
    private const val TAG = "LocalTop10Coroutine"
    var isBroadcastSent = false

    suspend fun getTop10(context : Context, isLocal: Boolean) {
        Log.d(TAG, "getTop10")
        try {
            when(isLocal) {
                true -> {
                    Log.d(TAG, "getTop10.getLocalAndSendBack")
                    getLocalAndSendBack(context)
                }
                false -> {
                    Log.d(TAG, "getTop10.getGlobalAndSendBack")
                    getGlobalAndSendBack(context)
                }
            }
        } catch (ex: Exception) {
            Log.d(TAG, "getTop10.Exception")
        }
    }

    suspend fun getGlobalAndSendBack(context: Context) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "getLocalAndSendBack.PlayerRecordRest.GetGlobalTop10()")
            val players = PlayerRecordRest.GetGlobalTop10("1")
            Intent(ACTION_NAME).let {
                Bundle().apply {
                    putParcelableArrayList(Constants.TOP10_PLAYERS, players)
                    it.putExtras(this)
                }
                LocalBroadcastManager.getInstance(context).apply {
                    Log.d(TAG, "getLocalAndSendBack.sent result.")
                    sendBroadcast(it)
                    isBroadcastSent = true
                }
            }
        }
    }

    suspend fun getLocalAndSendBack(context : Context) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "getLocalAndSendBack")
            ScoreSQLite(context).let {
                Log.d(TAG, "getLocalAndSendBack.PlayerRecordRest.GetLocalTop10()")
                val players = PlayerRecordRest.GetLocalTop10(it)
                it.close()
                Intent(ACTION_NAME).let {
                    Bundle().apply {
                        putParcelableArrayList(Constants.TOP10_PLAYERS, players)
                        it.putExtras(this)
                    }
                    LocalBroadcastManager.getInstance(context).apply {
                        Log.d(TAG, "getLocalAndSendBack.sent result.")
                        sendBroadcast(it)
                        isBroadcastSent = true
                    }
                }
            }
        }
    }
}