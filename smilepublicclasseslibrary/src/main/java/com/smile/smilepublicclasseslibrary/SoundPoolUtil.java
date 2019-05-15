package com.smile.smilepublicclasseslibrary;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

public class SoundPoolUtil {

    private SoundPool soundPool;
    private int soundId;

    public SoundPoolUtil(final Context context, final int rawId) {
        int maxStreams = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }
        soundId = soundPool.load(context, rawId, 1);
    }

    public void playSound() {
        soundPool.play(soundId, 1, 1, 1, 0, 1f);
    }

    public void release() {
        soundPool.release();
        soundPool = null;
    }
}
