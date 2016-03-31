package com.espressif.iot.action.device.sensor;

import java.util.List;

import com.espressif.iot.action.IEspActionDB;
import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.device.IEspActionSensor;
import com.espressif.iot.object.db.IGenericDataDB;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.IEspStatusSensor;

public interface IEspActionSensorGetStatusListInternetDB extends IEspActionSensor, IEspActionInternet, IEspActionDB
{
    /**
     * get the statusSensor list to the Sensor by Internet
     * 
     * @param deviceId the device id
     * @param deviceKey the device key
     * @param startTimestamp the start of UTC timestamp
     * @param endTimestamp the end of UTC timestamp
     * @param interval the interval of each picture
     * @param deviceType the type of device
     * @return the list of EspStatusSensor
     */
    List<IEspStatusSensor> doActionSensorGetStatusListInternetDB(final long deviceId, final String deviceKey,
        final long startTimestamp, final long endTimestamp, final long interval, final EspDeviceType deviceType);
    
    /**
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this task should be interrupted; otherwise,
     *            in-progress tasks are allowed to complete
     * 
     * @return <tt>false</tt> if the task could not be cancelled, typically because it has already completed normally;
     *         <tt>true</tt> otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);
    
    /**
     * parse status from GenericDataDB to IEspStatusSensor
     * @param dataInDB data in the db
     * @return the IEspStatusSensor
     */
    IEspStatusSensor parseStatus(IGenericDataDB dataInDB);
    
    /**
     * parse status from IEspStatusSensor to GenericDataDB
     * @param deviceId the device id
     * @param statusSensor the status of sensor
     * @return the GenericDataDB
     */
    IGenericDataDB parseStatus(long deviceId, IEspStatusSensor statusSensor);
    
    /**
     * do the concrete command for the specific sensor to get status list from the Internet
     * @param deviceKey the device key
     * @param startTimestampFromServer the start of UTC timestamp
     * @param endTimestampFromServer the end of UTC timestamp
     * @return the list of status sensor
     */
    List<IEspStatusSensor> doCommandSensorGetStatusListInternet(String deviceKey, long startTimestampFromServer,
        long endTimestampFromServer);
    
}
