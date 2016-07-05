package com.espressif.iot.device;

import com.espressif.iot.type.device.status.IEspStatusSoundbox;

public interface IEspDeviceSoundbox extends IEspDevice {
    public void setStatusSoundbox(IEspStatusSoundbox status);

    public IEspStatusSoundbox getStatusSoundbox();
}
