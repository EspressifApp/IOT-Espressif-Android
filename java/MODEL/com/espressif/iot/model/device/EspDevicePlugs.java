package com.espressif.iot.model.device;

import java.util.List;

import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;

public class EspDevicePlugs extends EspDevice implements IEspDevicePlugs
{
    private IEspStatusPlugs mStatus;
    
    private List<IAperture> mApertureList;
    
    public EspDevicePlugs()
    {
        mStatus = new EspStatusPlugs();
        mApertureList = mStatus.getStatusApertureList();
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        EspDevicePlugs device = (EspDevicePlugs)super.clone();
        
        device.setStatusPlugs((EspStatusPlugs)((EspStatusPlugs)mStatus).clone());
        
        return device;
    }

    @Override
    public IEspStatusPlugs getStatusPlugs()
    {
        return mStatus;
    }

    @Override
    public void setStatusPlugs(IEspStatusPlugs status)
    {
        mStatus.setStatusApertureList(status.getStatusApertureList());
    }

    @Override
    public List<IAperture> getApertureList()
    {
        return mStatus.getStatusApertureList();
    }

    @Override
    public void setApertureList(List<IAperture> apertures)
    {
        mStatus.setStatusApertureList(apertures);
    }

    @Override
    public void updateAperture(IAperture newAperture)
    {
        for (IAperture aperture : mApertureList)
        {
            if (aperture.getId() == newAperture.getId())
            {
                aperture.setTitle(newAperture.getTitle());
                aperture.setOn(newAperture.isOn());
                return;
            }
        }
        
        mApertureList.add(newAperture);
    }

    @Override
    public boolean updateApertureOnOff(IAperture newAperture)
    {
        for (IAperture aperture : mApertureList)
        {
            if (aperture.getId() == newAperture.getId())
            {
                aperture.setOn(newAperture.isOn());
                return true;
            }
        }
        
        return false;
    }
}
