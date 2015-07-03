package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceConfigure;
import com.espressif.iot.object.IEspObjectBuilder;

public interface IBEspDeviceConfigure extends IEspObjectBuilder
{
    IEspDeviceConfigure alloc(String bssid, String randomToken);
}
