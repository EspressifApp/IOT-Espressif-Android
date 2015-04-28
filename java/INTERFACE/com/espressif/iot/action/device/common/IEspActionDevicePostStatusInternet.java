package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.device.IEspActionActivated;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspActionDevicePostStatusInternet extends IEspActionActivated, IEspActionInternet
{
    /**
     * post the status to device via internet
     * 
     * @param device the device
     * @param status the new status
     * @return whether the post action is suc
     */
    boolean doActionDevicePostStatusInternet(final IEspDevice device, final IEspDeviceStatus status);
    
    boolean doActionDevicePostStatusInternet(final IEspDevice device, final IEspDeviceStatus status, boolean isBroadcast);
}
