package com.espressif.iot.command;

import com.espressif.iot.object.IEspObject;

/**
 * IEspCommand is the foundation of on @see IEspAction. IEspCommand only has simple logic IEspUser and IEspDevice will
 * do IEspAction instead of IEspCommand
 * 
 * @author afunx
 * 
 */
public interface IEspCommand extends IEspObject
{
    static final String Authorization = "Authorization";
    
    static final String Token = "token";
    
    static final String Status = "status";
}
