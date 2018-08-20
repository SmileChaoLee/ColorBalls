package com.smile.utility;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.smile.colorballs.R;

import static android.content.ContentValues.TAG;

/**
 * Created by chaolee on 2017-10-19.
 */

public class SoundUtil {

    private static ToneGenerator pTone = null;
    private static MediaPlayer mediaPlayer = null;

    public static void playTone() {
        Thread toneThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "playTone");
                try {
                    // final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
                    final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME);
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 100);
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "release toneGenerator");
                            toneGenerator.stopTone();
                            toneGenerator.release();
                            handler.removeCallbacksAndMessages(null);
                        }
                    }, 100);
                } catch (Exception e) {
                    Log.d(TAG, "Exception while playing tone: " + e);
                }
            }
        });

        toneThread.start();
    }

    public static void playTone1() {
        Thread toneThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (pTone != null) {
                    pTone.stopTone();
                    pTone.release();
                    pTone = null;
                }
                pTone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME);
                if (pTone != null) {
                    pTone.startTone(ToneGenerator.TONE_PROP_ACK, 100);
                }
            }
        });
        toneThread.run();
    }

    public static void playUhOhSound(final Context ctx) {
        Thread mediaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                mediaPlayer = MediaPlayer.create(ctx,R.raw.uhoh);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });

        mediaThread.run();
    }
}
