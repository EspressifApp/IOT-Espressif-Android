package com.espressif.iot.type.device.status;

public class EspStatusPlug implements IEspStatusPlug, Cloneable
{
    private boolean mIsOn;
    
    @Override
    public boolean isOn()
    {
        return mIsOn;
    }
    
    @Override
    public void setIsOn(boolean isOn)
    {
        mIsOn = isOn;
    }
    
    @Override
    public String toString()
    {
        return "EspStatusPlug: (mIsOn=[" + mIsOn + "])";
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        return super.clone();
    }
}
