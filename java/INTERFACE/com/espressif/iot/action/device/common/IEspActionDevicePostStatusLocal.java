package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.action.device.IEspActionActivated;
import com.espressif.iot.action.device.IEspActionUnactivated;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspActionDevicePostStatusLocal extends IEspActionActivated, IEspActionUnactivated, IEspActionLocal
{
    static final int FIRST_CHILD_LEVEL = 2;
    
    /**
     * post the status to device via local
     * 
     * @param device the device
     * @param status the new status
     * @return whether the post action is suc
     */
    boolean doActionDevicePostStatusLocal(final IEspDevice device, final IEspDeviceStatus status);
    
}
