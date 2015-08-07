package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceRoot;
import com.espressif.iot.object.IEspObjectBuilder;

public interface IBEspDeviceRoot extends IEspObjectBuilder
{
    IEspDeviceRoot getLocalRoot();
    
    IEspDeviceRoot getInternetRoot();
    
    IEspDeviceRoot getVirtualMeshRoot();
}
