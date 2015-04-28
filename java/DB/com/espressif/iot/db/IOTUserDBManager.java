package com.espressif.iot.db;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.db.greenrobot.daos.UserDBDao.Properties;
import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.db.greenrobot.daos.UserDB;
import com.espressif.iot.db.greenrobot.daos.UserDBDao;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.object.db.IUserDB;
import com.espressif.iot.object.db.IUserDBManager;

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
     * @param password user's password
     * @param key user's key
     * @param isPwdSaved whether it is saved password
     * @param isAutoLogin whether it is auto login in
     */
    @Override
    public void changeUserInfo(long id, String email, String password, String key, boolean isPwdSaved,
        boolean isAutoLogin)
    {
        log.info(Thread.currentThread().toString() + "##changeUserInfo(id=[" + id + "],email=[" + email
            + "],password=[" + password + "],key=[" + key + "],isPwdSaved=[" + isPwdSaved + "],isAutoLogin=["
            + isAutoLogin + "])");
        Query<UserDB> query = userDao.queryBuilder().where(Properties.IsLastLogin.eq(true)).build();
        UserDB result = query.unique();
        if (result != null)
        {
            // clear the old login info
            result.setIsLastLogin(false);
            result.setIsPwdSaved(false);
            result.setIsAutoLogin(false);
            userDao.update(result);
        }
        result = new UserDB(id, email, password, key, true, isPwdSaved, isAutoLogin);
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
        Query<UserDB> query = userDao.queryBuilder().where(Properties.IsAutoLogin.eq(true)).build();
        UserDB result = null;
        result = query.unique();
        if (result != null)
        {
            // isSkip is set
            log.debug(Thread.currentThread().toString() + "##load(): " + result);
            return result;
        }
        query = userDao.queryBuilder().where(Properties.IsPwdSaved.eq(true)).build();
        result = query.unique();
        if (result != null)
        {
            // isPwdSaved is set
            log.debug(Thread.currentThread().toString() + "##load(): " + result);
            return result;
        }
        query = userDao.queryBuilder().where(Properties.IsLastLogin.eq(true)).build();
        result = query.unique();
        if (result != null)
        {
            // forget the password
            result.setPassword("");
            log.debug(Thread.currentThread().toString() + "##load(): " + result);
            return result;
        }
        // the app hasn't been login yet
        log.debug(Thread.currentThread().toString() + "##load(): " + result);
        return result;
    }
    
    @Override
    public List<DeviceDB> getUserDeviceList(long userId)
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
            return result;
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
