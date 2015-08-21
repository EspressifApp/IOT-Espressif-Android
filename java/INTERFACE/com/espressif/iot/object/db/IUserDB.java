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
    /**
     * get the user's id
     * @return the user's id
     */
    long getId();
    
    /**
     * set the user's id
     * @param id the user's id
     */
    void setId(long id);
    
    /**
     * get the user's email
     * @return the user's email
     */
    String getEmail();
    
    /**
     * set the user's email
     * @param email the user's email
     */
    void setEmail(String email);
    
    /**
     * get the user's key
     * @return the user's key
     */
    String getKey();
    
    /**
     * set the user's key
     * @param key the user's key
     */
    void setKey(String key);
    
    /**
     * get user name
     * @return user name
     */
    String getName();
    
    /**
     * set user name
     * 
     * @param name
     */
    void setName(String name);
    
    /**
     * get whether the user is last login
     * @return whether the user is last login
     */
    boolean getIsLastLogin();
    
    /**
     * set whether the user is last login
     * @param isLastLogin whether the user is last login
     */
    void setIsLastLogin(boolean isLastLogin);
    
}
