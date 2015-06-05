package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.device.IEspActionActivated;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspActionDevicePostStatusInternet extends IEspActionActivated, IEspActionInternet
{
    static int FIRST_CHILD_LEVEL = 1;
    /**
     * post the status to device via Internet
     * 
     * @param device the device
     * @param status the new status
     * @return whether the post action is suc
     */
    boolean doActionDevicePostStatusInternet(final IEspDevice device, final IEspDeviceStatus status);
    
    /**
     * post the status to device via Internet
     * 
     * @param device the device
     * @param status the new status
     * @param isBroadcast whether it is a broadcast action, broadcast action will make the device and its child devices'
     *            statuses as the new status, only mesh device support it
     * @return whether the post action is suc
     */
    boolean doActionDevicePostStatusInternet(final IEspDevice device, final IEspDeviceStatus status, boolean isBroadcast);
}
