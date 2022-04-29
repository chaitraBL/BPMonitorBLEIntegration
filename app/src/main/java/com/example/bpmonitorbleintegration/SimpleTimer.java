package com.example.bpmonitorbleintegration;

import android.os.Handler;

public abstract class SimpleTimer {
    abstract void onTimer();

    private Runnable runnableCode = null;
    private Handler handler = new Handler();

    void startDelayed(final int intervalMS, int delayMS) {
        runnableCode = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnableCode, intervalMS);
                onTimer();
            }
        };
        handler.postDelayed(runnableCode, delayMS);
    }

    void start(final int intervalMS) {
        startDelayed(intervalMS, 0);
    }

    void stop() {
        handler.removeCallbacks(runnableCode);
    }
}
