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
        val playerNames = ArrayList<String>()
        val playerScores = ArrayList<Int>()
        // getTop10Scores(playerNames, playerScores)
        ScoreSQLite(applicationContext).let {
            PlayerRecordRest.GetLocalTop10(it, playerNames, playerScores)
            it.close()
        }
        Intent(ACTION_NAME).let {
            Bundle().apply {
                putStringArrayList(Constants.PLAYER_NAMES, playerNames)
                putIntegerArrayList(Constants.PLAYER_SCORES, playerScores)
                it.putExtras(this)
            }
            LocalBroadcastManager.getInstance(applicationContext).apply {
                Log.d(TAG, "getDataAndSendBack.sent result.")
                sendBroadcast(it)
            }
        }
        Log.d(TAG, "getDataAndSendBack.stopSelf().")
        stopSelf()
    }

    private fun getTop10Scores(playerNames: ArrayList<String>,
        playerScores: ArrayList<Int>): Boolean {
        var status = true
        try {
            ScoreSQLite(applicationContext).let {
                PlayerRecordRest.GetLocalTop10(it, playerNames, playerScores)
                it.close()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            status = false
        }
        Log.d(TAG, "getLocalTop10Scores.status = $status")
        return status
    }

    companion object {
        const val ACTION_NAME = "com.smile.Service.LocalTop10Service"
        private const val TAG = "LocalTop10Service"
    }
}