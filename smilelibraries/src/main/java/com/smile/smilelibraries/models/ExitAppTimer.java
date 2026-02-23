package com.smile.smilelibraries.models;

import android.os.Handler;
import android.os.Looper;

public class ExitAppTimer { // Singleton class

    private static ExitAppTimer exitAppTimer;

    private final int timePeriod;
    private final Handler timerHandler;
    private final Runnable timerRunnable;
    private int numOfBackPressTouched = 0;

    private ExitAppTimer(int timePeriod) {
        this.timePeriod = timePeriod;
        numOfBackPressTouched = 0;
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = () -> {
            numOfBackPressTouched = 0;
            timerHandler.removeCallbacksAndMessages(null);
        };
    }

    public static synchronized ExitAppTimer getInstance(int timePeriod) {
        if (exitAppTimer == null) {
            exitAppTimer = new ExitAppTimer(timePeriod);
        }
        return exitAppTimer;
    }

    public void start() {
        numOfBackPressTouched++;
        timerHandler.removeCallbacksAndMessages(null);
        timerHandler.postDelayed(timerRunnable, timePeriod);
    }

    public boolean canExit() {
        return numOfBackPressTouched > 0;
    }
}
