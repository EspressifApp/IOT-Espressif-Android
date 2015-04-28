package com.espressif.iot.type.device;

import com.espressif.iot.object.IEspObject;

public interface IEspDeviceState extends IEspObject
{
    public enum Enum
    {
        NEW, LOCAL, INTERNET, OFFLINE, CONFIGURING, UPGRADING_LOCAL, UPGRADING_INTERNET, ACTIVATING, DELETED, RENAMED,CLEAR
    }
    
    IEspDeviceState.Enum getDeviceState();
    
    int getStateValue();
    
    void setStateValue(int state);
    
    void addStateNew();
    
    boolean isStateNew();
    
    void clearStateNew();
    
    void addStateLocal();
    
    void clearStateLocal();
    
    boolean isStateLocal();
    
    void addStateInternet();
    
    void clearStateInternet();
    
    boolean isStateInternet();
    
    void addStateOffline();
    
    void clearStateOffline();
    
    boolean isStateOffline();
    
    void addStateConfiguring();
    
    void clearStateConfiguring();
    
    boolean isStateConfiguring();
    
    void addStateActivating();
    
    void clearStateActivating();
    
    boolean isStateActivating();
    
    void addStateUpgradingLocal();
    
    void clearStateUpgradingLocal();
    
    boolean isStateUpgradingLocal();
    
    void addStateUpgradingInternet();
    
    void clearStateUpgradingInternet();
    
    boolean isStateUpgradingInternet();
    
    void addStateDeleted();
    
    void clearStateDeleted();
    
    boolean isStateDeleted();
    
    void addStateRenamed();
    
    void clearStateRenamed();
    
    boolean isStateRenamed();
    
    void clearState();
    
    boolean isStateClear();
}
