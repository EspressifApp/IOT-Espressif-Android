package com.espressif.iot.model.device;

import com.espressif.iot.device.IEspDeviceSoundbox;
import com.espressif.iot.type.device.status.EspStatusPlug;
import com.espressif.iot.type.device.status.EspStatusSoundbox;
import com.espressif.iot.type.device.status.IEspStatusSoundbox;

public class EspDeviceSoundbox extends EspDevice implements IEspDeviceSoundbox {
    private IEspStatusSoundbox mStatus;

    public EspDeviceSoundbox() {
        mStatus = new EspStatusSoundbox();
    }

    public void setStatusSoundbox(IEspStatusSoundbox status) {
        mStatus = status;
    }

    public IEspStatusSoundbox getStatusSoundbox() {
        return mStatus;
    }

    @Override
    public Object clone()
        throws CloneNotSupportedException {
        EspDeviceSoundbox device = (EspDeviceSoundbox)super.clone();
        // deep copy
        IEspStatusSoundbox status = device.getStatusSoundbox();
        device.mStatus = (IEspStatusSoundbox)((EspStatusPlug)status).clone();
        return device;
    }
}
