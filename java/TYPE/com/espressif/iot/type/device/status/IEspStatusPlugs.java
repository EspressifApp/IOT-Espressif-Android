package com.espressif.iot.type.device.status;

import java.util.List;

import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspStatusPlugs extends IEspDeviceStatus
{
    public interface IAperture {
        int getId();
        
        void setTitle(String title);
        
        String getTitle();
        
        void setOn(boolean isOn);
        
        boolean isOn();
    }
    
    public void setStatusApertureList(List<IAperture> list);
    
    public List<IAperture> getStatusApertureList();
    
    public void updateAperture(IAperture newAperture);
    
    public boolean updateApertureOnOff(IAperture newAperture);
}
