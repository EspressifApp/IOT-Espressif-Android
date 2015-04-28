package com.espressif.iot.type.device.status;

import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspStatusRemote extends IEspDeviceStatus
{
    /**
     * Get the address of the Remote(the address of Remote Command)
     * 
     * @return the address of the Remote(the address of Remote Command)
     */
    int getAddress();
    
    /**
     * Set the address of the Remote(the address of Remote Command)
     * 
     * @param addr the address of the Remote(the address of Remote Command)
     */
    void setAddress(int addr);
    
    /**
     * Get the command of the Remote
     * 
     * @return the command of the Remote
     */
    int getCommand();
    
    /**
     * Set the command of the Remote
     * 
     * @param cmd the command of the Remote
     */
    void setCommand(int cmd);
    
    /**
     * Get the repeat time of the Remote executed
     * 
     * @return the repeat time of the Remote executed
     */
    int getRepeat();
    
    /**
     * Set the repeat time of the Remote executed
     * 
     * @param rep the repeat time of the Remote executed
     */
    void setRepeat(int rep);
}
