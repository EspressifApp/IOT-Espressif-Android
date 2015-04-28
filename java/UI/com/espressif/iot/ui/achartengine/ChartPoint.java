package com.espressif.iot.ui.achartengine;

public class ChartPoint
{
    /**
     * Label of X Axis
     */
    private String mXLabel;
    
    /**
     * X value of left axis point
     */
    private double x;
    
    /**
     * Y value of left axis point
     */
    private double y;
    
    public ChartPoint(double x, double y)
    {
        mXLabel = "";
        
        this.x = x;
        this.y = y;
    }
    
    /**
     * Set X Axis label of the point
     * 
     * @param xTitle
     */
    public void setXLabel(String xLabel)
    {
        mXLabel = xLabel;
    }
    
    /**
     * Get X Axis label of the point
     * 
     * @return
     */
    public String getXLabel()
    {
        return mXLabel;
    }
    
    /**
     * Get the point X value
     * 
     * @return Left Y Axis x
     */
    public double getX()
    {
        return x;
    }
    
    /**
     * Get the point Y value
     * 
     * @return Left Y Axis y
     */
    public double getY()
    {
        return y;
    }
    
}
