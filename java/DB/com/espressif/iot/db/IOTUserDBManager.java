package com.espressif.iot.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.db.greenrobot.daos.UserDBDao.Properties;
import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.db.greenrobot.daos.UserDB;
import com.espressif.iot.db.greenrobot.daos.UserDBDao;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.object.db.IDeviceDB;
import com.espressif.iot.object.db.IUserDB;
import com.espressif.iot.object.db.IUserDBManager;
import com.espressif.iot.user.IEspUser;

import de.greenrobot.dao.query.Query;

public class IOTUserDBManager implements IUserDBManager, IEspSingletonObject
{
    private static final Logger log = Logger.getLogger(IOTUserDBManager.class);
    
    private UserDBDao userDao;
    
    // Singleton Pattern
    private static IOTUserDBManager instance = null;
    
    private IOTUserDBManager(DaoSession daoSession)
    {
        this.userDao = daoSession.getUserDBDao();
    }
    
    public static void init(DaoSession daoSession)
    {
        instance = new IOTUserDBManager(daoSession);
    }
    
    public static IOTUserDBManager getInstance()
    {
        return instance;
    }
    
    /**
     * change User info in local db
     * 
     * @param id user's id
     * @param email user's email
     * @param key user's key
     * @param name user name
     */
    @Override
    public void changeUserInfo(long id, String email, String key, String name)
    {
        log.info(Thread.currentThread().toString() + "##changeUserInfo(id=[" + id + "],email=[" + email
            + "],key=[" + key + "]");
        Query<UserDB> query = userDao.queryBuilder().where(Properties.IsLastLogin.eq(true)).build();
        UserDB result = query.unique();
        if (result != null)
        {
            // clear the old login info
            result.setIsLastLogin(false);
            userDao.update(result);
        }
        result = new UserDB(id, email, key, name, true);
        userDao.insertOrReplace(result);
    }
    
    /**
     * load the UserDB from local db
     * 
     * @return UserDB info
     */
    @Override
    public IUserDB load()
    {
        UserDB result = null;
        
        Query<UserDB> query = userDao.queryBuilder().where(Properties.IsLastLogin.eq(true)).build();
        result = query.unique();
        if (result != null)
        {
            log.debug(Thread.currentThread().toString() + "##load(): " + result);
            return result;
        }
        // the app hasn't been login yet
        log.debug(Thread.currentThread().toString() + "##load(): " + result);
        return result;
    }
    
    private List<IDeviceDB> __getUserDeviceList(long userId)
    {
        Query<UserDB> query = userDao.queryBuilder().where(Properties.Id.eq(userId)).build();
        UserDB user = query.unique();
        List<DeviceDB> result = null;
        if (user != null)
        {
            user.resetDevices();
            result = user.getDevices();
        }
        log.debug(Thread.currentThread().toString() + "##getUserDeviceList(userId=[" + userId + "]): " + result);
        if (result != null)
        {
            List<IDeviceDB> deviceList = new ArrayList<IDeviceDB>();
            deviceList.addAll(result);
            return deviceList;
        }
        else
        {
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<IDeviceDB> getUserDeviceList(long userId)
    {
        List<IDeviceDB> deviceList = new ArrayList<IDeviceDB>();
//        if (userId > 0)
//        {
//            List<IDeviceDB> userDeviceList = __getUserDeviceList(userId);
//            deviceList.addAll(userDeviceList);
//            List<IDeviceDB> guestDeviceList = __getUserDeviceList(IEspUser.GUEST_USER_ID);
//            deviceList.addAll(guestDeviceList);
//        }
//        else
//        {
            List<IDeviceDB> userDeviceList = __getUserDeviceList(userId);
            deviceList.addAll(userDeviceList);
//        }
        return deviceList;
    }
}
