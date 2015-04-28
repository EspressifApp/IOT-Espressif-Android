package com.espressif.iot.action.device.New;

import com.espressif.iot.action.IEspActionDB;
import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.device.IEspActionNew;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionDeviceNewActivateInternet extends IEspActionNew, IEspActionInternet, IEspActionDB
{
    /**
     * activate device in the Internet, if suc, delete the negative device id device in local db
     * 
     * @param userId the user's id
     * @param userKey the user's key
     * @param randomToken the random token
     * @param negativeDeviceId the negative device id(negative device id is used by activating device)
     * @return IEspDevice the device
     * @throws InterruptedException when the action is interrupted
     */
    IEspDevice doActionDeviceNewActivateInternet(long userId, String userKey, String randomToken, long negativeDeviceId) throws InterruptedException;
}
