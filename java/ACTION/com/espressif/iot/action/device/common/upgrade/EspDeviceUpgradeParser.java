package com.espressif.iot.action.device.common.upgrade;

import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceType;

public class EspDeviceUpgradeParser implements IEspDeviceUpgradeParser
{
    /*
     * Singleton lazy initialization start
     */
    private EspDeviceUpgradeParser()
    {
    }
    
    private static class InstanceHolder
    {
        static EspDeviceUpgradeParser instance = new EspDeviceUpgradeParser();
    }
    
    public static EspDeviceUpgradeParser getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    // [v|b]Num1.Num2.Num3.tPTYPE([o|l|a|n])
    private Boolean getIsReleased(String version)
    {
        // get the first char
        char c = version.charAt(0);
        if (c == 'v')
        {
            return true;
        }
        else if (c == 'b')
        {
            return false;
        }
        else
        {
            return null;
        }
    }
    
    // [v|b]Num1.Num2.Num3.tPTYPE([o|l|a|n])
    private EspUpgradeDeviceType getUpgradeDeviceSupportType(String version)
    {
        // get the last but one
        char c = version.charAt(version.length() - 2);
        if (c == 'o')
        {
            return EspUpgradeDeviceType.SUPPORT_ONLINE_ONLY;
        }
        else if (c == 'l')
        {
            return EspUpgradeDeviceType.SUPPORT_LOCAL_ONLY;
        }
        else if (c == 'a')
        {
            return EspUpgradeDeviceType.SUPPORT_ONLINE_LOCAL;
        }
        else if (c == 'n')
        {
            return EspUpgradeDeviceType.NOT_SUPPORT_UPGRADE;
        }
        else
        {
            return null;
        }
    }
    
    // [v|b]Num1.Num2.Num3.tPTYPE([o|l|a|n])
    private EspDeviceType getDeviceType(String version)
    {
        char c;
        int indexOfT = -1;
        int len = version.length();
        // get the position of 't'
        for (int i = len - 1; i > 0; i--)
        {
            c = version.charAt(i);
            if (c == 't')
            {
                indexOfT = i;
                break;
            }
        }
        // get DeviceTypeEnum
        if (indexOfT != -1)
        {
            String ptypeIntStr = version.substring(indexOfT + 1, len - 3);
            int serial = Integer.parseInt(ptypeIntStr);
            return EspDeviceType.getEspTypeEnumBySerial(serial);
        }
        return null;
    }
    
    // [v|b]Num1.Num2.Num3.tPTYPE([o|l|a|n])
    private Integer getVersionValue(String version, int deviceSerialLen)
    {
        String numsStr = version.substring(1, version.length() - deviceSerialLen - 4);
        String[] numsArray = numsStr.split("\\.");
        return Integer.parseInt(numsArray[0]) * 1000 * 1000 + Integer.parseInt(numsArray[1]) * 1000
            + Integer.parseInt(numsArray[2]);
    }
    
    @Override
    public IEspDeviceUpgradeInfo parseUpgradeInfo(String version)
    {
        try
        {
            // whether it is released version
            boolean isReleased = getIsReleased(version);
            // upgrade device type
            EspUpgradeDeviceType upgradeDeviceType = getUpgradeDeviceSupportType(version);
            // device type
            EspDeviceType deviceType = getDeviceType(version);
            String deviceTypeSerial = Integer.toString(deviceType.getSerial());
            // version value
            Integer versionValue = getVersionValue(version, deviceTypeSerial.length());
            // create UpgradeInfoDevice
            IEspDeviceUpgradeInfo upgradeDeviceInfo =
                EspDeviceUpgradeInfo.createUpgradeInfoDevice(deviceType, upgradeDeviceType, versionValue, isReleased);
            return upgradeDeviceInfo;
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
}
