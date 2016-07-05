package com.espressif.iot.model.longsocket2;

import com.espressif.iot.action.longsocket2.EspActionLongSocket;
import com.espressif.iot.action.longsocket2.IEspActionLongSocket;
import com.espressif.iot.device.IEspDevice;
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
    public void addStatus(IEspDevice device, IEspDeviceStatus status, Runnable disconnectedCallback) {
        mEspActionLongSocket.addStatus(device, status, disconnectedCallback);
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
