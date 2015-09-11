package com.espressif.iot.command.group;

public interface IEspCommandGroupRemoveDeviceInternet extends IEspCommandGroup
{
    public static final String URL_REMOVE_DEVICE = URL_COMMON + "?action=remove_from_group&method=PUT";
    
    /**
     * Remove device from group
     * 
     * @param userKey
     * @param deviceId
     * @param groupId
     * @return
     */
    boolean doCommandRemoveDevicefromGroupInternet(String userKey, long deviceId, long groupId);
}
