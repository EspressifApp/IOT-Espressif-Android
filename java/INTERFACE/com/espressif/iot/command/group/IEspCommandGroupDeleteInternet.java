package com.espressif.iot.command.group;

public interface IEspCommandGroupDeleteInternet extends IEspCommandGroup
{
    public static final String URL_DELETE = URL_COMMON + "?method=DELETE";
    
    /**
     * Modify group info
     * 
     * @param userKey
     * @param groupId
     * @return success or failed
     */
    boolean doCommandDeleteGroupInternet(String userKey, long groupId);
}
