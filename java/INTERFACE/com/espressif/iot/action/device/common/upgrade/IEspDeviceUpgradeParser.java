package com.espressif.iot.action.device.common.upgrade;

public interface IEspDeviceUpgradeParser extends IEspDeviceUpgrade
{
    /**
     * parse the version to IEspDeviceUpgradeInfo
     * 
     * @param version the version
     * @return @see IEspDeviceUpgradeInfo
     */
    IEspDeviceUpgradeInfo parseUpgradeInfo(String version);
}
