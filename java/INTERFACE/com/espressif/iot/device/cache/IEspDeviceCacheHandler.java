package com.espressif.iot.device.cache;

/**
 * the default Handler for IUser to process the device in @see EspDeviceCache
 * 
 * @author afunx
 * 
 */
public interface IEspDeviceCacheHandler
{
    /**
     * when IUser start handle, it can't be interruptible
     * 
     * @return nothing(use Void to indicate it is a blocking method)
     */
    Void handleUninterruptible(boolean isStateMachine);
}
