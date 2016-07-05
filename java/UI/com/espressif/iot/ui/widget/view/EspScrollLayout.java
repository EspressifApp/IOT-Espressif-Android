package com.espressif.iot.ui.widget.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class EspScrollLayout extends LinearLayout {
    private Point mCenterPoint;
    private float mTouchDownX;
    private int mLastScrollX;
    private int mMaxScrollX;
    private int mMinScrollX;

    private int mTotalWidth;

    private volatile boolean mScrolling = false;

    public EspScrollLayout(Context context) {
        super(context);
        init(context);
    }

    public EspScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EspScrollLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        mCenterPoint = new Point();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        System.out.println("onLayout()");
        super.onLayout(changed, l, t, r, b);

        int width = getWidth();
        int height = getHeight();
        mCenterPoint.x = width / 2;
        mCenterPoint.y = height / 2;

        mTotalWidth = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            MarginLayoutParams mlp = (MarginLayoutParams)child.getLayoutParams();
            mTotalWidth += (child.getWidth() + mlp.leftMargin + mlp.rightMargin);
        }
        View firstView = null;
        View lastView = null;
        if (getChildCount() == 1) {
            firstView = lastView = getChildAt(0);
        } else if (getChildCount() > 1) {
            firstView = getChildAt(0);
            lastView = getChildAt(getChildCount() - 1);
        }
        if (firstView != null && lastView != null) {
            int firstViewCenterX = (firstView.getRight() - firstView.getLeft()) / 2;
            mMinScrollX = -Math.abs(mCenterPoint.x - firstViewCenterX);

            int lastViewCenterX = (lastView.getRight() - lastView.getLeft()) / 2;
            mMaxScrollX = mTotalWidth - width + Math.abs(lastViewCenterX - mCenterPoint.x);
        } else {
            mMinScrollX = 0;
            mMaxScrollX = 0;
        }

        setChildrenScale();
        int nearestCenter = getNearestCenterDistance();
        if (nearestCenter != 0) {
            scrollBy(nearestCenter, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getChildCount() == 0) {
            return true;
        }

        final float x = event.getX();
        final int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN) {
            mTouchDownX = x;
            mLastScrollX = getScrollX();
        }

        if (mScrolling) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                touchScroll(x);
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                touchScroll(x);
                checkChildrenPosition();
                return true;
        }
        return true;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        setChildrenScale();
    }

    private void touchScroll(float touchX) {
        float distance = mTouchDownX - touchX;
        int scrollX = (int)(mLastScrollX + distance);
        scrollX = Math.max(mMinScrollX, scrollX);
        scrollX = Math.min(mMaxScrollX, scrollX);
        setScrollX(scrollX);
    }

    private int getCenterDistance(View child) {
        int childCenterX = child.getLeft() + (child.getRight() - child.getLeft()) / 2;
        int distance = (childCenterX - getScrollX()) - mCenterPoint.x;

        return distance;
    }

    private int getNearestCenterDistance() {
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int nearCenter = getCenterDistance(child);
            if (Math.abs(minDistance) > Math.abs(nearCenter)) {
                minDistance = nearCenter;
            }
        }

        return minDistance;
    }

    private void setChildrenScale() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int nearCenter = getCenterDistance(child);
            float scale = (1f - ((float)Math.abs(nearCenter) / ((float)mTotalWidth / 2)));
            child.setScaleX(scale);
            child.setScaleY(scale);
        }
    }

    private void checkChildrenPosition() {
        if (mScrolling) {
            return;
        }

        mScrolling = true;
        int minDistance = getNearestCenterDistance();
        if (minDistance != 0) {
            new TranslateCenterThread(minDistance).start();
        } else {
            mScrolling = false;
        }
    }

    private class TranslateCenterThread extends Thread {
        private int mTotleTranslate;
        private int mDirectionCoefficient;

        public TranslateCenterThread(int totleTranslate) {
            mTotleTranslate = Math.abs(totleTranslate);
            if (totleTranslate > 0) {
                mDirectionCoefficient = 1;
            } else {
                mDirectionCoefficient = -1;
            }
        }

        @Override
        public void run() {
            final int duration = 200;
            final int interval = 30;

            int translateOnce = mTotleTranslate / (duration / interval);
            if (translateOnce < 10) {
                translateOnce = mTotleTranslate;
            }

            int translatedDis = 0;
            while (true) {
                int translate;
                if (translatedDis + translateOnce >= mTotleTranslate) {
                    translate = mTotleTranslate - translatedDis;
                } else {
                    translate = translateOnce;
                }

                scrollBy(translate * mDirectionCoefficient, 0);

                translatedDis += translate;
                if (translatedDis >= mTotleTranslate) {
                    break;
                }

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            mScrolling = false;
        }
    }
}
