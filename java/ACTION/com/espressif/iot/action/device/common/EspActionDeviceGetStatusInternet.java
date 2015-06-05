package com.espressif.iot.action.device.common;

import com.espressif.iot.command.device.flammable.EspCommandFlammableGetStatusInternet;
import com.espressif.iot.command.device.flammable.IEspCommandFlammableGetStatusInternet;
import com.espressif.iot.command.device.humiture.EspCommandHumitureGetStatusInternet;
import com.espressif.iot.command.device.humiture.IEspCommandHumitureGetStatusInternet;
import com.espressif.iot.command.device.light.EspCommandLightGetStatusInternet;
import com.espressif.iot.command.device.light.IEspCommandLightGetStatusInternet;
import com.espressif.iot.command.device.plug.EspCommandPlugGetStatusInternet;
import com.espressif.iot.command.device.plug.IEspCommandPlugGetStatusInternet;
import com.espressif.iot.command.device.plugs.EspCommandPlugsGetStatusInternet;
import com.espressif.iot.command.device.plugs.IEspCommandPlugsGetStatusInternet;
import com.espressif.iot.command.device.remote.EspCommandRemoteGetStatusInternet;
import com.espressif.iot.command.device.remote.IEspCommandRemoteGetStatusInternet;
import com.espressif.iot.command.device.voltage.EspCommandVoltageGetStatusInternet;
import com.espressif.iot.command.voltage.IEspCommandVoltageGetStatusInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceFlammable;
import com.espressif.iot.device.IEspDeviceHumiture;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.device.IEspDeviceRemote;
import com.espressif.iot.device.IEspDeviceVoltage;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.IEspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusHumiture;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusRemote;
import com.espressif.iot.type.device.status.IEspStatusVoltage;

public class EspActionDeviceGetStatusInternet implements IEspActionDeviceGetStatusInternet
{
    
    @Override
    public boolean doActionDeviceGetStatusInternet(IEspDevice device)
    {
        EspDeviceType deviceType = device.getDeviceType();
        String deviceKey = device.getKey();
        boolean suc = false;
        switch (deviceType)
        {
            case FLAMMABLE:
                IEspCommandFlammableGetStatusInternet flammalbeCommand = new EspCommandFlammableGetStatusInternet();
                IEspStatusFlammable flammableStatus = flammalbeCommand.doCommandFlammableGetStatusInternet(deviceKey);
                if (flammableStatus != null)
                {
                    suc = true;
                    IEspDeviceFlammable flammable = (IEspDeviceFlammable)device;
                    flammable.getStatusFlammable().setAt(flammableStatus.getAt());
                    flammable.getStatusFlammable().setX(flammableStatus.getX());
                }
                return suc;
            case HUMITURE:
                IEspCommandHumitureGetStatusInternet humitureCommand = new EspCommandHumitureGetStatusInternet();
                IEspStatusHumiture humitureStatus = humitureCommand.doCommandHumitureGetStatusInternet(deviceKey);
                if (humitureStatus != null)
                {
                    suc = true;
                    IEspDeviceHumiture humiture = (IEspDeviceHumiture)device;
                    humiture.getStatusHumiture().setAt(humitureStatus.getAt());
                    humiture.getStatusHumiture().setX(humitureStatus.getX());
                    humiture.getStatusHumiture().setY(humitureStatus.getY());
                }
                return suc;
            case VOLTAGE:
                IEspCommandVoltageGetStatusInternet voltageCommand = new EspCommandVoltageGetStatusInternet();
                IEspStatusVoltage voltageStatus = voltageCommand.doCommandVoltageGetStatusInternet(deviceKey);
                if (voltageStatus != null)
                {
                    suc = true;
                    IEspDeviceVoltage voltage = (IEspDeviceVoltage)device;
                    voltage.getStatusVoltage().setAt(voltageStatus.getAt());
                    voltage.getStatusVoltage().setX(voltageStatus.getX());
                }
                return suc;
            case LIGHT:
                IEspCommandLightGetStatusInternet lightCommand = new EspCommandLightGetStatusInternet();
                IEspStatusLight lightStatus = lightCommand.doCommandLightGetStatusInternet(deviceKey);
                if (lightStatus != null)
                {
                    suc = true;
                    IEspDeviceLight light = (IEspDeviceLight)device;
                    light.getStatusLight().setPeriod(lightStatus.getPeriod());
                    light.getStatusLight().setRed(lightStatus.getRed());
                    light.getStatusLight().setGreen(lightStatus.getGreen());
                    light.getStatusLight().setBlue(lightStatus.getBlue());
                    light.getStatusLight().setCWhite(lightStatus.getCWhite());
                    light.getStatusLight().setWWhite(lightStatus.getWWhite());
                }
                return suc;
            case PLUG:
                IEspCommandPlugGetStatusInternet plugCommand = new EspCommandPlugGetStatusInternet();
                IEspStatusPlug plugStatus = plugCommand.doCommandPlugGetStatusInternet(deviceKey);
                if (plugStatus != null)
                {
                    suc = true;
                    IEspDevicePlug plug = (IEspDevicePlug)device;
                    plug.getStatusPlug().setIsOn(plugStatus.isOn());
                }
                return suc;
            case REMOTE:
                IEspCommandRemoteGetStatusInternet remoteCommand = new EspCommandRemoteGetStatusInternet();
                IEspStatusRemote remoteStatus = remoteCommand.doCommandRemoteGetStatusInternet(deviceKey);
                if (remoteStatus != null)
                {
                    suc = true;
                    IEspDeviceRemote remote = (IEspDeviceRemote)device;
                    remote.getStatusRemote().setAddress(remoteStatus.getAddress());
                    remote.getStatusRemote().setCommand(remoteStatus.getCommand());
                    remote.getStatusRemote().setRepeat(remoteStatus.getRepeat());
                }
                return suc;
            case PLUGS:
                IEspCommandPlugsGetStatusInternet plugsCommand = new EspCommandPlugsGetStatusInternet();
                IEspStatusPlugs plugsStatus = plugsCommand.doCommandPlugsGetStatusInternet(deviceKey);
                if (plugsStatus != null)
                {
                    suc = true;
                    IEspDevicePlugs plugs = (IEspDevicePlugs)device;
                    plugs.setStatusPlugs(plugsStatus);
                }
                return suc;
            case ROOT:
                return false;
            case NEW:
                break;
        }
        throw new IllegalArgumentException();
    }
    
}
