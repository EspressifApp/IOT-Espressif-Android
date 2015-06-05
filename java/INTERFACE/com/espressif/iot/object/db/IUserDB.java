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
     * get the user's password
     * @return the user's password
     */
    String getPassword();
    
    /**
     * set the user's password
     * @param password the user's password
     */
    void setPassword(String password);
    
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
     * get whether the user is last login
     * @return whether the user is last login
     */
    boolean getIsLastLogin();
    
    /**
     * set whether the user is last login
     * @param isLastLogin whether the user is last login
     */
    void setIsLastLogin(boolean isLastLogin);
    
    /**
     * get whether the user's password is saved
     * @return whether the user's password is saved
     */
    boolean getIsPwdSaved();
    
    /**
     * set whether the user's password is saved
     * @param isPwdSaved whether the user's password is saved
     */
    void setIsPwdSaved(boolean isPwdSaved);
    
    /**
     * get whether the user is auto login
     * @return whether the user is auto login
     */
    boolean getIsAutoLogin();
    
    /**
     * set whether the user is auto login
     * @param isAutoLogin whether the user is auto login
     */
    void setIsAutoLogin(boolean isAutoLogin);
    
}
