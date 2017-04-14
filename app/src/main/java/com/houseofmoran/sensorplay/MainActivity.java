package com.houseofmoran.sensorplay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private NorthCrossingDeterminer mListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AzimuthEstimateView azimuthEstimateView = new AzimuthEstimateView(this);
        setContentView(azimuthEstimateView);

        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mListener = new NorthCrossingDeterminer(mSensorManager, mMagneticSensor, mAccelSensor,
                azimuthEstimateView, new BuzzOnNorthCrossing(vibrator));
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
