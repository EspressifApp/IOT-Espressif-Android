package com.espressif.iot.base.net.rest2;

public interface IEspHttpRequest
{
    /**
     * put the header param into map
     * 
     * @param key the key of the param
     * @param value the value of the param
     */
    public void putHeaderParams(String key, String value);
    
    /**
     * Get the relative Url
     * 
     * @return the relative Url
     */
    public String getRelativeUrl();
    
    /**
     * Get the scheme of the uri
     * 
     * @return the scheme of the uri
     */
    public String getScheme();
    
    /**
     * Get the host of the uri
     * 
     * @return the host of the uri
     */
    public String getHost();
    
    /**
     * Get the content of the request(usually it is a json String)
     * 
     * @return the content of the request
     */
    public String getContent();
    
}
