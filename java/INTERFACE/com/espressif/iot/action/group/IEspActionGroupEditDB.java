package com.espressif.iot.action.group;

import com.espressif.iot.group.IEspGroup;

public interface IEspActionGroupEditDB extends IEspActionGroupDB
{
    /**
     * Create group
     * 
     * @param groupName
     * @param userKey
     */
    void doActionGroupCreate(String groupName, String userKey);
    
    /**
     * Create group
     * 
     * @param groupName
     * @param groupTypeOrdinal
     * @param userKey
     */
    void doActionGroupCreate(String groupName, int groupTypeOrdinal, String userKey);
    
    /**
     * Rename group
     * 
     * @param group
     * @param newName
     */
    void doActionGroupRename(IEspGroup group, String newName);
    
    /**
     * Delete group
     * 
     * @param group
     */
    void doActionGroupDelete(IEspGroup group);
}
