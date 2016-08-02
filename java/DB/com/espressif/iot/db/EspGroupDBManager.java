package com.espressif.iot.db;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.db.greenrobot.daos.GroupCloudDeviceDB;
import com.espressif.iot.db.greenrobot.daos.GroupCloudDeviceDBDao;
import com.espressif.iot.db.greenrobot.daos.GroupDB;
import com.espressif.iot.db.greenrobot.daos.GroupDBDao;
import com.espressif.iot.db.greenrobot.daos.GroupLocalDeviceDBDao;
import com.espressif.iot.db.greenrobot.daos.GroupRemoveDeviceDB;
import com.espressif.iot.db.greenrobot.daos.GroupRemoveDeviceDBDao;
import com.espressif.iot.db.greenrobot.daos.GroupDBDao.Properties;
import com.espressif.iot.db.greenrobot.daos.GroupLocalDeviceDB;
import com.espressif.iot.group.IEspGroup;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.object.db.IEspGroupDBManager;
import com.espressif.iot.object.db.IGroupDB;

import de.greenrobot.dao.query.Query;

public class EspGroupDBManager implements IEspGroupDBManager, IEspSingletonObject {
    private static EspGroupDBManager instance = null;

    public static void init(DaoSession daoSession) {
        instance = new EspGroupDBManager(daoSession);
    }

    public static EspGroupDBManager getInstance() {
        return instance;
    }

    private GroupDBDao mGroupDao;
    private GroupLocalDeviceDBDao mLocalDeviceDBDao;
    private GroupCloudDeviceDBDao mCloudDeviceDBDao;
    private GroupRemoveDeviceDBDao mRemoveDeviceDBDao;

    private EspGroupDBManager(DaoSession daoSession) {
        mGroupDao = daoSession.getGroupDBDao();
        mLocalDeviceDBDao = daoSession.getGroupLocalDeviceDBDao();
        mCloudDeviceDBDao = daoSession.getGroupCloudDeviceDBDao();
        mRemoveDeviceDBDao = daoSession.getGroupRemoveDeviceDBDao();
    }

    @Override
    public List<IGroupDB> getUserDBLocalGroup(String userKey) {
        if (userKey == null) {
            userKey = "";
        }
        Query<GroupDB> query = mGroupDao.queryBuilder()
            .where(Properties.UserKey.eq(userKey), Properties.Id.lt(0))
            .orderDesc(Properties.Id)
            .build();

        List<GroupDB> groupDBList = query.list();
        List<IGroupDB> result = new ArrayList<IGroupDB>();
        result.addAll(groupDBList);
        return result;
    }

    @Override
    public List<IGroupDB> getUserDBCloudGroup(String userKey) {
        if (userKey == null) {
            userKey = "";
        }
        Query<GroupDB> query = mGroupDao.queryBuilder()
            .where(Properties.UserKey.eq(userKey), Properties.Id.gt(0))
            .orderAsc(Properties.Id)
            .build();
        List<GroupDB> groupDBList = query.list();

        List<IGroupDB> result = new ArrayList<IGroupDB>();
        result.addAll(groupDBList);
        return result;
    }

    @Override
    public List<IGroupDB> getUserGroup(String userKey) {
        if (userKey == null) {
            userKey = "";
        }
        Query<GroupDB> query = mGroupDao.queryBuilder().where(Properties.UserKey.eq(userKey)).build();
        List<IGroupDB> result = new ArrayList<IGroupDB>();
        result.addAll(query.list());
        return result;
    }

    @Override
    public synchronized long insertOrReplace(long groupId, String groupName, String userKey, int state, int type) {
        if (groupId == IEspGroup.ID_NEW) {
            Query<GroupDB> query = mGroupDao.queryBuilder().build();
            List<GroupDB> groupList = query.list();
            for (GroupDB groupDB : groupList) {
                groupId = Math.min(groupId, groupDB.getId());
            }
            groupId--;
        }
        GroupDB groupDB = new GroupDB(groupId);
        groupDB.setName(groupName);
        groupDB.setUserKey(userKey);
        groupDB.setState(state);
        groupDB.setType(type);

        mGroupDao.insertOrReplace(groupDB);

        return groupId;
    }

    @Override
    public synchronized void delete(long groupId) {
        List<GroupLocalDeviceDB> localDevices = mLocalDeviceDBDao._queryGroupDB_LocalDevices(groupId);
        for (GroupLocalDeviceDB db : localDevices) {
            mLocalDeviceDBDao.delete(db);
        }
        List<GroupCloudDeviceDB> cloudDevices = mCloudDeviceDBDao._queryGroupDB_CloudDevices(groupId);
        for (GroupCloudDeviceDB db : cloudDevices) {
            mCloudDeviceDBDao.delete(db);
        }
        List<GroupRemoveDeviceDB> removeDevices = mRemoveDeviceDBDao._queryGroupDB_RemoveDevices(groupId);
        for (GroupRemoveDeviceDB db : removeDevices) {
            mRemoveDeviceDBDao.delete(db);
        }
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        if (groupDB != null) {
            mGroupDao.delete(groupDB);
        }
    }

    @Override
    public IGroupDB getGroupDB(long groupId) {
        return mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
    }

    @Override
    public synchronized void addLocalBssid(long groupId, String bssid) {
        List<GroupLocalDeviceDB> list = mLocalDeviceDBDao.queryBuilder()
            .where(GroupLocalDeviceDBDao.Properties.Bssid.eq(bssid),
                GroupLocalDeviceDBDao.Properties.GroupId.eq(groupId))
            .list();
        if (list.isEmpty()) {
            GroupLocalDeviceDB db = new GroupLocalDeviceDB(null, groupId, bssid);
            mLocalDeviceDBDao.insertOrReplace(db);
        }
    }

    @Override
    public void addLocalBssids(long groupId, List<String> bssids) {
        for (String bssid : bssids) {
            addLocalBssid(groupId, bssid);
        }
    }

    @Override
    public synchronized void deleteLocalBssidIfExist(long groupId, String bssid) {
        List<GroupLocalDeviceDB> list = mLocalDeviceDBDao.queryBuilder()
            .where(GroupLocalDeviceDBDao.Properties.Bssid.eq(bssid),
                GroupLocalDeviceDBDao.Properties.GroupId.eq(groupId))
            .list();
        for (GroupLocalDeviceDB db : list) {
            db.delete();
        }
    }

    @Override
    public List<String> getLocalBssids(long groupId) {
        List<GroupLocalDeviceDB> list = mLocalDeviceDBDao._queryGroupDB_LocalDevices(groupId);
        List<String> result = new ArrayList<String>();
        for (GroupLocalDeviceDB db : list) {
            result.add(db.getBssid());
        }
        return result;
    }

    @Override
    public synchronized void addCloudBssid(long groupId, String bssid) {
        List<GroupCloudDeviceDB> list = mCloudDeviceDBDao.queryBuilder()
            .where(GroupCloudDeviceDBDao.Properties.Bssid.eq(bssid),
                GroupCloudDeviceDBDao.Properties.GroupId.eq(groupId))
            .list();
        if (list.isEmpty()) {
            GroupCloudDeviceDB db = new GroupCloudDeviceDB(null, groupId, bssid);
            mCloudDeviceDBDao.insertOrReplace(db);
        }
    }

    @Override
    public void addCloudBssids(long groupId, List<String> bssids) {
        for (String bssid : bssids) {
            addCloudBssid(groupId, bssid);
        }
    }

    @Override
    public synchronized void deleteCloudBssidIfExist(long groupId, String bssid) {
        List<GroupCloudDeviceDB> list = mCloudDeviceDBDao.queryBuilder()
            .where(GroupCloudDeviceDBDao.Properties.Bssid.eq(bssid),
                GroupCloudDeviceDBDao.Properties.GroupId.eq(groupId))
            .list();
        for (GroupCloudDeviceDB db : list) {
            db.delete();
        }
    }

    @Override
    public List<String> getCloudBssids(long groupId) {
        List<GroupCloudDeviceDB> list = mCloudDeviceDBDao._queryGroupDB_CloudDevices(groupId);
        List<String> result = new ArrayList<String>();
        for (GroupCloudDeviceDB db : list) {
            result.add(db.getBssid());
        }
        return result;
    }

    @Override
    public synchronized void addRemoveBssid(long groupId, String bssid) {
        List<GroupRemoveDeviceDB> list = mRemoveDeviceDBDao.queryBuilder()
            .where(GroupRemoveDeviceDBDao.Properties.Bssid.eq(bssid),
                GroupRemoveDeviceDBDao.Properties.GroupId.eq(groupId))
            .list();
        if (list.isEmpty()) {
            GroupRemoveDeviceDB db = new GroupRemoveDeviceDB(null, groupId, bssid);
            mRemoveDeviceDBDao.insertOrReplace(db);
        }
    }

    @Override
    public void addRemoveBssids(long groupId, List<String> bssids) {
        for (String bssid : bssids) {
            addRemoveBssid(groupId, bssid);
        }
    }

    @Override
    public synchronized void deleteRemoveBssidIfExist(long groupId, String bssid) {
        List<GroupRemoveDeviceDB> list = mRemoveDeviceDBDao.queryBuilder()
            .where(GroupRemoveDeviceDBDao.Properties.Bssid.eq(bssid),
                GroupRemoveDeviceDBDao.Properties.GroupId.eq(groupId))
            .list();
        for (GroupRemoveDeviceDB db : list) {
            db.delete();
        }
    }

    @Override
    public List<String> getRemoveBssids(long groupId) {
        List<GroupRemoveDeviceDB> list = mRemoveDeviceDBDao._queryGroupDB_RemoveDevices(groupId);
        List<String> result = new ArrayList<String>();
        for (GroupRemoveDeviceDB db : list) {
            result.add(db.getBssid());
        }
        return result;
    }

    @Override
    public synchronized void updateLocalGroupUserKey(String userKey) {
        if (TextUtils.isEmpty(userKey)) {
            return;
        }

        List<GroupDB> userGroupDBs = mGroupDao.queryBuilder().where(Properties.UserKey.eq(userKey)).build().list();
        List<String> userGroupNames = new ArrayList<String>();
        for (GroupDB groupDB : userGroupDBs) {
            userGroupNames.add(groupDB.getName());
        }
        List<GroupDB> localGroupDBs = mGroupDao.queryBuilder().where(Properties.UserKey.eq("")).build().list();
        for (int i = 0; i < localGroupDBs.size(); i++) {
            GroupDB localGroupDB = localGroupDBs.get(i);
            String newName = getNotDuplicateGroupName(localGroupDB.getName(), userGroupNames, 0);
            localGroupDB.setName(newName);
            localGroupDB.setUserKey(userKey);
            localGroupDB.update();
        }
    }

    private String getNotDuplicateGroupName(String groupName, List<String> names, int temp) {
        String result = groupName;
        if (temp > 0) {
            result = groupName + "-" + temp;
        }
        if (!names.contains(result)) {
            return result;
        } else {
            return getNotDuplicateGroupName(groupName, names, ++temp);
        }
    }

    @Override
    public synchronized void updateState(long groupId, int state) {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        groupDB.setState(state);
        groupDB.update();
    }

    @Override
    public synchronized void updateName(long groupId, String name) {
        GroupDB groupDB = mGroupDao.queryBuilder().where(Properties.Id.eq(groupId)).unique();
        groupDB.setName(name);
        groupDB.update();
    }
}
