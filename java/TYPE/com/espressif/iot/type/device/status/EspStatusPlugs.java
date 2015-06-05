package com.espressif.iot.type.device.status;

import java.util.List;
import java.util.Vector;

public class EspStatusPlugs implements IEspStatusPlugs, Cloneable
{
    private List<IAperture> mApertureList;
    
    public EspStatusPlugs()
    {
        mApertureList = new Vector<IAperture>();
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public void setStatusApertureList(List<IAperture> list)
    {
        mApertureList.clear();
        mApertureList.addAll(list);
    }

    @Override
    public List<IAperture> getStatusApertureList()
    {
        return mApertureList;
    }
    
    @Override
    public void updateOrAddAperture(IAperture newAperture)
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
