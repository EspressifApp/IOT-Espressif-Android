package com.espressif.iot.db;

import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.db.greenrobot.daos.GenericDataDB;
import com.espressif.iot.db.greenrobot.daos.GenericDataDirectoryDB;
import com.espressif.iot.db.greenrobot.daos.GenericDataDirectoryDBDao;
import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.db.greenrobot.daos.GenericDataDirectoryDBDao.Properties;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.object.db.IGenericDataDirectoryDBManager;
import com.espressif.iot.util.TimeUtil;

import de.greenrobot.dao.query.Query;

public class IOTGenericDataDirectoryDBManager implements IGenericDataDirectoryDBManager, IEspSingletonObject
{
    private final static Logger log = Logger.getLogger(IOTGenericDataDirectoryDBManager.class);
    
    private GenericDataDirectoryDBDao genericDataDirectoryDao;
    
    // Singleton Pattern
    private static IOTGenericDataDirectoryDBManager instance = null;
    
    private IOTGenericDataDirectoryDBManager(DaoSession daoSession)
    {
        genericDataDirectoryDao = daoSession.getGenericDataDirectoryDBDao();
    }
    
    public static void init(DaoSession daoSession)
    {
        instance = new IOTGenericDataDirectoryDBManager(daoSession);
    }
    
    public static IOTGenericDataDirectoryDBManager getInstance()
    {
        return instance;
    }
    
    private GenericDataDirectoryDB __getGenericDataDirectory(long deviceId, long dayStartTimestamp)
    {
        Query<GenericDataDirectoryDB> query =
            genericDataDirectoryDao.queryBuilder()
                .where(Properties.DeviceId.eq(deviceId), Properties.Day_start_timestamp.eq(dayStartTimestamp))
                .build();
        GenericDataDirectoryDB result = query.unique();
        log.debug(Thread.currentThread().toString() + "##__getGenericDataDirectory(deviceId=[" + deviceId
            + "],dayStartTimestamp=[" + TimeUtil.getDateStr(dayStartTimestamp, null) + "]): " + result);
        return result;
    }
    
    private GenericDataDirectoryDB __getExpiredGenericDataDirectory()
    {
        Query<GenericDataDirectoryDB> query =
            genericDataDirectoryDao.queryBuilder().orderAsc(Properties.Lastest_accessed_timestamp).limit(1).build();
        GenericDataDirectoryDB result = query.uniqueOrThrow();
        log.debug(Thread.currentThread().toString() + "##__getExpiredGenericDataDirectory(): " + result);
        return result;
    }
    
    @Override
    public synchronized void deleteExpiredDataDirectoryAndData()
    {
        IOTGenericDataDBManager genericDataDBManager = IOTGenericDataDBManager.getInstance();
        if (genericDataDBManager.__getDataTotalCount() < MAX_DATA_COUNT)
        {
            log.debug(Thread.currentThread().toString()
                + "##deleteExpiredDataDirectoryAndData() isn't overabundance, delete stop");
            return;
        }
        log.debug(Thread.currentThread().toString() + "##deleteExpiredDataDirectoryAndData() executed()");
        // get expired data directory
        GenericDataDirectoryDB dataDirectoryDB = __getExpiredGenericDataDirectory();
        // get expired data directory's data list
        dataDirectoryDB.resetDatas();
        List<GenericDataDB> dataDBList = dataDirectoryDB.getDatas();
        // delete expired data list
        genericDataDBManager.__deleteDataList(dataDBList);
        // delete expired data directory, if the app is shutdown here, we can't get data of the day anymore,
        // but the probability is very little and the affect is very little, so we don't handle it for the moment
        genericDataDirectoryDao.delete(dataDirectoryDB);
        // check whether need to deleteExpiredDataDirectoryAndData() once more
        if (genericDataDBManager.__getDataTotalCount() >= MAX_DATA_COUNT)
        {
            log.debug(Thread.currentThread().toString()
                + "##deleteExpiredDataDirectoryAndData() is still overabundance, delete continue");
            deleteExpiredDataDirectoryAndData();
        }
    }
    
    @Override
    public synchronized long __insertOrReplaceDataDirectory(long deviceId, long dayStartTimestamp, long indexTimestamp)
    {
        GenericDataDirectoryDB dataDirectoryDB = __getGenericDataDirectory(deviceId, dayStartTimestamp);
        long result;
        long latestAccessedTimestamp = System.currentTimeMillis();
        if (dataDirectoryDB == null)
        {
            dataDirectoryDB =
                new GenericDataDirectoryDB(null, deviceId, dayStartTimestamp, indexTimestamp, latestAccessedTimestamp);
        }
        else
        {
            // only indexTimestamp bigger is valid
            if (indexTimestamp > dataDirectoryDB.getIndex_timestamp())
            {
                dataDirectoryDB.setIndex_timestamp(indexTimestamp);
            }
            dataDirectoryDB.setLastest_accessed_timestamp(latestAccessedTimestamp);
        }
        result = genericDataDirectoryDao.insertOrReplace(dataDirectoryDB);
        log.error(Thread.currentThread().toString() + "##__insertOrReplaceDataDirectory(deviceId=[" + deviceId
            + "],dayStartTimestamp=[" + TimeUtil.getDateStr(dataDirectoryDB.getDay_start_timestamp(), null)
            + "],indexTimestamp=[" + TimeUtil.getDateStr(dataDirectoryDB.getIndex_timestamp(), null)
            + "]): directoryId=" + result);
        return result;
    }
    
    @Override
    public long getStartTimestampFromServer(long deviceId, long startTimestampFromUI, long endTimestampFromUI)
    {
        long result;
        long dayStartTimestamp = TimeUtil.getUTCDayFloor(startTimestampFromUI);
        long indexTimestamp = startTimestampFromUI;
        GenericDataDirectoryDB currentDataDirectory = __getGenericDataDirectory(deviceId, dayStartTimestamp);
        log.error("##getStartTimestampFromServer():currentDataDirectory1=" + currentDataDirectory);
        while (currentDataDirectory != null)
        {
            indexTimestamp = currentDataDirectory.getIndex_timestamp();
            // indexTimestamp >= endTimestampFromUI means that we only need data from local db
            // indexTimestamp != dayStartTimestamp + TimeUtil.ONE_DAY_LONG_VALUE means that the directory isn't full
            if (indexTimestamp >= endTimestampFromUI
                || indexTimestamp != dayStartTimestamp + TimeUtil.ONE_DAY_LONG_VALUE)
            {
                break;
            }
            dayStartTimestamp += TimeUtil.ONE_DAY_LONG_VALUE;
            currentDataDirectory = __getGenericDataDirectory(deviceId, dayStartTimestamp);
            log.error("##getStartTimestampFromServer():currentDataDirectory2=" + currentDataDirectory);
        }
        // if result >= endTimestampFromUI means that we only need data from local db
        result = indexTimestamp;
        // we should get the next data, so result = indexTimestamp + 1 second, if the result isn't UTC day
        if (!TimeUtil.isUTCDay(indexTimestamp))
        {
            result += TimeUtil.ONE_SECOND_LONG_VALUE;
        }
        log.debug(Thread.currentThread().toString() + "##getStartTimestampFromServer(deviceId=[" + deviceId
            + "],startTimestampFromUI=[" + TimeUtil.getDateStr(startTimestampFromUI, null) + "],endTimestampFromUI=["
            + TimeUtil.getDateStr(endTimestampFromUI, null) + "]): " + TimeUtil.getDateStr(result, null));
        return result;
    }
    
    @Override
    public long getEndTimestampFromServer(long deviceId, long startTimestampFromUI, long endTimestampFromUI)
    {
        log.error("##getEndTimestampFromServer():startTimestampFromUI="
            + TimeUtil.getDateStr(startTimestampFromUI, null));
        log.error("##getEndTimestampFromServer():endTimestampFromUI=" + TimeUtil.getDateStr(endTimestampFromUI, null));
        long result;
        long dayStartTimestamp = TimeUtil.getUTCDayCeil(endTimestampFromUI - TimeUtil.ONE_DAY_LONG_VALUE);
        long indexTimestamp;
        GenericDataDirectoryDB currentDataDirectory = __getGenericDataDirectory(deviceId, dayStartTimestamp);
        log.error("##getEndTimestampFromServer():currentDataDirectory1=" + currentDataDirectory);
        while (currentDataDirectory != null)
        {
            indexTimestamp = currentDataDirectory.getIndex_timestamp();
            // indexTimestamp <= startTimestampFromUI means that we only need data from local db
            // indexTimestamp != dayStartTimestamp + TimeUtil.ONE_DAY_LONG_VALUE means that the directory isn't full
            if (dayStartTimestamp <= startTimestampFromUI - TimeUtil.ONE_DAY_LONG_VALUE
                || indexTimestamp != dayStartTimestamp + TimeUtil.ONE_DAY_LONG_VALUE)
            {
                break;
            }
            dayStartTimestamp -= TimeUtil.ONE_DAY_LONG_VALUE;
            currentDataDirectory = __getGenericDataDirectory(deviceId, dayStartTimestamp);
            log.error("##getEndTimestampFromServer():currentDataDirectory2=" + currentDataDirectory);
        }
        // if result <= startTimestampFromUI means that we only need data from local db
        result = dayStartTimestamp + TimeUtil.ONE_DAY_LONG_VALUE;
        log.error(Thread.currentThread().toString() + "##getEndTimestampFromServer(deviceId=[" + deviceId
            + "],startTimestampFromUI=[" + TimeUtil.getDateStr(startTimestampFromUI, null) + "],endTimestampFromUI=["
            + TimeUtil.getDateStr(endTimestampFromUI, null) + "]): " + TimeUtil.getDateStr(result, null));
        return result;
    }
    
}
