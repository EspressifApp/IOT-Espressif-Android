package com.espressif.iot.object.db;

import java.util.List;

import com.espressif.iot.db.greenrobot.daos.GroupCloudDeviceDB;
import com.espressif.iot.db.greenrobot.daos.GroupLocalDeviceDB;
import com.espressif.iot.db.greenrobot.daos.GroupRemoveDeviceDB;

public interface IGroupDB {
    public long getId();

    public void setId(long id);

    /** Not-null value. */
    public String getName();

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name);

    public String getUserKey();

    public void setUserKey(String userKey);

    public int getState();

    public void setState(int state);

    public int getType();

    public void setType(int type);

    public List<GroupLocalDeviceDB> getLocalDevices();

    public List<GroupCloudDeviceDB> getCloudDevices();

    public List<GroupRemoveDeviceDB> getRemoveDevices();
}
