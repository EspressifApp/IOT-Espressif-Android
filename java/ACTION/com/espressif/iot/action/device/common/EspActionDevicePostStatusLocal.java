package com.espressif.iot.action.device.common;

import java.net.InetAddress;
import java.util.List;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.light.EspCommandLightPostStatusLocal;
import com.espressif.iot.command.device.light.IEspCommandLightPostStatusLocal;
import com.espressif.iot.command.device.plug.EspCommandPlugPostStatusLocal;
import com.espressif.iot.command.device.plug.IEspCommandPlugPostStatusLocal;
import com.espressif.iot.command.device.plugs.EspCommandPlugsPostStatusLocal;
import com.espressif.iot.command.device.plugs.IEspCommandPlugsPostStatusLocal;
import com.espressif.iot.command.device.remote.EspCommandRemotePostStatusLocal;
import com.espressif.iot.command.device.remote.IEspCommandRemotePostStatusLocal;
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
import com.espressif.iot.type.device.status.IEspStatusRemote;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;

public class EspActionDevicePostStatusLocal implements IEspActionDevicePostStatusLocal
{
    private boolean __doActionDevicePostStatusLocal(IEspDevice device, IEspDeviceStatus status)
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
                return executePostLightStatusLocal(device, (IEspStatusLight)status);
            case PLUG:
                return executePostPlugStatusLocal(device, (IEspStatusPlug)status);
            case REMOTE:
                return executePostRemoteStatusLocal(device, (IEspStatusRemote)status);
            case PLUGS:
                return executePostPlugsStatusLocal(device, (IEspStatusPlugs)status);
            case ROOT:
                executePostRootStatusLocal((IEspDeviceRoot)device, status);
                return true;
            case NEW:
                break;
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public boolean doActionDevicePostStatusLocal(IEspDevice device, IEspDeviceStatus status)
    {
        return __doActionDevicePostStatusLocal(device, status);
    }
    
    private boolean executePostLightStatusLocal(IEspDevice device, IEspStatusLight status)
    {
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean result = false;
        
        IEspCommandLightPostStatusLocal lightCommand = new EspCommandLightPostStatusLocal();
        result = lightCommand.doCommandLightPostStatusLocal(inetAddress, status, deviceBssid, isMeshDevice);
        if (result)
        {
            IEspStatusLight lightStatus = ((IEspDeviceLight)device).getStatusLight();
            lightStatus.setPeriod(status.getPeriod());
            lightStatus.setRed(status.getRed());
            lightStatus.setGreen(status.getGreen());
            lightStatus.setBlue(status.getBlue());
            lightStatus.setCWhite(status.getCWhite());
            lightStatus.setWWhite(status.getWWhite());
        }
        
        return result;
    }
    
    private boolean executePostPlugStatusLocal(IEspDevice device, IEspStatusPlug status)
    {
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean result = false;
        
        IEspCommandPlugPostStatusLocal plugCommand = new EspCommandPlugPostStatusLocal();
        result = plugCommand.doCommandPlugPostStatusLocal(inetAddress, status, deviceBssid, isMeshDevice);
        if (result)
        {
            IEspStatusPlug plugStatus = ((IEspDevicePlug)device).getStatusPlug();
            plugStatus.setIsOn(status.isOn());
        }
        
        return result;
    }
    
    private boolean executePostRemoteStatusLocal(IEspDevice device, IEspStatusRemote status)
    {
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean result = false;
        
        IEspCommandRemotePostStatusLocal remoteCommand = new EspCommandRemotePostStatusLocal();
        result = remoteCommand.doCommandRemotePostStatusLocal(inetAddress, status, deviceBssid, isMeshDevice);
        if (result)
        {
            IEspStatusRemote remoteStatus = ((IEspDeviceRemote)device).getStatusRemote();
            remoteStatus.setAddress(remoteStatus.getAddress());
            remoteStatus.setCommand(remoteStatus.getCommand());
            remoteStatus.setRepeat(remoteStatus.getRepeat());
        }
        
        return result;
    }
    
    private boolean executePostPlugsStatusLocal(IEspDevice device, IEspStatusPlugs status)
    {
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean result = false;
        
        IEspCommandPlugsPostStatusLocal plugsCommand = new EspCommandPlugsPostStatusLocal();
        result = plugsCommand.doCommandPlugsPostStatusLocal(inetAddress, status, deviceBssid, isMeshDevice);
        if (result) {
            IEspDevicePlugs devicePlugs = (IEspDevicePlugs)device;
            for (IAperture postAperture : status.getStatusApertureList()) {
                devicePlugs.updateApertureOnOff(postAperture);
            }
        }
        
        return result;
    }
    
    private void executePostRootStatusLocal(IEspDeviceRoot device, IEspDeviceStatus status)
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
            InetAddress inetAddress = device.getInetAddress();
            String bssid = device.getBssid();
            
            if (status instanceof IEspStatusLight)
            {
                IEspStatusLight lightStatus = (IEspStatusLight)status;
                IEspCommandLightPostStatusLocal lightCommand = new EspCommandLightPostStatusLocal();
                lightCommand.doCommandLightPostStatusLocal(inetAddress, lightStatus, bssid, device.getIsMeshDevice());
            }
            else if (status instanceof IEspStatusPlug)
            {
                IEspStatusPlug plugStatus = (IEspStatusPlug)status;
                IEspCommandPlugPostStatusLocal plugCommand = new EspCommandPlugPostStatusLocal();
                plugCommand.doCommandPlugPostStatusLocal(inetAddress, plugStatus, bssid, device.getIsMeshDevice());
            }
            else if (status instanceof IEspStatusRemote)
            {
                IEspStatusRemote remoteStatus = (IEspStatusRemote)status;
                IEspCommandRemotePostStatusLocal remoteCommand = new EspCommandRemotePostStatusLocal();
                remoteCommand.doCommandRemotePostStatusLocal(inetAddress, remoteStatus, bssid, device.getIsMeshDevice());
            }
        }
        
    }
}
