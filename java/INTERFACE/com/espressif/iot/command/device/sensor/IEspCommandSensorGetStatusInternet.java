package com.espressif.iot.command.device.sensor;

import org.json.JSONObject;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandSensor;
import com.espressif.iot.type.device.status.IEspStatusSensor;

public interface IEspCommandSensorGetStatusInternet extends IEspCommandInternet, IEspCommandSensor
{
    /**
     * get the statusSensor to the Sensor by Internet
     * 
     * @param deviceKey the device key
     * @return the status of the Sensor or null(if executed fail)
     */
    IEspStatusSensor doCommandSensorGetStatusInternet(String deviceKey);
    
    /**
     * parse the json to IEspStatusSensor
     * 
     * @param json the json to be parsed
     * @return the IEspStatusSensor
     */
    IEspStatusSensor parseJson(JSONObject json);
    
    /**
     * get the url used by http request
     * 
     * @return the url used by http request
     */
    String getUrl();
}
