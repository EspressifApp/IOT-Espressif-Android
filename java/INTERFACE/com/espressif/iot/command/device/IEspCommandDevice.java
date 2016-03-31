package com.espressif.iot.command.device;

import com.espressif.iot.command.IEspCommand;

/**
 * IEspCommandDevice indicate that the command is belong to devices
 * 
 * @author afunx
 * 
 */
public interface IEspCommandDevice extends IEspCommand
{
    public static final String URL_MULTICAST =
        "https://iot.espressif.cn/v1/device/rpc/?deliver_to_device=true&action=multicast&bssids=";
    
    public static final int MULTICAST_GROUP_LENGTH_LIMIT = 50;
    
    public static final String KEY_GROUP = "group";
}
