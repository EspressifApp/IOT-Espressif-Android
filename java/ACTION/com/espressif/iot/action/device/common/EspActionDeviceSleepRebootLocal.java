package com.espressif.iot.action.device.common;

import com.espressif.iot.command.device.common.EspCommandDeviceSleepRebootLocal;
import com.espressif.iot.command.device.common.IEspCommandDeviceSleepRebootLocal;
import com.espressif.iot.type.device.EspDeviceType;

public class EspActionDeviceSleepRebootLocal implements IEspActionDeviceSleepRebootLocal
{
    
    @Override
    public void doActionDeviceSleepRebootLocal(EspDeviceType type)
    {
        IEspCommandDeviceSleepRebootLocal command = new EspCommandDeviceSleepRebootLocal();
        switch(type) {
            case FLAMMABLE:
            case HUMITURE:
            case VOLTAGE:
                command.doCommandDeviceSleepLocal();
                break;
            case LIGHT:
            case PLUG:
            case REMOTE:
            case PLUGS:
            case SOUNDBOX:
                command.doCommandDeviceRebootLocal();
                break;
            case NEW:
            case ROOT:
                break;
        }
    }
    
}
