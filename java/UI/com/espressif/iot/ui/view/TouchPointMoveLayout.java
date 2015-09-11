package com.espressif.iot.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class TouchPointMoveLayout extends FrameLayout
{
    private boolean mTouchMove;
    private float mTouchPointX;
    private float mTouchPointY;
    
    private View mMoveView;
    private LayoutParams mMoveViewLP;
    private RectF mMoveRectF;
    private float mMoveViewOffsetX;
    private float mMoveViewOffsetY;
    
    private List<IntersectsView> mIntersectsViews;
    private View mIntersectsView;
    private OnTouchMoveListener mOnTouchMoveListener;
    
    /**
     * The view need check intersect the touch point view
     */
    public interface IntersectsView
    {
        View getView();
        
        RectF getRectF();
    }
    
    public interface OnTouchMoveListener
    {
        void onIntersectsChanged(View moveView, View intersectsView);
        
        void onTouchMoveEnd(View moveView, View intersectsView);
    }
    
    public TouchPointMoveLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }
    
    public TouchPointMoveLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }
    
    public TouchPointMoveLayout(Context context)
    {
        super(context);
        init(context);
    }
    
    private void init(Context context)
    {
        mTouchMove = false;
        
        mMoveViewLP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        mMoveRectF = new RectF();
        mIntersectsViews = new ArrayList<IntersectsView>();
        
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        mTouchPointX = ev.getX(ev.getPointerCount() - 1);
        mTouchPointY = ev.getY(ev.getPointerCount() - 1);
        
        return mTouchMove;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mTouchPointX = event.getX(event.getPointerCount() - 1);
        mTouchPointY = event.getY(event.getPointerCount() - 1);
        switch(event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_MOVE:
                if (mTouchMove)
                {
                    moveToucMoveView();
                    checkIntersects();
                    return true;
                }
                
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getActionIndex() != event.getPointerCount() - 1)
                {
                    break;
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mTouchMove)
                {
                    moveToucMoveView();
                    checkIntersects();
                    
                    if (mOnTouchMoveListener != null)
                    {
                        mOnTouchMoveListener.onTouchMoveEnd(mMoveView, mIntersectsView);
                    }
                    
                    mTouchMove = false;
                    mIntersectsViews.clear();
                    removeView(mMoveView);
                    mMoveView = null;
                }
                mTouchPointX = Float.NaN;
                mTouchPointY = Float.NaN;
                break;
        
        }
        
        return super.onTouchEvent(event);
    }
    
    private void moveToucMoveView()
    {
        mMoveView.setX(mTouchPointX - mMoveViewOffsetX);
        mMoveView.setY(mTouchPointY - mMoveViewOffsetY);
        mMoveRectF.left = mMoveView.getX();
        mMoveRectF.right = mMoveRectF.left + mMoveView.getWidth();
        mMoveRectF.top = mMoveView.getY();
        mMoveRectF.bottom = mMoveRectF.top + mMoveView.getHeight();
    }
    
    private void checkIntersects()
    {
        List<IntersectsView> intersectedViews = new ArrayList<IntersectsView>();
        for (IntersectsView iv : mIntersectsViews)
        {
            if (RectF.intersects(iv.getRectF(), mMoveRectF))
            {
                intersectedViews.add(iv);
            }
        }
        
        View maxIntersectsAreaView = null;
        float maxArea = 0;
        for (IntersectsView iv : intersectedViews)
        {
            RectF rf = iv.getRectF();
            float x1 = Math.max(rf.left, mMoveRectF.left);
            float x2 = Math.min(rf.right, mMoveRectF.right);
            float y1 = Math.max(rf.top, mMoveRectF.top);
            float y2 = Math.min(rf.bottom, mMoveRectF.bottom);
            float width = Math.abs(x1 - x2);
            float height = Math.abs(y1 - y2);
            float area = width * height;
            
            if (maxArea < area)
            {
                maxArea = area;
                maxIntersectsAreaView = iv.getView();
            }
        }
        
        if (mIntersectsView != maxIntersectsAreaView)
        {
            mIntersectsView = maxIntersectsAreaView;
            if (mOnTouchMoveListener != null)
            {
                mOnTouchMoveListener.onIntersectsChanged(mMoveView, mIntersectsView);
            }
        }
    }
    
    /**
     * 
     * @param moveView the view which move with touching
     * @param xInLayout the init x axis in TouchPointMoveLayout
     * @param yInLayout the init y axis in TouchPointMoveLayout
     * @param list the views need check moveView intersects them
     */
    public void startTouchMove(View moveView, float xInLayout, float yInLayout, List<IntersectsView> list)
    {
        if (mTouchMove)
        {
            throw new IllegalStateException("Last touch move is still continue...");
        }
        mTouchMove = true;
        mIntersectsViews.clear();
        if (list != null)
        {
            mIntersectsViews.addAll(list);
        }
        mMoveView = moveView; 
        if (mMoveView.getLayoutParams() == null)
        {
            addView(mMoveView, mMoveViewLP);
        }
        else
        {
            addView(mMoveView);
        }
        mMoveView.setX(xInLayout);
        mMoveView.setY(yInLayout);
        mMoveViewOffsetX = mTouchPointX - xInLayout;
        mMoveViewOffsetY = mTouchPointY - yInLayout;
    }
    
    public void setOnIntersectsChangeListener(OnTouchMoveListener listener)
    {
        mOnTouchMoveListener = listener;
    }
}
