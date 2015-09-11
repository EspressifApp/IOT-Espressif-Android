package com.espressif.iot.action.group;

public interface IEspActionGroupRemoveDeviceInternet extends IEspActionGroupInternet
{
    /**
     * Remove device from group
     * 
     * @param userKey
     * @param deviceId
     * @param groupId
     * @return
     */
    boolean doActionRemoveDevicefromGroupInternet(String userKey, long deviceId, long groupId);
}
