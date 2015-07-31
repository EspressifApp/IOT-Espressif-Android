package com.espressif.iot.action.device.common;

import java.net.InetAddress;
import java.util.List;

import com.espressif.iot.command.device.light.EspCommandLightGetEspnowLocal;
import com.espressif.iot.command.device.light.EspCommandLightGetStatusLocal;
import com.espressif.iot.command.device.light.IEspCommandLightGetEspnowLocal;
import com.espressif.iot.command.device.light.IEspCommandLightGetStatusLocal;
import com.espressif.iot.command.device.plug.EspCommandPlugGetStatusLocal;
import com.espressif.iot.command.device.plug.IEspCommandPlugGetStatusLocal;
import com.espressif.iot.command.device.plugs.EspCommandPlugsGetStatusLocal;
import com.espressif.iot.command.device.plugs.IEspCommandPlugsGetStatusLocal;
import com.espressif.iot.command.device.remote.EspCommandRemoteGetStatusLocal;
import com.espressif.iot.command.device.remote.IEspCommandRemoteGetStatusLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.device.IEspDeviceRemote;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.IEspStatusEspnow;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public class EspActionDeviceGetStatusLocal implements IEspActionDeviceGetStatusLocal
{
    
    @Override
    public boolean doActionDeviceGetStatusLocal(IEspDevice device)
    {
        EspDeviceType deviceType = device.getDeviceType();
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        String router = device.getRouter();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean suc = false;
        switch (deviceType)
        {
            case FLAMMABLE:
                break;
            case HUMITURE:
                break;
            case VOLTAGE:
                break;
            case LIGHT:
                IEspStatusLight statusLight;
                if (device instanceof IEspDeviceSSS)
                {
                    statusLight = (IEspStatusLight)((IEspDeviceSSS)device).getDeviceStatus();
                }
                else
                {
                    statusLight = ((IEspDeviceLight)device).getStatusLight();
                }
                
                // Get rgb period white value
                IEspCommandLightGetStatusLocal lightCommand = new EspCommandLightGetStatusLocal();
                IEspStatusLight lightStatus =
                    lightCommand.doCommandLightGetStatusLocal(inetAddress, deviceBssid, isMeshDevice);
                if (lightStatus != null)
                {
                    suc = true;
                    statusLight.setPeriod(lightStatus.getPeriod());
                    statusLight.setRed(lightStatus.getRed());
                    statusLight.setGreen(lightStatus.getGreen());
                    statusLight.setBlue(lightStatus.getBlue());
                    statusLight.setCWhite(lightStatus.getCWhite());
                    statusLight.setWWhite(lightStatus.getWWhite());
                }
                
                // Get battery value
                boolean batterySuc = false;
                IEspCommandLightGetEspnowLocal batteryCommand = new EspCommandLightGetEspnowLocal();
                List<IEspStatusEspnow> espnowStatusList =
                    batteryCommand.doCommandLightGetEspnowLocal(inetAddress, deviceBssid, isMeshDevice);
                List<IEspStatusEspnow> deviceEspnowStatusList = device.getEspnowStatusList();
                deviceEspnowStatusList.clear();
                if (espnowStatusList != null)
                {
                    batterySuc = true;
                    deviceEspnowStatusList.addAll(espnowStatusList);
                }
                
                return suc && batterySuc;
            case PLUG:
                IEspCommandPlugGetStatusLocal plugCommand = new EspCommandPlugGetStatusLocal();
                IEspStatusPlug plugStatus =
                    plugCommand.doCommandPlugGetStatusLocal(inetAddress, deviceBssid, isMeshDevice);
                if (plugStatus != null)
                {
                    suc = true;
                    IEspStatusPlug statusPlug;
                    if (device instanceof IEspDeviceSSS)
                    {
                        statusPlug = (IEspStatusPlug)((IEspDeviceSSS)device).getDeviceStatus();
                    }
                    else
                    {
                        statusPlug = ((IEspDevicePlug)device).getStatusPlug();
                    }
                    statusPlug.setIsOn(plugStatus.isOn());
                }
                return suc;
            case REMOTE:
                IEspCommandRemoteGetStatusLocal remoteCommand = new EspCommandRemoteGetStatusLocal();
                IEspStatusRemote remoteStatus =
                    remoteCommand.doCommandRemoteGetStatusLocal(inetAddress, deviceBssid, isMeshDevice);
                if (remoteStatus != null)
                {
                    suc = true;
                    IEspStatusRemote statusRemote;
                    if (device instanceof IEspDeviceSSS)
                    {
                        statusRemote = (IEspStatusRemote)((IEspDeviceSSS)device).getDeviceStatus();
                    }
                    else
                    {
                        statusRemote = ((IEspDeviceRemote)device).getStatusRemote();
                    }
                    statusRemote.setAddress(remoteStatus.getAddress());
                    statusRemote.setCommand(remoteStatus.getCommand());
                    statusRemote.setRepeat(remoteStatus.getRepeat());
                }
                return suc;
            case PLUGS:
                IEspCommandPlugsGetStatusLocal plugsCommand = new EspCommandPlugsGetStatusLocal();
                IEspStatusPlugs plugsStatus =
                    plugsCommand.doCommandPlugsGetStatusLocal(inetAddress, deviceBssid, isMeshDevice);
                if (plugsStatus != null)
                {
                    suc = true;
                    
                    if (device instanceof IEspDevicePlugs)
                    {
                        IEspDevicePlugs plugs = (IEspDevicePlugs)device;
                        plugs.setStatusPlugs(plugsStatus);
                    }
                    else if (device instanceof IEspDeviceSSS)
                    {
                        IEspStatusPlugs statusPlugs = (IEspStatusPlugs)((IEspDeviceSSS)device).getDeviceStatus();
                        statusPlugs.setStatusApertureList(plugsStatus.getStatusApertureList());
                    }
                }
                return suc;
            case NEW:
                break;
            case ROOT:
                return false;
        }
        throw new IllegalArgumentException();
    }
    
}
