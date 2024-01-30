package com.smile.colorballs.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.smile.colorballs.ColorBallsApp;
import com.smile.colorballs.Constants;
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest;

import java.util.ArrayList;

public class MyGlobalTop10Service extends Service {

    public final static String Action_Name = "com.smile.Service.MyGlobalTop10Service";
    private final static String TAG = "MyGlobalTop10Service";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() is called.");

        getDataAndSendBack(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getDataAndSendBack(Intent intent) {
        final ArrayList<String> playerNames = new ArrayList<>();
        final ArrayList<Integer> playerScores = new ArrayList<>();
        final String gameId = intent.getStringExtra(Constants.GameIdString);
        final Handler getDataHandler = new Handler(Looper.getMainLooper());

        Thread getDataThread = new Thread( () -> {
            SystemClock.sleep(100);
            synchronized (getDataHandler) {
                PlayerRecordRest.GetGlobalTop10(gameId, playerNames, playerScores);
                Log.d(TAG, "getDataAndSendBack.notifyAll().");
                getDataHandler.notifyAll();
            }
        });
        getDataThread.start();

        synchronized (getDataHandler) {
            try {
                Log.d(TAG, "getDataAndSendBack.wait.");
                getDataHandler.wait();
                Log.d(TAG, "getDataAndSendBack.get notified.");
            } catch (InterruptedException e) {
                Log.d(TAG, "getDataAndSendBack.wait exception.");
                e.printStackTrace();
            }
        }

        Log.d(TAG, "getDataAndSendBack.sent result.");
        Intent notificationIntent = new Intent(Action_Name);
        Bundle extras = new Bundle();
        extras.putStringArrayList("PlayerNames", playerNames);
        extras.putIntegerArrayList("PlayerScores", playerScores);
        notificationIntent.putExtras(extras);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(notificationIntent);

        ColorBallsApp.isShowingLoadingMessage = false;
        ColorBallsApp.isProcessingJob = false;

        stopSelf();
    }
}
