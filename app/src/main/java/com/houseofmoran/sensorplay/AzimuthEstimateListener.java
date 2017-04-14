package com.houseofmoran.sensorplay;

interface AzimuthEstimateListener {

    void onEstimated(float rawAzimuth, float smoothedAzimuth);
}
