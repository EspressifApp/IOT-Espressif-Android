package com.espressif.iot.espbutton;

import java.util.List;

public interface IEspButton
{
    public void setMac(String mac);
    
    public String getMac();
    
    public List<IEspButtonGroup> getGroups();
    
    public void setGroups(List<IEspButtonGroup> groups);
    
    public void addGroups(List<IEspButtonGroup> groups);
    
    public void deleteGroups(List<IEspButtonGroup> groups);
    
    public void addGroup(IEspButtonGroup group);
    
    public void deleteGroup(IEspButtonGroup group);
}
