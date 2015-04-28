package com.espressif.iot.type.device.status;

public class EspStatusLight implements IEspStatusLight, Cloneable
{
    private int mRed;
    
    private int mGreen;
    
    private int mBlue;
    
    private int mFreq;
    
    @Override
    public int getRed()
    {
        return mRed;
    }
    
    @Override
    public void setRed(int red)
    {
        mRed = red;
    }
    
    @Override
    public int getGreen()
    {
        return mGreen;
    }
    
    @Override
    public void setGreen(int green)
    {
        mGreen = green;
    }
    
    @Override
    public int getBlue()
    {
        return mBlue;
    }
    
    @Override
    public void setBlue(int blue)
    {
        mBlue = blue;
    }
    
    @Override
    public int getFreq()
    {
        return mFreq;
    }
    
    @Override
    public void setFreq(int freq)
    {
        mFreq = freq;
    }
    
    @Override
    public String toString()
    {
        return "EspStatusLight: (mRed=[" + mRed + "],mGreen=[" + mGreen + "],mBlue=[" + mBlue + "],mFreq=[" + mFreq
            + "])";
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        return super.clone();
    }
    
}
