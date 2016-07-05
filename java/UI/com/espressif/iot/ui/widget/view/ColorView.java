package com.espressif.iot.ui.widget.view;

import com.espressif.iot.R;
import com.espressif.iot.util.ColorUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View {
    private Resources mResources;
    private Paint mRingShadowPaint1;
    private Paint mRingShadowPaint2;
    private Paint mRingHighlightPaint;

    private Paint mMainPaint;

    private boolean mDrawShadowRing = false;
    private boolean mDrawHighlightRing = true;

    public ColorView(Context context) {
        super(context);

        init(context);
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public ColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        mResources = context.getResources();
        mRingShadowPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        int ringColor1 = mResources.getColor(R.color.esp_color_view_ring1);
        mRingShadowPaint1.setColor(ringColor1);
        mRingShadowPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        int ringColor2 = mResources.getColor(R.color.esp_color_view_ring2);
        mRingShadowPaint2.setColor(ringColor2);

        mRingHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMainPaint.setColor(ringColor2);
        mRingHighlightPaint.setColor(ringColor2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float circleX = width / 2;
        float circleY = height / 2;
        int radiusRing1 = Math.min(width, height) / 2;
        int radiusRing2 = radiusRing1 * 93 / 100;
        int radiusRingHighlight = radiusRing1 * 85 / 100;
        int radiusMain = radiusRing1 * 80 / 100;

        if (mDrawShadowRing) {
            canvas.drawCircle(circleX, circleY, radiusRing1, mRingShadowPaint1);
            canvas.drawCircle(circleX, circleY, radiusRing2, mRingShadowPaint2);
        }
        if (mDrawHighlightRing) {
            canvas.drawCircle(circleX, circleY, radiusRingHighlight, mRingHighlightPaint);
        }
        canvas.drawCircle(circleX, circleY, radiusMain, mMainPaint);
    }

    public void setColor(int color) {
        if (color != mMainPaint.getColor()) {
            mMainPaint.setColor(color);
            mRingHighlightPaint.setColor(getHighlightColor(color));
            invalidate();
        }
    }

    public int getColor() {
        return mMainPaint.getColor();
    }

    private int getHighlightColor(int color) {
        int dColor = Color.WHITE;
        int sColor = color;
        float percent = 0.2f;

        int result = ColorUtil.getGradientColor(sColor, dColor, percent);

        return result;
    }

    public void setDrawShadowRing(boolean draw) {
        if (mDrawShadowRing != draw) {
            mDrawShadowRing = draw;
            invalidate();
        }
    }
}
