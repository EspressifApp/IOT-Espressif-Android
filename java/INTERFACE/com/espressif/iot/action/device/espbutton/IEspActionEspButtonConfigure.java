package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.command.device.espbutton.IEspButtonConfigureListener;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionEspButtonConfigure extends IEspActionLocal
{
    /**
     * Add or replace a EspButton
     * 
     * @param newTempKey
     * @param newMacAddress
     * @param permitAllRequest
     * @param deviceList
     * @param isBroadcast
     * @param listener
     * @param oldMacAddress
     * @return
     */
    boolean doActionEspButtonConfigure(String newTempKey, String newMacAddress, boolean permitAllRequest,
        List<IEspDevice> deviceList, boolean isBroadcast, IEspButtonConfigureListener listener, String... oldMacAddress);
}
