package com.espressif.iot.action.group;

public interface IEspActionGroupMoveDeviceInternet extends IEspActionGroupInternet
{
    /**
     * Move device into group
     * 
     * @param userKey
     * @param deviceId
     * @param groupId
     * @param reservePreGroup
     * @return success or failed
     */
    boolean doActionMoveDeviceIntoGroupInternet(String userKey, long deviceId, long groupId, boolean reservePreGroup);
}
