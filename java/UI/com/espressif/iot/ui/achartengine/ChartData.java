package com.espressif.iot.ui.achartengine;

import java.util.List;

import android.graphics.Color;

public class ChartData
{
    private String mTitle;
    
    private String mYTitle;
    
    private int mColor;
    
    private List<ChartPoint> mPointList;
    
    public ChartData(String title, String YTitle, List<ChartPoint> pointList)
    {
        mTitle = title;
        mYTitle = YTitle;
        mColor = Color.RED;
        mPointList = pointList;
    }
    
    public String getTitle()
    {
        return mTitle;
    }
    
    public String getYTitle()
    {
        return mYTitle;
    }
    
    /**
     * Set the line color
     * 
     * @param color
     */
    public void setColor(int color)
    {
        mColor = color;
    }
    
    /**
     * Get the line color
     * 
     * @return
     */
    public int getColor()
    {
        return mColor;
    }
    
    /**
     * Get the Data Point list
     * 
     * @return
     */
    public List<ChartPoint> getPointList()
    {
        return mPointList;
    }
}
