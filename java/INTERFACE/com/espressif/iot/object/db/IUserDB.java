package com.espressif.iot.object.db;

import com.espressif.iot.object.IEspDBObject;

/**
 * the User db should save such properties as follows
 * 
 * @author afunx
 * 
 */
public interface IUserDB extends IEspDBObject
{
    long getId();
    
    void setId(long id);
    
    String getEmail();
    
    void setEmail(String email);
    
    String getPassword();
    
    void setPassword(String password);
    
    String getKey();
    
    void setKey(String key);
    
    boolean getIsLastLogin();
    
    void setIsLastLogin(boolean isLastLogin);
    
    boolean getIsPwdSaved();
    
    void setIsPwdSaved(boolean isPwdSaved);
    
    boolean getIsAutoLogin();
    
    void setIsAutoLogin(boolean isAutoLogin);
    
}
