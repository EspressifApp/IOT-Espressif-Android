package com.espressif.iot.action;

import com.espressif.iot.object.IEspObject;

/**
 * IEspAction is based on @see IEspCommand. IEspAction has some logic depend on IEspCommand. 
 * IEspUser and IEspDevice will do IEspAction instead of IEspCommand.
 * 
 * @author afunx
 * 
 */
public interface IEspAction extends IEspObject
{
    
}
