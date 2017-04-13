package com.houseofmoran.sensorplay;

import android.os.Vibrator;
import android.util.Log;

class BuzzOnNorthCrossing implements NorthCrossingListener {

    private Vibrator mVibrator;

    public BuzzOnNorthCrossing(Vibrator vibrator) {
        this.mVibrator = vibrator;
    }

    @Override
    public void onNorthCrossed(float prev, float curr) {
        Log.i(this.getClass().getName(), String.format("crossed north: %f -> %f", prev, curr));
        this.mVibrator.vibrate(100);
    }
}
