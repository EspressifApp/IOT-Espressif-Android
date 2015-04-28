package com.espressif.iot.user.builder;

import com.espressif.iot.db.IOTUserDBManager;
import com.espressif.iot.model.user.EspUser;
import com.espressif.iot.object.db.IUserDB;
import com.espressif.iot.user.IBEspUser;
import com.espressif.iot.user.IEspUser;

public class BEspUser implements IBEspUser
{
    /*
     * Singleton lazy initialization start
     */
    private IEspUser instanceSingleton = null;
    
    private BEspUser()
    {
        instanceSingleton = new EspUser();
    }
    
    private static class InstanceHolder
    {
        static BEspUser instanceBuilder = new BEspUser();
    }
    
    public static BEspUser getBuilder()
    {
        return InstanceHolder.instanceBuilder;
    }
    
    @Override
    public IEspUser getInstance()
    {
        return instanceSingleton;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public IEspUser loadUser()
    {
        IUserDB userDB = IOTUserDBManager.getInstance().load();
        if (userDB != null)
        {
            instanceSingleton.setIsPwdSaved(userDB.getIsPwdSaved());
            instanceSingleton.setAutoLogin(userDB.getIsAutoLogin());
            instanceSingleton.setUserId(userDB.getId());
            instanceSingleton.setUserKey(userDB.getKey());
            instanceSingleton.setUserEmail(userDB.getEmail());
            instanceSingleton.setUserPassword(userDB.getPassword());
        }
        return instanceSingleton;
    }
}
