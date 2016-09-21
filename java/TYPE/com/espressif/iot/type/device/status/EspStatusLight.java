package com.espressif.iot.type.device.status;

import android.graphics.Color;

public class EspStatusLight implements IEspStatusLight, Cloneable
{
    private int mCWhite;
    private int mWWhite;
    private int mWhite;
    
    private int mRed;
    private int mGreen;
    private int mBlue;
    
    private int mPeriod;
    
    private int mStatus = STATUS_NULL;
    
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
            && other.getBlue() == this.mBlue && other.getWhite() == this.mWhite;
    }
    
    @Override
    public String toString() {
        return "EspStatusLight: (mStatus=[" + mStatus + "],mRed=[" + mRed + "],mGreen=[" + mGreen + "],mBlue=[" + mBlue
            + "],mWhite=[" + mWhite + "],mPeriod=[" + mPeriod + "])";
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

    @Override
    public int getWhite() {
        return mWhite;
    }

    @Override
    public void setWhite(int white) {
        mWhite = white;
        mWWhite = white;
        mCWhite = white;
    }

    @Override
    public int getStatus() {
        return mStatus;
    }

    @Override
    public void setStatus(int status) {
        mStatus = status;
    }

    @Override
    public int getCurrentColor() {
        boolean isColorStatus = true;
        switch (mStatus) {
            case STATUS_OFF:
                return Color.BLACK;
            case STATUS_ON:
                if (mRed == mGreen && mRed == mBlue && mRed == 0) {
                    isColorStatus = false;
                }
                break;
            case STATUS_COLOR:
                isColorStatus = true;
                break;
            case STATUS_BRIGHT:
                isColorStatus = false;
                break;
        }
        
        int color;
        if (isColorStatus) {
            color = Color.rgb(mRed, mGreen, mBlue);
        } else {
            color = Color.rgb(mWhite, mWhite, mWhite);
        }
        return color;
    }
}
