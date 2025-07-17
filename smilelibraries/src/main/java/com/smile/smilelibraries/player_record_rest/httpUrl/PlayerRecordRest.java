package com.smile.smilelibraries.player_record_rest.httpUrl;

import android.util.Log;
import android.util.Pair;

import com.smile.smilelibraries.models.Player;
import com.smile.smilelibraries.scoresqlite.ScoreSQLite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PlayerRecordRest {
    // ASP.NET Core
    private static String Top10Url
            = "http://137.184.120.171/Playerscore/GetTop10PlayerscoresREST?gameId=";
    private static String AddScoreUrl
            = "http://137.184.120.171/Playerscore/AddOneRecordREST";
    private static final String TAG = "PlayerRecordRest";
    private static final String SUCCEEDED = "0";
    private static final String FAILED = "1";
    private static final String EXCEPTION = "2";

    public static boolean addOneRecord(JSONObject json) {
        Log.d(TAG, "addOneRecord");
        if ((json == null) || (json.length() == 0)) {
            Log.d(TAG, "addOneRecord.json is empty");
            return false;
        }
        boolean yn;
        try {
            URL url = new URL(AddScoreUrl);
            HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
            myConnection.setReadTimeout(15000);
            myConnection.setConnectTimeout(15000);
            myConnection.setRequestMethod("POST");
            myConnection.setDoInput(true);
            myConnection.setDoOutput(true); // this method trigger POST request

            OutputStream outputStream = myConnection.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write("PlayerscoreJSON=" + json);    // write parameter to Web service
            outputStreamWriter.flush();
            outputStreamWriter.close();
            outputStream.close();

            // myConnection.connect();  // no needed
            int responseCode = myConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // succeeded to request
                Log.d(TAG, "addOneRecord.Succeeded to connect.");
                InputStream inputStream = myConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                        StandardCharsets.UTF_8);

                StringBuilder sb = new StringBuilder();
                int readBuff;
                while ((readBuff = inputStreamReader.read()) != -1) {
                    sb.append((char) readBuff);
                }
                inputStreamReader.close();
                inputStream.close();

                String webResult = sb.toString();
                yn = webResult.equals("1");
            } else {
                Log.d(TAG, "addOneRecord.Failed to connect.");
                yn = false;
            }
        } catch (Exception ex) {
            Log.d(TAG, "addOneRecord.Exception = " + ex.getMessage());
            yn = false;
        }

        return yn;
    }

    public static String[] getGlobalTop10(String webUrl) {
        if ((webUrl == null) || (webUrl.isEmpty())) {
            Log.d(TAG, "getGlobalTop10.webUrl is empty.");
            return null;
        }

        String[] result = new String[]{"", ""};
        try {
            URL url = new URL(webUrl);
            HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
            // myConnection.setReadTimeout(15000);
            // myConnection.setConnectTimeout(15000);
            // myConnection.setRequestMethod("GET");   // get method is default if not set
            // when use setRequestMethod("GET") then cannot use setDoOutput(true)
            // because setDoOutput(true) triggers POST request
            myConnection.setRequestMethod("GET");
            myConnection.setDoInput(true);
            // myConnection.connect();  // no needed
            int responseCode = myConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // succeeded to request
                Log.d(TAG, "getGlobalTop10.Succeeded to connect.");
                InputStream inputStream = myConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                        StandardCharsets.UTF_8);

                StringBuilder sb = new StringBuilder();
                int readBuff;
                while ((readBuff = inputStreamReader.read()) != -1) {
                    sb.append((char) readBuff);
                }
                result[0] = SUCCEEDED;    // succeeded
                result[1] = sb.toString();

                Log.d(TAG, "getGlobalTop10.Web status -> " + result[0]);
                Log.d(TAG, "getGlobalTop10.Web output -> " + result[1]);

                inputStreamReader.close();
                inputStream.close();

            } else {
                Log.d(TAG, "getGlobalTop10.Failed to connect.");
                result[0] = FAILED;    // failed
            }
        } catch (Exception ex) {
            Log.d(TAG, "getGlobalTop10.Exception = " + ex.getMessage());
            result[0] = EXCEPTION;
        }

        return result;
    }

    public static ArrayList<Player> GetGlobalTop10(String gId) {
        Log.d(TAG, "getGlobalTop10.gameId = " + gId);
        ArrayList<Player> players = new ArrayList<>();
        String[] result = PlayerRecordRest.getGlobalTop10(Top10Url +gId);
        String status = result[0].toUpperCase();
        if (status.equals(SUCCEEDED)) {
            // Succeeded
            try {
                JSONArray jArray = new JSONArray(result[1]);
                int id;
                String name;
                int score;
                int gameId;
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json = jArray.getJSONObject(i);
                    id = json.getInt("Id");
                    name = json.getString("PlayerName");
                    score = json.getInt("Score");
                    gameId = json.getInt("GameId");
                    players.add(new Player(id, name, score, gameId));
                }
            } catch (JSONException ex) {
                Log.d(TAG, "GetGlobalTop10.JSONException = " + ex.getMessage());
                players.add(new Player(-1, "JSONException", -1, -1));
            }
        } else if (status.equals(FAILED)) {
            // Failed
            players.add(new Player(-1, "Web Connection Failed.", -1, -1));
        } else {
            // Exception
            players.add(new Player(-1, "Exception on Web read.", -1, -1));
        }

        return players;
    }

    public static ArrayList<Player> GetLocalTop10(ScoreSQLite scoreSQLite) {
        ArrayList<Player> players = new ArrayList<>();
        try {
             ArrayList<Pair<String, Integer>> resultList = scoreSQLite.readTop10ScoreList();
            int id;
            String playerName;
            int score;
            int gameId;
            for (int i = 0; i<resultList.size(); i++) {
                id = i;
                playerName = resultList.get(i).first;
                score = resultList.get(i).second;
                gameId = -1; // not used for now
                players.add(new Player(id, playerName, score, gameId));
            }
        } catch (Exception ex) {
            Log.d(TAG, "GetLocalTop10.Exception = " + ex.getMessage());
            players.add(new Player(-1, "ScoreSQLite Exception", -1, -1));
        }

        return players;
    }
}
