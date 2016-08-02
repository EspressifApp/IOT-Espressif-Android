package com.espressif.iot.object.db;

import java.util.List;

public interface IEspGroupDBManager {
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
     * insert or replace group db
     * 
     * @param groupId group id
     * @param groupName group name
     * @param userKey user key
     * @param state the state of the group(rename, delete and etc.)
     * @param type the type of the group
     * @return the groupId updated in DB
     */
    long insertOrReplace(long groupId, String groupName, String userKey, int state, int type);

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
     * add local bssid to a group
     * 
     * @param groupId group id
     * @param bssid local bssid
     */
    void addLocalBssid(long groupId, String bssid);

    /**
     * Add local bssids to a group
     * 
     * @param groupId
     * @param bssids
     */
    void addLocalBssids(long groupId, List<String> bssids);

    /**
     * delete local bssid if exist from a group
     * 
     * @param groupId group id
     * @param bssid local bssid to be deleted
     */
    void deleteLocalBssidIfExist(long groupId, String bssid);

    /**
     * Get local bssids of the group
     * 
     * @param groupId
     * @return
     */
    List<String> getLocalBssids(long groupId);

    /**
     * add cloud bssid to a group
     * 
     * @param groupId group id
     * @param bssid cloud bssid
     */
    void addCloudBssid(long groupId, String bssid);

    /**
     * Add cloud bssids to a group
     * 
     * @param groupId
     * @param bssids
     */
    void addCloudBssids(long groupId, List<String> bssids);

    /**
     * delete cloud bssid if exist from a group
     * 
     * @param groupId group id
     * @param bssid cloud bssid to be deleted
     */
    void deleteCloudBssidIfExist(long groupId, String bssid);

    /**
     * Get cloud bssids of the group
     * 
     * @param groupId
     * @return
     */
    List<String> getCloudBssids(long groupId);

    /**
     * add remove bssid to a group
     * 
     * @param groupId group id
     * @param bssid remove bssid
     */
    void addRemoveBssid(long groupId, String bssid);

    /**
     * Add remove bssids to a group
     * 
     * @param groupId
     * @param bssids
     */
    void addRemoveBssids(long groupId, List<String> bssids);

    /**
     * delete remove bssid if exist from a group
     * 
     * @param groupId group id
     * @param bssid remove bssid to be deleted
     */
    void deleteRemoveBssidIfExist(long groupId, String bssid);

    /**
     * Get remove bssids of the group
     * 
     * @param groupId
     * @return
     */
    List<String> getRemoveBssids(long groupId);

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
