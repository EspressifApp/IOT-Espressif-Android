package com.espressif.iot.db;

import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.db.greenrobot.daos.DeviceDBDao;
import com.espressif.iot.db.greenrobot.daos.DeviceDBDao.Properties;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.object.db.IDeviceDBManager;

import de.greenrobot.dao.query.Query;

public class IOTDeviceDBManager implements IDeviceDBManager, IEspSingletonObject
{
    private final static Logger log = Logger.getLogger(IOTDeviceDBManager.class);
    
    private DeviceDBDao deviceDao;
    
    // Singleton Pattern
    private static IOTDeviceDBManager instance = null;
    
    private IOTDeviceDBManager(DaoSession daoSession)
    {
        deviceDao = daoSession.getDeviceDBDao();
    }
    
    public static void init(DaoSession daoSession)
    {
        instance = new IOTDeviceDBManager(daoSession);
    }
    
    public static IOTDeviceDBManager getInstance()
    {
        return instance;
    }
    
    /**
     * get the next negative id for activating device used as device id
     * 
     * @return
     */
    private long getNextNegativeId()
    {
        Query<DeviceDB> query = deviceDao.queryBuilder().where(Properties.Id.lt(0)).build();
        long nextId = 0;
        List<DeviceDB> deviceList = query.list();
        for (DeviceDB device : deviceList)
        {
            long deviceId = device.getId();
            if (deviceId < nextId)
            {
                nextId = deviceId;
            }
        }
        // the nextId should be less than the most min device id
        --nextId;
        log.debug(Thread.currentThread().toString() + "##getNextNegativeId(): " + nextId);
        return nextId;
    }
    
    private long getNegativeDeviceIdByBssid(String bssid)
    {
        Query<DeviceDB> query = deviceDao.queryBuilder().where(Properties.Bssid.eq(bssid)).build();
        DeviceDB device = query.unique();
        if (device == null)
        {
            log.info(Thread.currentThread().toString() + "##getNegativeDeviceIdByBssid(bssid=[" + bssid + "]): 0");
            return 0;
        }
        else
        {
            long result = device.getId();
            log.info(Thread.currentThread().toString() + "##getNegativeDeviceIdByBssid(bssid=[" + bssid + "]): "
                + result);
            return result;
        }
    }
    
    private DeviceDB getDeviceById(long deviceId)
    {
        Query<DeviceDB> query = deviceDao.queryBuilder().where(Properties.Id.eq(deviceId)).build();
        DeviceDB result = query.unique();
        log.debug(Thread.currentThread().toString() + "##getDeviceById(deviceId=[" + deviceId + "]): " + result);
        return result;
    }
    
    private DeviceDB getDeviceByBssid(String bssid)
    {
        Query<DeviceDB> query = deviceDao.queryBuilder().where(Properties.Bssid.eq(bssid)).build();
        DeviceDB result = query.unique();
        log.debug(Thread.currentThread().toString() + "##getDeviceByBssid(bssid=[" + bssid + "]): " + result);
        return result;
    }
    
    public synchronized void renameDevice(long deviceId, String name)
    {
        DeviceDB device = getDeviceById(deviceId);
        if (device != null)
        {
            device.setName(name);
            deviceDao.update(device);
            log.info(Thread.currentThread().toString() + "##renameDevice(deviceId=[" + deviceId + "],name=[" + name
                + "])");
        }
        else
        {
            log.debug(Thread.currentThread().toString() + "##renameDevice(deviceId=[" + deviceId + "],name=[" + name
                + "]): is ignored,for device isn't exist in local db yet.");
        }
    }
    
    @Override
    public synchronized long insertActivatingDevice(String key, String bssid, int type, int state, String name,
        String rom_version, String latest_rom_version, long timestamp, long userId)
    {
        // to support local device save into db, bssid will become unique
        deleteDevicesByBssid(bssid);
        long deviceId = getNegativeDeviceIdByBssid(bssid);
        if (deviceId >= 0)
        {
            deviceId = getNextNegativeId();
        }
        DeviceDB deviceDB =
            new DeviceDB(deviceId, key, bssid, type, state, true, name, rom_version, latest_rom_version, timestamp,
                0, userId);
        log.info(Thread.currentThread().toString() + "##insertActivactingDevice(deviceId=[" + deviceId + "],bssid=["
            + bssid + "],type=[" + type + "],state=[" + state + "],isOwner=[" + true + "],name=[" + name
            + "],rom_version=[" + rom_version + "],latest_rom_version=[" + latest_rom_version + "],timestamp=["
            + timestamp + "],userId=[" + userId + "])");
        return deviceDao.insertOrReplace(deviceDB);
    }
    
    private void deleteDevicesByBssid(long newId, String bssid)
    {
        if (newId > 0)
        {
//            deleteDevicesByBssid(bssid);
            Query<DeviceDB> query = deviceDao.queryBuilder().where(Properties.Bssid.eq(bssid)).build();
            List<DeviceDB> deviceList = query.list();
            for (int i = deviceList.size() - 1; i >= 0; i--)
            {
                long deviceId = deviceList.get(i).getId();
                if (deviceId < 0)
                {
                    deviceList.remove(i);
                }
            }
            if (!deviceList.isEmpty())
            {
                deviceDao.deleteInTx(deviceList);
                log.info(Thread.currentThread().toString() + "##deleteDevicesByBssid(newId=" + newId + " bssid="
                    + bssid + "]):" + deviceList);
            }
        }
        else
        {
            Query<DeviceDB> query = deviceDao.queryBuilder().where(Properties.Bssid.eq(bssid)).build();
            List<DeviceDB> deviceList = query.list();
            for (int i = deviceList.size() - 1; i >= 0; i--)
            {
                long deviceId = deviceList.get(i).getId();
                if (deviceId > 0)
                {
                    deviceList.remove(i);
                }
            }
            if (!deviceList.isEmpty())
            {
                deviceDao.deleteInTx(deviceList);
                log.info(Thread.currentThread().toString() + "##deleteDevicesByBssid(newId=" + newId + " bssid="
                    + bssid + "]):" + deviceList);
            }
        }
    }
    
    @Override
    public synchronized void insertOrReplace(long deviceId, String key, String bssid, int type, int state,
        boolean isOwner, String name, String rom_version, String latest_rom_version, long timestamp,
        long activatedTime, long userId)
    {
        // to support local device save into db, bssid will become unique
        // but guest can't delete user's device
        deleteDevicesByBssid(deviceId, bssid);
        DeviceDB deviceDB =
            new DeviceDB(deviceId, key, bssid, type, state, isOwner, name, rom_version, latest_rom_version, timestamp,
                activatedTime, userId);
        deviceDao.insertOrReplace(deviceDB);
        log.info(Thread.currentThread().toString() + "##insertOrReplace(deviceId=[" + deviceId + "],bssid=[" + bssid
            + "],type=[" + type + "],state=[" + state + "],isOwner=[" + isOwner + "],name=[" + name + "],timestamp=["
            + timestamp + "],userId=[" + userId + "])");
    }
    
    @Override
    public synchronized void delete(long deviceId)
    {
        Query<DeviceDB> query = deviceDao.queryBuilder().where(Properties.Id.eq(deviceId)).build();
        DeviceDB result = query.unique();
        if (result != null)
        {
            deviceDao.delete(result);
            log.info(Thread.currentThread().toString() + "##delete(deviceId=[" + deviceId + "]): " + result);
        }
        else
        {
            log.debug(Thread.currentThread().toString() + "##delete(deviceId=[" + deviceId
                + "]): the device isn't exist");
        }
    }

    @Override
    public void deleteDevicesByDeviceId(long deviceId)
    {
        Query<DeviceDB> query = deviceDao.queryBuilder().where(Properties.Id.eq(deviceId)).build();
        DeviceDB result = query.unique();
        if (result != null)
        {
            String bssid = result.getBssid();
            deleteDevicesByBssid(bssid);
        }
        else
        {
            log.debug(Thread.currentThread().toString() + "##deleteDevicesByDeviceId(deviceId=[" + deviceId
                + "]): the device isn't exist");
        }
    }
    
    @Override
    public void deleteDevicesByBssid(String bssid)
    {
        Query<DeviceDB> query = deviceDao.queryBuilder().where(Properties.Bssid.eq(bssid)).build();
        List<DeviceDB> deviceList = query.list();
        if (!deviceList.isEmpty())
        {
            deviceDao.deleteInTx(deviceList);
            log.info(Thread.currentThread().toString() + "##deleteDevicesByDeviceId(bssid=" + bssid + "]):"
                + deviceList);
        }
    }
}
