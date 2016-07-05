package com.espressif.iot.ui.widget.view;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class TouchPointMoveLayout extends FrameLayout
{
    /**
     * Whether the user is dragging a view
     */
    private boolean mDragging;
    /**
     * The X axis of touch point in TouchPointMoveLayout
     */
    private float mTouchPointX;
    /**
     * The Y axis of touch point in TouchPointMoveLayout
     */
    private float mTouchPointY;
    
    /**
     * The view that user drag
     */
    private View mDragView;
    private RectF mDragRectF;
    private float mDragViewOffsetX;
    private float mDragViewOffsetY;
    
    /**
     * The views need check whether mMoveView intersect when mMoveView is dragging.
     */
    private List<IntersectsView> mIntersectsViews;
    /**
     * The view that mMoveView has intersected.
     */
    private View mIntersectsView;
    private OnTouchMoveListener mOnTouchMoveListener;
    
    /**
     * The view need check intersect the touch point view
     */
    public interface IntersectsView
    {
        View getView();
        
        /**
         * The position in TouchPointMoveLayout
         * 
         * @return
         */
        RectF getRectF();
    }
    
    /**
     * Interface definition for a callback to be invoked when a view is dragged.
     *
     */
    public interface OnTouchMoveListener
    {
        /**
         * Call when the view that dragged view intersect has changed
         * 
         * @param moveView the view user dragged
         * @param intersectsView the view that moveView intersect, it could be null.
         */
        void onIntersectsChanged(View moveView, View intersectsView);
        
        /**
         * Call when a drag action is finished.
         * 
         * @param moveView the view user dragged
         * @param intersectsView the view that moveView intersect, it could be null.
         */
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
        mDragging = false;
        
        mDragRectF = new RectF();
        mIntersectsViews = new ArrayList<IntersectsView>();
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        mTouchPointX = ev.getX(ev.getPointerCount() - 1);
        mTouchPointY = ev.getY(ev.getPointerCount() - 1);
        
        return mDragging;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mTouchPointX = event.getX(event.getPointerCount() - 1);
        mTouchPointY = event.getY(event.getPointerCount() - 1);
        switch(event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_MOVE:
                if (mDragging)
                {
                    moveDragView();
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
                if (mDragging)
                {
                    moveDragView();
                    checkIntersects();
                    
                    if (mOnTouchMoveListener != null)
                    {
                        mOnTouchMoveListener.onTouchMoveEnd(mDragView, mIntersectsView);
                    }
                    
                    mDragging = false;
                    mIntersectsViews.clear();
                    removeView(mDragView);
                    mDragView = null;
                }
                mTouchPointX = Float.NaN;
                mTouchPointY = Float.NaN;
                break;
        
        }
        
        return super.onTouchEvent(event);
    }
    
    /**
     * Set the drag view's position when touch
     */
    private void moveDragView()
    {
        mDragView.setX(mTouchPointX - mDragViewOffsetX);
        mDragView.setY(mTouchPointY - mDragViewOffsetY);
        mDragRectF.left = mDragView.getX();
        mDragRectF.right = mDragRectF.left + mDragView.getWidth();
        mDragRectF.top = mDragView.getY();
        mDragRectF.bottom = mDragRectF.top + mDragView.getHeight();
    }
    
    /**
     * Check the drag view intersect mIntersectsViews
     */
    private void checkIntersects()
    {
        List<IntersectsView> intersectedViews = new ArrayList<IntersectsView>();
        for (IntersectsView iv : mIntersectsViews)
        {
            if (RectF.intersects(iv.getRectF(), mDragRectF))
            {
                intersectedViews.add(iv);
            }
        }
        
        View maxIntersectsAreaView = null;
        float maxArea = 0;
        for (IntersectsView iv : intersectedViews)
        {
            RectF rf = iv.getRectF();
            float x1 = Math.max(rf.left, mDragRectF.left);
            float x2 = Math.min(rf.right, mDragRectF.right);
            float y1 = Math.max(rf.top, mDragRectF.top);
            float y2 = Math.min(rf.bottom, mDragRectF.bottom);
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
                mOnTouchMoveListener.onIntersectsChanged(mDragView, mIntersectsView);
            }
        }
    }
    
    /**
     * Notification that the user start drag a view
     * 
     * @param moveView the view which move with touching
     * @param xInLayout the init x axis in TouchPointMoveLayout
     * @param yInLayout the init y axis in TouchPointMoveLayout
     * @param list the views need check moveView intersects them
     */
    public void startTouchMove(View moveView, float xInLayout, float yInLayout, List<IntersectsView> list)
    {
        if (mDragging)
        {
            throw new IllegalStateException("Last touch move is still continue...");
        }

        mDragging = true;

        mIntersectsViews.clear();
        if (list != null)
        {
            mIntersectsViews.addAll(list);
        }

        mDragView = generateDragView(moveView);
        addView(mDragView);
        mDragView.setX(xInLayout);
        mDragView.setY(yInLayout);
        mDragViewOffsetX = mTouchPointX - xInLayout;
        mDragViewOffsetY = mTouchPointY - yInLayout;
    }
    
    private Bitmap generateViewBitmap(View view)
    {
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        view.draw(canvas);

        return bmp;
    }
    
    private View generateDragView(View view)
    {
        ImageView iv = new ImageView(getContext());
        LayoutParams lp = new LayoutParams(view.getWidth(), view.getHeight());
        iv.setLayoutParams(lp);
        iv.setScaleType(ScaleType.CENTER_INSIDE);
        iv.setImageBitmap(generateViewBitmap(view));
        iv.setBackgroundColor(getResources().getColor(R.color.esp_drag_view_background));
        iv.setTag(view.getTag());

        return iv;
    }
    
    public void setOnIntersectsChangeListener(OnTouchMoveListener listener)
    {
        mOnTouchMoveListener = listener;
    }
}
