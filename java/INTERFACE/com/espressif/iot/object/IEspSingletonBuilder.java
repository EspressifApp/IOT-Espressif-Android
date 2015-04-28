package com.espressif.iot.object;

/**
 * all objects in com.espressif.iot used to build Singleton Object implement it
 * 
 * @author afunx
 * 
 */
public interface IEspSingletonBuilder
{
    /**
     * 
     * @return the Singleton instance
     */
    IEspSingletonObject getInstance();
}
