package com.espressif.iot.object.db;

import java.util.List;

public interface IEspGroupDBManager
{
    /**
     * get local group list by user key(group list haven't been synchronized to server)
     * 
     * @param userKey user key
     * @return local group list(group list haven't been synchronized to server)
     */
    List<IGroupDB> getUserDBLocalGroup(String userKey);
    
    /**
     * get cloud group list by user key(group list have been synchronized to server)
     * 
     * @param userKey user key
     * @return cloud group list(group list have been synchronized to server)
     */
    List<IGroupDB> getUserDBCloudGroup(String userKey);
    
    /**
     * get local and cloud group list together(group list no matter whether have been synchronized to server or not)
     * 
     * @param userKey user key
     * @return local and cloud group list together(group list no matter whether have been synchronized to server or not)
     */
    List<IGroupDB> getUserGroup(String userKey);
    
    /**
     * get device bssid list by bssids String seperated by comma
     * 
     * @param bssidsStr devices String seperated by comma
     * @return device bssid list
     */
    List<String> getDeviceBssids(String bssidsStr);
    
    /**
     * get device bssids String seperated by comma by bssid list
     * 
     * @param bssids bssid list
     * @return device bssids String seperated by comma
     */
    String getDeviceBssidsText(List<String> bssids);
    
    /**
     * insert or replace group db
     * 
     * @param groupId group id
     * @param groupName group name
     * @param userKey user key
     * @param state the state of the group(rename, delete and etc.)
     * @return the groupId updated in DB
     */
    long insertOrReplace(long groupId, String groupName, String userKey, int state);
    
    /**
     * insert or replace
     * 
     * @param id group id
     * @param name group name
     * @param userKey user key
     * @param state the state of the group(rename, delete and etc.)
     * @param localDeviceBssids local device bssids seperated by comma
     * @param cloudDeviceBssids cloud device bssids seperated by comma
     * @param removeDeviceBssids remove device bssids seperated by comma
     */
    void insertOrReplace(long id, String name, String userKey, int state, String localDeviceBssids,
        String cloudDeviceBssids, String removeDeviceBssids);
    
    /**
     * delete group by group id
     * 
     * @param groupId the group id which group to be deleted
     */
    void delete(long groupId);
    
    /**
     * get group by group id
     * 
     * @param groupId group id
     * @return group
     */
    IGroupDB getGroupDB(long groupId);
    
    /**
     * update local bssids by group id
     * 
     * @param groupId group id
     * @param bssids local bssids
     */
    void updateLocalBssids(long groupId, List<String> bssids);
    
    /**
     * update cloud bssids by group id
     * 
     * @param groupId group id
     * @param bssids cloud bssids
     */
    void updateCloudBssids(long groupId, List<String> bssids);
    
    /**
     * update remove bssids by group id
     * 
     * @param groupId group id
     * @param bssids remove bssids
     */
    void updateRemoveBssids(long groupId, List<String> bssids);
    
    /**
     * add local bssid to a group
     * 
     * @param groupId group id
     * @param bssid local bssid
     */
    void addLocalBssid(long groupId, String bssid);
    
    /**
     * add cloud bssid to a group
     * 
     * @param groupId group id
     * @param bssid cloud bssid
     */
    void addCloudBssid(long groupId, String bssid);
    
    /**
     * add remove bssid to a group
     * 
     * @param gtoupId group id
     * @param bssid remove bssid
     */
    void addRemoveBssid(long gtoupId, String bssid);
    
    /**
     * delete remove bssid if exist from a group
     * 
     * @param groupId group id
     * @param bssid remove bssid to be deleted
     */
    void deleteRemoveBssidIfExist(long groupId, String bssid);
    
    /**
     * delete local bssid if exist from a group
     * 
     * @param groupId group id
     * @param bssid local bssid to be deleted
     */
    void deleteLocalBssidIfExist(long groupId, String bssid);
    
    /**
     * delete cloud bssid if exist from a group
     * 
     * @param groupId group id
     * @param bssid cloud bssid to be deleted
     */
    void deleteCloudBssidIfExist(long groupId, String bssid);
    
    /**
     * update local group user key(it is used in the situation: firstly, user don't login and generate some local group.
     * later, the user want to synchronize the group into his account in the server)
     * 
     * @param userKey the user key
     */
    void updateLocalGroupUserKey(String userKey);
    
    /**
     * update group state
     * 
     * @param groupId group id
     * @param state group state
     */
    void updateState(long groupId, int state);
    
    /**
     * update group name
     * 
     * @param groupId group id
     * @param name group name
     */
    void updateName(long groupId, String name);
}
