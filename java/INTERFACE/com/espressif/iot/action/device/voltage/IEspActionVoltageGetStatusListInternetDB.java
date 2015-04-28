package com.espressif.iot.action.device.voltage;

import java.util.List;

import com.espressif.iot.action.IEspActionDB;
import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.device.IEspActionVoltage;
import com.espressif.iot.type.device.status.IEspStatusVoltage;

public interface IEspActionVoltageGetStatusListInternetDB extends IEspActionVoltage, IEspActionInternet,
    IEspActionDB
{
    /**
     * get the statusVoltage list to the Voltage by Internet
     * 
     * @param deviceId the device id
     * @param deviceKey the device key
     * @param startTimestamp the start of UTC timestamp
     * @param endTimestamp the end of UTC timestamp
     * @param interval the interval of each picture
     * @return the list of EspStatusVoltage
     */
    List<IEspStatusVoltage> doActionVoltageGetStatusListInternetDB(final long deviceId, final String deviceKey,
        final long startTimestamp, final long endTimestamp, final long interval);
    
    /**
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this task should be interrupted; otherwise,
     *            in-progress tasks are allowed to complete
     * 
     * @return <tt>false</tt> if the task could not be cancelled, typically because it has already completed normally;
     *         <tt>true</tt> otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);
}
