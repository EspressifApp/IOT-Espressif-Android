package com.espressif.iot.command.device.common;

import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.net.IOTAddress;

public class EspCommandDeviceDiscoverLocal implements IEspCommandDeviceDiscoverLocal
{
    private final static Logger log = Logger.getLogger(EspCommandDeviceDiscoverLocal.class);
    
    @Override
    public List<IOTAddress> doCommandDeviceDiscoverLocal()
    {
        List<IOTAddress> result = EspBaseApiUtil.discoverDevices();
        log.debug(Thread.currentThread().toString() + "##doCommandDeviceDiscoverLocal(): " + result);
        return result;
    }
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        throw new RuntimeException("EspCommandDeviceSleepRebootLocal don't support getLocalUrl");
    }
    
}
