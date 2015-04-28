package com.espressif.iot.device.upgrade;

public interface IEspDeviceUpgradeParser
{
    /**
     * parse the version to IEspDeviceUpgradeInfo
     * 
     * @param version the version
     * @return @see IEspDeviceUpgradeInfo
     */
    IEspDeviceUpgradeInfo parseUpgradeInfo(String version);
}
