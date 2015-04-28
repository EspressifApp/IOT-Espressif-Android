package com.espressif.iot.action.device.common;

import com.espressif.iot.command.device.New.EspCommandDeviceNewActivateInternet;
import com.espressif.iot.command.device.New.IEspCommandDeviceNewActivateInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.cache.IEspDeviceCache.NotifyType;
import com.espressif.iot.model.device.cache.EspDeviceCache;

public class EspActionDeviceActivateSharedInternet implements IEspActionDeviceActivateSharedInternet
{
    
    @Override
    public boolean doActionDeviceActivateSharedInternet(long userId, String userKey, String sharedDeviceKey)
    {
        IEspCommandDeviceNewActivateInternet command = new EspCommandDeviceNewActivateInternet();
        IEspDevice device = command.doCommandNewActivateInternet(userId, userKey, sharedDeviceKey);
        if (device != null) {
            EspDeviceCache.getInstance().addSharedDeviceCache(device);
            EspDeviceCache.getInstance().notifyIUser(NotifyType.STATE_MACHINE_UI);
            return true;
        } else {
            return false;
        }
    }
    
}
