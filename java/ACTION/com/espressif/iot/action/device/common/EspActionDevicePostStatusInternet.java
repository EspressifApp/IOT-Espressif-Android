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
        EspDeviceType deviceType = device.getDeviceType();
        switch (deviceType)
        {
            case FLAMMABLE:
                break;
            case HUMITURE:
                break;
            case VOLTAGE:
                break;
            case LIGHT:
                return executePostLightStatusInternet((IEspDeviceLight)device, (IEspStatusLight)status);
            case PLUG:
                return executePostPlugStatusInternet((IEspDevicePlug)device, (IEspStatusPlug)status);
            case REMOTE:
                return executePostRemoteStatusInternet((IEspDeviceRemote)device, (IEspStatusRemote)status);
            case PLUGS:
                return executePostPlugsStatusInternet((IEspDevicePlugs)device, (IEspStatusPlugs)status);
            case ROOT:
                executePostRootRouterStatusInternet((IEspDeviceRoot)device, status);
                return true;
            case NEW:
                break;
        }
        throw new IllegalArgumentException();
    }
    
    private boolean executePostLightStatusInternet(IEspDeviceLight light, IEspStatusLight status)
    {
        boolean result = false;
        
        IEspCommandLightPostStatusInternet lightCommand = new EspCommandLightPostStatusInternet();
        result = lightCommand.doCommandLightPostStatusInternet(light, status);
        if (result)
        {
            light.getStatusLight().setStatus(status.getStatus());
            light.getStatusLight().setPeriod(status.getPeriod());
            light.getStatusLight().setRed(status.getRed());
            light.getStatusLight().setGreen(status.getGreen());
            light.getStatusLight().setBlue(status.getBlue());
            light.getStatusLight().setWhite(status.getWhite());
        }
        
        return result;
    }
    
    private boolean executePostPlugStatusInternet(IEspDevicePlug plug, IEspStatusPlug status)
    {
        boolean result = false;
        
        IEspCommandPlugPostStatusInternet plugCommand = new EspCommandPlugPostStatusInternet();
        result = plugCommand.doCommandPlugPostStatusInternet(plug.getKey(), status);
        if (result)
        {
            plug.getStatusPlug().setIsOn(status.isOn());
        }
        
        return result;
    }
    
    private boolean executePostRemoteStatusInternet(IEspDeviceRemote remote, IEspStatusRemote status)
    {
        boolean result = false;
        
        IEspCommandRemotePostStatusInternet remoteCommand = new EspCommandRemotePostStatusInternet();
        result = remoteCommand.doCommandRemotePostStatusInternet(remote.getKey(), status);
        if (result)
        {
            remote.getStatusRemote().setAddress(status.getAddress());
            remote.getStatusRemote().setCommand(status.getCommand());
            remote.getStatusRemote().setRepeat(status.getRepeat());
        }
        
        return result;
    }
    
    private boolean executePostPlugsStatusInternet(IEspDevicePlugs plugs, IEspStatusPlugs status)
    {
        boolean result = false;
        
        IEspCommandPlugsPostStatusInternet plugsCommand = new EspCommandPlugsPostStatusInternet();
        result = plugsCommand.doCommandPlugsPostStatusInternet(plugs.getKey(), status);
        if (result)
        {
            for (IAperture postAperture : status.getStatusApertureList())
            {
                plugs.updateApertureOnOff(postAperture);
            }
        }
        
        return result;
    }
    
    private void executePostRootRouterStatusInternet(IEspDeviceRoot device, IEspDeviceStatus status)
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
                lightCommand.doCommandLightPostStatusInternet(device, lightStatus);
            }
            else if (status instanceof IEspStatusPlug)
            {
                IEspStatusPlug plugStatus = (IEspStatusPlug)status;
                IEspCommandPlugPostStatusInternet plugCommand = new EspCommandPlugPostStatusInternet();
                plugCommand.doCommandPlugPostStatusInternet(device.getKey(), plugStatus);
            }
            else if (status instanceof IEspStatusRemote)
            {
                IEspStatusRemote remoteStatus = (IEspStatusRemote)status;
                IEspCommandRemotePostStatusInternet remoteCommand = new EspCommandRemotePostStatusInternet();
                remoteCommand.doCommandRemotePostStatusInternet(device.getKey(), remoteStatus);
            }
        }
    }
    
}
