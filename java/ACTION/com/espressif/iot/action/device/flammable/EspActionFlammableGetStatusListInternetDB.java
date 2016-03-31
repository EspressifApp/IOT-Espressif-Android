package com.espressif.iot.action.device.flammable;

import java.util.List;

import com.espressif.iot.action.device.sensor.EspActionSensorGetStatusListInternetDB;
import com.espressif.iot.command.device.flammable.EspCommandFlammableGetStatusListInternet;
import com.espressif.iot.command.device.flammable.IEspCommandFlammableGetStatusListInternet;
import com.espressif.iot.db.greenrobot.daos.GenericDataDB;
import com.espressif.iot.object.db.IGenericDataDB;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.EspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusSensor;

public class EspActionFlammableGetStatusListInternetDB extends EspActionSensorGetStatusListInternetDB implements
    IEspActionFlammableGetStatusListInternetDB
{
    
    @Override
    public IEspStatusSensor parseStatus(IGenericDataDB dataInDB)
    {
        StringBuilder sb = new StringBuilder(dataInDB.getData());
        String[] datas = sb.toString().split(",");
        double x = Double.parseDouble(datas[0]);
        long at = dataInDB.getTimestamp();
        IEspStatusFlammable statusFlammable = new EspStatusFlammable();
        statusFlammable.setX(x);
        statusFlammable.setAt(at);
        return statusFlammable;
    }
    
    @Override
    public IGenericDataDB parseStatus(long deviceId, IEspStatusSensor statusSensor)
    {
        double x = statusSensor.getX();
        String data = x + ",";
        long timestamp = statusSensor.getAt();
        IGenericDataDB genericDataDB = new GenericDataDB(null, deviceId, timestamp, data, 0);
        return genericDataDB;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<IEspStatusSensor> doCommandSensorGetStatusListInternet(String deviceKey, long startTimestampFromServer,
        long endTimestampFromServer)
    {
        IEspCommandFlammableGetStatusListInternet command = new EspCommandFlammableGetStatusListInternet();
        return (List<IEspStatusSensor>)(List<?>)command.doCommandFlammableGetStatusListInternet(deviceKey,
            startTimestampFromServer,
            endTimestampFromServer);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<IEspStatusFlammable> doActionFlammableGetStatusListInternetDB(long deviceId, String deviceKey,
        long startTimestamp, long endTimestamp, long interval)
    {
        return (List<IEspStatusFlammable>)(List<?>)super.doActionSensorGetStatusListInternetDB(deviceId,
            deviceKey,
            startTimestamp,
            endTimestamp,
            interval,
            EspDeviceType.FLAMMABLE);
    }
    
}
