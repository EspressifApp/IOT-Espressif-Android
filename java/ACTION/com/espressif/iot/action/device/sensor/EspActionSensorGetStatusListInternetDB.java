package com.espressif.iot.action.device.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.espressif.iot.action.device.sensor.IEspActionSensorGetStatusListInternetDB;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.db.IOTGenericDataDBManager;
import com.espressif.iot.db.IOTGenericDataDirectoryDBManager;
import com.espressif.iot.object.db.IGenericDataDB;
import com.espressif.iot.object.db.IGenericDataDBManager;
import com.espressif.iot.object.db.IGenericDataDirectoryDBManager;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.EspStatusFlammable;
import com.espressif.iot.type.device.status.EspStatusHumiture;
import com.espressif.iot.type.device.status.EspStatusVoltage;
import com.espressif.iot.type.device.status.IEspStatusSensor;
import com.espressif.iot.util.TimeUtil;

public abstract class EspActionSensorGetStatusListInternetDB implements IEspActionSensorGetStatusListInternetDB
{
    
    private final static Logger log = Logger.getLogger(EspActionSensorGetStatusListInternetDB.class);
    
    private Future<List<IEspStatusSensor>> mFuture1DB = null;
    
    private Future<List<IEspStatusSensor>> mFuture2Internet = null;
    
    private Future<List<IEspStatusSensor>> mFuture3DB = null;
    
    /**
     * when boolean cancel(boolean mayInterruptIfRunning) is called, mFuture2Internet is maybe null. so we should use it
     * to let mFuture2Internet be cancelled later
     */
    private volatile boolean mIsCancelled = false;
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        mIsCancelled = true;
        if (mFuture2Internet == null)
        {
            return true;
        }
        else
        {
            return mFuture2Internet.cancel(mayInterruptIfRunning);
        }
    }
    
    private List<IEspStatusSensor> parseStatusSensorList(final List<IGenericDataDB> dataListInDB)
    {
        List<IEspStatusSensor> result = new ArrayList<IEspStatusSensor>();
        for (IGenericDataDB dataInDB : dataListInDB)
        {
            IEspStatusSensor statusSensor = this.parseStatus(dataInDB);
            result.add(statusSensor);
        }
        return result;
    }
    
    private List<IGenericDataDB> parseDataDBList(final long deviceId, final List<IEspStatusSensor> statusSensorList)
    {
        List<IGenericDataDB> result = new ArrayList<IGenericDataDB>();
        for (IEspStatusSensor statusSensor : statusSensorList)
        {
            IGenericDataDB genericDataDB = this.parseStatus(deviceId, statusSensor);
            result.add(genericDataDB);
        }
        return result;
    }
    
    /**
     * update accessed timestamp, save data list get from server and remove the expired data in local db asyn
     * 
     * @param deviceId the device id
     * @param statusSensorList the status of Sensor list
     * @param startTimestampUTCDay start timestamp and it should be the 00:00:00 at UTC time
     * @param endTimestampUTCDay end timestamp and it should be the 00:00:00 at UTC time
     */
    private void __updateAccessedTimestampSaveDataRemoveExpiredDataInDBAsyn(final long deviceId,
        final List<IEspStatusSensor> statusSensorList, final long startTimestampUTCDay,
        final long endTimestampUTCDay)
    {
        final IGenericDataDBManager iotGenericDataDBManager = IOTGenericDataDBManager.getInstance();
        final IGenericDataDirectoryDBManager iotGenericDataDirectoryDBManager =
            IOTGenericDataDirectoryDBManager.getInstance();
        Runnable task = new Runnable()
        {
            
            @Override
            public void run()
            {
                List<IGenericDataDB> dataDBList = parseDataDBList(deviceId, statusSensorList);
                log.info("insertOrReplaceDataList start");
                iotGenericDataDBManager.insertOrReplaceDataList(dataDBList, startTimestampUTCDay, endTimestampUTCDay);
                log.info("deleteExpiredDataDirectoryAndData start");
                iotGenericDataDirectoryDBManager.deleteExpiredDataDirectoryAndData();
            }
            
        };
        EspBaseApiUtil.submit(task);
    }
    
    private IEspStatusSensor __createStatusByDeviceType(EspDeviceType deviceType)
    {
        switch(deviceType)
        {
            case FLAMMABLE:
                return new EspStatusFlammable();
            case HUMITURE:
                return new EspStatusHumiture();
            case VOLTAGE:
                return new EspStatusVoltage();
            case LIGHT:
                break;
            case NEW:
                break;
            case PLUG:
                break;
            case PLUGS:
                break;
            case REMOTE:
                break;
            case SOUNDBOX:
                break;
            case ROOT:
                break;
        }
        return null;
    }
    
    /**
     * process the data List for UI display
     * 
     * @param dataList the data list
     * @param startTimestamp startTimestamp get from UI
     * @param endTimestamp endTimestamp get from UI
     * @param interval the interval of each points
     * @param deviceType the type of device
     * @return the data List to be displayed in UI
     */
    private List<IEspStatusSensor> __processDataList(final List<IEspStatusSensor> dataList,
        final long startTimestamp, final long endTimestamp, final long interval, final EspDeviceType deviceType)
    {
        log.info("##__processDataList: startTimestamp = " + TimeUtil.getDateStr(startTimestamp, null));
        log.info("##__processDataList: endTimestamp = " + TimeUtil.getDateStr(endTimestamp, null));
        if (dataList.isEmpty())
        {
            return Collections.emptyList();
        }
        List<IEspStatusSensor> result = new ArrayList<IEspStatusSensor>();
        long sectionCount = 0;
        long sectionAt = startTimestamp / interval * interval;
        double sectionX = 0;
        double sectionY = 0;
        long currentAt;
        // padding null at head
        long headAt = dataList.get(0).getAt() / interval * interval;
        while (sectionAt < headAt)
        {
            result.add(null);
            sectionAt += interval;
        }
        // padding data at middle
        for (IEspStatusSensor statusData : dataList)
        {
            currentAt = statusData.getAt();
            sectionCount++;
            sectionX += statusData.getX();
            if(statusData.isYSupported())
            {
                sectionY += statusData.getY();
            }
            
            if (currentAt - sectionAt > interval)
            {
                // add data into result
                IEspStatusSensor data = __createStatusByDeviceType(deviceType);
                data.setAt(sectionAt);
                data.setX(sectionX / sectionCount);
                if(statusData.isYSupported())
                {
                    data.setY(sectionY / sectionCount);
                }
                result.add(data);
                // refresh sectionCount, sectionAt, sectionX and sectionY
                sectionCount = 0;
                sectionAt += interval;
                sectionX = 0;
                if(data.isYSupported())
                {
                    sectionY = 0;
                }
                while (currentAt - sectionAt > interval)
                {
                    result.add(null);
                    sectionAt += interval;
                }
            }
        }
        // add remnant data
        if (sectionCount != 0)
        {
            // add data into result
            IEspStatusSensor data = __createStatusByDeviceType(deviceType);
            data.setAt(sectionAt);
            data.setX(sectionX / sectionCount);
            if(data.isYSupported())
            {
                data.setY(sectionY / sectionCount);
            }
            result.add(data);
        }
        // padding null at tail
        long tailAt = (dataList.get(dataList.size() - 1).getAt() + interval - 1) / interval * interval;
        while (sectionAt < tailAt)
        {
            result.add(null);
            sectionAt += interval;
        }
        // cut off the valid data list
        int start = 0;
        int end = 0;
        IEspStatusSensor currentSensor;
        long currentTimestamp;
        for (int i = 0; i < result.size(); i++)
        {
            currentSensor = result.get(i);
            if (currentSensor == null)
            {
                continue;
            }
            currentTimestamp = currentSensor.getAt();
            if (currentTimestamp < startTimestamp)
            {
                start = i;
            }
            if (currentTimestamp < endTimestamp)
            {
                end = i;
            }
        }
        // for end isn't including, so we should add 1
        end++;
        for (int i = start; i < end; i++)
        {
            if (result.get(i) == null)
            {
                continue;
            }
//            log.error("##__processDataList: dataTime = " + TimeUtil.getDateStr(result.get(i).getAt(), null));
        }
        
//        log.error("##__processDataList: start = " + start + ",end = " + end + ",size = " + result.size());
        return result.subList(start, end);
    }
    
    @Override
    public List<IEspStatusSensor> doActionSensorGetStatusListInternetDB(final long deviceId,
        final String deviceKey, final long startTimestamp, final long endTimestamp, final long interval, final EspDeviceType deviceType)
    {
        log.info("startTimestamp = " + TimeUtil.getDateStr(startTimestamp, null));
        log.info("endTimestamp = " + TimeUtil.getDateStr(endTimestamp, null));
        final long startTimestampUTCday = TimeUtil.getUTCDayFloor(startTimestamp);
        final long endTimestampUTCday = TimeUtil.getUTCDayCeil(endTimestamp);
        final IGenericDataDBManager iotGenericDataDBManager = IOTGenericDataDBManager.getInstance();
        final IGenericDataDirectoryDBManager iotGenericDataDirectoryDBManager =
            IOTGenericDataDirectoryDBManager.getInstance();
        // get startTimestamp and endTimestamp from local db
        final long startTimestampFromServer =
            iotGenericDataDirectoryDBManager.getStartTimestampFromServer(deviceId,
                startTimestampUTCday,
                endTimestampUTCday);
        log.info("startTimestampFromServer = " + TimeUtil.getDateStr(startTimestampFromServer, null));
        final long endTimestampFromServer =
            iotGenericDataDirectoryDBManager.getEndTimestampFromServer(deviceId,
                startTimestampUTCday,
                endTimestampUTCday);
        log.info("endTimestampFromServer = " + TimeUtil.getDateStr(endTimestampFromServer, null));
        // local db: [startTimestamp , startTimestampFromServer)
        // server: [startTimestampFromServer, endTimestampFromServer)
        // local db: [endTimestampFromServer , endTimestamp)
        
        // get data from local db and internet asyn
        Callable<List<IEspStatusSensor>> task1DB = new Callable<List<IEspStatusSensor>>()
        {
            
            @Override
            public List<IEspStatusSensor> call()
                throws Exception
            {
                List<IGenericDataDB> dataListInDB =
                    iotGenericDataDBManager.getDataList(deviceId, startTimestampUTCday, startTimestampFromServer);
                List<IEspStatusSensor> result = parseStatusSensorList(dataListInDB);
                return result;
            }
            
        };
        Callable<List<IEspStatusSensor>> task2Internet = new Callable<List<IEspStatusSensor>>()
        {
            
            @Override
            public List<IEspStatusSensor> call()
                throws Exception
            {
                if (startTimestampFromServer >= endTimestampFromServer)
                {
                    return Collections.emptyList();
                }
                List<IEspStatusSensor> result =
                    EspActionSensorGetStatusListInternetDB.this.doCommandSensorGetStatusListInternet(deviceKey,
                        startTimestampFromServer,
                        endTimestampFromServer);
                return result;
            }
            
        };
        Callable<List<IEspStatusSensor>> task3DB = new Callable<List<IEspStatusSensor>>()
        {
            
            @Override
            public List<IEspStatusSensor> call()
                throws Exception
            {
                List<IGenericDataDB> dataListInDB =
                    iotGenericDataDBManager.getDataList(deviceId, endTimestampFromServer, endTimestampUTCday);
                List<IEspStatusSensor> result = parseStatusSensorList(dataListInDB);
                return result;
            }
            
        };
        mFuture1DB = EspBaseApiUtil.submit(task1DB);
        mFuture2Internet = EspBaseApiUtil.submit(task2Internet);
        mFuture3DB = EspBaseApiUtil.submit(task3DB);
        
        if (mIsCancelled)
        {
            mFuture2Internet.cancel(true);
        }
        
        // wait all task finished
        while (!mFuture1DB.isDone() || !mFuture2Internet.isDone() || !mFuture3DB.isDone())
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                // don't support cancel now
                e.printStackTrace();
            }
        }
        if (mFuture2Internet.isCancelled())
        {
            log.warn("doActionSensorGetStatusListInternetDB() is canceled");
            return Collections.emptyList();
        }
        // combine the local and internet
        List<IEspStatusSensor> listAll = new ArrayList<IEspStatusSensor>();
        try
        {
            List<IEspStatusSensor> list1 = mFuture1DB.get();
            List<IEspStatusSensor> list2 = mFuture2Internet.get();
            // insert into local db of list get from server
            __updateAccessedTimestampSaveDataRemoveExpiredDataInDBAsyn(deviceId,
                list2,
                startTimestampUTCday,
                endTimestampUTCday);
            List<IEspStatusSensor> list3 = mFuture3DB.get();
            listAll.addAll(list1);
            listAll.addAll(list2);
            listAll.addAll(list3);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        return __processDataList(listAll, startTimestamp, endTimestamp, interval, deviceType);
    }
}
