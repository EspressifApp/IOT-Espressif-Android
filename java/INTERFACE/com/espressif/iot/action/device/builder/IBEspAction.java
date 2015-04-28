package com.espressif.iot.action.device.builder;

import com.espressif.iot.action.IEspAction;
import com.espressif.iot.object.IEspObjectBuilder;

public interface IBEspAction extends IEspObjectBuilder
{
    /**
     * alloc the EspAction by its interface class
     * 
     * @param clazz the interface class
     * @return the new EspAction
     */
    IEspAction alloc(Class<?> clazz);
}
