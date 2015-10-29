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
        "https://iot.espressif.cn/v1/device/rpc/?deliver_to_device=true&action=multicast";
    
    public static final String BROADCAST_MAC = "000000000000";
    
    public static final String MULTICAST_MAC = "01005E000000";
    
    public final static String FAKE_SIP = "FFFFFFFF";
    
    public final static String FAKE_SPORT = "FFFF";
    
    public static final int MULTICAST_GROUP_LENGTH_LIMIT = 50;
    
    public static final String KEY_GROUP_LENGTH = "glen";
    
    public static final String KEY_GROUP = "group";
    
    public static final String KEY_MDEV_MAC = "mdev_mac";
    
    public static final String KEY_SIP = "sip";
    
    public static final String KEY_SPORT = "sport";
}
