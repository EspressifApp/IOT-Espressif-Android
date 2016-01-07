package com.espressif.iot.action.device.New;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.BSSIDUtil;

public class EspActionDeviceNewGetInfoLocal implements IEspActionDeviceNewGetInfoLocal
{
    private final Logger log = Logger.getLogger(EspActionDeviceNewGetInfoLocal.class);
    @Override
    public IOTAddress doActionNewGetInfoLocal(IEspDeviceNew device)
    {
        // Connect SoftAP
        boolean connectResult;
        try
        {
            connectResult =
                EspBaseApiUtil.connect(device.getSsid(), device.getWifiCipherType(), device.getDefaultPassword());
        }
        catch (InterruptedException e)
        {
            return null;
        }
        
        if (!connectResult)
        {
            log.debug("Connect SoftAp failed " + device.getSsid());
            return null;
        }
        
        // Get SoftAP info
        IOTAddress iotAddress = null;
        for (int i = 0; i < 5; i++)
        {
            iotAddress = EspBaseApiUtil.discoverDevice(BSSIDUtil.restoreSoftApBSSID(device.getBssid()));
            if (iotAddress != null)
            {
                break;
            }
        }
        return iotAddress;
    }
    
}
