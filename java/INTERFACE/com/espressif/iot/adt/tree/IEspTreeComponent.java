package com.espressif.iot.adt.tree;

import java.util.List;

import com.espressif.iot.object.IEspObject;

public interface IEspTreeComponent<T> extends IEspObject
{
    /**
     * attach to parent
     * 
     * @param parent the parent to be attached
     */
    void attachToParent(IEspTreeComponent<T> parent);
    
    /**
     * attach child to the parent
     * 
     * @param child the child to be attached
     */
    void attachChild(IEspTreeComponent<T> child);
    
    /**
     * detach from parent
     * 
     * @param parent the parent to be detached
     */
    void detachFromParent(IEspTreeComponent<T> parent);
    
    /**
     * detach one of its child
     * 
     * @param child the child to be detached
     */
    void detachChild(IEspTreeComponent<T> child);
    
    /**
     * detach all of its children
     */
    void detachAllChildren();
    
    /**
     * get the parent
     * 
     * @return the parent or null(if the element is root)
     */
    IEspTreeComponent<T> getParent();
    
    /**
     * set the parent
     * 
     * @param parent the parent to be set
     */
    void setParent(IEspTreeComponent<T> parent);
    
    /**
     * get the children list
     * 
     * @return the children list
     */
    List<IEspTreeComponent<T>> getChildrenList();
    
    /**
     * get the element
     * 
     * @return the element
     */
    T getEspElement();
    
    /**
     * check whether the component is leaf
     * 
     * @return whether the component is leaf
     */
    boolean isLeaf();
    
    /**
     * check whether the component is root
     * 
     * @return whether the component is root
     */
    boolean isRoot();
    
}
