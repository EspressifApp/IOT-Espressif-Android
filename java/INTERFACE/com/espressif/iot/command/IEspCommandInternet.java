package com.espressif.iot.command;

/**
 * IEspCommandInternet indicate that the action is related to internet
 * 
 * @author afunx
 * 
 */
public interface IEspCommandInternet extends IEspCommand
{
    static final String Time_Zone = "Time-Zone";
    
    static final String Epoch = "Epoch";
    
    static final String Bssid = "bssid";
    
    static final String Rom_Version = "rom_version";
    
    static final String Latest_Rom_Version = "latest_rom_version";
    
    static final String Email = "email";
    
    static final String Password = "password";
    
    static final String IsOwnerKey = "is_owner_key";
    
    static final String Ptype = "ptype";
    
    static final String Id = "id";
    
    static final String Key = "key";
    
    static final String Keys = "keys";
    
    // static final String Metadata = "metadata";
    static final String Device = "device";
    
    static final String Datapoint = "datapoint";
    
    static final String Datapoints = "datapoints";
    
    static final String X = "x";
    
    static final String Y = "y";
    
    static final String Z = "z";
    
    static final String K = "k";
    
    static final String L = "l";
    
    static final String Payload = "payload";
    
    static final String Info = "info";
    
    static final String Rssi = "rssi";
    
    static final String Name = "name";
    
    static final String Scope = "scope";
    
    static final String Remember = "remember";
    
    static final String Parent_Mdev_Mac = "parent_mdev_mac";
    
    static final String METHOD_PUT = "method=PUT";
    
    static final String METHOD_DELETE = "method=DELETE";
}
