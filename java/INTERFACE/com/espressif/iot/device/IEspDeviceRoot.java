package com.espressif.iot.device;

import java.util.List;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;

public interface IEspDeviceRoot extends IEspDevice
{
    /**
     * Get all tree element of the root router
     * @return
     */
    List<IEspDeviceTreeElement> getDeviceTreeElementList();
}
