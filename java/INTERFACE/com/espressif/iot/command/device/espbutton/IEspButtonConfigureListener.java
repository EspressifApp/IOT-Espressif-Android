package com.espressif.iot.command.device.espbutton;

import java.util.Queue;

import com.espressif.iot.device.IEspDevice;

public interface IEspButtonConfigureListener
{
    public static final String PAIR_PERMIT = "pair_permit";
    public static final String PAIR_FORBID = "pair_forbid";
    public static final String PAIR_CONTINUE = "pair_continue";
    public static final String PAIR_OVER = "pair_over";
    
    /**
     * Set the the task over
     * 
     */
    void interrupt();
    
    /**
     * Get the task is over or not
     * 
     * @return
     */
    boolean isInterrupted();
    
    /**
     * Broadcast pair configure
     * 
     * @param rootDevice
     * @param result
     */
    void onBroadcastComplete(IEspDevice rootDevice, boolean result);
    
    /**
     * Receive pair request from device
     * 
     * @param deviceMac
     * @param buttonMac
     * @param queue
     */
    void receivePairRequest(String deviceMac, String buttonMac, Queue<String> queue);
    
    /**
     * Receive pair result from device
     * 
     * @param deviceMac
     * @param success
     * @param queue
     */
    void receivePairResult(String deviceMac, boolean success, Queue<String> queue);
}
