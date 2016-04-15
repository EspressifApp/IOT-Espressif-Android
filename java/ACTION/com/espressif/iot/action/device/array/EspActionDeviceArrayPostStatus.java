package com.espressif.iot.action.device.array;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.action.device.common.EspActionDevicePostStatusInternet;
import com.espressif.iot.action.device.common.EspActionDevicePostStatusLocal;
import com.espressif.iot.action.device.common.IEspActionDevicePostStatusInternet;
import com.espressif.iot.action.device.common.IEspActionDevicePostStatusLocal;
import com.espressif.iot.command.device.light.EspCommandLightPostStatusInternet;
import com.espressif.iot.command.device.light.EspCommandLightPostStatusLocal;
import com.espressif.iot.command.device.light.IEspCommandLightPostStatusInternet;
import com.espressif.iot.command.device.light.IEspCommandLightPostStatusLocal;
import com.espressif.iot.command.device.plug.EspCommandPlugPostStatusInternet;
import com.espressif.iot.command.device.plug.EspCommandPlugPostStatusLocal;
import com.espressif.iot.command.device.plug.IEspCommandPlugPostStatusInternet;
import com.espressif.iot.command.device.plug.IEspCommandPlugPostStatusLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusPlug;
import com.espressif.iot.user.builder.BEspUser;

public class EspActionDeviceArrayPostStatus implements IEspActionDeviceArrayPostStatus
{
    private class NetworkGroup
    {
        String rootDeviceKey;
        
        IEspDeviceState state;
        
        StringBuilder bssids;
        
        InetAddress inetAddress;
        
        List<String> postBssidList;
        
        public NetworkGroup()
        {
            state = new EspDeviceState();
            bssids = new StringBuilder();
            postBssidList = new ArrayList<String>();
        }
    }
    
    @Override
    public void doActionDeviceArrayPostStatus(IEspDeviceArray deviceArray, IEspDeviceStatus status)
    {
        List<IEspDevice> devices = deviceArray.getDeviceList();
        List<IEspDevice> nonMeshDevices = new ArrayList<IEspDevice>();
        List<IEspDevice> meshDevices = new ArrayList<IEspDevice>();
        for (IEspDevice deviceInArray : devices)
        {
            if (deviceInArray.getIsMeshDevice())
            {
                meshDevices.add(deviceInArray);
            }
            else
            {
                nonMeshDevices.add(deviceInArray);
            }
        }
        
        processNonMeshDevices(nonMeshDevices, status);
        
        processMeshDevices(meshDevices, status);
    }
    
    /**
     * Post status one by one
     * 
     * @param nonMeshDevices
     * @param status
     */
    private void processNonMeshDevices(List<IEspDevice> nonMeshDevices, IEspDeviceStatus status)
    {
        for (IEspDevice nonMeshDevice : nonMeshDevices)
        {
            if (nonMeshDevice.getDeviceState().isStateLocal())
            {
                IEspActionDevicePostStatusLocal actionLocal = new EspActionDevicePostStatusLocal();
                actionLocal.doActionDevicePostStatusLocal(nonMeshDevice, status);
            }
            else
            {
                IEspActionDevicePostStatusInternet actionInternet = new EspActionDevicePostStatusInternet();
                actionInternet.doActionDevicePostStatusInternet(nonMeshDevice, status);
            }
            
        }
    }
    
    /**
     * Post status by Multicast
     * 
     * @param meshDevices
     * @param status
     */
    private void processMeshDevices(List<IEspDevice> meshDevices, IEspDeviceStatus status)
    {
        if (meshDevices.size() > 0)
        {
            List<NetworkGroup> deviceNetworkGroups = new ArrayList<NetworkGroup>();
            groupingNetwork(deviceNetworkGroups);
            
            for (IEspDevice meshDevice : meshDevices)
            {
                for (NetworkGroup netGroup : deviceNetworkGroups)
                {
                    if (netGroup.bssids.toString().contains(meshDevice.getBssid()))
                    {
                        netGroup.postBssidList.add(meshDevice.getBssid());
                        break;
                    }
                }
            }
            for (NetworkGroup netGroup : deviceNetworkGroups)
            {
                if (netGroup.postBssidList.size() > 0)
                {
                    if (netGroup.state.isStateLocal())
                    {
                        postMulticastLocal(meshDevices.get(0).getDeviceType(), netGroup, status);
                    }
                    else
                    {
                        postMulticastInternet(meshDevices.get(0).getDeviceType(), netGroup, status);
                    }
                }
            }
        }
    }
    
    /**
     * Grouping user devices by network environment
     * 
     * @param networkGroups
     */
    private void groupingNetwork(List<NetworkGroup> networkGroups)
    {
        List<IEspDevice> userDevices = BEspUser.getBuilder().getInstance().getAllDeviceList();
        
        // Root devices
        for (int i = 0; i < userDevices.size(); i++)
        {
            IEspDevice device = userDevices.get(i);
            String rootBssid = device.getRootDeviceBssid();
            String bssid = device.getBssid();
            if (rootBssid.equals(bssid))
            {
                NetworkGroup networkGroup = new NetworkGroup();
                networkGroup.rootDeviceKey = device.getKey();
                networkGroup.bssids.append(bssid).append(',');
                addDeviceState(networkGroup, device);
                networkGroups.add(networkGroup);
                userDevices.remove(i--);
                continue;
            }
        }
        
        // Devices under Root device
        for (int i = 0; i < userDevices.size(); i++)
        {
            IEspDevice device = userDevices.get(i);
            String rootBssid = device.getRootDeviceBssid();
            String bssid = device.getBssid();
            if (!rootBssid.equals(bssid))
            {
                for (NetworkGroup ng : networkGroups)
                {
                    if (ng.bssids.toString().contains(rootBssid))
                    {
                        ng.bssids.append(bssid).append(',');
                        addDeviceState(ng, device);
                        break;
                    }
                }
            }
        }
        
        // Uncontrollable devices
        for (int i = 0; i < userDevices.size(); i++)
        {
            IEspDeviceState state = userDevices.get(i).getDeviceState();
            if (!state.isStateLocal() && !state.isStateInternet())
            {
                userDevices.remove(i--);
            }
        }
        
        // Devices under other user's root device
        for (int i = 0; i < userDevices.size(); i++)
        {
            IEspDevice device = userDevices.get(i);
            String bssid = device.getBssid();
            NetworkGroup networkGroup = new NetworkGroup();
            networkGroup.rootDeviceKey = device.getKey();
            networkGroup.bssids.append(bssid).append(',');
            addDeviceState(networkGroup, device);
            networkGroups.add(networkGroup);
        }
    }
    
    private void addDeviceState(NetworkGroup group, IEspDevice device)
    {
        if (device.getDeviceState().isStateLocal())
        {
            group.state.addStateLocal();
            group.inetAddress = device.getInetAddress();
        }
        if (device.getDeviceState().isStateInternet())
        {
            group.state.addStateInternet();
        }
    }
    
    private boolean postMulticastLocal(EspDeviceType deviceType, NetworkGroup netGroup, IEspDeviceStatus status)
    {
        switch (deviceType)
        {
            case PLUG:
                IEspCommandPlugPostStatusLocal plugCmd = new EspCommandPlugPostStatusLocal();
                return plugCmd.doCommandMulticastPostStatusLocal(netGroup.inetAddress,
                    (IEspStatusPlug)status,
                    netGroup.postBssidList);
            case LIGHT:
                IEspCommandLightPostStatusLocal lightCmd = new EspCommandLightPostStatusLocal();
                return lightCmd.doCommandMulticastPostStatusLocal(netGroup.inetAddress,
                    (IEspStatusLight)status,
                    netGroup.postBssidList);
                
            case FLAMMABLE:
                break;
            case HUMITURE:
                break;
            case NEW:
                break;
            case PLUGS:
                break;
            case REMOTE:
                break;
            case ROOT:
                break;
            case VOLTAGE:
                break;
            case SOUNDBOX:
                break;
        }
        
        return false;
    }
    
    private boolean postMulticastInternet(EspDeviceType deviceType, NetworkGroup netGroup, IEspDeviceStatus status)
    {
        switch (deviceType)
        {
            case PLUG:
                IEspCommandPlugPostStatusInternet plugCmd = new EspCommandPlugPostStatusInternet();
                return plugCmd.doCommandMulticastPostStatusInternet(netGroup.rootDeviceKey,
                    (IEspStatusPlug)status,
                    netGroup.postBssidList);
            case LIGHT:
                IEspCommandLightPostStatusInternet lightCmd = new EspCommandLightPostStatusInternet();
                return lightCmd.doCommandMulticastPostStatusInternet(netGroup.rootDeviceKey,
                    (IEspStatusLight)status,
                    netGroup.postBssidList);
                
            case FLAMMABLE:
                break;
            case HUMITURE:
                break;
            case NEW:
                break;
            case PLUGS:
                break;
            case REMOTE:
                break;
            case ROOT:
                break;
            case VOLTAGE:
                break;
            case SOUNDBOX:
                break;
        }
        
        return false;
    }
}
