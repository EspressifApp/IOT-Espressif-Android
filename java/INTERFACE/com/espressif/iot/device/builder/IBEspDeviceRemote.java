package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceRemote;
import com.espressif.iot.object.IEspObjectBuilder;

public interface IBEspDeviceRemote extends IEspObjectBuilder
{
    IEspDeviceRemote alloc();
}
