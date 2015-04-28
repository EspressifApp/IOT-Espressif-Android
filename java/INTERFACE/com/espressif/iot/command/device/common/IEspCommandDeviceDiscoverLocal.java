package com.espressif.iot.command.device.common;

import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandActivated;
import com.espressif.iot.command.device.IEspCommandUnactivated;
import com.espressif.iot.type.net.IOTAddress;

public interface IEspCommandDeviceDiscoverLocal extends IEspCommandUnactivated, IEspCommandActivated, IEspCommandLocal
{
    /**
     * discover the @see IOTAddress in the same AP
     * @return the list of IOTAddress
     */
    List<IOTAddress> doCommandDeviceDiscoverLocal();
}
