package com.houseofmoran.sensorplay;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.util.Log;

class NorthCrossingDeterminer implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    private Sensor mAccelSensor;
    private final AzimuthEstimateListener mAzimuthEstimateListener;
    private NorthCrossingListener mNorthCrossingListener;

    private float[] mLastAccel = null;
    private long mLastAccelSampleTime;
    private float[] mLastMagnetic = null;
    private long mLastMagneticSampleTime;

    private static final float UNSET_AZIMUTH = Float.MIN_VALUE;
    private float mLastAzimuth = UNSET_AZIMUTH;

    private float MAX_AZIMUTH_CHANGE_PER_MILLI = 1.0f / 2000.0f;
    private long mLastAzimuthChangeTime;

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
            mLastAccelSampleTime = System.currentTimeMillis();
        } else if (event.sensor == mMagneticSensor) {
            mLastMagnetic = copyArray(event.values);
            mLastMagneticSampleTime = System.currentTimeMillis();
        }

        if (mLastAccel != null && mLastMagnetic != null
                && (Math.abs(mLastAccelSampleTime - mLastMagneticSampleTime) < 100)) {
            float azimuth = toAzimuth(mLastAccel, mLastMagnetic);
            long azimuthChangeTime = Math.max(mLastAccelSampleTime, mLastMagneticSampleTime);

            if (mLastAzimuth == UNSET_AZIMUTH) {
                mLastAzimuth = azimuth;
            }
            else {
                long elapsed = azimuthChangeTime - mLastAzimuthChangeTime;
                float wantedChange = azimuth - mLastAzimuth;
                float allowedChangeSize = elapsed * MAX_AZIMUTH_CHANGE_PER_MILLI;
                float clampedAzimuthChange =
                        Math.signum(wantedChange) * Math.min(Math.abs(wantedChange), allowedChangeSize);
                float clampedAzimuth = mLastAzimuth + clampedAzimuthChange;

                Log.i(this.getClass().getName(),
                        String.format("azimuth: raw: %f, clamped: %f", azimuth, clampedAzimuth));
                this.mAzimuthEstimateListener.onEstimated(azimuth, clampedAzimuth);

                notifyOnCrossingNorth(mLastAzimuth, clampedAzimuth);
                mLastAzimuth = clampedAzimuth;
            }

            mLastAzimuthChangeTime = azimuthChangeTime;
        }
    }

    private void notifyOnCrossingNorth(float previous, float latest) {
        if (previous != UNSET_AZIMUTH) {
            if (350.0f <= previous && previous < 360.0f) {
                if (0.0f < latest && latest <= 10.0f) {
                    this.mNorthCrossingListener.onNorthCrossed(previous, latest);
                }
            } else if (0.0f < previous && previous <= 10.0f) {
                if (350.0f <= latest && latest < 360.0f) {
                    this.mNorthCrossingListener.onNorthCrossed(previous, latest);
                }
            }
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
        int samplingPeriodUs = 1000 * 100;
        mSensorManager.registerListener(this, mMagneticSensor, samplingPeriodUs);
        mSensorManager.registerListener(this, mAccelSensor, samplingPeriodUs);
    }

    public void stopListening() {
        mSensorManager.unregisterListener(this, mMagneticSensor);
        mSensorManager.unregisterListener(this, mAccelSensor);
    }
}
