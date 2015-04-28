package com.espressif.iot.type.device.status;

public class EspStatusHumiture implements IEspStatusHumiture, Cloneable
{
    
    private long mAt;
    
    private double mX;
    
    private double mY;
    
    @Override
    public long getAt()
    {
        return mAt;
    }
    
    @Override
    public void setAt(long at)
    {
        mAt = at;
    }
    
    @Override
    public double getX()
    {
        return mX;
    }
    
    @Override
    public void setX(double x)
    {
        mX = x;
    }
    
    @Override
    public double getY()
    {
        return mY;
    }
    
    @Override
    public void setY(double y)
    {
        mY = y;
    }
    
    @Override
    public String toString()
    {
        return "EspStatusHumiture: (mAt=[" + mAt + "],mX=[" + mX + "],mY=[" + mY + "])";
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public boolean isXSupported()
    {
        return true;
    }

    @Override
    public boolean isYSupported()
    {
        return true;
    }
    
}
