package com.smile.colorballs.services

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.colorballs.constants.Constants
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.models.Player

class GlobalTop10Service : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        getDataAndSendBack(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun getDataAndSendBack(intent: Intent) {
        var players = ArrayList<Player>()
        val gameId = intent.getStringExtra(Constants.GAME_ID_STRING)
        val lock = Object()
        val getDataThread = Thread {
            SystemClock.sleep(100)
            synchronized(lock) {
                players = PlayerRecordRest.GetGlobalTop10(gameId)
                Log.d(TAG, "getDataAndSendBack.notifyAll().")
                lock.notifyAll()
            }
        }
        getDataThread.start()
        synchronized(lock) {
            try {
                Log.d(TAG, "getDataAndSendBack.wait.")
                lock.wait()
                Log.d(TAG, "getDataAndSendBack.get notified.")
            } catch (e: InterruptedException) {
                Log.d(TAG, "getDataAndSendBack.wait exception.")
                e.printStackTrace()
            }
        }
        Log.d(TAG, "getDataAndSendBack.sent result.")
        Intent(Constants.GLOBAL_TOP10_ACTION_NAME).let {
            Bundle().apply {
                putParcelableArrayList(Constants.TOP10_PLAYERS, players)
                it.putExtras(this)
            }
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(it)
        }

        stopSelf()
    }

    companion object {
        private const val TAG = "GlobalTop10Service"
    }
}
