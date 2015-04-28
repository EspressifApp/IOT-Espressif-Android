package com.espressif.iot.model.adt.tree;

import java.util.Collections;
import java.util.List;

import com.espressif.iot.adt.tree.IEspTreeComponent;
import com.espressif.iot.adt.tree.IEspTreeLeaf;

public class EspTreeLeaf<T> implements IEspTreeLeaf<T>
{
    
    private T mSelf;
    
    private IEspTreeComponent<T> mParent;
    
    public EspTreeLeaf(T element)
    {
        this.mSelf = element;
        this.mParent = null;
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
        // just ignore it for leaf don't have child
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
        // just ignore it for leaf don't have child
    }
    
    @Override
    public void detachAllChildren()
    {
        // just ignore it for leaf don't have child
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
        // return empty list for leaf don't have child
        return Collections.emptyList();
    }
    
    @Override
    public T getEspElement()
    {
        return this.mSelf;
    }
    
    @Override
    public boolean isLeaf()
    {
        return true;
    }
    
    @Override
    public boolean isRoot()
    {
        return this.mParent == null;
    }
    
}
