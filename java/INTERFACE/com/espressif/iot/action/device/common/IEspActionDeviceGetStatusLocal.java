package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.action.device.IEspActionActivated;
import com.espressif.iot.action.device.IEspActionUnactivated;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionDeviceGetStatusLocal extends IEspActionActivated, IEspActionUnactivated, IEspActionLocal
{
    /**
     * get the current status of device via local
     * 
     * @param device the device
     * @return whether the get action is suc
     */
    boolean doActionDeviceGetStatusLocal(final IEspDevice device);
}
