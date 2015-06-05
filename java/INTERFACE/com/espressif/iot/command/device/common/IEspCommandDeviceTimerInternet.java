package com.espressif.iot.command.device.common;

import org.json.JSONObject;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandDevice;

public interface IEspCommandDeviceTimerInternet extends IEspCommandDevice, IEspCommandInternet
{
    static final String URL_GET = "https://iot.espressif.cn/v1/device/timers/?is_humanize_format=true";
    
    static final String URL_NEW = "https://iot.espressif.cn/v1/device/timers/?deliver_to_device=true&is_humanize_format=true";
    
    static final String URL_EDIT =
        "https://iot.espressif.cn/v1/device/timers/?method=PUT&deliver_to_device=true&is_humanize_format=true";
    
    static final String URL_DELETE =
        "https://iot.espressif.cn//v1/device/timers/?method=DELETE&deliver_to_device=true&is_humanize_format=true";
    
    /**
     * Post the edited Device timer
     * 
     * @param timerJSON
     * @return post the timer suc or failed
     */
    boolean doCommandDeviceTimerPost(JSONObject timerJSON);
    
    /**
     * Get the timers from server
     * 
     * @return get the timers suc or failed
     */
    boolean doCommandDeviceTimerGet();
    
    /**
     * Delete the timer
     * 
     * @param timerId
     * @return delete suc or failed
     */
    boolean doCommandDeviceTimerDelete(long timerId);
}
