package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.object.IEspObjectBuilder;

public interface IBEspDevicePlug extends IEspObjectBuilder
{
    IEspDevicePlug alloc();
}
