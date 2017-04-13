package com.houseofmoran.sensorplay;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

import static android.R.attr.orientation;

public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    private Sensor mAccelSensor;
    private final BearingDeterminer mListener = new BearingDeterminer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    protected void onResume() {
        super.onResume();

        int samplingPeriodUs = 1000 * 1000;
        mSensorManager.registerListener(mListener, mMagneticSensor, samplingPeriodUs);
        mSensorManager.registerListener(mListener, mAccelSensor, samplingPeriodUs);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mListener, mMagneticSensor);
        mSensorManager.unregisterListener(mListener, mAccelSensor);
    }

    private class BearingDeterminer implements SensorEventListener {
        private float[] mLastAccel = null;
        private float[] mLastMagnetic = null;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor == mAccelSensor) {
                mLastAccel = copyArray(event.values);
            }
            else if (event.sensor == mMagneticSensor) {
                mLastMagnetic = copyArray(event.values);
            }

            if (mLastAccel != null && mLastMagnetic != null) {
                float azimuth = toAzimuth(mLastAccel, mLastMagnetic);
                Log.i(this.getClass().getName(), String.format("azimuth: %f", azimuth));
            }
        }

        private float toAzimuth(float[] accel, float[] magnetic) {
            float[] rotationMatrix = new float[9];
            float[] orientation = new float[3];
            SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnetic);
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuthInRadians = orientation[0];
            float azimuthInDegrees = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            return azimuthInDegrees;
        }

        private float[] copyArray(float[] values) {
            float[] copied = new float[3];
            System.arraycopy(values, 0, copied, 0, values.length);
            return copied;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // ignore
        }
    }
}
