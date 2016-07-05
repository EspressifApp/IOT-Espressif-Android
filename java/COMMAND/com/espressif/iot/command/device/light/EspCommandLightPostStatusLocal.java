package com.espressif.iot.command.device.light;

import java.net.InetAddress;
import java.util.List;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspCommandLightPostStatusLocal extends EspCommandLight implements IEspCommandLightPostStatusLocal {

    @Override
    public String getLocalUrl(InetAddress inetAddress) {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=light";
    }

    @Override
    public boolean doCommandLightPostStatusLocal(IEspDevice device, IEspStatusLight statusLight) {
        int deviceVersionValue = getDeviceVersionValue(device);
        if (deviceVersionValue < getProtocolVersionValue()) {
            EspCommandLightOldProtocol cmdOld = new EspCommandLightOldProtocol();
            return cmdOld.postLocal(device, statusLight);
        } else {
            EspCommandLightNewProtocol cmdNew = new EspCommandLightNewProtocol();
            return cmdNew.postLocal(device, statusLight);
        }
    }

    @Override
    public void doCommandLightPostStatusLocalInstantly(IEspDevice device, IEspStatusLight statusLight,
        Runnable disconnectedCallback) {
        int deviceVersionValue = getDeviceVersionValue(device);
        if (deviceVersionValue < getProtocolVersionValue()) {
            EspCommandLightOldProtocol cmdOld = new EspCommandLightOldProtocol();
            cmdOld.postLocalInstantly(device, statusLight, disconnectedCallback);
        } else {
            EspCommandLightNewProtocol cmdNew = new EspCommandLightNewProtocol();
            cmdNew.postLocalInstantly(device, statusLight, disconnectedCallback);
        }
    }

    @Override
    public boolean doCommandMulticastPostStatusLocal(InetAddress address, IEspStatusLight statusLight,
        List<String> bssids) {
        EspCommandLightNewProtocol cmdNew = new EspCommandLightNewProtocol();
        return cmdNew.postLocalMulticast(address, statusLight, bssids);
    }
}
