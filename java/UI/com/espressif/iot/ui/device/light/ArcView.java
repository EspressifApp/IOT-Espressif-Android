package com.espressif.iot.ui.device.light;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class ArcView extends View {

    private Paint mPaint;
    private RectF mRectf;

    public ArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0xff525659);
        mPaint.setStyle(Style.STROKE);
        float strokeWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getContext().getResources().getDisplayMetrics());
        mPaint.setStrokeWidth(strokeWidth);
        mRectf = new RectF();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int rectfHalfLength = Math.min(width, height) / 2;
        int centerX = getPaddingLeft() + width / 2;
        int centerY = getPaddingTop() + height / 2;

        mRectf.set(centerX - rectfHalfLength,
            centerY - rectfHalfLength,
            centerX + rectfHalfLength,
            centerY + rectfHalfLength);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.drawArc(mRectf, 120f, 120f, false, mPaint);
        canvas.drawArc(mRectf, 300f, 120f, false, mPaint);
    }
}
