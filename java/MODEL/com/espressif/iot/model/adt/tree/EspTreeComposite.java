package com.espressif.iot.model.adt.tree;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.adt.tree.IEspTreeComponent;
import com.espressif.iot.adt.tree.IEspTreeComposite;

public class EspTreeComposite<T> implements IEspTreeComposite<T>
{
    private T mSelf;
    
    private IEspTreeComponent<T> mParent;
    
    private List<IEspTreeComponent<T>> mChildList;
    
    public EspTreeComposite(T element)
    {
        this.mSelf = element;
        this.mParent = null;
        this.mChildList = new ArrayList<IEspTreeComponent<T>>();
    }
    
    @Override
    public void attachToParent(IEspTreeComponent<T> parent)
    {
        this.setParent(parent);
        parent.attachChild(this);
    }
    
    @Override
    public void attachChild(IEspTreeComponent<T> child)
    {
        this.mChildList.add(child);
        child.setParent(this);
    }
    
    @Override
    public void detachFromParent(IEspTreeComponent<T> parent)
    {
        this.setParent(null);
        parent.detachChild(this);
    }
    
    @Override
    public void detachChild(IEspTreeComponent<T> child)
    {
        for (IEspTreeComponent<T> childInList : this.mChildList)
        {
            if (childInList.equals(child))
            {
                childInList.setParent(null);
                this.mChildList.remove(childInList);
                break;
            }
        }
    }
    
    @Override
    public void detachAllChildren()
    {
        for (IEspTreeComponent<T> childInList : this.mChildList)
        {
            childInList.setParent(null);
        }
        this.mChildList.clear();
    }
    
    @Override
    public IEspTreeComponent<T> getParent()
    {
        return this.mParent;
    }
    
    @Override
    public void setParent(IEspTreeComponent<T> parent)
    {
        this.mParent = parent;
    }
    
    @Override
    public List<IEspTreeComponent<T>> getChildrenList()
    {
        return this.mChildList;
    }
    
    @Override
    public T getEspElement()
    {
        return this.mSelf;
    }
    
    @Override
    public boolean isLeaf()
    {
        return false;
    }
    
    @Override
    public boolean isRoot()
    {
        return this.mParent == null;
    }
    
}
