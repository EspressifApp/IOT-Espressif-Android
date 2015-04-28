package com.espressif.iot.action.user;

import org.apache.log4j.Logger;

import com.espressif.iot.action.user.IEspActionUserDevicesUpdated;
import com.espressif.iot.model.device.cache.EspDeviceCacheHandler;

public class EspActionUserDevicesUpdated implements IEspActionUserDevicesUpdated
{
    
    private final static Logger log = Logger.getLogger(EspActionUserDevicesUpdated.class);
    
    @Override
    public Void doActionDevicesUpdated(boolean isStateMachine)
    {
        log.debug(Thread.currentThread().toString() + "##doActionDevicesUpdated(): isStateMachine=[" + isStateMachine + "])");
        return EspDeviceCacheHandler.getInstance().handleUninterruptible(isStateMachine);
    }
    
}
