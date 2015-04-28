package com.espressif.iot.model.device;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.EspStatusPlug;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.EspStatusRemote;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.user.builder.EspSSSUser;
import com.espressif.iot.util.RandomUtil;

/**
 * Support SoftAP and Station device
 *
 */
public class EspDeviceSSS extends EspDevice implements IEspDeviceSSS
{
    private IOTAddress mIOTAddress;
    
    private IEspDeviceStatus mStatus;
    
    public EspDeviceSSS(IOTAddress iotAddress)
    {
        setKey(RandomUtil.randomString(20));
        init(iotAddress);
    }
    
    private void init(IOTAddress iotAddress)
    {
        mIOTAddress = iotAddress;
        
        setDeviceState(EspDeviceState.LOCAL);
        setName(mIOTAddress.getSSID());
        setBssid(mIOTAddress.getBSSID());
        setInetAddress(mIOTAddress.getInetAddress());
        setDeviceType(mIOTAddress.getDeviceTypeEnum());
        setRouter(mIOTAddress.getRouter());
        setIsMeshDevice(iotAddress.isMeshDevice());
        
        switch(mIOTAddress.getDeviceTypeEnum())
        {
            case LIGHT:
                mStatus = new EspStatusLight();
                break;
            case PLUG:
                mStatus = new EspStatusPlug();
                break;
            case REMOTE:
                mStatus = new EspStatusRemote();
                break;
            case PLUGS:
                mStatus = new EspStatusPlugs();
                break;
            case ROOT:
                break;
            case FLAMMABLE:
                break;
            case HUMITURE:
                break;
            case VOLTAGE:
                break;
            case NEW:
                break;
        }
    }
    
    @Override
    public void setIOTAddress(IOTAddress iotAddress)
    {
        mIOTAddress = iotAddress;
        
        init(iotAddress);
    }
    
    @Override
    public IOTAddress getIOTAddress()
    {
        return mIOTAddress;
    }

    @Override
    public IEspDeviceStatus getDeviceStatus()
    {
        return mStatus;
    }
    
    @Override
    public List<IEspDeviceTreeElement> getDeviceTreeElementList()
    {
        List<IEspDevice> devices = new ArrayList<IEspDevice>();
        devices.add(this);
        devices.addAll(EspSSSUser.getInstance().getDeviceList());
        return getDeviceTreeElementList(devices);
    }
    
    @Override
    public boolean equals(Object o)
    {
        // check the type
        if (o == null || !(o instanceof IEspDevice))
        {
            return false;
        }
        IEspDevice other = (IEspDevice)o;
        return other.getBssid().equals(mBssid);
    }
}
