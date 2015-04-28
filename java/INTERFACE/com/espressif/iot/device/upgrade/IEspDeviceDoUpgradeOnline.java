package com.espressif.iot.device.upgrade;

import com.espressif.iot.device.IEspDevice;

public interface IEspDeviceDoUpgradeOnline extends IEspDeviceUpgrade
{
    final String URL_UPGRADE_ONLINE = "https://iot.espressif.cn/v1/device/rpc/" + "?deliver_to_device=true"
        + "&action=sys_upgrade";
    
    final String URL_GET_DEVICE = "https://iot.espressif.cn/v1/device/";
    
    final String URL_REBOOT_DEVICE = "https://iot.espressif.cn/v1/device/rpc/" + "?deliver_to_device=true"
        + "&action=sys_reboot";
    
    // 2 minutes
    final long TIMEOUT_MILLISECONDS = 2 * 60 * 1000;
    
    // 5 seconds
    final long RETRY_TIME_MILLISECONDS = 5 * 1000;
    /**
     * upgrade device online by the latestRomVersion
     * 
     * @param deviceKey the device key
     * @param latestRomVersion the latest rom version
     * @return the new upgraded IEspDevice if upgrade suc, or null if upgrade fail
     */
    IEspDevice doUpgradeOnline(String deviceKey, String latestRomVersion);
}
