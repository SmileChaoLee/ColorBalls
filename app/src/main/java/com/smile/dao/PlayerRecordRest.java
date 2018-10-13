package com.smile.dao;

import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlayerRecordRest {
    private static final String TAG = new String("com.smile.dao.PlayerRecordRest");
    private static final String SUCCEEDED = "0";
    private static final String FAILED = "1";
    private static final String EXCEPTION = "2";

    public static boolean addOneRecord(String webUrl, JSONObject jsonObject) {
        boolean yn = false;

        if  ( (webUrl == null) || (webUrl.isEmpty()) ) {
            return yn;
        }

        if ( (jsonObject == null) || (jsonObject.length()==0) ) {
            return yn;
        }

        try {
            URL url = new URL(webUrl);
            HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
            myConnection.setReadTimeout(15000);
            myConnection.setConnectTimeout(15000);
            myConnection.setRequestMethod("POST");
            myConnection.setDoInput(true);
            myConnection.setDoOutput(true); // this method trigger POST request

            Log.i(TAG, "Getting OutputStream ....");
            OutputStream outputStream = myConnection.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write("PlayerscoreJSON=" + jsonObject.toString());    // write parameter to Web service
            outputStreamWriter.flush();
            Log.i(TAG, "OutputStream closing ....");
            outputStreamWriter.close();
            outputStream.close();

            // myConnection.connect();  // no needed
            int responseCode = myConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // succeeded to request
                Log.i(TAG, "REST Web Service -> Succeeded to connect.");
                InputStream inputStream = myConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");

                StringBuilder sb = new StringBuilder("");
                int readBuff = -1;
                while ((readBuff = inputStreamReader.read()) != -1) {
                    sb.append((char) readBuff);
                }
                inputStreamReader.close();
                inputStream.close();

                String webResult = sb.toString();
                if (webResult.equals("1")) {
                    yn = true;
                } else {
                    yn = false;
                }

            } else {
                Log.i(TAG, "REST Web Service -> Failed to connect.");
                yn = false;
            }
        } catch (Exception ex) {
            String errorMsg = ex.toString();
            Log.d(TAG, "REST Web Service -> Exception occurred." + "\n" + errorMsg);
            ex.printStackTrace();
            yn = false;
        }

        return yn;
    }

    public static String[] getTop10Scores(String webUrl) {
        if ( (webUrl == null) || (webUrl.isEmpty()) ) {
            return null;
        }

        String[] result = new String[] {"",""};
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
                Log.i(TAG, "REST Web Service -> Succeeded to connect.");
                InputStream inputStream = myConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");

                StringBuilder sb = new StringBuilder("");
                int readBuff = -1;
                while ((readBuff = inputStreamReader.read()) != -1) {
                    sb.append((char) readBuff);
                }
                result[0] = SUCCEEDED;    // succeeded
                result[1] = sb.toString();

                Log.i(TAG, "Web status -> " + result[0]);
                Log.i(TAG, "Web output -> " + result[1]);

                inputStreamReader.close();
                inputStream.close();

            } else {
                Log.i(TAG, "REST Web Service -> Failed to connect.");
                result[0] = FAILED;    // failed
            }
        } catch (Exception ex) {
            String errorMsg = ex.toString();
            Log.d(TAG, "REST Web Service -> Exception occurred." + "\n" + errorMsg);
            ex.printStackTrace();
            result[0] = EXCEPTION;
        }

        return result;
    }
}
