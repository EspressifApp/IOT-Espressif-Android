package com.espressif.iot.group.builder;

import com.espressif.iot.group.IEspGroup;
import com.espressif.iot.model.group.EspGroup;

public class BEspGroup implements IBEspGroup
{
    
    /*
     * Singleton lazy initialization start
     */
    private BEspGroup()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspGroup instance = new BEspGroup();
    }
    
    public static BEspGroup getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public IEspGroup alloc()
    {
        IEspGroup group = new EspGroup();
        return group;
    }
}
