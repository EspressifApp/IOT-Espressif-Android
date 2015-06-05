package com.espressif.iot.model.adt.tree;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.device.IEspDevice;

public class EspDeviceTreeElement implements IEspDeviceTreeElement
{
    public EspDeviceTreeElement(IEspDevice device, String parentDeviceKey, boolean hasParent, boolean hasChild,
        int level)
    {
        this.mDevice = device;
        this.mHasParent = hasParent;
        this.mHasChild = hasChild;
        this.mLevel = level;
        this.mId = device.getKey();
        this.mParentId = parentDeviceKey;
    }
    
    private IEspDevice mDevice;
    
    // current node id
    private String mId = null;
    
    // current node title
    private String mTitle = null;
    
    // whether has parent node
    private boolean mHasParent = false;
    
    // whether has child
    private boolean mHasChild = false;
    
    // parent node id
    private String mParentId = null;
    
    // current level
    private int mLevel = -1;
    
    // whether opened state
    private boolean mFold = false;
    
    @Override
    public String getId()
    {
        return mId;
    }
    
    @Override
    public String getTitle()
    {
        return this.mDevice.getName();
    }
    
    @Override
    public boolean isHasParent()
    {
        return mHasParent;
    }
    
    @Override
    public boolean isHasChild()
    {
        return mHasChild;
    }
    
    @Override
    public String getParentId()
    {
        return mParentId;
    }
    
    @Override
    public int getLevel()
    {
        return mLevel;
    }
    
    @Override
    public void setRelativeLevel(int referLevel)
    {
        // for level 1,2,3... are visible
        this.mLevel = mLevel - referLevel + 1;
    }
    
    @Override
    public boolean isFold()
    {
        return mFold;
    }
    
    @Override
    public void setFold(boolean fold)
    {
        this.mFold = fold;
    }
    
    @Override
    public IEspDevice getCurrentDevice()
    {
        return mDevice;
    }
    
    @Override
    public boolean equals(Object o)
    {
        // check the type
        if (o == null || !(o instanceof IEspDeviceTreeElement))
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        IEspDeviceTreeElement other = (IEspDeviceTreeElement)o;
        return other.getId().equals(this.getId());
    }
    
    @Override
    public String toString()
    {
        return "id:" + this.mId + "-level:" + this.mLevel + "-title:" + this.mTitle + "-fold:" + this.mFold
            + "-hasChidl:" + this.mHasChild + "-hasParent:" + this.mHasParent + "-parentId:" + this.mParentId;
    }
    
}
