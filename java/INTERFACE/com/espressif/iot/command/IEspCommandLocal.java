package com.espressif.iot.command;

import java.net.InetAddress;

/**
 * IEspCommandLocal indicate that the action is related to local
 * 
 * @author afunx
 * 
 */
public interface IEspCommandLocal extends IEspCommand
{
    /**
     * get the local url by device's inetAddress
     * @param inetAddress device's inetAddress
     * @return local url
     */
    String getLocalUrl(InetAddress inetAddress);
}
