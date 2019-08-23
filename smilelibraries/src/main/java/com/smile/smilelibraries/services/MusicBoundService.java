package com.smile.smilelibraries.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

public class MusicBoundService extends Service {

    public static final String ActionName = new String("com.smile.smilepublicclasseslibrary.services.MusicService");
    public static final int ServiceStopped = 0x00;
    public static final int ServiceStarted = 0x01;
    public static final int MusicPlaying = 0x02;
    public static final int MusicPaused = 0x03;

    private static final String TAG = new String("com.smile.smilepublicclasseslibrary.services.MusicService");
    private final int maxVolume = 100;
    private int soundVolume = maxVolume - 1;   // full volume
    private MediaPlayer mediaPlayer = null;
    private Thread backgroundThread = null;
    private int musicResourceId;

    // create a Binder for communicate with clients using this Binder
    private IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        public MusicBoundService getService() {
            return MusicBoundService.this;
        }
    }

    public static class MusicServiceConnection implements ServiceConnection {
        private MusicBoundService musicBoundService;
        private boolean isServiceConnected;
        private boolean hasMusic;

        public MusicServiceConnection() {
            hasMusic = true;
            isServiceConnected = false;
        }
        public MusicServiceConnection(boolean hasMusic) {
            this.hasMusic = hasMusic;
            isServiceConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Bound service connected");
            MusicBoundService.ServiceBinder musicBinder = (MusicBoundService.ServiceBinder)service;
            musicBoundService = musicBinder.getService();
            isServiceConnected = true;
            if (musicBoundService != null) {
                if (hasMusic) {
                    musicBoundService.playMusic();
                } else {
                    musicBoundService.pauseMusic();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Bound service disconnected");
            isServiceConnected = false;
        }

        public boolean isServiceConnected() {
            return isServiceConnected;
        }
        public MusicBoundService getMusicBoundService() {
            return musicBoundService;
        }
    }

    public MusicBoundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Service started by startService()");

        // send broadcast to receiver
        Intent broadcastIntent = new Intent(ActionName);
        broadcastIntent.putExtra("PlayStatus", ServiceStarted);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(broadcastIntent);

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind called");

        Bundle extras = intent.getExtras();
        musicResourceId = 0;    // no resource
        if (extras != null) {
            musicResourceId = extras.getInt("MusicResourceId", 0);
            soundVolume = extras.getInt("SoundVolume", maxVolume - 1);
        }

        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"backgroundThread running");
                startMusic();
            }
        });

        backgroundThread.start();

        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind() called");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (backgroundThread != null) {
            boolean retry = true;
            while (retry) {
                try {
                    backgroundThread.join();
                    Log.d(TAG, "backgroundThread.Join()........\n");
                    retry = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
            backgroundThread = null;
        }

        serviceBinder = null;
    }

    private void startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), musicResourceId);
            if (mediaPlayer != null) {
                float log1 = (float) (1 - (Math.log(maxVolume - soundVolume) / Math.log(maxVolume)));
                mediaPlayer.setVolume(log1, log1);
                mediaPlayer.setLooping(true);
            }
        }
    }

    public void playMusic() {
        if (mediaPlayer != null) {
            try {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    // send broadcast to receiver
                    Intent broadcastIntent = new Intent(ActionName);
                    Bundle extras = new Bundle();
                    extras.putInt("PlayStatus", MusicPlaying);
                    broadcastIntent.putExtras(extras);

                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
                    localBroadcastManager.sendBroadcast(broadcastIntent);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void pauseMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();

                // send broadcast to receiver
                Intent broadcastIntent = new Intent(ActionName);
                Bundle extras = new Bundle();
                extras.putInt("PlayStatus", MusicPaused);
                broadcastIntent.putExtras(extras);

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
                localBroadcastManager.sendBroadcast(broadcastIntent);
            }
        }
    }

    public boolean isMusicPlaying() {

        boolean isMediaPlaying = false;
        if (mediaPlayer != null) {
            isMediaPlaying = mediaPlayer.isPlaying();
        }
        return isMediaPlaying;
    }

    public void terminateService() {
        Log.i(TAG, "stopSelf()ing.");
        stopSelf();
        // send broadcast to receiver
        Intent broadcastIntent = new Intent(ActionName);
        Bundle extras = new Bundle();
        extras.putInt("PlayStatus", ServiceStopped);
        broadcastIntent.putExtras(extras);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }
}
