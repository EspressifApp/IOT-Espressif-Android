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
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusRemote;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.espressif.iot.util.RouterUtil;

public class EspActionDevicePostStatusLocal implements IEspActionDevicePostStatusLocal
{
    private boolean __doActionDevicePostStatusLocal(IEspDevice device, IEspDeviceStatus status, boolean isBroadcast)
    {
        if (isBroadcast && !device.getIsMeshDevice())
        {
            throw new IllegalArgumentException("only mesh device support broadcast action");
        }
        
        EspDeviceType deviceType = device.getDeviceType();
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        String router = device.getRouter();
        boolean isMeshDevice = device.getIsMeshDevice();
        if (isBroadcast)
        {
            router = RouterUtil.getBroadcastRouter(router);
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
                IEspCommandLightPostStatusLocal lightCommand = new EspCommandLightPostStatusLocal();
//                suc = lightCommand.doCommandLightPostStatusLocal(inetAddress, lightStatus, deviceBssid, router);
                suc = lightCommand.doCommandLightPostStatusLocal(inetAddress, lightStatus, deviceBssid, isMeshDevice);
                if (suc)
                {
                    IEspStatusLight statusLight;
                    if (device instanceof IEspDeviceSSS)
                    {
                        statusLight = (IEspStatusLight)((IEspDeviceSSS)device).getDeviceStatus();
                    }
                    else
                    {
                        statusLight = ((IEspDeviceLight)device).getStatusLight();
                    }
                    statusLight.setPeriod(lightStatus.getPeriod());
                    statusLight.setRed(lightStatus.getRed());
                    statusLight.setGreen(lightStatus.getGreen());
                    statusLight.setBlue(lightStatus.getBlue());
                    statusLight.setCWhite(lightStatus.getCWhite());
                    statusLight.setWWhite(lightStatus.getWWhite());
                }
                return suc;
            case PLUG:
                IEspStatusPlug plugStatus = (IEspStatusPlug)status;
                IEspCommandPlugPostStatusLocal plugCommand = new EspCommandPlugPostStatusLocal();
//                suc = plugCommand.doCommandPlugPostStatusLocal(inetAddress, plugStatus, deviceBssid, router);
                suc = plugCommand.doCommandPlugPostStatusLocal(inetAddress, plugStatus, deviceBssid, isMeshDevice);
                if (suc)
                {
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
                IEspStatusRemote remoteStatus = (IEspStatusRemote)status;
                IEspCommandRemotePostStatusLocal remoteCommand = new EspCommandRemotePostStatusLocal();
//                suc = remoteCommand.doCommandRemotePostStatusLocal(inetAddress, remoteStatus, deviceBssid, router);
                suc = remoteCommand.doCommandRemotePostStatusLocal(inetAddress, remoteStatus, deviceBssid, isMeshDevice);
                if (suc)
                {
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
                IEspStatusPlugs plugsStatus = (IEspStatusPlugs)status;
                IEspCommandPlugsPostStatusLocal plugsCommand = new EspCommandPlugsPostStatusLocal();
//                suc = plugsCommand.doCommandPlugsPostStatusLocal(inetAddress, plugsStatus, deviceBssid, router);
                suc = plugsCommand.doCommandPlugsPostStatusLocal(inetAddress, plugsStatus, deviceBssid, isMeshDevice);
                if (suc)
                {
                    if (device instanceof IEspDevicePlugs)
                    {
                        IEspDevicePlugs devicePlugs = (IEspDevicePlugs)device;
                        for (IAperture postAperture : plugsStatus.getStatusApertureList())
                        {
                            devicePlugs.updateApertureOnOff(postAperture);
                        }
                    }
                    else if (device instanceof IEspDeviceSSS)
                    {
                        IEspStatusPlugs statusPlugs = (IEspStatusPlugs)((IEspDeviceSSS)device).getDeviceStatus();
                        for (IAperture postAperture : plugsStatus.getStatusApertureList())
                        {
                            statusPlugs.updateApertureOnOff(postAperture);
                        }
                    }
                }
                return suc;
            case ROOT:
                doRootRouterCommandLocal((IEspDeviceRoot)device, status);
                return true;
            case NEW:
                break;
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public boolean doActionDevicePostStatusLocal(IEspDevice device, IEspDeviceStatus status)
    {
        return __doActionDevicePostStatusLocal(device, status, false);
    }
    
    @Override
    public boolean doActionDevicePostStatusLocal(IEspDevice device, IEspDeviceStatus status, boolean isBroadcast)
    {
        return __doActionDevicePostStatusLocal(device, status, isBroadcast);
    }
    
    private void doRootRouterCommandLocal(IEspDeviceRoot device, IEspDeviceStatus status)
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
            String router = RouterUtil.getBroadcastRouter(device.getRouter());
            
            if (status instanceof IEspStatusLight)
            {
                IEspStatusLight lightStatus = (IEspStatusLight)status;
                IEspCommandLightPostStatusLocal lightCommand = new EspCommandLightPostStatusLocal();
                lightCommand.doCommandLightPostStatusLocal(inetAddress, lightStatus, bssid, router);
            }
            else if (status instanceof IEspStatusPlug)
            {
                IEspStatusPlug plugStatus = (IEspStatusPlug)status;
                IEspCommandPlugPostStatusLocal plugCommand = new EspCommandPlugPostStatusLocal();
                plugCommand.doCommandPlugPostStatusLocal(inetAddress, plugStatus, bssid, router);
            }
            else if (status instanceof IEspStatusRemote)
            {
                IEspStatusRemote remoteStatus = (IEspStatusRemote)status;
                IEspCommandRemotePostStatusLocal remoteCommand = new EspCommandRemotePostStatusLocal();
                remoteCommand.doCommandRemotePostStatusLocal(inetAddress, remoteStatus, bssid, router);
            }
        }
        
    }
}
