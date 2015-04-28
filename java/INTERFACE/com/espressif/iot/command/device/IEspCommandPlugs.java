package com.espressif.iot.command.device;

public interface IEspCommandPlugs extends IEspCommandDevice
{
    final String URL = "https://iot.espressif.cn/v1/datastreams/plugs/datapoint/?deliver_to_device=true";
    
    final String KEY_PLUGS_STATUS = "plugs_status";
    final String KEY_PLUGS_VALUE = "plugs_value";
    final String KEY_APERTURE_COUNT = "plugs_num";
    
    final String KEY_DATA_POINT = "datapoint";
}
