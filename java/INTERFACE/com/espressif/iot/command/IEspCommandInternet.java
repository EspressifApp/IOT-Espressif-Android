package com.espressif.iot.command;

/**
 * IEspCommandInternet indicate that the action is related to internet
 * 
 * @author afunx
 * 
 */
public interface IEspCommandInternet extends IEspCommand
{
    static final int PAGE_NUMBER = 1000;
    // static final String MethodPut = "?method=PUT";
    static final String At = "at";
    
    static final String Authorization = "Authorization";
    
    static final String Time_Zone = "Time-Zone";
    
    static final String Epoch = "Epoch";
    
    // static final String epoch = "epoch";
    static final String Bssid = "bssid";
    
    // static final String Content = "content";
    // static final String Updated = "updated";
    static final String User_Id = "user_id";
    
    // static final String M_Type = "m_type";
    // static final String Read_Message_Time = "read_message_time";
    // static final String Created = "created";
    // static final String Device_Id = "device_id";
    // static final String Id = "id";
    // static final String Attrs_Json = "attrs_json";
    static final String Rom_Version = "rom_version";
    
    static final String Latest_Rom_Version = "latest_rom_version";
    
    // static final String Pre_Rom_Version = "pre_rom_version";
    // static final String Device_Name = "device_name";
    static final String Email = "email";
    
    static final String Password = "password";
    
    static final String Token = "token";
    
    static final String IsOwnerKey = "is_owner_key";
    
    static final String Ptype = "ptype";
    
    static final String Status = "status";
    
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
    
    static final String Name = "name";
    
    static final String Scope = "scope";
    
    // static final String Description = "description";
    // static final String Location = "location";
    // static final String Message = "message";
    // static final String Messages = "messages";
    // static final String Datatime = "datetime";
    // static final String Offset = "offset";
    // static final String RowCount = "row_count";
    // static final String Start = "start";
    // static final String End = "end";
    static final String USER = "user";
    
    static final String User_Name = "username";
    
    static final String User_Email = "email";
    
    static final String User_Password = "password";
    // static final String DELIVER_TO_DEVICE_TRUE = "?deliver_to_device=true";
    // static final String DELIVER_TO_DEVICE_FALSE = "?deliver_to_device=false";
    //
    // static final String Method_Delete = "?method=DELETE";
    static final String Root_Device_Id = "root_device_id";
    
    static final String Router = "router";
}
