package com.espressif.iot.model.device.upgrade;

import org.apache.log4j.Logger;

import com.espressif.iot.device.upgrade.IEspDeviceCheckCompatibility;
import com.espressif.iot.device.upgrade.IEspDeviceUpgradeInfo;
import com.espressif.iot.device.upgrade.IEspDeviceUpgradeParser;
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
        IEspDeviceUpgradeInfo deviceUpgradeInfoMin =
            parser.parseUpgradeInfo(OLDEST_IOT_DEMO_VERSION);
        IEspDeviceUpgradeInfo deviceUpgradeInfoMax =
            parser.parseUpgradeInfo(NEWEST_IOT_DEMO_VERSION);
        if (deviceUpgradeInfo.getVersionValue() < deviceUpgradeInfoMin.getVersionValue())
        {
            log.debug("device version < apk support min version, return DEVICE_NEED_UPGRADE");
            return EspUpgradeDeviceCompatibility.DEVICE_NEED_UPGRADE;
        }
        else if (deviceUpgradeInfo.getVersionValue() > deviceUpgradeInfoMax.getVersionValue())
        {
            log.debug("device version > apk support max version, return APK_NEED_UPGRADE");
            return EspUpgradeDeviceCompatibility.APK_NEED_UPGRADE;
        }
        return EspUpgradeDeviceCompatibility.COMPATIBILITY;
    }
    
}
