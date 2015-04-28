package com.espressif.iot.base.net.rest.mesh;

import org.json.JSONObject;

public interface IEspPureSocketResponse
{
    /**
     * Get the response status
     * @return the response status
     */
    public int getStatus();
    
    /**
     * Check whether the response exist status
     * @return whether the response exist status
     */
    public boolean isStatusExist();
    
    /**
     * Get the response status
     * @return the response status
     */
    public long getNonce();
    
    /**
     * Check whether the response exist nonce
     * @return whether the response exist nonce
     */
    public boolean isNonceExist();
    
    /**
     * Check whether the response is valid
     * @return whether the response is valid
     */
    public boolean isValid();
    
    /**
     * Get the json of the response
     * @return the json of the response
     */
    public JSONObject getJson();
}
