package com.smile.smilelibraries.utilities;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

public class SoundPoolUtil {

    private final String mTAG = "SoundPoolUtil";
    private SoundPool soundPool;
    private final int soundId;

    public SoundPoolUtil(final Context context, final int rawId) {
        Log.d(mTAG, "SoundPoolUtil");
        int maxStreams = 1;
        soundPool = new SoundPool.Builder()
                .setMaxStreams(maxStreams)
                .build();
        soundId = soundPool.load(context, rawId, 1);
        Log.d(mTAG, "SoundPoolUtil.soundId.soundPool = " + soundPool);
    }

    public void playSound() {
        Log.d(mTAG, "playSound.soundPool = " + soundPool);
        if (soundPool != null) {
            soundPool.play(soundId, 1, 1,
                    1, 0, 1f);
        }
    }

    public void release() {
        Log.d(mTAG, "release.soundPool = " + soundPool);
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
