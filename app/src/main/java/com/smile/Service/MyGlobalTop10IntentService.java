package com.smile.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.smile.colorballs.ColorBallsApp;
import com.smile.dao.PlayerRecordRest;

public class MyGlobalTop10IntentService extends IntentService {

    public final static String Action_Name = "MyGlobalTop10IntentService";

    public MyGlobalTop10IntentService() {
        super(Action_Name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        System.out.println("MyGlobalTop10IntentService --> onHandleIntent() is called.");

        String webUrl = new String(ColorBallsApp.REST_Website + "/GetTop10PlayerscoresREST");   // ASP.NET Core
        String[] result = PlayerRecordRest.getTop10Scores(webUrl);

        // wait for 3 seconds
        try { Thread.sleep(3000); } catch (InterruptedException ex) { ex.printStackTrace(); }

        Intent notificationIntent = new Intent(Action_Name);
        Bundle extras = new Bundle();
        extras.putStringArray("RESULT", result);
        notificationIntent.putExtras(extras);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(notificationIntent);
        System.out.println("MyTop10ScoresIntentService sent result");
    }
}
