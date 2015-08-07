package com.espressif.iot.type.net;

import java.net.InetAddress;

import com.espressif.iot.object.IEspObject;
import com.espressif.iot.type.device.EspDeviceType;

// it is used to process the message got from the IOT Device
// "I'm Plug.98:fe:34:77:ce:00 192.168.4.1"
public class IOTAddress implements IEspObject
{
    
    public static IOTAddress EmptyIOTAddress = new IOTAddress("EmptyIOTAddress", null);
    
    private String mSSID;
    
    /**
     * the device's bssid
     */
    private String mBSSID;
    
    /**
     * the device's InetAddress(IP Address)
     */
    private InetAddress mInetAddress;
    
    /**
     * the device type enum, @see EspDeviceType
     */
    private EspDeviceType mDeviceTypeEnum;
    
    /**
     * the parent device's bssid(only mesh device has parent device)
     */
    private String mParentBssid;
    
    /**
     * whether the device is mesh device or not
     */
    private boolean mIsMeshDevice;
    
    public IOTAddress(String BSSID, InetAddress inetAddress)
    {
        this(BSSID, inetAddress, false);
    }
    
    public IOTAddress(String BSSID, InetAddress inetAddress, boolean isMeshDevice)
    {
        this.mBSSID = BSSID;
        this.mInetAddress = inetAddress;
        this.mIsMeshDevice = isMeshDevice;
    }
    
    public void setEspDeviceTypeEnum(EspDeviceType deviceTypeEnum)
    {
        this.mDeviceTypeEnum = deviceTypeEnum;
    }
    
    public EspDeviceType getDeviceTypeEnum()
    {
        return this.mDeviceTypeEnum;
    }
    
    public void setIsMeshDevice(boolean isMeshDevice)
    {
        this.mIsMeshDevice = isMeshDevice;
    }
    
    public boolean isMeshDevice()
    {
        return mIsMeshDevice;
    }
    
    public void setBSSID(String BSSID)
    {
        this.mBSSID = BSSID;
    }
    
    public String getBSSID()
    {
        return mBSSID;
    }
    
    public void setInetAddr(InetAddress inetAddress)
    {
        this.mInetAddress = inetAddress;
    }
    
    public InetAddress getInetAddress()
    {
        return mInetAddress;
    }
    
    public void setSSID(String ssid)
    {
        mSSID = ssid;
    }
    
    public String getSSID()
    {
        return mSSID;
    }
    
    public void setParentBssid(String parentBssid)
    {
        mParentBssid = parentBssid;
    }
    
    public String getParentBssid()
    {
        return mParentBssid;
    }
    
    @Override
    public String toString()
    {
        return "BSSID:" + mBSSID + ",InetAddress:" + mInetAddress + "," + "DeviceTypeEnum:" + mDeviceTypeEnum + ","
            + "mParentBssid:" + mParentBssid;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof IOTAddress))
            return false;
        
        final IOTAddress other = (IOTAddress)o;
        if (this.mDeviceTypeEnum == other.mDeviceTypeEnum && this.mBSSID.equals(other.mBSSID))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public int hashCode()
    {
        return mBSSID.hashCode();
    }
}
