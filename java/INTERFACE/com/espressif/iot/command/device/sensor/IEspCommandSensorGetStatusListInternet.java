package com.espressif.iot.command.device.sensor;

import java.util.List;

import org.json.JSONObject;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandSensor;
import com.espressif.iot.type.device.status.IEspStatusSensor;

public interface IEspCommandSensorGetStatusListInternet extends IEspCommandInternet, IEspCommandSensor
{
    /**
     * get the statusSensor list to the Sensor by Internet
     * 
     * @param deviceKey the device key
     * @param startTimestamp the start of UTC timestamp
     * @param endTimestamp the end of UTC timestamp
     * @return the list of EspStatusSensor
     */
    List<IEspStatusSensor> doCommandSensorGetStatusListInternet(String deviceKey, long startTimestamp,
        long endTimestamp);
    
    /**
     * parse the json to IEspStatusSensor
     * 
     * @param json the json to be parsed
     * @return the IEspStatusSensor
     */
    IEspStatusSensor parseJson(JSONObject json);
    
    /**
     * get the url used by http request
     * @return the url used by http request
     */
    String getUrl();
}
