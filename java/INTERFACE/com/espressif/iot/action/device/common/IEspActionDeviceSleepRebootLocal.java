package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionDevice;
import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.type.device.EspDeviceType;

public interface IEspActionDeviceSleepRebootLocal extends IEspActionDevice, IEspActionLocal
{
    /**
     * Sleep or Reboot the device
     * 
     * @param type
     */
    void doActionDeviceSleepRebootLocal(EspDeviceType type);
}
