package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.action.device.IEspActionActivated;
import com.espressif.iot.command.IEspCommandUser;

public interface IEspActionDeviceSynchronizeInterentDiscoverLocal extends IEspCommandUser, IEspActionActivated,
    IEspActionLocal, IEspActionInternet
{
    /**
     * The min times that send UDP Broadcast when onRefreshing
     */
    static final int UDP_EXECUTE_MIN_TIMES = 1;
    
    /**
     * The max times that send UDP Broadcast when onRefreshing
     */
    static final int UDP_EXECUTE_MAX_TIMES = 5;
    
    /**
     * Synchronize devices from Server and Discovery devices on local
     * 
     * @param userKey the user key
     */
    void doActionDeviceSynchronizeInterentDiscoverLocal(final String userKey);
    
    /**
     * Synchronize devices from Discovery devices on local only
     * @param isSyn whether execute it syn or asyn
     */
    void doActionDeviceSynchronizeDiscoverLocal(boolean isSyn);
    
    /**
     * Synchronize devices from Server only
     * @param userKey the user key
     */
    void doActionDeviceSynchronizeInternet(final String userKey);
}
