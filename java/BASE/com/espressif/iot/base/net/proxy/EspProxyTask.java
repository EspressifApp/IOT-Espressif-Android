package com.espressif.iot.base.net.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public interface EspProxyTask
{
    static final int M_PROTO_NONE = 0;
    static final int M_PROTO_HTTP = 1;
    static final int M_PROTO_JSON = 2;
    static final int M_PROTO_MQTT = 3;
    
    /**
     * set the response buffer by other
     * 
     * @param buffer the response buffer
     */
    void setResponseBuffer(byte[] buffer);
    
    /**
     * get the response buffer set by other
     * 
     * @return the response buffer set by other
     */
    byte[] getResponseBuffer();
    
    /**
     * get the request from the source socket
     */
    byte[] getRequestBytes();
    
    /**
     * get the target's timeout in seconds
     * 
     * @return the target's timeout in seconds
     */
    int getTargetTimeout();
    
    /**
     * get target's inetAddress(the root device's ip address)
     * 
     * @return the target's inetAddress(the root device's ip address)
     */
    InetAddress getTargetInetAddress();
    
    /**
     * get target's bssid(the device's bssid)
     * 
     * @return the target's bssid(the device's bssid)
     */
    String getTargetBssid();
    
    /**
     * update the timestamp for the proxy task
     */
    void updateTimestamp();
    
    /**
     * get whether the proxy task is expired
     * @return whether the proxy task is expired
     */
    boolean isExpired();
    
    /**
     * reply the response to the source socket, when the task executed finish and suc, including remove the mesh head
     * from the response
     * @throws IOException 
     */
    void replyResponse() throws IOException;
    
    /**
     * close the source socket when the task encountered some exception
     */
    void replyClose();
    
    /**
     * get whether the EspProxyTask is finished
     * 
     * @return whether the EspProxyTask is finished
     */
    boolean isFinished();
    
    /**
     * set the EspProxyTask finished
     */
    void setFinished(boolean isFinished);
    
    /**
     * set whether the request is valid
     * 
     * @param isRequestValid whether the request is valid
     */
    void setRequestValid(boolean isRequestValid);
    
    /**
     * get whether the request is valid
     * 
     * @return whether the request is valid
     */
    boolean isRequestValid();
    
    /**
     * set whether the response is valid
     * 
     * @param isResponseValid whether the response is valid
     */
    void setResponseVaild(boolean isResponseValid);
    
    /**
     * get whether the response is valid
     * 
     * @return whether the response is valid
     */
    boolean isResponseValid();
    
    /**
     * Just read response, forbid sending request
     * 
     * @return
     */
    boolean isReadOnlyTask();
    
    /**
     * Whether reply response is required
     * 
     * @return
     */
    boolean isReplyRequired();
    
    /**
     * Get proto type
     * 
     * @return
     */
    int getProtoType();
    
    /**
     * Get long socket task serial
     * 
     * @return
     */
    int getLongSocketSerial();
    
    /**
     * Get task timeout
     * 
     * @return
     */
    int getTaskTimeout();
    
    /**
     * get group bssid list
     * 
     * @return group bssid list
     */
    List<String> getGroupBssidList();
    
    /**
     * set group bssid list
     * 
     * @param groupBssidList the group bssid list
     */
    void setGroupBssidList(List<String> groupBssidList);
}
