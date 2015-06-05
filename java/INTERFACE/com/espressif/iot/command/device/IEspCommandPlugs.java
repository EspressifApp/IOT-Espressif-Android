package com.espressif.iot.command.device;

/**
 * IEspCommandPlugs indicate that the command is belong to plugs
 * 
 * @author xxj
 * 
 */
public interface IEspCommandPlugs extends IEspCommandDevice
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/plugs/datapoint/?deliver_to_device=true";
    
    static final String KEY_PLUGS_STATUS = "plugs_status";
    
    static final String KEY_PLUGS_VALUE = "plugs_value";
    
    static final String KEY_APERTURE_COUNT = "plugs_num";
}
