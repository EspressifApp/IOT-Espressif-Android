package com.espressif.iot.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.text.TextUtils;

import com.espressif.iot.db.greenrobot.daos.ApDB;
import com.espressif.iot.db.greenrobot.daos.ApDBDao;
import com.espressif.iot.db.greenrobot.daos.ApDBDao.Properties;
import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.object.db.IApDB;
import com.espressif.iot.object.db.IApDBManager;

import de.greenrobot.dao.query.Query;

public class IOTApDBManager implements IApDBManager, IEspSingletonObject
{
    private final static Logger log = Logger.getLogger(IOTDeviceDBManager.class);
    
    private ApDBDao apDao;
    
    // Singleton Pattern
    private static IOTApDBManager instance = null;
    
    private IOTApDBManager(DaoSession daoSession)
    {
        apDao = daoSession.getApDBDao();
    }
    
    public static void init(DaoSession daoSession)
    {
        instance = new IOTApDBManager(daoSession);
    }
    
    public static IOTApDBManager getInstance()
    {
        return instance;
    }
    
    private ApDB __getLastSelectedApDB()
    {
        Query<ApDB> query = apDao.queryBuilder().where(Properties.IsLastSelected.eq(true)).build();
        return query.unique();
    }
    
    private ApDB __getApDB(String bssid)
    {
        Query<ApDB> query = apDao.queryBuilder().where(Properties.Bssid.eq(bssid)).build();
        return query.unique();
    }
    
    private List<ApDB> __getApDBList(String ssid)
    {
        Query<ApDB> query = apDao.queryBuilder().where(Properties.Ssid.eq(ssid)).build();
        return query.list();
    }
    
    @Override
    public String getPassword(String bssid)
    {
        ApDB apDB = __getApDB(bssid);
        String result = null;
        if (apDB != null)
        {
            result = apDB.getPassword();
        }
        log.debug(Thread.currentThread().toString() + "##getPassword(bssid=[" + bssid + "]): " + result);
        return result;
    }
    
    private void __setIsLastSelectedFalse()
    {
        ApDB apDB = __getLastSelectedApDB();
        if (apDB != null)
        {
            log.info(Thread.currentThread().toString() + "##__setIsLastSelectedFalse(bssid=[" + apDB.getBssid()
                + "],ssid=[" + apDB.getSsid() + "],password=[" + apDB.getPassword() + "]) setIsLastSelected false");
            apDB.setIsLastSelected(false);
            apDao.insertOrReplace(apDB);
        }
    }
    
    @Override
    public void insertOrReplace(String bssid, String ssid, String password)
    {
        insertOrReplace(bssid, ssid, password, "");
    }
    
    @Override
    public IApDB getLastSelectedApDB()
    {
        ApDB apDB = __getLastSelectedApDB();
        if (apDB == null)
        {
            log.debug(Thread.currentThread().toString() + "##getLastSelectedApDB null");
        }
        else
        {
            log.debug(Thread.currentThread().toString() + "##getLastSelectedApDB(bssid=[" + apDB.getBssid()
                + "],ssid=[" + apDB.getSsid() + "],password=[" + apDB.getPassword() + "])");
        }
        
        return apDB;
    }
    
    private List<ApDB> __getAllApDBList()
    {
        return apDao.loadAll();
    }
    
    @Override
    public List<IApDB> getAllApDBList()
    {
        List<IApDB> apDBList = new ArrayList<IApDB>();
        apDBList.addAll(__getAllApDBList());
        return apDBList;
    }
    
    @Override
    public synchronized void insertOrReplace(String bssid, String ssid, String password, String deviceBssid)
    {
        __setIsLastSelectedFalse();
        
        // Delete deviceBssid from other ap
        List<ApDB> list = __getAllApDBList();
        for (ApDB ap : list)
        {
            if (!ap.getBssid().equals(bssid) && ap.getDeviceBssids().contains(deviceBssid))
            {
                StringBuilder sb = new StringBuilder();
                String[] devices = ap.getDeviceBssids().split(SEPARATOR);
                for (int i = 0; i < devices.length; i++)
                {
                    if (!TextUtils.isEmpty(devices[i]) && !devices[i].equals(deviceBssid))
                    {
                        sb.append(SEPARATOR).append(devices[i]);
                    }
                }
                ap.setDeviceBssids(sb.toString());
                apDao.insertOrReplace(ap);
            }
        }
        
        ApDB apDB = __getApDB(bssid);
        if (apDB == null)
        {
            apDB = new ApDB(null, bssid, ssid, password, true, 0, deviceBssid);
        }
        else
        {
            apDB.setBssid(bssid);
            if (!password.equals(apDB.getPassword()))
            {
                apDB.setConfiguredFailedCount(0);
            }
            apDB.setPassword(password);
            apDB.setSsid(ssid);
            apDB.setIsLastSelected(true);
            String deviceBssids = apDB.getDeviceBssids();
            if (!deviceBssids.contains(deviceBssid))
            {
                deviceBssids += SEPARATOR;
                deviceBssids += deviceBssid;
                apDB.setDeviceBssids(deviceBssids);
            }
        }
        log.info(Thread.currentThread().toString() + "##insertOrReplace(bssid=[" + bssid + "],ssid=[" + ssid
            + "],deviceBssid=[" + deviceBssid + "],password=[" + password + "])");
        apDao.insertOrReplace(apDB);
    }
    
    @Override
    public synchronized void updateApInfo(String deviceBssid, boolean isConfiguredSuc)
    {
        ApDB apDB = null;
        List<ApDB> list = __getAllApDBList();
        for (ApDB ap : list)
        {
            if (ap.getDeviceBssids().contains(deviceBssid))
            {
                apDB = ap;
                break;
            }
        }
        
        if (apDB == null)
        {
            log.debug("updateApInfo not found deviceBssid" + deviceBssid);
            return;
        }
        
        // remove deviceBssid
        String deviceBssids[] = apDB.getDeviceBssids().split(SEPARATOR);
        StringBuilder sb = new StringBuilder();
        for (String _deviceBssid : deviceBssids)
        {
            if (!_deviceBssid.equals(deviceBssid) && !_deviceBssid.equals(""))
            {
                sb.append(SEPARATOR);
                sb.append(_deviceBssid);
            }
        }
        apDB.setDeviceBssids(sb.toString());
        int configuredFailedCount = apDB.getConfiguredFailedCount();
        if (isConfiguredSuc)
        {
            // update apDB's configuredFailedCount to 0
            apDB.setConfiguredFailedCount(0);
        }
        else
        {
            // update apDB's configuredFailedCount to +1
            configuredFailedCount++;
            apDB.setConfiguredFailedCount(configuredFailedCount);
        }
        if (configuredFailedCount < MAX_FAILED_COUNT)
        {
            // update apDB
            apDao.update(apDB);
        }
        else
        {
            // delete apDB
            apDB.delete();
        }
    }
    
    @Override
    public void delete(String ssid)
    {
        List<ApDB> apDBList = __getApDBList(ssid);
        apDao.deleteInTx(apDBList);
    }
    
    @Override
    public void updatePassword(String ssid, String password)
    {
        List<ApDB> apDBList = __getApDBList(ssid);
        for (ApDB apDB : apDBList)
        {
            apDB.setPassword(password);
            apDB.setConfiguredFailedCount(0);
        }
        apDao.updateInTx(apDBList);
    }
    
}
