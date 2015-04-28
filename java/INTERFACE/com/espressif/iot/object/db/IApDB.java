package com.espressif.iot.object.db;

import com.espressif.iot.object.IEspDBObject;

/**
 * the Ap db should save such properties as follows
 * 
 * @author afunx
 * 
 */
public interface IApDB extends IEspDBObject
{
    Long getId();
    
    void setId(Long id);
    
    String getBssid();
    
    void setBssid(String bssid);
    
    String getPassword();
    
    void setPassword(String password);
    
    boolean getIsLastSelected();
    
    void setIsLastSelected(boolean isLastSelected);
}
