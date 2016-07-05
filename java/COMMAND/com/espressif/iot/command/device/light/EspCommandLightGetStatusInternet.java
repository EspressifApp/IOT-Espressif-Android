package com.espressif.iot.command.device.light;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspCommandLightGetStatusInternet extends EspCommandLight implements IEspCommandLightGetStatusInternet {

    @Override
    public IEspStatusLight doCommandLightGetStatusInternet(IEspDevice device) {
        int deviceVersionValue = getDeviceVersionValue(device);
        if (deviceVersionValue < getProtocolVersionValue()) {
            EspCommandLightOldProtocol cmdOld = new EspCommandLightOldProtocol();
            return cmdOld.getInternet(device);
        } else {
            EspCommandLightNewProtocol cmdNew = new EspCommandLightNewProtocol();
            return cmdNew.getInternet(device);
        }
    }
}
