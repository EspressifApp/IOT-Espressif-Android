package com.espressif.iot.action.device.humiture;

import java.util.List;

import com.espressif.iot.action.device.sensor.EspActionSensorGetStatusListInternetDB;
import com.espressif.iot.command.device.humiture.EspCommandHumitureGetStatusListInternet;
import com.espressif.iot.command.device.humiture.IEspCommandHumitureGetStatusListInternet;
import com.espressif.iot.db.greenrobot.daos.GenericDataDB;
import com.espressif.iot.object.db.IGenericDataDB;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.EspStatusHumiture;
import com.espressif.iot.type.device.status.IEspStatusHumiture;
import com.espressif.iot.type.device.status.IEspStatusSensor;

public class EspActionHumitureGetStatusListInternetDB extends EspActionSensorGetStatusListInternetDB implements
    IEspActionHumitureGetStatusListInternetDB
{
    @Override
    public IEspStatusSensor parseStatus(IGenericDataDB dataInDB)
    {
        StringBuilder sb = new StringBuilder(dataInDB.getData());
        String[] datas = sb.toString().split(",");
        double x = Double.parseDouble(datas[0]);
        double y = Double.parseDouble(datas[1]);
        long at = dataInDB.getTimestamp();
        IEspStatusSensor statusHumiture = new EspStatusHumiture();
        statusHumiture.setX(x);
        statusHumiture.setY(y);
        statusHumiture.setAt(at);
        return statusHumiture;
    }
    
    @Override
    public IGenericDataDB parseStatus(long deviceId, IEspStatusSensor statusSensor)
    {
        double x = statusSensor.getX();
        double y = statusSensor.getY();
        String data = x + "," + y;
        long timestamp = statusSensor.getAt();
        IGenericDataDB genericDataDB = new GenericDataDB(null, deviceId, timestamp, data, 0);
        return genericDataDB;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<IEspStatusSensor> doCommandSensorGetStatusListInternet(String deviceKey, long startTimestampFromServer,
        long endTimestampFromServer)
    {
        IEspCommandHumitureGetStatusListInternet command = new EspCommandHumitureGetStatusListInternet();
        return (List<IEspStatusSensor>)(List<?>)command.doCommandHumitureGetStatusListInternet(deviceKey,
            startTimestampFromServer,
            endTimestampFromServer);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<IEspStatusHumiture> doActionHumitureGetStatusListInternetDB(long deviceId, String deviceKey,
        long startTimestamp, long endTimestamp, long interval)
    {
        return (List<IEspStatusHumiture>)(List<?>)super.doActionSensorGetStatusListInternetDB(deviceId,
            deviceKey,
            startTimestamp,
            endTimestamp,
            interval,
            EspDeviceType.HUMITURE);
    }
    
}
