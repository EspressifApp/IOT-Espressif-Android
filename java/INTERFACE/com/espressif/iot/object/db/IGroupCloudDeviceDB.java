package com.espressif.iot.object.db;

public interface IGroupCloudDeviceDB {
    public long getGroupId();

    public void setGroupId(long groupId);

    public String getBssid();

    public void setBssid(String bssid);
}
