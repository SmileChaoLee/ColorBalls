package com.smile.colorballs.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.colorballs.Constants
import com.smile.smilelibraries.scoresqlite.ScoreSQLite

class MyTop10ScoresService : Service() {
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
        getLocalTop10Scores(playerNames, playerScores)
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

    private fun getLocalTop10Scores(
        playerNames: ArrayList<String>,
        playerScores: ArrayList<Int>
    ): Boolean {
        var status = true
        playerNames.clear()
        playerScores.clear()
        try {
            ScoreSQLite(applicationContext).let {
                for (pair in it.readTop10ScoreList()) {
                    playerNames.add(pair.first as String)
                    playerScores.add(pair.second as Int)
                }
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
        const val Action_Name = "com.smile.Service.MyTop10ScoresService"
        private const val TAG = "MyTop10ScoresService"
    }
}