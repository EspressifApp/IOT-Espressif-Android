package com.espressif.iot.object;

/**
 * all Singleton class will implement it
 * 
 * all of these classes will implement such method:
 * 
 * public static IEspSingletonObject getInstance() { return instance; }
 * 
 * otherwise, it should have IBxxx, IB = Interface Builder, @see IEspSingletonBuilder
 * 
 * @author afunx
 * 
 */
public interface IEspSingletonObject
{
    
}
