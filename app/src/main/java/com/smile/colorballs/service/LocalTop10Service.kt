package com.smile.colorballs.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.colorballs.Constants
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest
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
        Intent(Action_Name).let {
            Bundle().apply {
                putStringArrayList(Constants.PlayerNamesKey, playerNames)
                putIntegerArrayList(Constants.PlayerScoresKey, playerScores)
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
        const val Action_Name = "com.smile.Service.LocalTop10Service"
        private const val TAG = "LocalTop10Service"
    }
}