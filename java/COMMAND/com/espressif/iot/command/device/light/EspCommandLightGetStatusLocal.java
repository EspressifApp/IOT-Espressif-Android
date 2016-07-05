package com.espressif.iot.command.device.light;

import java.net.InetAddress;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspCommandLightGetStatusLocal extends EspCommandLight implements IEspCommandLightGetStatusLocal {

    @Override
    public String getLocalUrl(InetAddress inetAddress) {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=light";
    }

    @Override
    public IEspStatusLight doCommandLightGetStatusLocal(IEspDevice device) {
        int deviceVersionValue = getDeviceVersionValue(device);
        if (deviceVersionValue < getProtocolVersionValue()) {
            EspCommandLightOldProtocol cmdOld = new EspCommandLightOldProtocol();
            return cmdOld.getLocal(device);
        } else {
            EspCommandLightNewProtocol cmdNew = new EspCommandLightNewProtocol();
            return cmdNew.getLocal(device);
        }
    }
}
