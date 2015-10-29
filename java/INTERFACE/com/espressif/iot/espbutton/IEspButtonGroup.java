package com.espressif.iot.espbutton;

import java.util.List;

public interface IEspButtonGroup
{
    public void setId(long id);
    
    public long getId();
    
    public List<String> getDevicesBssid();
    
    public void setDevicesBssid(List<String> bssids);
    
    public void addDevicesBssid(List<String> bssids);
    
    public void removeDevicesBssid(List<String> bssids);
    
    public void addDeviceBssid(String bssid);
    
    public void removeDeviceBssid(String bssid);
}
