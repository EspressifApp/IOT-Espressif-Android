package com.espressif.iot.ui.device.light;

import com.meetme.android.horizontallistview.HorizontalListView;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public class EspHorizontalListView extends HorizontalListView {
    private Point mCenterPoint;
    private Handler mHandler;

    private boolean mTouched = false;

    public interface OnItemViewSelectedListener {
        public void onItemViewSelected(View view);
    }

    private OnItemViewSelectedListener mOnItemViewSelectedListener;

    public EspHorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCenterPoint = new Point();
        mHandler = new Handler();
        super.setOnScrollStateChangedListener(mScrollStateChangedListener);
    }

    @Override
    protected int initPosition() {
        return Integer.MAX_VALUE / 2;
    }

    @Override
    public void setOnScrollStateChangedListener(OnScrollStateChangedListener listener) {
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof InfiniteAdapter)) {
            throw new IllegalArgumentException("Must set a InfiniteAdapter");
        }
        super.setAdapter(adapter);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mCenterPoint.x = getWidth() / 2;
        mCenterPoint.y = getHeight() / 2;

        setChildrenScale();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mTouched = true;
        return super.onTouchEvent(event);
    }

    private void setChildrenScale() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            float distance = getCenterDistance(child);
            float scale = 1f - Math.abs(distance) / ((float)mCenterPoint.x);
            if (scale < 0f) {
                scale = 0f;
            }
            child.setScaleX(scale);
            child.setScaleY(scale);
        }
    }

    private int getCenterDistance(View child) {
        int childCenterX = child.getLeft() + (child.getRight() - child.getLeft()) / 2;
        int distance = childCenterX - mCenterPoint.x;

        return distance;
    }

    private int getNearestCenterDistance() {
        if (getChildCount() == 0) {
            return 0;
        }
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int distance = getCenterDistance(child);
            if (Math.abs(distance) < Math.abs(minDistance)) {
                minDistance = distance;
            }
        }

        return minDistance;
    }

    private View getCenterView() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int distance = getCenterDistance(child);
            if (distance == 0) {
                return child;
            }
        }

        return null;
    }

    private OnScrollStateChangedListener mScrollStateChangedListener = new OnScrollStateChangedListener() {

        @Override
        public void onScrollStateChanged(ScrollState scrollState) {
            if (scrollState == OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE) {
                adjustChildrenLocation();
            }
        }
    };

    public void adjustChildrenLocation() {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                int nearestCenterDis = getNearestCenterDistance();
                if (nearestCenterDis != 0) {
                    scrollBy(nearestCenterDis);
                } else {
                    if (mTouched && mOnItemViewSelectedListener != null) {
                        mOnItemViewSelectedListener.onItemViewSelected(getCenterView());
                    }
                }
            }
        });
    }

    public static abstract class InfiniteAdapter extends BaseAdapter {
        @Override
        public final int getCount() {
            return Integer.MAX_VALUE;
        }
    }

    public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
        if (mOnItemViewSelectedListener != listener) {
            mOnItemViewSelectedListener = listener;
        }
    }
}
