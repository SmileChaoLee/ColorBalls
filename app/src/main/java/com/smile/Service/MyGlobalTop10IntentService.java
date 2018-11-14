package com.smile.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.smile.colorballs.ColorBallsApp;
import com.smile.smilepublicclasseslibrary.player_record_rest.PlayerRecordRest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyGlobalTop10IntentService extends IntentService {

    public final static String Action_Name = "MyGlobalTop10IntentService";
    private final String SUCCEEDED = "0";
    private final String FAILED = "1";

    public MyGlobalTop10IntentService() {
        super(Action_Name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        System.out.println("MyGlobalTop10IntentService --> onHandleIntent() is called.");

        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Integer> playerScores = new ArrayList<>();

        String webUrl = intent.getStringExtra("WebUrl");
        String[] result = PlayerRecordRest.getTop10Scores(webUrl);

        String status = result[0].toUpperCase();

        if (status.equals(SUCCEEDED)) {
            // Succeeded
            try {
                JSONArray jArray = new JSONArray(result[1]);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jo = jArray.getJSONObject(i);
                    playerNames.add(jo.getString("PlayerName"));
                    playerScores.add(jo.getInt("Score"));
                }

            } catch (JSONException ex) {
                String errorMsg = ex.toString();
                ex.printStackTrace();
                playerNames.add("JSONException->JSONArray");
                playerScores.add(0);
            }

        } else if (status.equals(FAILED)) {
            // Failed
            playerNames.add("Web Connection Failed.");
            playerScores.add(0);
        } else {
            // Exception
            playerNames.add("Exception on Web read.");
            playerScores.add(0);
        }

        // wait for 3 seconds
        // try { Thread.sleep(3000); } catch (InterruptedException ex) { ex.printStackTrace(); }

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

        System.out.println("MyGlobalTop10IntentService sent result");
    }
}
