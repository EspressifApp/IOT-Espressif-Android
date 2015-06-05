package com.espressif.iot.action.device.common;

import java.util.List;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.light.EspCommandLightPostStatusInternet;
import com.espressif.iot.command.device.light.IEspCommandLightPostStatusInternet;
import com.espressif.iot.command.device.plug.EspCommandPlugPostStatusInternet;
import com.espressif.iot.command.device.plug.IEspCommandPlugPostStatusInternet;
import com.espressif.iot.command.device.plugs.EspCommandPlugsPostStatusInternet;
import com.espressif.iot.command.device.plugs.IEspCommandPlugsPostStatusInternet;
import com.espressif.iot.command.device.remote.EspCommandRemotePostStatusInternet;
import com.espressif.iot.command.device.remote.IEspCommandRemotePostStatusInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.device.IEspDeviceRemote;
import com.espressif.iot.device.IEspDeviceRoot;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public class EspActionDevicePostStatusInternet implements IEspActionDevicePostStatusInternet
{
    
    @Override
    public boolean doActionDevicePostStatusInternet(IEspDevice device, IEspDeviceStatus status)
    {
        return doActionDevicePostStatusInternet(device, status, false);
    }
    
    @Override
    public boolean doActionDevicePostStatusInternet(IEspDevice device, IEspDeviceStatus status, boolean isBroadcast)
    {
        EspDeviceType deviceType = device.getDeviceType();
        String deviceKey = device.getKey();
        String deviceRouter = null;
        if (isBroadcast)
        {
            deviceRouter = device.getRouter();
        }
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
                IEspStatusLight lightStatus = (IEspStatusLight)status;
                IEspCommandLightPostStatusInternet lightCommand = new EspCommandLightPostStatusInternet();
                suc = lightCommand.doCommandLightPostStatusInternet(deviceKey, lightStatus, deviceRouter);
                if (suc)
                {
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
                IEspStatusPlug plugStatus = (IEspStatusPlug)status;
                IEspCommandPlugPostStatusInternet plugCommand = new EspCommandPlugPostStatusInternet();
                suc = plugCommand.doCommandPlugPostStatusInternet(deviceKey, plugStatus, deviceRouter);
                if (suc)
                {
                    IEspDevicePlug plugDevice = (IEspDevicePlug)device;
                    plugDevice.getStatusPlug().setIsOn(plugStatus.isOn());
                }
                return suc;
            case REMOTE:
                IEspStatusRemote remoteStatus = (IEspStatusRemote)status;
                IEspCommandRemotePostStatusInternet remoteCommand = new EspCommandRemotePostStatusInternet();
                suc = remoteCommand.doCommandRemotePostStatusInternet(deviceKey, remoteStatus, deviceRouter);
                if (suc)
                {
                    IEspDeviceRemote remote = (IEspDeviceRemote)device;
                    remote.getStatusRemote().setAddress(remoteStatus.getAddress());
                    remote.getStatusRemote().setCommand(remoteStatus.getCommand());
                    remote.getStatusRemote().setRepeat(remoteStatus.getRepeat());
                }
                return suc;
            case PLUGS:
                IEspStatusPlugs plugsStatus = (IEspStatusPlugs)status;
                IEspCommandPlugsPostStatusInternet plugsCommand = new EspCommandPlugsPostStatusInternet();
                suc = plugsCommand.doCommandPlugsPostStatusInternet(deviceKey, plugsStatus, deviceRouter);
                if (suc)
                {
                    IEspDevicePlugs plugs = (IEspDevicePlugs)device;
                    for (IAperture postAperture : plugsStatus.getStatusApertureList())
                    {
                        plugs.updateApertureOnOff(postAperture);
                    }
                }
                return suc;
            case ROOT:
                doRootRouterCommandInternet((IEspDeviceRoot)device, status);
                return true;
            case NEW:
                break;
        }
        throw new IllegalArgumentException();
    
    }
    
    private void doRootRouterCommandInternet(IEspDeviceRoot device, IEspDeviceStatus status)
    {
        List<IEspDeviceTreeElement> childList = device.getDeviceTreeElementList();
        for (IEspDeviceTreeElement element : childList)
        {
            if (element.getLevel() == FIRST_CHILD_LEVEL)
            {
                IEspDevice child = element.getCurrentDevice();
                EspBaseApiUtil.submit(new RootStatusRunnable(child, status));
            }
        }
    }
    
    private class RootStatusRunnable implements Runnable
    {
        private IEspDevice device;
        
        private IEspDeviceStatus status;
        
        public RootStatusRunnable(IEspDevice device, IEspDeviceStatus status)
        {
            this.device = device;
            this.status = status;
        }
        
        @Override
        public void run()
        {
            if (status instanceof IEspStatusLight)
            {
                IEspStatusLight lightStatus = (IEspStatusLight)status;
                IEspCommandLightPostStatusInternet lightCommand = new EspCommandLightPostStatusInternet();
                lightCommand.doCommandLightPostStatusInternet(device.getKey(), lightStatus, device.getRouter());
            }
            else if (status instanceof IEspStatusPlug)
            {
                IEspStatusPlug plugStatus = (IEspStatusPlug)status;
                IEspCommandPlugPostStatusInternet plugCommand = new EspCommandPlugPostStatusInternet();
                plugCommand.doCommandPlugPostStatusInternet(device.getKey(), plugStatus, device.getRouter());
            }
            else if (status instanceof IEspStatusRemote)
            {
                IEspStatusRemote remoteStatus = (IEspStatusRemote)status;
                IEspCommandRemotePostStatusInternet remoteCommand = new EspCommandRemotePostStatusInternet();
                remoteCommand.doCommandRemotePostStatusInternet(device.getKey(), remoteStatus, device.getRouter());
            }
        }
    }

}
