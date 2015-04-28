package com.espressif.iot.action.device.builder;

import com.espressif.iot.action.IEspAction;
import com.espressif.iot.action.device.flammable.EspActionFlammableGetStatusListInternetDB;
import com.espressif.iot.action.device.flammable.IEspActionFlammableGetStatusListInternetDB;
import com.espressif.iot.action.device.humiture.EspActionHumitureGetStatusListInternetDB;
import com.espressif.iot.action.device.humiture.IEspActionHumitureGetStatusListInternetDB;
import com.espressif.iot.action.device.voltage.EspActionVoltageGetStatusListInternetDB;
import com.espressif.iot.action.device.voltage.IEspActionVoltageGetStatusListInternetDB;
import com.espressif.iot.action.softap_sta_support.ISSSActionDeviceUpgradeLocal;
import com.espressif.iot.action.softap_sta_support.SSSActionDeviceUpgradeLocal;

public class BEspAction implements IBEspAction
{
    /*
     * Singleton lazy initialization start
     */
    private BEspAction()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspAction instance = new BEspAction();
    }
    
    public static BEspAction getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public IEspAction alloc(Class<?> clazz)
    {
        if (clazz == IEspActionFlammableGetStatusListInternetDB.class)
        {
            return new EspActionFlammableGetStatusListInternetDB();
        }
        else if (clazz == IEspActionHumitureGetStatusListInternetDB.class)
        {
            return new EspActionHumitureGetStatusListInternetDB();
        }
        else if (clazz == IEspActionVoltageGetStatusListInternetDB.class)
        {
            return new EspActionVoltageGetStatusListInternetDB();
        }
        else if (clazz == ISSSActionDeviceUpgradeLocal.class)
        {
            return new SSSActionDeviceUpgradeLocal();
        }
        return null;
    }
    
}
