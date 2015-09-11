package com.espressif.iot.action.device.common.upgrade;

import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceType;

public interface IEspDeviceUpgradeInfo extends IEspDeviceUpgrade
{
    /**
     * 
     * @return device type, @see EspDeviceType
     */
    EspDeviceType getDeviceType();
    
    /**
     * 
     * @return device upgrade type, @see EspUpgradeDeviceType
     */
    EspUpgradeDeviceType getUpgradeDeviceTypeEnum();
    
    /**
     * for device's each version, the version value is the same, but the id value is different.
     * e.g. plug_v1.0 VersionValue = 10, IdValue = 200
     *      light_v1.0 VersionValue = 10, IdValue = 201 
     * @return the version value
     */
    int getVersionValue();
    
    /**
     * 
     * @return whether the version is released version
     */
    boolean getIsReleased();
    
    /**
     * for device's each version, the version value is the same, but the id value is different.
     * e.g. plug_v1.0 VersionValue = 10, IdValue = 200
     *      light_v1.0 VersionValue = 10, IdValue = 201 
     * @return the id value
     */
    long getIdValue();
}
