package com.houseofmoran.sensorplay;

import android.util.Log;

class BuzzOnNorthCrossing implements NorthCrossingListener {

    @Override
    public void onNorthCrossed(float prev, float curr) {
        Log.i(this.getClass().getName(), String.format("crossed north: %f -> %f", prev, curr));
    }
}
