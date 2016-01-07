package com.espressif.iot.base.net.proxy;

public class EspMeshResponse
{
    private int mPackageLength;
    
    private int mOptionLength;
    
    private String mTargetBssid;
    
    private int mProto;
    
    private boolean mIsDeviceAvailable;
    
    private EspMeshOption mMeshOption;
    
    private byte[] mResponseBytes;
    
    private EspMeshResponse(byte[] first4bytes)
    {
        mPackageLength = EspMeshPackageUtil.getResponsePackageLength(first4bytes);
    }
    
    static EspMeshResponse createInstance(byte[] first4bytes)
    {
        return new EspMeshResponse(first4bytes);
    }
    
    public boolean fillInAll(byte[] responseBytes)
    {
        try
        {
            mResponseBytes = responseBytes;
            mIsDeviceAvailable = EspMeshPackageUtil.isDeviceAvailable(mResponseBytes);
            mOptionLength = EspMeshPackageUtil.getResponseOptionLength(responseBytes);
            if (mOptionLength > 0)
            {
                mMeshOption = EspMeshOption.createInstance(responseBytes, mPackageLength, mOptionLength);
            }
            else
            {
                mMeshOption = null;
            }
            mProto = EspMeshPackageUtil.getResponseProto(responseBytes);
            mTargetBssid = EspMeshPackageUtil.getDeviceBssid(responseBytes);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public int getPackageLength()
    {
        return mPackageLength;
    }
    
    public int getOptionLength()
    {
        return mOptionLength;
    }
    
    public int getProto()
    {
        return mProto;
    }
    
    public String getTargetBssid()
    {
        return mTargetBssid;
    }
    
    public boolean hasMeshOption()
    {
        return mMeshOption != null;
    }
    
    public EspMeshOption getMeshOption()
    {
        return mMeshOption;
    }
    
    public boolean isBodyEmpty()
    {
        return mPackageLength - mOptionLength == EspMeshPackageUtil.M_HEADER_LEN;
    }
    
    public boolean isDeviceAvailable()
    {
        return mIsDeviceAvailable;
    }
    
    public byte[] getPureResponseBytes()
    {
        int pureResponseOffset = EspMeshPackageUtil.M_HEADER_LEN + mOptionLength;
        int pureResponseCount = mPackageLength - mOptionLength - EspMeshPackageUtil.M_HEADER_LEN;
        byte[] pureResponseBytes = new byte[pureResponseCount];
        for (int i = 0; i < pureResponseCount; ++i)
        {
            pureResponseBytes[i] = mResponseBytes[pureResponseOffset + i];
        }
        return pureResponseBytes;
    }
    
    @Override
    public String toString()
    {
        return "[EspMeshResponse mPackageLength = " + mPackageLength + " | " + "mOptionLength = " + mOptionLength
            + " | " + "mOptionLength = " + mOptionLength + " | " + "mTargetBssid = " + mTargetBssid + " | "
            + "mProto = " + mProto + " | " + "hasMeshOption = " + hasMeshOption() + " | " + "isBodyEmpty = "
            + isBodyEmpty() + " | " + "mIsDeviceAvailable = " + mIsDeviceAvailable + "]";
    }
}
