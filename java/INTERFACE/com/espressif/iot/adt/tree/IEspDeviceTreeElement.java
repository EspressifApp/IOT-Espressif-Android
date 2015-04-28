package com.espressif.iot.adt.tree;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.object.IEspObject;

public interface IEspDeviceTreeElement extends IEspObject
{
    /**
     * Get the element id
     * 
     * @return the element id
     */
    String getId();
    
    /**
     * Get the title of the element
     * 
     * @return the title of the element
     */
    String getTitle();
    
    /**
     * Get whether the element has parent
     * 
     * @return whether the element has parent
     */
    boolean isHasParent();
    
    /**
     * Get whether the element has child
     * 
     * @return whether the element has child
     */
    boolean isHasChild();
    
    /**
     * Get the element parent id
     * 
     * @return the element parent id
     */
    String getParentId();
    
    /**
     * Get the relative level of the element
     * 
     * @return the relative level of the element
     */
    int getLevel();
    
    /**
     * Set the relative level of the element
     * 
     * @param referLevel the refer level used to calculate relative level
     */
    void setRelativeLevel(int referLevel);
    
    /**
     * Get whether the element is fold
     * 
     * @return whether the element is fold
     */
    boolean isFold();
    
    /**
     * Set whether the element is fold
     * 
     * @param fold whether the element is fold
     */
    void setFold(boolean fold);

    /**
     * Get the current device belong to the device tree element
     * @return the current device belong to the device tree element
     */
    IEspDevice getCurrentDevice();
}
