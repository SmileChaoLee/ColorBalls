package com.smile.smilelibraries.utilities;

import android.content.Context;
import android.media.SoundPool;

public class SoundPoolUtil {

    private SoundPool soundPool;
    private int soundId;

    @SuppressWarnings("deprecation")
    public SoundPoolUtil(final Context context, final int rawId) {
        int maxStreams = 1;
        soundPool = new SoundPool.Builder()
                .setMaxStreams(maxStreams)
                .build();
        soundId = soundPool.load(context, rawId, 1);
    }

    public void playSound() {
        soundPool.play(soundId, 1,1, 1, 0, 1f);
    }

    public void release() {
        soundPool.release();
        soundPool = null;
    }
}
