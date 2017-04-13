package com.houseofmoran.sensorplay;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private NorthCrossingDeterminer mListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mListener = new NorthCrossingDeterminer(mSensorManager, mMagneticSensor, mAccelSensor,
                new BuzzOnNorthCrossing());
    }

    protected void onResume() {
        super.onResume();
        mListener.startListening();
    }

    protected void onPause() {
        super.onPause();
        mListener.stopListening();
    }

}
