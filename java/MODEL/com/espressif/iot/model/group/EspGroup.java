package com.espressif.iot.model.group;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.group.IEspGroup;

public class EspGroup implements IEspGroup {
    private long mId;
    private String mName;
    private int mState = 0;
    private Type mType = Type.COMMON;

    private List<IEspDevice> mDeviceList;

    public EspGroup() {
        mDeviceList = new ArrayList<IEspDevice>();
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public void setId(long id) {
        mId = id;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    @Override
    public void addDevice(IEspDevice device) {
        if (!mDeviceList.contains(device)) {
            mDeviceList.add(device);
        }
    }

    @Override
    public void removeDevice(IEspDevice device) {
        if (mDeviceList.contains(device)) {
            mDeviceList.remove(device);
        }
    }

    @Override
    public List<IEspDevice> getDeviceList() {
        return mDeviceList;
    }

    @Override
    public List<String> generateDeviceBssidList() {
        List<String> bssids = new ArrayList<String>();
        for (IEspDevice device : mDeviceList) {
            bssids.add(device.getBssid());
        }

        return bssids;
    }

    @Override
    public void setState(int stateValue) {
        mState = stateValue;
    }

    @Override
    public int getStateValue() {
        return mState;
    }

    @Override
    public void addState(State state) {
        mState |= 1 << state.ordinal();
    }

    @Override
    public void clearState(State state) {
        mState &= (~(1 << state.ordinal()));
    }

    @Override
    public void clearAllState() {
        mState = 0;
    }

    private boolean isStateXXX(State state) {
        return (mState & (1 << state.ordinal())) != 0;
    }

    @Override
    public boolean isStateDeleted() {
        return isStateXXX(State.DELETED);
    }

    @Override
    public boolean isStateRenamed() {
        return isStateXXX(State.RENAMED);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EspGroup) {
            EspGroup group = (EspGroup)o;
            return getId() == group.getId();
        }

        return super.equals(o);
    }

    @Override
    public void setType(Type type) {
        mType = type;
    }

    @Override
    public void setType(int typeOrdinal) {
        mType = Type.values()[typeOrdinal];
    }

    @Override
    public Type getType() {
        return mType;
    }
}
