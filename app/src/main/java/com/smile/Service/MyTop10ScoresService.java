package com.smile.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.smile.colorballs.ColorBallsApp;
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest;

import java.util.ArrayList;

public class MyTop10ScoresService extends Service {
    public final static String Action_Name = "com.smile.Service.MyTop10ScoresService";
    private final static String TAG = new String("MyTop10ScoresService");

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MyTop10ScoresService --> onStartCommand() is called.");
        getDataAndSendBack();
        stopSelf();
        Log.d(TAG, "MyTop10ScoresService --> stopSelf() is called.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getDataAndSendBack() {
        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Integer> playerScores = new ArrayList<>();

        String status = PlayerRecordRest.GetLocalTop10Scores(ColorBallsApp.ScoreSQLiteDB, playerNames, playerScores);

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

        Log.d(TAG, "MyTop10ScoresService --> sent result.");
    }
}
