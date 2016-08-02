package com.espressif.iot.action.group;

import com.espressif.iot.db.EspGroupDBManager;
import com.espressif.iot.group.IEspGroup;

public class EspActionGroupEditDB implements IEspActionGroupEditDB
{
    private EspGroupDBManager mDBManager;
    
    public EspActionGroupEditDB()
    {
        mDBManager = EspGroupDBManager.getInstance();
    }
    
    @Override
    public void doActionGroupCreate(String groupName, String userKey)
    {
        doActionGroupCreate(groupName, IEspGroup.Type.COMMON.ordinal(), userKey);
    }

    @Override
    public void doActionGroupCreate(String groupName, int groupTypeOrdinal, String userKey) {
        if (userKey == null) {
            userKey = "";
        }
        mDBManager.insertOrReplace(IEspGroup.ID_NEW, groupName, userKey, 0, groupTypeOrdinal);
    }

    @Override
    public void doActionGroupRename(IEspGroup group, String newName)
    {
        group.addState(IEspGroup.State.RENAMED);
        group.setName(newName);
        mDBManager.updateName(group.getId(), newName);
        mDBManager.updateState(group.getId(), group.getStateValue());
    }
    
    @Override
    public void doActionGroupDelete(IEspGroup group)
    {
        long groupId = group.getId();
        group.addState(IEspGroup.State.DELETED);
        if (group.getId() < 0)
        {
            mDBManager.delete(groupId);
        }
        else
        {
            mDBManager.updateState(groupId, group.getStateValue());
        }
    }
}
