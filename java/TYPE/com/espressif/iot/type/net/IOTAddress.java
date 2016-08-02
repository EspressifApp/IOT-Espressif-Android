package com.espressif.iot.type.net;

import java.net.InetAddress;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.object.IEspObject;
import com.espressif.iot.type.device.EspDeviceType;

import android.os.Parcel;
import android.os.Parcelable;

// it is used to process the message got from the IOT Device
// "I'm Plug.98:fe:34:77:ce:00 192.168.4.1"
public class IOTAddress implements IEspObject, Parcelable
{
    public static IOTAddress EmptyIOTAddress = new IOTAddress("EmptyIOTAddress", null);
    
    private String mSSID;
    
    /**
     * the root device's bssid(only mesh device has root device)
     */
    private String mRootBssid;
    
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
    
    /**
     * the device rom version
     */
    private String mRomVersion;

    /**
     * the device rssi
     */
    private int mRssi = IEspDevice.RSSI_NULL;

    /**
     * the device info
     */
    private String mInfo;

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
    
    public void setRootBssid(String rootBssid)
    {
        mRootBssid = rootBssid;
    }
    
    public String getRootBssid()
    {
        return mRootBssid;
    }
    
    public void setParentBssid(String parentBssid)
    {
        mParentBssid = parentBssid;
    }
    
    public String getParentBssid()
    {
        return mParentBssid;
    }
    
    public void setRomVersion(String romVersion)
    {
        mRomVersion = romVersion;
    }
    
    public String getRomVersion()
    {
        return mRomVersion;
    }
    
    public void setRssi(int rssi)
    {
        mRssi = rssi;
    }
    
    public int getRssi()
    {
        return mRssi;
    }
    
    public void setInfo(String info)
    {
        mInfo = info;
    }
    
    public String getInfo()
    {
        return mInfo;
    }
    
    @Override
    public String toString()
    {
        return "BSSID:" + mBSSID + ",InetAddress:" + mInetAddress + "," + "DeviceTypeEnum:" + mDeviceTypeEnum + ","
            + "mParentBssid:" + mParentBssid + "mRomVersion:" + mRomVersion;
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

    private IOTAddress(Parcel in) {
        mSSID = in.readString();
        mBSSID = in.readString();
        mRootBssid = in.readString();
        mParentBssid = in.readString();
        mDeviceTypeEnum = EspDeviceType.getEspTypeEnumBySerial(in.readInt());
        boolean[] bools = new boolean[1];
        in.readBooleanArray(bools);
        mIsMeshDevice = bools[0];
        mInetAddress = (InetAddress)in.readSerializable();
        mRomVersion = in.readString();
        mRssi = in.readInt();
        mInfo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSSID);
        dest.writeString(mBSSID);
        dest.writeString(mRootBssid);
        dest.writeString(mParentBssid);
        dest.writeInt(mDeviceTypeEnum.getSerial());
        dest.writeBooleanArray(new boolean[] {mIsMeshDevice});
        dest.writeSerializable(mInetAddress);
        dest.writeString(mRomVersion);
        dest.writeInt(mRssi);
        dest.writeString(mInfo);
    }

    public static final Parcelable.Creator<IOTAddress> CREATOR = new Creator<IOTAddress>() {

        @Override
        public IOTAddress[] newArray(int size) {
            return new IOTAddress[size];
        }

        @Override
        public IOTAddress createFromParcel(Parcel source) {
            return new IOTAddress(source);
        }
    };
}
