package com.espressif.iot.db;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.db.greenrobot.daos.GroupDB;
import com.espressif.iot.db.greenrobot.daos.GroupDBDao;
import com.espressif.iot.db.greenrobot.daos.GroupDBDao.Properties;
import com.espressif.iot.group.IEspGroup;

import de.greenrobot.dao.query.Query;

public class EspGroupDBManager
{
    private static EspGroupDBManager instance = null;
    
    public static void init(DaoSession daoSession)
    {
        instance = new EspGroupDBManager(daoSession);
    }
    
    public static EspGroupDBManager getInstance()
    {
        return instance;
    }
    
    private static final String BSSID_SEPARATOR = ",";
    
    private GroupDBDao mGroupDao;
    
    private EspGroupDBManager(DaoSession daoSession)
    {
        mGroupDao = daoSession.getGroupDBDao();
    }
    
    public List<GroupDB> getUserDBLocalGroup(String userKey)
    {
        if (userKey == null)
        {
            userKey = "";
        }
        Query<GroupDB> query =
            mGroupDao.queryBuilder()
                .where(Properties.UserKey.eq(userKey), Properties.Id.lt(0))
                .orderDesc(Properties.Id)
                .build();
        return query.list();
    }
    
    public List<GroupDB> getUserDBCloudGroup(String userKey)
    {
        if (userKey == null)
        {
            userKey = "";
        }
        Query<GroupDB> query =
            mGroupDao.queryBuilder()
                .where(Properties.UserKey.eq(userKey), Properties.Id.gt(0))
                .orderAsc(Properties.Id)
                .build();
        return query.list();
    }
    
    public List<GroupDB> getUserGroup(String userKey)
    {
        if (userKey == null)
        {
            userKey = "";
        }
        Query<GroupDB> query = mGroupDao.queryBuilder().where(Properties.UserKey.eq(userKey)).build();
        return query.list();
    }
    
    public List<String> getDeviceBssids(String bssidsStr)
    {
        List<String> list = new ArrayList<String>();
        if (!TextUtils.isEmpty(bssidsStr))
        {
            String bssids[] = bssidsStr.split(BSSID_SEPARATOR);
            for (String bssid : bssids)
            {
                list.add(bssid);
            }
        }
        
        return list;
    }
    
    public String getDeviceBssidsText(List<String> bssids)
    {
        StringBuilder bssidsStr = new StringBuilder();
        for (String bssid : bssids)
        {
            bssidsStr.append(bssid).append(BSSID_SEPARATOR);
        }
        
        return bssidsStr.toString();
    }
    
    /**
     * 
     * @param groupId
     * @param groupName
     * @param userKey
     * @param state
     * @return the groupId updated in DB
     */
    public synchronized long insertOrReplace(long groupId, String groupName, String userKey, int state)
    {
        if (groupId == IEspGroup.ID_NEW)
        {
            Query<GroupDB> query = mGroupDao.queryBuilder().build();
            List<GroupDB> groupList = query.list();
            for (GroupDB groupDB : groupList)
            {
                groupId = Math.min(groupId, groupDB.getId());
            }
            groupId--;
        }
        GroupDB groupDB = new GroupDB(groupId);
        groupDB.setName(groupName);
        groupDB.setUserKey(userKey);
        groupDB.setState(state);
        
        mGroupDao.insertOrReplace(groupDB);
        
        return groupId;
    }
    
    public synchronized void insertOrReplace(GroupDB groupDB)
    {
        mGroupDao.insertOrReplace(groupDB);
    }
    
    public synchronized void delete(long groupId)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        if (groupDB != null)
        {
            mGroupDao.delete(groupDB);
        }
    }
    
    public GroupDB getGroupDB(long groupId)
    {
        return mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
    }
    
    /**
     * The id of the group need modify
     * 
     * @param groupId
     * @param bssids
     */
    public synchronized void updateLocalBssids(long groupId, List<String> bssids)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        groupDB.setLocalDeviceBssids(getDeviceBssidsText(bssids));
        groupDB.update();
    }
    
    public synchronized void addLocalBssid(long groupId, String bssid)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        StringBuilder newBssids = new StringBuilder();
        String bssids = groupDB.getLocalDeviceBssids();
        if (!TextUtils.isEmpty(bssids))
        {
            newBssids.append(bssids);
        }
        if (!newBssids.toString().contains(bssid))
        {
            newBssids.append(bssid).append(BSSID_SEPARATOR);
            groupDB.setLocalDeviceBssids(newBssids.toString());
            groupDB.update();
        }
    }
    
    public synchronized void addCloudBssid(long groupId, String bssid)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        StringBuilder newBssids = new StringBuilder();
        String bssids = groupDB.getCloudDeviceBssids();
        if (!TextUtils.isEmpty(bssids))
        {
            newBssids.append(bssids);
        }
        if (!newBssids.toString().contains(bssid))
        {
            newBssids.append(bssid).append(BSSID_SEPARATOR);
            groupDB.setCloudDeviceBssids(newBssids.toString());
            groupDB.update();
        }
    }
    
    public synchronized void addRemoveBssid(long gtoupId, String bssid)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(gtoupId)).unique();
        StringBuilder newBssids = new StringBuilder();
        String bssids = groupDB.getRemoveDeviceBssids();
        if (!TextUtils.isEmpty(bssids))
        {
            newBssids.append(bssids);
        }
        if (!newBssids.toString().contains(bssid))
        {
            newBssids.append(bssid).append(BSSID_SEPARATOR);
            groupDB.setRemoveDeviceBssids(newBssids.toString());
            groupDB.update();
        }
    }
    
    public synchronized void deleteRemoveBssidIfExist(long groupId, String bssid)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        String removeBssids = groupDB.getRemoveDeviceBssids();
        if (removeBssids != null && removeBssids.contains(bssid))
        {
            String newBssids = removeBssids.replace(bssid + BSSID_SEPARATOR, "");
            groupDB.setRemoveDeviceBssids(newBssids);
            groupDB.update();
        }
    }
    
    public synchronized void deleteLocalBssidIfExist(long groupId, String bssid)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        String localeBssids = groupDB.getLocalDeviceBssids();
        if (localeBssids != null && localeBssids.contains(bssid))
        {
            String newBssids = localeBssids.replace(bssid + BSSID_SEPARATOR, "");
            groupDB.setLocalDeviceBssids(newBssids);
            groupDB.update();
        }
    }
    
    public synchronized void deleteCloudBssidIfExist(long groupId, String bssid)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        String cloudBssids = groupDB.getCloudDeviceBssids();
        if (cloudBssids != null && cloudBssids.contains(bssid))
        {
            String newBssids = cloudBssids.replace(bssid + BSSID_SEPARATOR, "");
            groupDB.setCloudDeviceBssids(newBssids);
            groupDB.update();
        }
    }
    
    public synchronized void updateCloudBssids(long groupId, List<String> bssids)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        groupDB.setCloudDeviceBssids(getDeviceBssidsText(bssids));
        groupDB.update();
    }
    
    public synchronized void updateRemoveBssids(long groupId, List<String> bssids)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        groupDB.setRemoveDeviceBssids(getDeviceBssidsText(bssids));
        groupDB.update();
    }
    
    public synchronized void updateUserKey(long groupId, String userKey)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        groupDB.setUserKey(userKey);
        groupDB.update();
    }
    
    public synchronized void updateLocalGroupUserKey(String userKey)
    {
        if (TextUtils.isEmpty(userKey))
        {
            return;
        }
        
        List<GroupDB> userGroupDBs = mGroupDao.queryBuilder().where(Properties.UserKey.eq(userKey)).build().list();
        List<String> userGroupNames = new ArrayList<String>();
        for (GroupDB groupDB : userGroupDBs)
        {
            userGroupNames.add(groupDB.getName());
        }
        List<GroupDB> localGroupDBs = mGroupDao.queryBuilder().where(Properties.UserKey.eq("")).build().list();
        for (int i = 0; i < localGroupDBs.size(); i++)
        {
            GroupDB localGroupDB = localGroupDBs.get(i);
            String newName = getNotDuplicateGroupName(localGroupDB.getName(), userGroupNames, 0);
            localGroupDB.setName(newName);
            localGroupDB.setUserKey(userKey);
            localGroupDB.update();
        }
    }
    
    private String getNotDuplicateGroupName(String groupName, List<String> names, int temp)
    {
        String result = groupName;
        if (temp > 0)
        {
            result = groupName + "-" + temp;
        }
        if (!names.contains(result))
        {
            return result;
        }
        else
        {
            return getNotDuplicateGroupName(groupName, names, ++temp);
        }
    }
    
    public synchronized void updateState(long groupId, int state)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        groupDB.setState(state);
        groupDB.update();
    }
    
    public synchronized void updateName(long groupId, String name)
    {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        groupDB.setName(name);
        groupDB.update();
    }
}
