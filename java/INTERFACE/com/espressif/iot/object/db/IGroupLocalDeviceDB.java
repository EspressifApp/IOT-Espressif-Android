package com.espressif.iot.object.db;

public interface IGroupLocalDeviceDB {
    public long getGroupId();

    public void setGroupId(long groupId);

    public String getBssid();

    public void setBssid(String bssid);
}
