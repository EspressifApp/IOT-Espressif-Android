package com.espressif.iot.model.longsocket2;

import java.net.InetAddress;

import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspLongSocket
{
    /**
     * add the status to be sent
     * 
     * @param deviceKey the device's device key
     * @param inetAddress the device's ip address
     * @param status the status to be sent
     * @param state the statue of the device
     * @param disconnectedCallback disconnected callback
     */
    void addStatus(String deviceKey, InetAddress inetAddress, IEspDeviceStatus status, IEspDeviceState state,
        Runnable disconnectedCallback);
    
    /**
     * add the status to be sent of mesh device
     * 
     * @param deviceKey the device's device key
     * @param inetAddress the device's ip address
     * @param bssid the device's bssid
     * @param status the status to be sent
     * @param state the statue of the device
     * @param disconnectedCallback disconnected callback
     */
    void addMeshStatus(String deviceKey, InetAddress inetAddress, String bssid, IEspDeviceStatus status,
        IEspDeviceState state, Runnable disconnectedCallback);
    
    /**
     * start the background task
     */
    void start();
    
    /**
     * stop the background task
     */
    void stop();
}
