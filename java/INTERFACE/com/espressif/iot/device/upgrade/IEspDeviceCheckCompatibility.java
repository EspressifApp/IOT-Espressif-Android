package com.espressif.iot.device.upgrade;

import com.espressif.iot.type.upgrade.EspUpgradeDeviceCompatibility;

public interface IEspDeviceCheckCompatibility extends IEspDeviceUpgrade
{
    final String OLDEST_IOT_DEMO_VERSION = "b1.0.1t45772(a)";
    
    final String NEWEST_IOT_DEMO_VERSION = "b1.0.4t45772(o)";
    
    EspUpgradeDeviceCompatibility checkDeviceCompatibility(String version);
}
