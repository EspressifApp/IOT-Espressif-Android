package com.espressif.iot.command.device.light;

import java.util.List;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspCommandLightPostStatusInternet extends EspCommandLight implements IEspCommandLightPostStatusInternet {

    @Override
    public boolean doCommandLightPostStatusInternet(IEspDevice device, IEspStatusLight statusLight) {
        int deviceVersionValue = getDeviceVersionValue(device);
        if (deviceVersionValue < getProtocolVersionValue()) {
            EspCommandLightOldProtocol cmdOld = new EspCommandLightOldProtocol();
            return cmdOld.postInternet(device, statusLight);
        } else {
            EspCommandLightNewProtocol cmdNew = new EspCommandLightNewProtocol();
            return cmdNew.postInternet(device, statusLight);
        }
    }

    @Override
    public boolean doCommandMulticastPostStatusInternet(String deviceKey, IEspStatusLight statusLight,
        List<String> bssids) {
        EspCommandLightNewProtocol cmdNew = new EspCommandLightNewProtocol();
        return cmdNew.postInternetMulticast(deviceKey, statusLight, bssids);
    }
}
