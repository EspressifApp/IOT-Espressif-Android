package com.espressif.iot.action.device.common.timer;

import org.json.JSONObject;

import com.espressif.iot.action.IEspActionDevice;
import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionDeviceTimerPostInternet extends IEspActionDevice, IEspActionInternet
{
    /**
     * Post the edited timer to the server
     * 
     * @param device
     * @param timerJSON
     * @return edit timer suc or failed
     */
    boolean doActionDeviceTimerPostInternet(IEspDevice device, JSONObject timerJSON);
}
