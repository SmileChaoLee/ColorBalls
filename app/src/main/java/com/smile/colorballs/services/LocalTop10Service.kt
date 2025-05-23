package com.smile.colorballs.services

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.colorballs.constants.Constants
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite

class LocalTop10Service : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        getDataAndSendBack()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun getDataAndSendBack() {
        Log.d(TAG, "getDataAndSendBack")
        ScoreSQLite(applicationContext).let {
            Log.d(TAG, "getDataAndSendBack.PlayerRecordRest.GetLocalTop10()")
            val players = PlayerRecordRest.GetLocalTop10(it)
            it.close()
            Intent(Constants.LOCAL_TOP10_ACTION_NAME).let {
                Bundle().apply {
                    putParcelableArrayList(Constants.TOP10_PLAYERS, players)
                    it.putExtras(this)
                }
                LocalBroadcastManager.getInstance(applicationContext).apply {
                    Log.d(TAG, "getDataAndSendBack.sent result.")
                    sendBroadcast(it)
                }
            }
        }
        Log.d(TAG, "getDataAndSendBack.stopSelf().")
        stopSelf()
    }

    companion object {
        private const val TAG = "LocalTop10Service"
    }
}