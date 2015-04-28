package com.espressif.iot.base.net.rest.mesh;

public interface IEspSocketResponse
{
    /**
     * Get the content header String
     * @return the content header String
     */
    public String getContentHeaderStr();
    
    /**
     * Get the content body length
     * @return the content body length
     */
    public int getContentBodyLength();
    
    /**
     * Get the content body by String
     * @return the content body by String
     */
    public String getContentBodyStr();
    
    /**
     * Get the status
     * @return the status of int
     */
    public int getStatus();
    
    /**
     * Get the header property by its key
     * @param key the key of header property
     * @return the value mapped to the key
     */
    public String getHeaderProperty(String key);
}
