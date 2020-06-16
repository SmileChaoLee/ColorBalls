package com.smile.Service;

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
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest;

import java.util.ArrayList;

public class MyGlobalTop10Service extends Service {

    public final static String Action_Name = "com.smile.Service.MyGlobalTop10Service";
    private final static String TAG = "MyGlobalTop10Service";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MyGlobalTop10Service --> onStartCommand() is called.");

        getDataAndSendBack(intent);

        stopSelf();
        Log.d(TAG, "MyGlobalTop10Service --> stopSelf() is called.");

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getDataAndSendBack(Intent intent) {
        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Integer> playerScores = new ArrayList<>();

        final String webUrl = intent.getStringExtra("WebUrl");
        final Handler getDataHandler = new Handler(Looper.getMainLooper());

        Thread getDataThread = new Thread( () -> {
            SystemClock.sleep(100);
            synchronized (getDataHandler) {
                String status = PlayerRecordRest.GetGlobalTop10Scores(webUrl, playerNames, playerScores);
                getDataHandler.notifyAll();
                Log.d(TAG, "MyGlobalTop10Service-->getDataAndSendBack()-->getDataThread-->notifyAll().");
            }
        });
        getDataThread.start();

        synchronized (getDataHandler) {
            try {
                Log.d(TAG, "MyGlobalTop10Service-->getDataAndSendBack()-->getDataThread wait.");
                getDataHandler.wait();
                Log.d(TAG, "MyGlobalTop10Service-->getDataAndSendBack()-->getDataThread-->get notified.");
            } catch (InterruptedException e) {
                Log.d(TAG, "MyGlobalTop10Service-->getDataAndSendBack()-->getDataThread wait exception.");
                e.printStackTrace();
            }
        }

        Intent notificationIntent = new Intent(Action_Name);
        Bundle extras = new Bundle();
        extras.putStringArrayList("PlayerNames", playerNames);
        extras.putIntegerArrayList("PlayerScores", playerScores);
        notificationIntent.putExtras(extras);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(notificationIntent);

        // added on 2018-11-11
        ColorBallsApp.isShowingLoadingMessage = false;
        ColorBallsApp.isProcessingJob = false;

        Log.d(TAG, "MyGlobalTop10Service --> sent result.");
    }
}
