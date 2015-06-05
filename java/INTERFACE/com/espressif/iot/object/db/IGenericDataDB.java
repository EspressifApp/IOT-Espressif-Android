package com.espressif.iot.object.db;

public interface IGenericDataDB
{
    /**
     * get the id of the generic data
     * @return the id of the generic data
     */
    public Long getId();
    
    /**
     * set the id of the generic data
     * @param id the id of the generic data
     */
    public void setId(Long id);
    
    /**
     * get the device id of the generic data
     * @return the device id of the generic data
     */
    public long getDeviceId();
    
    /**
     * set the device id of the generic data
     * @param deviceId the device id of the generic data
     */
    public void setDeviceId(long deviceId);
    
    /**
     * get the UTC timestamp when the data is pushed to the server
     * @return the UTC timestamp when the data is pushed to the server
     */
    public long getTimestamp();
    
    /**
     * set the UTC timestamp when the data is pushed to the server
     * @param timestamp the UTC timestamp when the data is pushed to the server
     */
    public void setTimestamp(long timestamp);
    
    /**
     * get the data of the generic data
     * @return the data of the generic data
     */
    public String getData();
    
    /**
     * set the data of the generic data
     * @param data the data of the generic data
     */
    public void setData(String data);
    
    /**
     * get the directory id of the generic data
     * @return the directory id of the generic data
     */
    public long getDirectoryId();
    
    /**
     * set the directory id of the generic data
     * @param directoryId the directory id of the generic data
     */
    public void setDirectoryId(long directoryId);
}
