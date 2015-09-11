package com.espressif.iot.action.group;

public interface IEspActionGroupDeleteInternet extends IEspActionGroupInternet
{
    /**
     * Modify group info
     * 
     * @param userKey
     * @param groupId
     * @return success or failed
     */
    boolean doActionDeleteGroupInternet(String userKey, long groupId);
}
