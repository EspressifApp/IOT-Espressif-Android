package com.espressif.iot.action.device.longsocket;

import org.json.JSONObject;

import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspLightResultParser
{
    static final String Period = "period";
    
    static final String Rgb = "rgb";
    
    static final String Red = "red";
    
    static final String Green = "green";
    
    static final String Blue = "blue";
    
    static final String Datapoint = "datapoint";
    
    static final String X = "x";
    
    static final String Y = "y";
    
    static final String Z = "z";
    
    static final String K = "k";
    
    static final String L = "l";
    
    static final String Status = "status";
    
    /**
     * parse the get status local result
     * 
     * @param result the result to be parsed
     * @return the IEspStatusLight
     */
    IEspStatusLight parseGetStatusLocalResult(JSONObject result);
    
    /**
     * parse the post status local result
     * 
     * @param result the result to be parsed
     * @return whether the post is executed suc
     */
    boolean parsePostStatusLocalResult(JSONObject result);
    
    /**
     * parse the get status Internet result
     * 
     * @param result the result to be parsed
     * @return the IEspStatusLight
     */
    IEspStatusLight parseGetStatusInternetResult(JSONObject result);
    
    /**
     * parse the post status Internet result
     * 
     * @param result the result to be parsed
     * @return whether the post is executed suc
     */
    boolean parsePostStatusInternetResult(JSONObject result);
}
