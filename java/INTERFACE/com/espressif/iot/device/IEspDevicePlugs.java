package com.espressif.iot.device;

import java.util.List;

import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;

public interface IEspDevicePlugs extends IEspDevice, Cloneable
{
    public static final int TIMER_TAIL_LENGTH = 2;
    
    public IEspStatusPlugs getStatusPlugs();
    
    public void setStatusPlugs(IEspStatusPlugs status);
    
    public List<IAperture> getApertureList();
    
    public void setApertureList(List<IAperture> apertures);
    
    public void updateAperture(IAperture newAperture);
    
    public boolean updateApertureOnOff(IAperture newAperture);
}
