package com.espressif.iot.base.net.proxy;

public interface EspProxyServer
{
    /**
     * start the EspProxyServer
     */
    void start();
    
    /**
     * stop the EspProxyServer
     */
    void stop();
    
    /**
     * get the EspProxyServer port
     * 
     * @return the EspProxyServer port
     */
    int getEspProxyServerPort();
    
}
