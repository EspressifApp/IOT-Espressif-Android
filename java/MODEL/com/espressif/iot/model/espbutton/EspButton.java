package com.espressif.iot.model.espbutton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.espressif.iot.espbutton.IEspButton;
import com.espressif.iot.espbutton.IEspButtonGroup;

public class EspButton implements IEspButton
{
    private String mMac;
    private Set<IEspButtonGroup> mGroups;
    
    public EspButton()
    {
        mGroups = new HashSet<IEspButtonGroup>();
    }
    
    @Override
    public void setMac(String mac)
    {
        mMac = mac;
    }

    @Override
    public String getMac()
    {
        return mMac;
    }

    @Override
    public List<IEspButtonGroup> getGroups()
    {
        List<IEspButtonGroup> result = new ArrayList<IEspButtonGroup>();
        result.addAll(mGroups);
        return result;
    }

    @Override
    public void setGroups(List<IEspButtonGroup> groups)
    {
        mGroups.clear();
        mGroups.addAll(groups);
    }

    @Override
    public void addGroups(List<IEspButtonGroup> groups)
    {
        mGroups.addAll(groups);
    }

    @Override
    public void deleteGroups(List<IEspButtonGroup> groups)
    {
        mGroups.removeAll(groups);
    }

    @Override
    public void addGroup(IEspButtonGroup group)
    {
        mGroups.add(group);
    }

    @Override
    public void deleteGroup(IEspButtonGroup group)
    {
        mGroups.remove(group);
    }
    
}
