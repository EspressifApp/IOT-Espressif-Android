package com.espressif.iot.type.device.status;

public class EspStatusFlammable implements IEspStatusFlammable, Cloneable
{
    private long mAt;
    
    private double mX;
    
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
    public String toString()
    {
        return "EspStatusFlammable :(mAt=[" + mAt + "],mX=[" + mX + "])";
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public double getY()
    {
        throw new RuntimeException("EspStatusFlammable don't support the method: double getY()");
    }

    @Override
    public void setY(double y)
    {
        throw new RuntimeException("EspStatusFlammable don't support the method: void setY(double y)");
    }

    @Override
    public boolean isXSupported()
    {
        return true;
    }

    @Override
    public boolean isYSupported()
    {
        return false;
    }
}
