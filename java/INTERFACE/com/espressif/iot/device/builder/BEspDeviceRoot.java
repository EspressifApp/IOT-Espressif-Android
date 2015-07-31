package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceRoot;
import com.espressif.iot.model.device.EspDeviceRoot;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.util.RandomUtil;

public class BEspDeviceRoot implements IBEspDeviceRoot
{
    private IEspDeviceRoot localRoot = null;
    private IEspDeviceRoot internetRoot = null;
    
    private BEspDeviceRoot()
    {
        localRoot = new EspDeviceRoot();
        EspDeviceState stateLocal = new EspDeviceState();
        stateLocal.addStateLocal();
        localRoot.setDeviceState(stateLocal);
        localRoot.setDeviceType(EspDeviceType.ROOT);
        localRoot.setKey(RandomUtil.randomString(20));
        localRoot.setRouter(IEspDeviceRoot.LOCAL_ROUTER);
        localRoot.setRootDeviceBssid(localRoot.getBssid());
        localRoot.setIsMeshDevice(true);
        localRoot.setId(Long.MIN_VALUE);
        
        internetRoot = new EspDeviceRoot();
        EspDeviceState stateInternet = new EspDeviceState();
        stateInternet.addStateInternet();
        internetRoot.setDeviceState(stateInternet);
        internetRoot.setDeviceType(EspDeviceType.ROOT);
        internetRoot.setKey(RandomUtil.randomString(20));
        internetRoot.setIsMeshDevice(true);
        internetRoot.setId(Long.MIN_VALUE + 1);
    }
    
    private static class InstanceHolder
    {
        static BEspDeviceRoot instanceBuilder = new BEspDeviceRoot();
    }
    
    public static BEspDeviceRoot getBuilder()
    {
        return InstanceHolder.instanceBuilder;
    }
    
    @Override
    public IEspDeviceRoot getLocalRoot()
    {
        return localRoot;
    }

    @Override
    public IEspDeviceRoot getInternetRoot()
    {
        return internetRoot;
    }

}
