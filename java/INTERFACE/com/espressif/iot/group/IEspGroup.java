package com.espressif.iot.group;

import java.util.List;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.object.IEspObject;

public interface IEspGroup extends IEspObject
{
    public static final long ID_NEW = -1;
    
    public static enum State
    {
        NORMAL, DELETED, RENAMED;
    }
    
    /**
     * 
     * @return the id of group
     */
    long getId();
    
    /**
     * Set group id
     * 
     * @param id
     */
    void setId(long id);
    
    /**
     * 
     * @return the name of group
     */
    String getName();
    
    /**
     * Set group name
     * 
     * @param name
     */
    void setName(String name);
    
    /**
     * Add device in the group
     * 
     * @param device
     */
    void addDevice(IEspDevice device);
    
    /**
     * Remove device from group
     * 
     * @param device
     */
    void removeDevice(IEspDevice device);
    
    /**
     * 
     * @return device list of the group
     */
    List<IEspDevice> getDeviceList();
    
    /**
     * Generate the list of devices' BSSID
     * 
     * @return BSSID list
     */
    List<String> generateDeviceBssidList();
    
    /**
     * Set state
     */
    void setState(int stateValue);
    
    /**
     * 
     * @return state value
     */
    int getStateValue();
    
    /**
     * Add state
     * 
     * @param state
     */
    void addState(State state);
    
    /**
     * clear state
     * 
     * @param state
     */
    void clearState(State state);
    
    /**
     * Clear all state
     */
    void clearAllState();
    
    /**
     * 
     * @return is DELETED state or not
     */
    boolean isStateDeleted();
    
    /**
     * 
     * @return is RENAMED state or not
     */
    boolean isStateRenamed();
}
