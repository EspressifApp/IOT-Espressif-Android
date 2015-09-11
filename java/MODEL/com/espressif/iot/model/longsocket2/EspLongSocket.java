package com.espressif.iot.model.longsocket2;

import java.net.InetAddress;

import com.espressif.iot.action.longsocket2.EspActionLongSocket;
import com.espressif.iot.action.longsocket2.IEspActionLongSocket;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.IEspDeviceStatus;

public class EspLongSocket implements IEspLongSocket
{
    
    private IEspActionLongSocket mEspActionLongSocket;
    
    private EspLongSocket()
    {
        mEspActionLongSocket = EspActionLongSocket.createInstance();
    }
    
    public static EspLongSocket createInstance()
    {
        EspLongSocket instance = new EspLongSocket();
        return instance;
    }
    
    @Override
    public void addStatus(String deviceKey, InetAddress inetAddress, IEspDeviceStatus status, IEspDeviceState state,
        Runnable disconnectedCallback)
    {
        mEspActionLongSocket.addStatus(deviceKey, inetAddress, status, state, disconnectedCallback);
    }
    
    @Override
    public void addMeshStatus(String deviceKey, InetAddress inetAddress, String bssid, IEspDeviceStatus status,
        IEspDeviceState state, Runnable disconnectedCallback)
    {
        mEspActionLongSocket.addMeshStatus(deviceKey, inetAddress, bssid, status, state, disconnectedCallback);
    }
    
    @Override
    public void start()
    {
        mEspActionLongSocket.start();
    }
    
    @Override
    public void stop()
    {
        mEspActionLongSocket.stop();
    }
    
}
