package com.espressif.iot.action.group;

public interface IEspActionGroupCreateInternet extends IEspActionGroupInternet
{
    /**
     * Create a new group on server
     * 
     * @param userKey
     * @param groupName
     * @return id of new group. -1 is failed
     */
    long doActionCreateGroupInternet(String userKey, String groupName);
}
