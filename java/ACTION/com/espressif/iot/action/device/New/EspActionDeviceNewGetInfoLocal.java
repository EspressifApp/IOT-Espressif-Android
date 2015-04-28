package com.espressif.iot.action.device.New;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.New.EspCommandDeviceNewGetInfoLocal;
import com.espressif.iot.command.device.New.IEspCommandDeviceNewGetInfoLocal;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.type.device.DeviceInfo;

public class EspActionDeviceNewGetInfoLocal implements IEspActionDeviceNewGetInfoLocal
{
    private final Logger log = Logger.getLogger(EspActionDeviceNewGetInfoLocal.class);
    @Override
    public DeviceInfo doActionNewGetInfoLocal(IEspDeviceNew device)
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
        IEspCommandDeviceNewGetInfoLocal command = new EspCommandDeviceNewGetInfoLocal();
        return command.doCommandDeviceNewGetInfoLocal(device);
    }
    
}
