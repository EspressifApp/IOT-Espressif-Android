package com.espressif.iot.base.net.longsocket;

import org.json.JSONObject;

/**
 * The interface of Esp long socket which is used to improve the effective of the code.
 * 
 * @author afunx
 *
 */
public interface IEspLongSocket
{
    // server port to be connected
    static final int SERVER_PORT = 80;
    
    // client port to be connected
    static final int DEVICE_PORT = 80;
    
    // connect to device timeout in milliseconds
    static final int DEVICE_CONNECTION_TIMEOUT = 5000;
    
    // connect to server timeout in milliseconds
    static final int SERVER_CONNECTION_TIMEOUT = 5000;
    
    // connect to device or server timeout in milliseconds
    static final int CONNECT_TIMEOUT = 5000;
    
    // when connecting fail, sleep some time before retry
    static final int CONNECT_FAIL_SLEEP_TIEMOUT = 1000;
    
    // read timeout in milliseconds
    static final int SO_TIMEOUT = 3000;
    
    // retry time of socket connection 
    // when the connect fail firstly,
    // or the socket is broken up when connecting
    static final int SO_CONNECT_RETRY = 2;
    
    interface EspLongSocketDisconnected
    {
        /**
         * callback when the long socket is disconnected
         */
        void onEspLongSocketDisconnected();
    }
    
    /**
     * set the listener when EspLongSocket is disconnected
     * @param listener when EspLongSocket is disconnected
     */
    void setEspLongSocketDisconnectedListener(EspLongSocketDisconnected listener);
    
    /**
     * set the target of the long socket
     * 
     * @param targetHost the host of the target
     * @param targetPort the port of the target
     * @param maxTaskSize the max task size, if the task is more than the maxTaskSize,
     * it will give up some tasks to prevent the situation that the consumer thread is more
     * slow than the producer thread so that the memory will 
     */
    void setTarget(String targetHost, int targetPort, int maxTaskSize);
    
    /**
     * connect to the target,
     * it will try {@link #SO_CONNECT_RETRY} times firstly,
     * after first socket is built up, it will retry connecting forever
     * 
     * @return whether connect suc
     */
    boolean connect();
    
    /**
     * add the request to be sent,
     * @param request the request to be sent
     */
    void addRequest(String request);
    
    /**
     * close the long socket immediately
     */
    void close();
    
    /**
     * it won't close the socket immediately.
     * it will be closed until the last task is executed.
     */
    void finish();
    
    /**
     * get the last response of JSONObject,
     * if the EspLongSocket hasn't been closed, it will return the last Response immediately.
     * if the EspLongSocket has been closed, it will read successively until the last response is read.
     * 
     * @return the last response of JSONObject
     */
    JSONObject getLastResponse();
}
