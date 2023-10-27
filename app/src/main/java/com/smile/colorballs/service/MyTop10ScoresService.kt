package com.smile.colorballs.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.smile.colorballs.Constants;
import com.smile.smilelibraries.scoresqlite.ScoreSQLite;
import java.util.ArrayList;

public class MyTop10ScoresService extends Service {
    public final static String Action_Name = "com.smile.Service.MyTop10ScoresService";
    private final static String TAG = "MyTop10ScoresService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() is called.");
        getDataAndSendBack();
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

        getLocalTop10Scores(playerNames, playerScores);
        Intent notificationIntent = new Intent(Action_Name);
        Bundle extras = new Bundle();
        extras.putStringArrayList(Constants.PlayerNamesKey, playerNames);
        extras.putIntegerArrayList(Constants.PlayerScoresKey, playerScores);
        notificationIntent.putExtras(extras);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(notificationIntent);
        Log.d(TAG, "getDataAndSendBack.sent result.");
        stopSelf();
        Log.d(TAG, "getDataAndSendBack.stopSelf().");
    }

    private boolean getLocalTop10Scores(ArrayList<String> playerNames, ArrayList<Integer> playerScores) {
        boolean status = true;
        playerNames.clear();
        playerScores.clear();
        try {
            ScoreSQLite scoreSQLiteDB = new ScoreSQLite(getApplicationContext());
            ArrayList<Pair<String, Integer>> resultList = scoreSQLiteDB.readTop10ScoreList();
            scoreSQLiteDB.close();
            for (Pair pair : resultList) {
                playerNames.add((String) pair.first);
                playerScores.add((Integer) pair.second);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        }
        Log.d(TAG, "getLocalTop10Scores.status = " + status);
        return status;
    }
}
