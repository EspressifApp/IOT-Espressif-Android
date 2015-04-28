package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.object.IEspObjectBuilder;
import com.espressif.iot.object.db.IDeviceDB;

public interface IBEspDevice extends IEspObjectBuilder
{
    IEspDevice alloc(String deviceName, long deviceId, String deviceKey, boolean isOwner, String bssid, int state,
        int ptype, String rom_version, String latest_rom_version, long userId, long... timestamp);
    
    IEspDevice alloc(IDeviceDB deviceDB);
}
