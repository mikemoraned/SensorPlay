package com.houseofmoran.sensorplay;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private NorthCrossingDeterminer mListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mListener = new NorthCrossingDeterminer(mSensorManager, mMagneticSensor, mAccelSensor);
    }

    protected void onResume() {
        super.onResume();
        mListener.startListening();
    }

    protected void onPause() {
        super.onPause();
        mListener.stopListening();
    }

    private class NorthCrossingDeterminer implements SensorEventListener {
        private SensorManager mSensorManager;
        private Sensor mMagneticSensor;
        private Sensor mAccelSensor;

        private float[] mLastAccel = null;
        private float[] mLastMagnetic = null;

        private static final float UNSET_AZIMUTH = Float.MIN_VALUE;
        private float mLastAzimuth = UNSET_AZIMUTH;

        public NorthCrossingDeterminer(SensorManager mSensorManager, Sensor mMagneticSensor, Sensor mAccelSensor) {
            this.mSensorManager = mSensorManager;
            this.mMagneticSensor = mMagneticSensor;
            this.mAccelSensor = mAccelSensor;
        }

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
                if (mLastAzimuth != UNSET_AZIMUTH) {
                    if (350.0f <= mLastAzimuth && mLastAzimuth < 360.0f) {
                        if (0.0f < azimuth && azimuth <= 10.0f) {
                            crossedNorth(mLastAzimuth, azimuth);
                        }
                    } else if (0.0f < mLastAzimuth && mLastAzimuth <= 10.0f) {
                        if (350.0f <= azimuth && azimuth < 360.0f) {
                            crossedNorth(mLastAzimuth, azimuth);
                        }
                    }
                }
                mLastAzimuth = azimuth;
            }
        }

        private void crossedNorth(float prev, float current) {
            Log.i(this.getClass().getName(), String.format("crossed north: %f -> %f", prev, current));
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

        public void startListening() {
            int samplingPeriodUs = 1000 * 1000;
            mSensorManager.registerListener(this, mMagneticSensor, samplingPeriodUs);
            mSensorManager.registerListener(this, mAccelSensor, samplingPeriodUs);
        }

        public void stopListening() {
            mSensorManager.unregisterListener(this, mMagneticSensor);
            mSensorManager.unregisterListener(this, mAccelSensor);
        }
    }
}
