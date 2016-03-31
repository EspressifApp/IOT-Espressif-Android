package com.espressif.iot.action.device.voltage;

import java.util.List;

import com.espressif.iot.action.device.sensor.EspActionSensorGetStatusListInternetDB;
import com.espressif.iot.command.device.voltage.EspCommandVoltageGetStatusListInternet;
import com.espressif.iot.command.voltage.IEspCommandVoltageGetStatusListInternet;
import com.espressif.iot.db.greenrobot.daos.GenericDataDB;
import com.espressif.iot.object.db.IGenericDataDB;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.EspStatusVoltage;
import com.espressif.iot.type.device.status.IEspStatusVoltage;
import com.espressif.iot.type.device.status.IEspStatusSensor;

public class EspActionVoltageGetStatusListInternetDB extends EspActionSensorGetStatusListInternetDB implements
    IEspActionVoltageGetStatusListInternetDB
{

    @Override
    public IEspStatusSensor parseStatus(IGenericDataDB dataInDB)
    {
        StringBuilder sb = new StringBuilder(dataInDB.getData());
        String[] datas = sb.toString().split(",");
        double x = Double.parseDouble(datas[0]);
        long at = dataInDB.getTimestamp();
        IEspStatusVoltage statusVoltage = new EspStatusVoltage();
        statusVoltage.setX(x);
        statusVoltage.setAt(at);
        return statusVoltage;
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
        IEspCommandVoltageGetStatusListInternet command = new EspCommandVoltageGetStatusListInternet();
        return (List<IEspStatusSensor>)(List<?>)command.doCommandVoltageGetStatusListInternet(deviceKey,
            startTimestampFromServer,
            endTimestampFromServer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IEspStatusVoltage> doActionVoltageGetStatusListInternetDB(long deviceId, String deviceKey,
        long startTimestamp, long endTimestamp, long interval)
    {
        return (List<IEspStatusVoltage>)(List<?>)super.doActionSensorGetStatusListInternetDB(deviceId,
            deviceKey,
            startTimestamp,
            endTimestamp,
            interval,
            EspDeviceType.VOLTAGE);
    }
    
    
}
