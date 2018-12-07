package com.smile.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;

import com.smile.colorballs.ColorBallsApp;
import com.smile.smilepublicclasseslibrary.player_record_rest.PlayerRecordRest;

import java.util.ArrayList;

public class MyTop10ScoresIntentService extends IntentService {
    public final static String Action_Name = "MyTop10ScoresIntentService";

    public MyTop10ScoresIntentService() {
        super(Action_Name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        System.out.println("MyTop10ScoresIntentService --> onHandleIntent() is called.");

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

        System.out.println("MyTop10ScoresIntentService sent result");
    }
}
