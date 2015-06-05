package com.espressif.iot.command.device;

/**
 * IEspCommandRemote indicate that the command is belong to remote
 * 
 * @author afunx
 * 
 */
public interface IEspCommandRemote extends IEspCommandDevice
{
    static final String Remote = "remote";
    
    static final String Addr = "addr";
    
    static final String Cmd = "cmd";
    
    static final String Rep = "rep";
    
    static final String URL = "https://iot.espressif.cn/v1/datastreams/remote/datapoint/?deliver_to_device=true";
}
