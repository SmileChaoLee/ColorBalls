package com.smile.colorballs.services

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.tools.LogUtil
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite

class LocalTop10Service : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        LogUtil.d(TAG, "onStartCommand")
        getDataAndSendBack()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun getDataAndSendBack() {
        LogUtil.d(TAG, "getDataAndSendBack")
        ScoreSQLite(applicationContext, Constants.NO_BARRIER_DATABASE_NAME).let {
            LogUtil.d(TAG, "getDataAndSendBack.PlayerRecordRest.GetLocalTop10()")
            val players = PlayerRecordRest.GetLocalTop10(it)
            it.close()
            Intent(Constants.LOCAL_TOP10_ACTION_NAME).let {
                Bundle().apply {
                    putParcelableArrayList(Constants.TOP10_PLAYERS, players)
                    it.putExtras(this)
                }
                LocalBroadcastManager.getInstance(applicationContext).apply {
                    LogUtil.d(TAG, "getDataAndSendBack.sent result.")
                    sendBroadcast(it)
                }
            }
        }
        LogUtil.d(TAG, "getDataAndSendBack.stopSelf().")
        stopSelf()
    }

    companion object {
        private const val TAG = "LocalTop10Service"
    }
}