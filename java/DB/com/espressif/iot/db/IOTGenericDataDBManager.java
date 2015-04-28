package com.espressif.iot.db;

import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.db.greenrobot.daos.GenericDataDBDao;
import com.espressif.iot.db.greenrobot.daos.GenericDataDB;
import com.espressif.iot.db.greenrobot.daos.GenericDataDBDao.Properties;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.object.db.IGenericDataDBManager;
import com.espressif.iot.object.db.IGenericDataDirectoryDBManager;
import com.espressif.iot.util.TimeUtil;

import de.greenrobot.dao.query.Query;

public class IOTGenericDataDBManager implements IGenericDataDBManager, IEspSingletonObject
{
    private static final Logger log = Logger.getLogger(IOTGenericDataDBManager.class);
    
    private GenericDataDBDao genericDataDao;
    
    // Singleton Pattern
    private static IOTGenericDataDBManager instance = null;
    
    private IOTGenericDataDBManager(DaoSession daoSession)
    {
        genericDataDao = daoSession.getGenericDataDBDao();
    }
    
    public static void init(DaoSession daoSession)
    {
        instance = new IOTGenericDataDBManager(daoSession);
    }
    
    public static IOTGenericDataDBManager getInstance()
    {
        return instance;
    }
    
    @Override
    public synchronized void updateDataDirectoryLastAccessedTime(long deviceId, long startTimestampUTCDay,
        long endTimestampUTCDay)
    {
        log.debug(Thread.currentThread().toString() + "##updateDataDirectoryLastAccessedTime(startTimestampUTCDay=["
            + TimeUtil.getDateStr(startTimestampUTCDay, null) + "],endTimestampUTCDay=["
            + TimeUtil.getDateStr(endTimestampUTCDay, null) + "]): start");
        // insert or replace data directory and set directory id,
        // we only need to update the last accessed timestamp,
        // use the method maybe do something unnecessary, but it will take very little time,
        // so we use the method here to make source code more generic
        List<GenericDataDB> result = getDataList(deviceId, startTimestampUTCDay, endTimestampUTCDay);
        __insertOrReplaceDataDirectory(result, startTimestampUTCDay, endTimestampUTCDay);
        log.debug(Thread.currentThread().toString() + "##updateDataDirectoryLastAccessedTime(startTimestampUTCDay=["
            + TimeUtil.getDateStr(startTimestampUTCDay, null) + "],endTimestampUTCDay=["
            + TimeUtil.getDateStr(endTimestampUTCDay, null) + "]): end");
    }
    
    @Override
    public List<GenericDataDB> getDataList(long deviceId, long startTimestamp, long endTimestamp)
    {
        log.info(Thread.currentThread().toString() + "##getDataList(startTimestamp=["
            + TimeUtil.getDateStr(startTimestamp, null) + "],endTimestamp=[" + TimeUtil.getDateStr(endTimestamp, null)
            + "]): start");
        Query<GenericDataDB> query =
            genericDataDao.queryBuilder()
                .where(Properties.Timestamp.ge(startTimestamp),
                    Properties.Timestamp.lt(endTimestamp),
                    Properties.DeviceId.eq(deviceId))
                .orderAsc(Properties.Timestamp)
                .build();
        List<GenericDataDB> result = query.list();
        log.info(Thread.currentThread().toString() + "##getDataList(startTimestamp=["
            + TimeUtil.getDateStr(startTimestamp, null) + "],endTimestamp=[" + TimeUtil.getDateStr(endTimestamp, null)
            + "]): end");
        return result;
    }
    
    private void __insertOrReplaceDataDirectory(List<GenericDataDB> dataDBList, long startTimestampUTCDay,
        long endTimestampUTCDay)
    {
        // check valid
        if (startTimestampUTCDay != TimeUtil.getUTCDayCeil(startTimestampUTCDay)
            || endTimestampUTCDay != TimeUtil.getUTCDayFloor(endTimestampUTCDay))
        {
            throw new AssertionError("startTimestampUTCDay = " + startTimestampUTCDay + ",endTimestampUTCDay = "
                + endTimestampUTCDay);
        }
        if (dataDBList == null || dataDBList.isEmpty())
        {
            log.warn(Thread.currentThread().toString() + "##__insertOrReplaceDataDirectory(dataDBList=[" + dataDBList
                + "]), return");
            return;
        }
        else
        {
            log.debug(Thread.currentThread().toString() + "##__insertOrReplaceDataDirectory(dataDBList.size()=["
                + dataDBList.size() + "])");
        }
        GenericDataDB firstData = dataDBList.get(0);
        GenericDataDB lastData = dataDBList.get(dataDBList.size() - 1);
        long deviceId = firstData.getDeviceId();
        IGenericDataDirectoryDBManager genericDataDirectoryDBManager = IOTGenericDataDirectoryDBManager.getInstance();
        // insert data directory for no value could get from internet at head
        int _indexOfData = 0;
        long _directoryId;
        long _indexTimestampUTCDay = startTimestampUTCDay;
        long _floorUTCDayOfFirstData = TimeUtil.getUTCDayFloor(firstData.getTimestamp());
        long _floorUTCDayOfLastData = TimeUtil.getUTCDayFloor(lastData.getTimestamp());
        long _ceilUTCDayOfLastData = TimeUtil.getUTCDayCeil(lastData.getTimestamp());
        long _lastDataTimestamp = lastData.getTimestamp();
        
        // [startTimeStampUTCDay , _floorUTCDayOfFirstData)
        while (_indexTimestampUTCDay < _floorUTCDayOfFirstData)
        {
            log.debug("__insertOrReplaceDataDirectory1: deviceId=" + deviceId + ",_indexTimestampUTCDay="
                + TimeUtil.getDateStr(_indexTimestampUTCDay, null) + ",indexTimestamp="
                + TimeUtil.getDateStr(_indexTimestampUTCDay + TimeUtil.ONE_DAY_LONG_VALUE, null));
            genericDataDirectoryDBManager.__insertOrReplaceDataDirectory(deviceId,
                _indexTimestampUTCDay,
                _indexTimestampUTCDay + TimeUtil.ONE_DAY_LONG_VALUE);
            _indexTimestampUTCDay += TimeUtil.ONE_DAY_LONG_VALUE;
        }
        // [_floorUTCDayOfFirstData, _floorUTCDayOfLastData)
        while (_indexTimestampUTCDay < _floorUTCDayOfLastData)
        {
            log.debug("__insertOrReplaceDataDirectory2: deviceId=" + deviceId + ",_indexTimestampUTCDay="
                + TimeUtil.getDateStr(_indexTimestampUTCDay, null) + ",indexTimestamp="
                + TimeUtil.getDateStr(_indexTimestampUTCDay + TimeUtil.ONE_DAY_LONG_VALUE, null));
            _directoryId =
                genericDataDirectoryDBManager.__insertOrReplaceDataDirectory(deviceId,
                    _indexTimestampUTCDay,
                    _indexTimestampUTCDay + TimeUtil.ONE_DAY_LONG_VALUE);
            while (_indexOfData < dataDBList.size()
                && dataDBList.get(_indexOfData).getTimestamp() < _indexTimestampUTCDay + TimeUtil.ONE_DAY_LONG_VALUE)
            {
                dataDBList.get(_indexOfData).setDirectoryId(_directoryId);
                _indexOfData++;
            }
            _indexTimestampUTCDay += TimeUtil.ONE_DAY_LONG_VALUE;
        }
        
        // [_floorUTCDayOfLastData, _ceilUTCDayOfLastData)
        // 1. _ceilUTCDayOfLastData < endTimestampUTCDay,
        if (_ceilUTCDayOfLastData < endTimestampUTCDay)
        {
            if (_indexTimestampUTCDay < _ceilUTCDayOfLastData)
            {
                log.debug("__insertOrReplaceDataDirectory3: deviceId=" + deviceId + ",_indexTimestampUTCDay="
                    + TimeUtil.getDateStr(_indexTimestampUTCDay, null) + ",indexTimestamp="
                    + TimeUtil.getDateStr(_indexTimestampUTCDay + TimeUtil.ONE_DAY_LONG_VALUE, null));
                _directoryId =
                    genericDataDirectoryDBManager.__insertOrReplaceDataDirectory(deviceId,
                        _indexTimestampUTCDay,
                        _indexTimestampUTCDay + TimeUtil.ONE_DAY_LONG_VALUE);
                while (_indexOfData < dataDBList.size())
                {
                    dataDBList.get(_indexOfData).setDirectoryId(_directoryId);
                    _indexOfData++;
                }
                _indexTimestampUTCDay += TimeUtil.ONE_DAY_LONG_VALUE;
            }
        }
        // 2. _ceilUTCDayOfLastData = endTimestampUTCDay
        else
        {
            if (_indexTimestampUTCDay < _ceilUTCDayOfLastData)
            {
                log.debug("__insertOrReplaceDataDirectory4: deviceId=" + deviceId + ",_indexTimestampUTCDay="
                    + TimeUtil.getDateStr(_indexTimestampUTCDay, null) + ",indexTimestamp="
                    + TimeUtil.getDateStr(_lastDataTimestamp, null));
                _directoryId =
                    genericDataDirectoryDBManager.__insertOrReplaceDataDirectory(deviceId,
                        _indexTimestampUTCDay,
                        _lastDataTimestamp);
                while (_indexOfData < dataDBList.size())
                {
                    dataDBList.get(_indexOfData).setDirectoryId(_directoryId);
                    _indexOfData++;
                }
                _indexTimestampUTCDay += TimeUtil.ONE_DAY_LONG_VALUE;
            }
        }
        // [_ceilUTCDayOfLastData, endTimestampUTCDay)
        if (_ceilUTCDayOfLastData < endTimestampUTCDay)
        {
            while (_indexTimestampUTCDay < endTimestampUTCDay)
            {
                log.debug("__insertOrReplaceDataDirectory5: deviceId=" + deviceId + ",_indexTimestampUTCDay="
                    + TimeUtil.getDateStr(_indexTimestampUTCDay, null) + ",indexTimestamp="
                    + TimeUtil.getDateStr(_indexTimestampUTCDay + TimeUtil.ONE_DAY_LONG_VALUE, null));
                genericDataDirectoryDBManager.__insertOrReplaceDataDirectory(deviceId,
                    _indexTimestampUTCDay,
                    _indexTimestampUTCDay + TimeUtil.ONE_DAY_LONG_VALUE);
                _indexTimestampUTCDay += TimeUtil.ONE_DAY_LONG_VALUE;
            }
        }
    }
    
    @Override
    public synchronized void insertOrReplaceDataList(List<GenericDataDB> dataDBList, long startTimestampUTCDay,
        long endTimestampUTCDay)
    {
        if (dataDBList != null && !dataDBList.isEmpty())
        {
            // insert or replace data directory and set directory id
            __insertOrReplaceDataDirectory(dataDBList, startTimestampUTCDay, endTimestampUTCDay);
            // insert data list in db
            genericDataDao.insertOrReplaceInTx(dataDBList);
            log.debug(Thread.currentThread().toString() + "##insertDataList(dataDBList.size()=[" + dataDBList.size()
                + "])");
        }
        else
        {
            log.warn(Thread.currentThread().toString() + "##insertDataList(dataDBList.size()=[" + dataDBList.size()
                + "])");
        }
    }
    
    @Override
    public long __getDataTotalCount()
    {
        long result = genericDataDao.count();
        log.debug(Thread.currentThread().toString() + "##__getDataTotalCount(): " + result);
        return result;
    }
    
    @Override
    public void __deleteDataList(List<GenericDataDB> dataDBList)
    {
        genericDataDao.deleteInTx(dataDBList);
        log.debug(Thread.currentThread().toString() + "##__deleteDataList(dataDBList.size()=[" + dataDBList.size()
            + "])");
    }
    
}
