package com.espressif.iot.command.group;

import com.espressif.iot.command.IEspCommandInternet;

public interface IEspCommandGroup extends IEspCommandInternet {
    public static final String URL_COMMON = "https://iot.espressif.cn/v1/user/devices/groups/";

    public static final String KEY_DEVICE_GROUPS = "deviceGroups";
    public static final String KEY_GROUP_NAME = "name";
    public static final String KEY_GROUP_ID = "group_id";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final String KEY_RESERVE_PRE_GROUP = "reserve_pre_group";
    public static final String KEY_GROUP_DESC = "desc";
    public static final String KEY_GROUP_TYPE = "type";
}
