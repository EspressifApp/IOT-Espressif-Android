package com.espressif.iot.command.group;

public interface IEspCommandGroupRenameInternet extends IEspCommandGroup
{
    public static final String URL_MODIFY = URL_COMMON + "?method=PUT";
    
    /**
     * Modify a name of the group
     * 
     * @param userKey
     * @param groupId
     * @param newName
     * @return success or failed
     */
    boolean doCommandRenameGroupInternet(String userKey, long groupId, String newName);
}
