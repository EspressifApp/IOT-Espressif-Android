package com.espressif.iot.action.group;

public interface IEspActionGroupRenameInternet extends IEspActionGroupInternet
{
    /**
     * Modify a name of the group
     * 
     * @param userKey
     * @param groupId
     * @param newName
     * @return success or failed
     */
    boolean doActionRenameGroupInternet(String userKey, long groupId, String newName);
}
