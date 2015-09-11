package com.espressif.iot.action.device.common.upgrade;

import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceType;

class EspDeviceUpgradeInfo implements IEspDeviceUpgradeInfo
{
    
    /**
     * 0x9e370001 = 2654 404 609 = 2^31+2^29-2^25+2^22-2^19-2^16+1
     */
    protected static final int HASH_MAGIC = 0x9e370001;
    
    /**
     * the len of hash_table = 2^7
     */
    protected static final int HASH_BITS = 7;
    
    private final EspDeviceType mDeviceType;// 0-1000*100=10^5
    
    private final EspUpgradeDeviceType mUpgradeDeviceType;// 0-9
    
    private final int mVersionValue;// 0-1000*1000*1000=10^9
    
    private final boolean mIsReleased;// 0-1
    
    private static final int UPGRADE_TYPE_BASE = 1;
    
    private static final int VERSION_VALUE_BASE = 9;
    
    private static final int IS_RELEASED_BASE = 1;
    
    private EspDeviceUpgradeInfo(EspDeviceType deviceType, EspUpgradeDeviceType upgradeDeviceType, int versionValue,
        boolean isReleased)
    {
        this.mDeviceType = deviceType;
        this.mUpgradeDeviceType = upgradeDeviceType;
        this.mVersionValue = versionValue;
        this.mIsReleased = isReleased;
    }
    
    public static EspDeviceUpgradeInfo createUpgradeInfoDevice(EspDeviceType deviceType,
        EspUpgradeDeviceType upgradeDeviceType, int versionValue, boolean isReleased)
    {
        return new EspDeviceUpgradeInfo(deviceType, upgradeDeviceType, versionValue, isReleased);
    }
    
    @Override
    public EspDeviceType getDeviceType()
    {
        return this.mDeviceType;
    }
    
    @Override
    public EspUpgradeDeviceType getUpgradeDeviceTypeEnum()
    {
        return this.mUpgradeDeviceType;
    }
    
    @Override
    public int getVersionValue()
    {
        return this.mVersionValue;
    }
    
    @Override
    public boolean getIsReleased()
    {
        return this.mIsReleased;
    }
    
    @Override
    public long getIdValue()
    {
        long idValue = 0;
        idValue += mDeviceType.getSerial();
        idValue *= Math.pow(10, UPGRADE_TYPE_BASE);
        idValue += mUpgradeDeviceType.ordinal();
        idValue *= Math.pow(10, VERSION_VALUE_BASE);
        idValue += mVersionValue;
        idValue *= Math.pow(10, IS_RELEASED_BASE);
        idValue += IS_RELEASED_BASE;
        return idValue;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == null || o == null || !(o instanceof EspDeviceUpgradeInfo))
        {
            return false;
        }
        EspDeviceUpgradeInfo otherDeviceInfo = (EspDeviceUpgradeInfo)o;
        return (mDeviceType.equals(otherDeviceInfo.mDeviceType)
            && mUpgradeDeviceType.equals(otherDeviceInfo.mUpgradeDeviceType)
            && mVersionValue == otherDeviceInfo.mVersionValue && mIsReleased == otherDeviceInfo.mIsReleased);
    }
    
    @Override
    public int hashCode()
    {
        int hash = (int)(getIdValue() * HASH_MAGIC);
        return (hash >> (32 - HASH_BITS));
    }
}
