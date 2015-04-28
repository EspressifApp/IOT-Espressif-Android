package com.espressif.iot.object.db;

import java.util.List;

import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.object.IEspDBManager;

public interface IUserDBManager extends IEspDBManager
{
    /**
     * load the user info from db (it is called when the application start launching)
     * 
     * @return @see IUserDB
     */
    IUserDB load();
    
    /**
     * change the user info in local db
     * 
     * @param id user id
     * @param email user email
     * @param password user password
     * @param key user key
     * @param isPwdSaved whether the password is saved
     * @param isAutoLogin whether it is auto login
     */
    void changeUserInfo(long id, String email, String password, String key, boolean isPwdSaved, boolean isAutoLogin);
    
    /**
     * get the user's device list (for greenDao use List<DeviceDB> as the return value, we have to use DeviceDB instead
     * of IDeviceDB)
     * 
     * @param userId the user Id
     * @return the device list
     */
    List<DeviceDB> getUserDeviceList(long userId);
}
