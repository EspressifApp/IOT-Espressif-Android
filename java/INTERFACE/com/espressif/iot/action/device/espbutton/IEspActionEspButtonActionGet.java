package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.EspButtonKeySettings;

public interface IEspActionEspButtonActionGet extends IEspActionLocal
{
    /**
     * Get settings of EspButton keys
     * 
     * @param device
     * @return
     */
    List<EspButtonKeySettings> doActionEspButtonActionGet(IEspDevice device);
}
