package com.espressif.iot.action.device.espbutton;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.EspButtonKeySettings;

public interface IEspActionEspButtonActionSet extends IEspActionLocal
{
    /**
     * Set settings of EspButton key
     * 
     * @param device
     * @param settings
     * @return
     */
    boolean doActionEspButtonActionSet(IEspDevice device, EspButtonKeySettings settings);
}
