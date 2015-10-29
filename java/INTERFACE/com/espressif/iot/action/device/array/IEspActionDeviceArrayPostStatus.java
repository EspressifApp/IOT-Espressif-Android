package com.espressif.iot.action.device.array;

import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspActionDeviceArrayPostStatus
{
    /**
     * Post IEspDeviceArray status command
     * 
     * @param deviceArray
     * @param status
     */
    void doActionDeviceArrayPostStatus(IEspDeviceArray deviceArray, IEspDeviceStatus status);
}
