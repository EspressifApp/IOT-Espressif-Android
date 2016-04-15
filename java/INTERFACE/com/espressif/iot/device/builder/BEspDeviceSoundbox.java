package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceSoundbox;
import com.espressif.iot.model.device.EspDeviceSoundbox;

public class BEspDeviceSoundbox implements IBEspDeviceSoundbox {

    private BEspDeviceSoundbox() {
    }

    private static class InstanceHolder {
        static BEspDeviceSoundbox instance = new BEspDeviceSoundbox();
    }

    public static BEspDeviceSoundbox getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public IEspDeviceSoundbox alloc() {
        return new EspDeviceSoundbox();
    }

}
