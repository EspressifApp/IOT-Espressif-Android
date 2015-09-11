package com.espressif.iot.action.device.common.upgrade;

import org.apache.log4j.Logger;

import com.espressif.iot.type.upgrade.EspUpgradeDeviceCompatibility;

public class EspDeviceCheckCompatibility implements IEspDeviceCheckCompatibility
{
    private final static Logger log = Logger.getLogger(EspDeviceCheckCompatibility.class);
    
    @Override
    public EspUpgradeDeviceCompatibility checkDeviceCompatibility(String version)
    {
        IEspDeviceUpgradeParser parser = EspDeviceUpgradeParser.getInstance();
        IEspDeviceUpgradeInfo deviceUpgradeInfo = parser.parseUpgradeInfo(version);
        if (deviceUpgradeInfo == null)
        {
            log.debug("the device is too old, it hasn't adopt compatibility policy, return COMPATIBILITY");
            return EspUpgradeDeviceCompatibility.COMPATIBILITY;
        }
        
        IEspDeviceUpgradeInfo deviceUpgradeInfoMin = parser.parseUpgradeInfo(OLDEST_IOT_DEMO_VERSION);
        IEspDeviceUpgradeInfo deviceUpgradeInfoMax = parser.parseUpgradeInfo(NEWEST_IOT_DEMO_VERSION);
        
        IEspDeviceUpgradeInfo deviceUpgradeInfoMinSpec = parser.parseUpgradeInfo(OLDEST_IOT_DEMO_VERSION_SPECIAL);
        IEspDeviceUpgradeInfo deviceUpgradeInfoMaxSpec = parser.parseUpgradeInfo(NEWEST_IOT_DEMO_VERSION_SPECIAL);
        
        int deviceVersion = deviceUpgradeInfo.getVersionValue();
        int deviceLegalVersionMin = deviceUpgradeInfoMin.getVersionValue();
        int deviceLegalVersionMax = deviceUpgradeInfoMax.getVersionValue();
        int deviceLegalVersionSpecMin = deviceUpgradeInfoMinSpec.getVersionValue();
        int deviceLegalVersionSpecMax = deviceUpgradeInfoMaxSpec.getVersionValue();
        
        if (deviceVersion >= deviceLegalVersionSpecMin && deviceVersion <= deviceLegalVersionSpecMax)
        {
            // The IOT demo version is not mesh, compatible device
            return EspUpgradeDeviceCompatibility.COMPATIBILITY;
        }
        else if (deviceVersion < deviceLegalVersionMin)
        {
            log.debug("device version < apk support min version, return DEVICE_NEED_UPGRADE");
            return EspUpgradeDeviceCompatibility.DEVICE_NEED_UPGRADE;
        }
        else if (deviceVersion > deviceLegalVersionMax)
        {
            log.debug("device version > apk support max version, return APK_NEED_UPGRADE");
            return EspUpgradeDeviceCompatibility.APK_NEED_UPGRADE;
        }
        
        return EspUpgradeDeviceCompatibility.COMPATIBILITY;
    }
    
}
