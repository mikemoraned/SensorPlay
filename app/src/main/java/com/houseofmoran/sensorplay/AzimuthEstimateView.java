package com.houseofmoran.sensorplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

class AzimuthEstimateView extends View implements AzimuthEstimateListener {

    private static final float UNSET_AZIMUTH = Float.MIN_VALUE;
    private float mLastAzimuth = UNSET_AZIMUTH;

    public AzimuthEstimateView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mLastAzimuth != UNSET_AZIMUTH) {

            Paint paint = new Paint();
            paint.setStrokeWidth(10);

            int width = getWidth();
            int height = getHeight();

            int minExtent = Math.min(width, height);
            int heightOffset = (int) Math.max(0, ((height - minExtent) / 2.0));
            int widthOffset = (int) Math.max(0, ((width - minExtent) / 2.0));

            paint.setColor(Color.RED);
            canvas.drawRect(widthOffset, heightOffset,
                    widthOffset + minExtent, heightOffset + minExtent,
                    paint);

            paint.setColor(Color.GREEN);
            drawAngleIndicator(northAtTop(0.0), canvas, paint, minExtent, heightOffset, widthOffset);

            paint.setColor(Color.BLUE);
            drawAngleIndicator(northAtTop(mLastAzimuth), canvas, paint, minExtent, heightOffset, widthOffset);
        }
    }

    private double northAtTop(double azimuth) {
        return azimuth - (Math.PI / 2) + (Math.PI * 2) % (Math.PI * 2);
    }

    private void drawAngleIndicator(double azimuthRadians, Canvas canvas,
                                    Paint paint,
                                    int extent,
                                    int heightOffset, int widthOffset) {
        float circleRadius = extent / 5 / 2;
        float circleX = (float) (((Math.cos(azimuthRadians) + 1.0) / 2.0) * extent)
                + widthOffset;
        float circleY = (float) (((Math.sin(azimuthRadians) + 1.0) / 2.0) * extent)
                + heightOffset;
        float centreX = (extent / 2) + widthOffset;
        float centreY = (extent / 2) + heightOffset;
        canvas.drawLine(centreX, centreY, circleX, circleY, paint);
        canvas.drawCircle(circleX, circleY, circleRadius, paint);
    }

    @Override
    public void onEstimated(float azimuth) {
        mLastAzimuth = azimuth;
        invalidate();
    }
}
