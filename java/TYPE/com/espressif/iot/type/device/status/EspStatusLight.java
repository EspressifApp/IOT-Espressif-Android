package com.espressif.iot.type.device.status;

public class EspStatusLight implements IEspStatusLight, Cloneable
{
    private int mCWhite;
    
    private int mWWhite;
    
    private int mRed;
    
    private int mGreen;
    
    private int mBlue;
    
    private int mPeriod;
    
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
    public int getPeriod()
    {
        return mPeriod;
    }
    
    @Override
    public void setPeriod(int period)
    {
        mPeriod = period;
    }
    
    @Override
    public boolean equals(Object o)
    {
        // check the type
        if (o == null || !(o instanceof IEspStatusLight))
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        IEspStatusLight other = (IEspStatusLight)o;
        return other.getPeriod() == this.mPeriod && other.getRed() == this.mRed && other.getGreen() == this.mGreen
            && other.getBlue() == this.mBlue && other.getCWhite() == this.mCWhite && other.getWWhite() == this.mWWhite;
    }
    
    @Override
    public String toString()
    {
        return "EspStatusLight: (mRed=[" + mRed + "],mGreen=[" + mGreen + "],mBlue=[" + mBlue + "],mPeriod=[" + mPeriod
            + "])";
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public int getCWhite()
    {
        return mCWhite;
    }

    @Override
    public void setCWhite(int white)
    {
        mCWhite = white;
    }

    @Override
    public int getWWhite()
    {
        return mWWhite;
    }

    @Override
    public void setWWhite(int white)
    {
        mWWhite = white;
    }
}
