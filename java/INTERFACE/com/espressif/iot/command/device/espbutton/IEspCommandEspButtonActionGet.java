package com.espressif.iot.command.device.espbutton;

import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.EspButtonKeySettings;

public interface IEspCommandEspButtonActionGet extends IEspCommandEspButton, IEspCommandLocal
{
    /**
     * Get settings of EspButton keys
     * 
     * @param device
     * @return
     */
    public List<EspButtonKeySettings> doCommandEspButtonActionGet(IEspDevice device);
}
