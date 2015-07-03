package com.espressif.iot.action.device.longsocket;

import java.net.InetAddress;

import com.espressif.iot.base.net.rest.mesh.EspSocketRequestBaseEntity;
import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspLightCommandBuilder
{
    static final String Period = "period";
    
    static final String Rgb = "rgb";
    
    static final String Red = "red";
    
    static final String Green = "green";
    
    static final String Blue = "blue";
    
    static final String CWhite = "cwhite";
    
    static final String WWhite = "wwhite";
    
    static final String Authorization = "Authorization";
    
    static final String Token = "token";
    
    static final String Datapoint = "datapoint";
    
    static final String X = "x";
    
    static final String Y = "y";
    
    static final String Z = "z";
    
    static final String K = "k";
    
    static final String L = "l";
    
    /**
     * build the local get status request
     * 
     * @param inetAddress the device's inetAddress
     * @param router the router(only mesh device has it)
     * @return the local get status request
     */
    EspSocketRequestBaseEntity buildLocalGetStatusRequest(InetAddress inetAddress, String router);
    
    /**
     * build the local get status request
     * 
     * @param inetAddress the device's inetAddress
     * @param statusLight the status of light
     * @param router the router(only mesh device has it)
     * @return the local post status request
     */
    EspSocketRequestBaseEntity buildLocalPostStatusRequest(InetAddress inetAddress, IEspStatusLight statusLight, String router);
    
    /**
     * build the Internet get status request
     * 
     * @param deviceKey the device key
     * @return the Internet get status request
     */
    EspSocketRequestBaseEntity buildInternetGetStatusRequest(String deviceKey);
    
    /**
     * build the Internet post status request
     * 
     * @param deviceKey the device key
     * @param statusLight the status of light
     * @param router the router(only mesh device has it)
     * @return the Internet post status request
     */
    EspSocketRequestBaseEntity buildInternetPostStatusRequest(String deviceKey, IEspStatusLight statusLight, String router);
}
