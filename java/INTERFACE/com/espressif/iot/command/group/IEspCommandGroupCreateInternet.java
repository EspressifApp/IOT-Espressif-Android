package com.espressif.iot.command.group;

public interface IEspCommandGroupCreateInternet extends IEspCommandGroup
{
    public static final String URL_CREATE = URL_COMMON;
    
    /**
     * Create a new group on server
     * 
     * @param userKey
     * @param groupName
     * @return id of new group. -1 is failed
     */
    long doCommandCreateGroupInternet(String userKey, String groupName);

    /**
     * Create a new group on server
     * 
     * @param userKey
     * @param groupName
     * @param groupType
     * @return id of new group. -1 is failed
     */
    long doCommandCreateGroupInternet(String userKey, String groupName, int groupType);
}
