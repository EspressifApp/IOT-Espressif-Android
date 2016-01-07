package com.espressif.iot.command.device.common;

import java.util.List;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandDevice;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger;

public interface IEspCommandDeviceTriggerInternet extends IEspCommandInternet, IEspCommandDevice
{
    public static final String URL = "https://iot.espressif.cn/v1/trigger/";
    
    public static final String KEY_TRIGGERS = "triggers";
    public static final String KEY_TRIGGER = "trigger";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DIMENSION = "dimension_index";
    public static final String KEY_STREAM = "stream_name";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_INTERVAL_FUNC = "interval_func";
    public static final String KEY_COMPARE_TYPE = "compare_type";
    public static final String KEY_COMPARE_VALUE = "threshold_value";
    public static final String KEY_NOTIFY_RULES = "notify_rules";
    public static final String KEY_SCOPE = "scope";
    public static final String KEY_VIA = "via";
    public static final String KEY_TMPL_TOKEN = "tmpl_token";
    
    /**
     * Get triggers list of the device from server
     * 
     * @param device
     * @return null is failed
     */
    public List<EspDeviceTrigger> getTriggersInternet(IEspDevice device);
    
    /**
     * Create a new trigger on server
     * 
     * @param device
     * @param trigger
     * @return id of the created trigger, -1 is failed
     */
    public long createTriggerInternet(IEspDevice device, EspDeviceTrigger trigger);
    
    /**
     * Update the trigger on server
     * 
     * @param device
     * @param trigger must contain it's id
     * @return true is successful, false is failed
     */
    public boolean updateTriggerInternet(IEspDevice device, EspDeviceTrigger trigger);
    
    /**
     * Delete the trigger on server
     * 
     * @param device
     * @param id the trigger's id
     * @return true is successful, false is failed
     */
    public boolean deleteTriggerInternet(IEspDevice device, long id);
}
