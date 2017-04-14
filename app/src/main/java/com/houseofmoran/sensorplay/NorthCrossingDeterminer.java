package com.houseofmoran.sensorplay;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

class NorthCrossingDeterminer implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    private Sensor mAccelSensor;
    private final AzimuthEstimateListener mAzimuthEstimateListener;
    private NorthCrossingListener mNorthCrossingListener;

    private float[] mLastAccel = null;
    private float[] mLastMagnetic = null;

    private static final float UNSET_AZIMUTH = Float.MIN_VALUE;
    private float mLastAzimuth = UNSET_AZIMUTH;

    public NorthCrossingDeterminer(SensorManager mSensorManager,
                                   Sensor mMagneticSensor,
                                   Sensor mAccelSensor,
                                   AzimuthEstimateListener azimuthEstimateListener,
                                   NorthCrossingListener northCrossingListener) {
        this.mSensorManager = mSensorManager;
        this.mMagneticSensor = mMagneticSensor;
        this.mAccelSensor = mAccelSensor;
        this.mAzimuthEstimateListener = azimuthEstimateListener;
        this.mNorthCrossingListener = northCrossingListener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelSensor) {
            mLastAccel = copyArray(event.values);
        } else if (event.sensor == mMagneticSensor) {
            mLastMagnetic = copyArray(event.values);
        }

        if (mLastAccel != null && mLastMagnetic != null) {
            float azimuth = toAzimuth(mLastAccel, mLastMagnetic);
            Log.i(this.getClass().getName(), String.format("azimuth: %f", azimuth));
            this.mAzimuthEstimateListener.onEstimated(azimuth);
            if (mLastAzimuth != UNSET_AZIMUTH) {
                if (350.0f <= mLastAzimuth && mLastAzimuth < 360.0f) {
                    if (0.0f < azimuth && azimuth <= 10.0f) {
                        this.mNorthCrossingListener.onNorthCrossed(mLastAzimuth, azimuth);
                    }
                } else if (0.0f < mLastAzimuth && mLastAzimuth <= 10.0f) {
                    if (350.0f <= azimuth && azimuth < 360.0f) {
                        this.mNorthCrossingListener.onNorthCrossed(mLastAzimuth, azimuth);
                    }
                }
            }
            mLastAzimuth = azimuth;
        }
    }

    private float toAzimuth(float[] accel, float[] magnetic) {
        float[] rotationMatrix = new float[9];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnetic);
        SensorManager.getOrientation(rotationMatrix, orientation);
        float azimuthInRadians = orientation[0];
        float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
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
