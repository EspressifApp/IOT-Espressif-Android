package com.espressif.iot.device;

import java.util.concurrent.Future;

import com.espressif.iot.action.device.common.IEspActionDeviceConfigureLocal;
import com.espressif.iot.action.device.configure.IEspActionDeviceConfigureActivateInternet;

/**
 * the device is configured already and will be activated on Server.
 * 
 * @author afunx
 * 
 */
public interface IEspDeviceConfigure extends IEspDevice, IEspActionDeviceConfigureActivateInternet,
    IEspActionDeviceConfigureLocal
{
    /**
     * @param mayInterruptIfRunning {@code true} if the thread executing this task should be interrupted; otherwise,
     *            in-progress tasks are allowed to complete
     * @return {@code false} if the task could not be cancelled, typically because it has already completed normally;
     *         {@code true} otherwise *
     */
    boolean cancel(boolean mayInterruptIfRunning);
    
    /**
     * set the future which is used to cancel activating task
     * 
     * @param future the future which is used to cancel activating task
     */
    void setFuture(Future<?> future);
    
}
