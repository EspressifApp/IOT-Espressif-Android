package com.espressif.iot.type.device;

import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;

public class EspPlugsAperture implements IAperture
{
    private final int mId;
    
    private String mTitle;
    
    private boolean mOn;
    
    public EspPlugsAperture(int id)
    {
        mId = id;
    }
    
    @Override
    public int getId()
    {
        return mId;
    }
    
    @Override
    public void setTitle(String title)
    {
        mTitle = title;
    }
    
    @Override
    public String getTitle()
    {
        return mTitle;
    }
    
    @Override
    public void setOn(boolean isOn)
    {
        mOn = isOn;
    }
    
    @Override
    public boolean isOn()
    {
        return mOn;
    }
    
}
