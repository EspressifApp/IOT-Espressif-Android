package com.espressif.iot.type.device;

import com.espressif.iot.type.net.IOTAddress;

/**
 * DeviceInfo is used when the user long tap the NEW device
 * @author afunx
 *
 */
public class DeviceInfo
{
    private final static int STATUS_DEVICE_ACTIVE_DONE = 41;
    
    public final static String TYPE_UNKONW = "unkonw";
    
    private final String mType;
    
    private final String mVersion;
    
    private final int mStatus;
    
    private IOTAddress mIOTAddress;
    
    /**
     * @param type the device's type
     * @param version the device's version
     * @param status the device's status
     */
    public DeviceInfo(String type, String version, int status)
    {
        this.mType = type;
        this.mVersion = version;
        this.mStatus = status;
    }
    
    /**
     * Get the device's type
     * 
     * @return the device's type
     */
    public String getType()
    {
        return mType;
    }
    
    /**
     * Get the device's version
     * 
     * @return the device's version
     */
    public String getVersion()
    {
        return mVersion;
    }
    
    /**
     * Get the device's status
     * 
     * @return the device's status
     */
    public int getStatus()
    {
        return mStatus;
    }
    
    /**
     * Check whether the device is authorized
     * 
     * @return whether the device is authorized
     */
    public boolean isAuthorized()
    {
        return mStatus == STATUS_DEVICE_ACTIVE_DONE;
    }
    
    /**
     * check whether the device's type is unknown
     * 
     * @return whether the device's type is unknown
     */
    public boolean isTypeUnknow()
    {
        return mType.equals(TYPE_UNKONW);
    }
    
    /**
     * Set the device's IOTAddress, @see IOTAddress
     * 
     * @param iotAddress the device's IOTAddress
     */
    public void setIOTAddress(IOTAddress iotAddress)
    {
        mIOTAddress = iotAddress;
    }
    
    /**
     * Get the device's IOTAddress, @see IOTAddress
     * 
     * @return the device's IOTAddress
     */
    public IOTAddress getIOTAddress()
    {
        return mIOTAddress;
    }
    
    @Override
    public String toString()
    {
        return "Type : " + mType + " || Version : " + mVersion + " || Status : " + mStatus;
    }
}
