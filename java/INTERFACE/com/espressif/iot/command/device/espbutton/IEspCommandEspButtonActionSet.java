package com.espressif.iot.command.device.espbutton;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.EspButtonKeySettings;

public interface IEspCommandEspButtonActionSet extends IEspCommandEspButton, IEspCommandLocal
{
    /**
     * Set settings of EspButton key
     * 
     * @param device
     * @param settings
     * @return
     */
    public boolean doCommandEspButtonActionSet(IEspDevice device, EspButtonKeySettings settings);
}
