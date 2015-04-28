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
        localRoot.setDeviceState(EspDeviceState.LOCAL);
        localRoot.setDeviceType(EspDeviceType.ROOT);
        localRoot.setKey(RandomUtil.randomString(20));
        localRoot.setRouter(IEspDeviceRoot.LOCAL_ROUTER);
        localRoot.setIsMeshDevice(true);
        
        internetRoot = new EspDeviceRoot();
        internetRoot.setDeviceState(EspDeviceState.INTERNET);
        internetRoot.setDeviceType(EspDeviceType.ROOT);
        internetRoot.setKey(RandomUtil.randomString(20));
        internetRoot.setIsMeshDevice(true);
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
