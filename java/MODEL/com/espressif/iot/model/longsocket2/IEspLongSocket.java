package com.espressif.iot.model.longsocket2;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspLongSocket {
    /**
     * add the status to be sent
     * 
     * @param device the device
     * @param status the status to be sent
     * @param disconnectedCallback disconnected callback
     */
    void addStatus(IEspDevice device, IEspDeviceStatus status, Runnable disconnectedCallback);

    /**
     * start the background task
     */
    void start();

    /**
     * stop the background task
     */
    void stop();
}
