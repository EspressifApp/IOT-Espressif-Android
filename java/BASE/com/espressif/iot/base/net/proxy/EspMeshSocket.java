package com.espressif.iot.base.net.proxy;

import java.net.InetAddress;
import java.util.List;

public interface EspMeshSocket
{
    
    InetAddress getInetAddress();
    
    /**
     * get the refresh proxy task list which hasn't been sent
     * 
     * @return the refresh proxy task list which hasn't been sent
     */
    List<EspProxyTask> getRefreshProxyTaskList();
    
    /**
     * offer the new proxy task
     * 
     * @param proxyTask the new proxy task
     */
    void offer(EspProxyTask proxyTask);
    
    /**
     * close the EspMeshSocket half, don't accept more new request
     */
    void halfClose();
    
    /**
     * close the EspMeshSocket
     */
    void close();
    
    /**
     * check whether the EspSocket is expired
     * 
     * @return whether the EspSocket is expired
     */
    boolean isExpired();
    
    /**
     * check the proxy tasks' states and proceed them 
     */
    void checkProxyTaskStateAndProc();
    
    /**
     * get whether the EspMeshSocket is connected to remote device
     * 
     * @return whether the EspMeshSocket is connected to remote device
     */
    boolean isConnected();
    
    /**
     * get whether the EspMeshSocket is closed
     * 
     * @return whether the EspMeshSocket is closed
     */
    boolean isClosed();
    
}
