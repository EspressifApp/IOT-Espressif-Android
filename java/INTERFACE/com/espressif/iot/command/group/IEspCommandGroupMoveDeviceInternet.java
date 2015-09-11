package com.espressif.iot.command.group;

public interface IEspCommandGroupMoveDeviceInternet extends IEspCommandGroup
{
    public static final String URL_MOVE_DEVICE = URL_COMMON + "?action=move_to_group&method=PUT";
    
    /**
     * Move device into group
     * 
     * @param userKey
     * @param deviceId
     * @param groupId
     * @param reservePreGroup
     * @return success or failed
     */
    boolean doCommandMoveDeviceIntoGroupInternet(String userKey, long deviceId, long groupId, boolean reservePreGroup);
}
