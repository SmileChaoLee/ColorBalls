package com.smile.colorballs.coroutines

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.colorballs.constants.Constants
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalTop10Coroutine {
    companion object {
        const val ACTION_NAME = "com.smile.Service.LocalTop10Service"
        private const val TAG = "LocalTop10Coroutine"
        var isBroadcastSent = false

        fun getLocalTop10(context : Context) {
            Log.d(TAG, "getLocalTop10")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(TAG, "getLocalTop10.getDataAndSendBack")
                    getDataAndSendBack(context)
                } catch (ex : Exception) {
                    Log.d(TAG, "getLocalTop10.CoroutineScope Exception")
                }
                Log.d(TAG, "getLocalTop10.finished CoroutineScope")
            }
        }

        suspend fun getDataAndSendBack(context : Context) {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "getDataAndSendBack")
                ScoreSQLite(context).let {
                    Log.d(TAG, "getDataAndSendBack.PlayerRecordRest.GetLocalTop10()")
                    val players = PlayerRecordRest.GetLocalTop10(it)
                    it.close()
                    Intent(ACTION_NAME).let {
                        Bundle().apply {
                            putParcelableArrayList(Constants.TOP10_PLAYERS, players)
                            it.putExtras(this)
                        }
                        LocalBroadcastManager.getInstance(context).apply {
                            Log.d(TAG, "getDataAndSendBack.sent result.")
                            sendBroadcast(it)
                            isBroadcastSent = true
                        }
                    }
                }
            }
        }
    }
}