package com.espressif.iot.ui.widget.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class EspViewPager extends ViewPager
{
    private boolean mInterceptTouchEvent;
    
    public EspViewPager(Context context)
    {
        super(context);
        init();
    }
    
    public EspViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }
    
    private void init()
    {
        mInterceptTouchEvent = false;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        if (mInterceptTouchEvent)
        {
            return super.onInterceptTouchEvent(event);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (mInterceptTouchEvent)
        {
            return super.onTouchEvent(event);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public void setCurrentItem(int position)
    {
        super.setCurrentItem(position, mInterceptTouchEvent);
    }
    
    public void setInterceptTouchEvent(boolean intercept)
    {
        mInterceptTouchEvent= intercept;
    }
}
