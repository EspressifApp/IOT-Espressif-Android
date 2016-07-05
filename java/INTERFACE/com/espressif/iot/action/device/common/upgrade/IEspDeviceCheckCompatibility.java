package com.espressif.iot.action.device.common.upgrade;

import com.espressif.iot.type.upgrade.EspUpgradeDeviceCompatibility;

public interface IEspDeviceCheckCompatibility extends IEspDeviceUpgrade
{
    static final String OLDEST_IOT_DEMO_VERSION = "b1.1.0t45772(a)";
    
    static final String NEWEST_IOT_DEMO_VERSION = "v1.3.9t45772(o)";
    
    /**
     * Lowest not mesh device version
     */
    static final String OLDEST_IOT_DEMO_VERSION_SPECIAL = "b1.0.5t45772(a)";
    
    /**
     * Highest not mesh device version
     */
    static final String NEWEST_IOT_DEMO_VERSION_SPECIAL = "b1.0.9t45772(o)";
    
    EspUpgradeDeviceCompatibility checkDeviceCompatibility(String version);
}
