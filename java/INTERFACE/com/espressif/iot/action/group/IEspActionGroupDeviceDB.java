package com.espressif.iot.action.group;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.group.IEspGroup;

public interface IEspActionGroupDeviceDB extends IEspActionGroupDB
{
    /**
     * Move the device into the group
     * 
     * @param userKey
     * @param device
     * @param group
     */
    void doActionMoveDeviceIntoGroupDB(String userKey, IEspDevice device, IEspGroup group);
    
    /**
     * Move the device out of the group
     * 
     * @param userKey
     * @param device
     * @param group
     */
    void doActionRemoveDevicefromGroupDB(String userKey, IEspDevice device, IEspGroup group);
}
