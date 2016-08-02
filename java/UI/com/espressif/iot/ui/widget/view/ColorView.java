package com.espressif.iot.ui.widget.view;

import com.espressif.iot.R;
import com.espressif.iot.util.ColorUtil;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View {
    private Resources mResources;

    private Paint mRingShadowPaint1;
    private Paint mRingShadowPaint2;
    private Paint mRingHighlightPaint;
    private Paint mMainPaint;

    private boolean mDrawShadowRing;
    private boolean mDrawHighlightRing;

    private int mShape;
    public static final int SHAPE_CIRCLE = -1;
    public static final int SHAPE_RECTANGLE = -2;

    public ColorView(Context context) {
        super(context);

        init(context, null, 0);
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0);
    }

    public ColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mResources = context.getResources();
        mRingShadowPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingShadowPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        final int defaultMainColor = mResources.getColor(R.color.esp_color_view_default);
        final int defaultRing1Color = mResources.getColor(R.color.esp_color_view_ring1);
        final int defaultRing2Color = mResources.getColor(R.color.esp_color_view_ring2);

        mShape = SHAPE_CIRCLE;
        mMainPaint.setColor(defaultMainColor);
        mRingHighlightPaint.setColor(defaultMainColor);
        mRingShadowPaint1.setColor(defaultRing1Color);
        mRingShadowPaint2.setColor(defaultRing2Color);
        mDrawShadowRing = false;
        mDrawHighlightRing = true;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorView, defStyleAttr, 0);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.ColorView_shape:
                    mShape = a.getInteger(attr, SHAPE_CIRCLE);
                    break;
                case R.styleable.ColorView_color:
                    int mainColor = a.getColor(attr, defaultMainColor);
                    int highlightColor = getHighlightColor(mainColor);
                    mMainPaint.setColor(mainColor);
                    mRingHighlightPaint.setColor(highlightColor);
                    break;
                case R.styleable.ColorView_drawShadow:
                    mDrawShadowRing = a.getBoolean(attr, false);
                    break;
                case R.styleable.ColorView_shadowColor1:
                    int ringColor1 = a.getColor(attr, defaultRing1Color);
                    mRingShadowPaint1.setColor(ringColor1);
                    break;
                case R.styleable.ColorView_shadowColor2:
                    int ringColor2 = a.getColor(attr, defaultRing2Color);
                    mRingShadowPaint2.setColor(ringColor2);
                    break;
            }
        }
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (mShape) {
            case SHAPE_CIRCLE:
                drawCircle(canvas);
                break;
            case SHAPE_RECTANGLE:
                drawRectangle(canvas);
                break;
        }
    }

    private static final int OFFSET_RING_PERCENT = 93;
    private static final int OFFSET_HIGHLIGHT_PERCENT = 85;
    private static final int OFFSET_MAIN_PERCENT = 80;

    private void drawCircle(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int radiusRing1 = Math.min(width, height) / 2;
        int radiusRing2 = radiusRing1 * OFFSET_RING_PERCENT / 100;
        int radiusRingHighlight = radiusRing1 * OFFSET_HIGHLIGHT_PERCENT / 100;
        int radiusMain = radiusRing1 * OFFSET_MAIN_PERCENT / 100;

        float circleX = width / 2;
        float circleY = height / 2;
        if (mDrawShadowRing) {
            canvas.drawCircle(circleX, circleY, radiusRing1, mRingShadowPaint1);
            canvas.drawCircle(circleX, circleY, radiusRing2, mRingShadowPaint2);
        }
        if (mDrawHighlightRing) {
            canvas.drawCircle(circleX, circleY, radiusRingHighlight, mRingHighlightPaint);
        }
        canvas.drawCircle(circleX, circleY, radiusMain, mMainPaint);
    }

    private void drawRectangle(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int base = Math.min(width, height) / 2;
        int offsetRing2 = base * (100 - OFFSET_RING_PERCENT) / 100;
        int offsetHighlight = base * (100 - OFFSET_HIGHLIGHT_PERCENT) / 100;
        int offsetMain = base * (100 - OFFSET_MAIN_PERCENT) / 100;

        Rect rectRing1 = new Rect(0, 0, width, height);
        Rect rectRing2 = new Rect(offsetRing2, offsetRing2, width - offsetRing2, height - offsetRing2);
        Rect rectHighlight =
            new Rect(offsetHighlight, offsetHighlight, width - offsetHighlight, height - offsetHighlight);
        Rect rectMain = new Rect(offsetMain, offsetMain, width - offsetMain, height - offsetMain);
        if (mDrawShadowRing) {
            canvas.drawRect(rectRing1, mRingShadowPaint1);
            canvas.drawRect(rectRing2, mRingShadowPaint2);
        }
        if (mDrawHighlightRing) {
            canvas.drawRect(rectHighlight, mRingHighlightPaint);
        }
        canvas.drawRect(rectMain, mMainPaint);
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

    /**
     * Set shape of ColorView
     * 
     * @param shape One of {@link #SHAPE_CIRCLE} or {@link #SHAPE_RECTANGLE}.
     */
    public void setShape(int shape) {
        if (mShape != shape) {
            mShape = shape;
            invalidate();
        }
    }
}
