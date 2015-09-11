package com.espressif.iot.action.group;

import com.espressif.iot.db.EspGroupDBManager;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.group.IEspGroup;

public class EspActionGroupDeviceDB implements IEspActionGroupDeviceDB
{
    private EspGroupDBManager mDBManager;
    
    public EspActionGroupDeviceDB()
    {
        mDBManager = EspGroupDBManager.getInstance();
    }
    
    @Override
    public void doActionMoveDeviceIntoGroupDB(String userKey, IEspDevice device, IEspGroup group)
    {
        mDBManager.addLocalBssid(group.getId(), device.getBssid());
        mDBManager.deleteRemoveBssidIfExist(group.getId(), device.getBssid());
    }
    
    @Override
    public void doActionRemoveDevicefromGroupDB(String userKey, IEspDevice device, IEspGroup group)
    {
        mDBManager.addRemoveBssid(group.getId(), device.getBssid());
        mDBManager.deleteLocalBssidIfExist(group.getId(), device.getBssid());
        mDBManager.deleteCloudBssidIfExist(group.getId(), device.getBssid());
    }
}
