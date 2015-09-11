package com.espressif.iot.action.device.common.upgrade;

import com.espressif.iot.action.device.IEspActionActivated;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionDeviceUpgradeOnline extends IEspDeviceUpgrade, IEspActionActivated
{
    static final String URL_UPGRADE_ONLINE = "https://iot.espressif.cn/v1/device/rpc/" + "?deliver_to_device=true"
        + "&action=sys_upgrade";
    
    static final String URL_GET_DEVICE = "https://iot.espressif.cn/v1/device/?query_device_mesh=true";
    
    static final String URL_REBOOT_DEVICE = "https://iot.espressif.cn/v1/device/rpc/" + "?deliver_to_device=true"
        + "&action=sys_reboot";
    
    static final String Parent_Mdev_Mac = "parent_mdev_mac";
    
    // 5 minutes
    static final long TIMEOUT_MILLISECONDS = 5 * 60 * 1000;
    
    // 5 seconds
    static final long RETRY_TIME_MILLISECONDS = 5 * 1000;
    
    /**
     * upgrade device online by the latestRomVersion
     * 
     * @param deviceKey the device key
     * @param latestRomVersion the latest rom version
     * @return the new upgraded IEspDevice if upgrade suc, or null if upgrade fail
     */
    IEspDevice doUpgradeOnline(String deviceKey, String latestRomVersion);
}
