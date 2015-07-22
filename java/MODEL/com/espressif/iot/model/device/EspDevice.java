package com.espressif.iot.model.device;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.text.TextUtils;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.db.IOTDeviceDBManager;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.upgrade.IEspDeviceUpgradeInfo;
import com.espressif.iot.device.upgrade.IEspDeviceUpgradeParser;
import com.espressif.iot.model.adt.tree.EspDeviceTreeElement;
import com.espressif.iot.model.device.upgrade.EspDeviceUpgradeParser;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.device.status.IEspStatusEspnow;
import com.espressif.iot.type.device.timer.EspDeviceTimer;
import com.espressif.iot.util.RouterUtil;

public class EspDevice implements IEspDevice, Cloneable
{
    protected String mBssid;
    
    protected long mDeviceId;
    
    protected String mDeviceKey;
    
    protected boolean mIsOwner;
    
    protected String mDeviceName;
    
    protected String mRomVersion;
    
    protected String mLatestRomVersion;
    
    protected long mTimeStamp;
    
    protected long mUserId;
    
    protected boolean _isDeviceRefreshed;
    
    protected EspDeviceType mDeviceType;
    
    protected IEspDeviceState mDeviceState;
    
    protected InetAddress mInetAddress;
    
    protected boolean mIsMeshDevice;
    
    protected String mRouter;
    
    protected long mRootDeviceId;
    
    protected String mRootDeviceBssid;
    
    protected List<EspDeviceTimer> mTimerList;
    
    private List<IEspStatusEspnow> mEspnowStatusList = new ArrayList<IEspStatusEspnow>();
    
    /**
     * empty device1 is used by EspDeviceCacheHandler to distinguish between Internet unaccessible and user with no
     * devices
     */
    public static EspDevice EmptyDevice1 = new EspDevice();
    
    /**
     * empty device2 is used by EspDeviceCacheHandler to distinguish between Internet unaccessible and user's device
     * list is empty
     */
    public static EspDevice EmptyDevice2 = new EspDevice();
    
    @Override
    public void setBssid(String bssid)
    {
        this.mBssid = bssid;
    }
    
    @Override
    public String getBssid()
    {
        return this.mBssid;
    }
    
    @Override
    public long getId()
    {
        return this.mDeviceId;
    }
    
    @Override
    public void setId(long id)
    {
        this.mDeviceId = id;
    }
    
    @Override
    public String getKey()
    {
        return this.mDeviceKey;
    }
    
    @Override
    public void setKey(String key)
    {
        this.mDeviceKey = key;
    }
    
    @Override
    public boolean getIsOwner()
    {
        return this.mIsOwner;
    }
    
    @Override
    public void setIsOwner(boolean isOwner)
    {
        this.mIsOwner = isOwner;
    }
    
    @Override
    public String getName()
    {
        return this.mDeviceName;
    }
    
    @Override
    public void setName(String name)
    {
        this.mDeviceName = name;
    }
    
    @Override
    public void setRom_version(String rom_version)
    {
        this.mRomVersion = rom_version;
    }
    
    @Override
    public String getRom_version()
    {
        return this.mRomVersion;
    }
    
    @Override
    public String getLatest_rom_version()
    {
        return this.mLatestRomVersion;
    }
    
    @Override
    public void setLatest_rom_version(String latest_rom_version)
    {
        this.mLatestRomVersion = latest_rom_version;
    }
    
    @Override
    public long getTimestamp()
    {
        return this.mTimeStamp;
    }
    
    @Override
    public void setTimestamp(long timestamp)
    {
        this.mTimeStamp = timestamp;
    }
    
    @Override
    public long getUserId()
    {
        return this.mUserId;
    }
    
    @Override
    public void setUserId(long userId)
    {
        this.mUserId = userId;
    }
    
    @Override
    public void __setDeviceRefreshed()
    {
        this._isDeviceRefreshed = true;
    }
    
    @Override
    public void __clearDeviceRefreshed()
    {
        this._isDeviceRefreshed = false;
    }
    
    @Override
    public boolean __isDeviceRefreshed()
    {
        return this._isDeviceRefreshed;
    }
    
    @Override
    public void setDeviceType(EspDeviceType deviceType)
    {
        this.mDeviceType = deviceType;
    }
    
    @Override
    public EspDeviceType getDeviceType()
    {
        return this.mDeviceType;
    }
    
    @Override
    public void setDeviceState(IEspDeviceState deviceState)
    {
        this.mDeviceState = deviceState;
    }
    
    @Override
    public IEspDeviceState getDeviceState()
    {
        return this.mDeviceState;
    }
    
    @Override
    public void setInetAddress(InetAddress inetAddress)
    {
        this.mInetAddress = inetAddress;
    }
    
    @Override
    public InetAddress getInetAddress()
    {
        return this.mInetAddress;
    }
    
    @Override
    public void setIsMeshDevice(boolean isMeshDevice)
    {
        this.mIsMeshDevice = isMeshDevice;
    }
    
    @Override
    public boolean getIsMeshDevice()
    {
        return this.mIsMeshDevice;
    }
    
    @Override
    public void setRootDeviceId(long rootDeviceId)
    {
        this.mRootDeviceId = rootDeviceId;
    }
    
    @Override
    public long getRootDeviceId()
    {
        return this.mRootDeviceId;
    }
    
    @Override
    public void setRouter(String router)
    {
        this.mRouter = router;
    }
    
    @Override
    public String getRouter()
    {
        return this.mRouter;
    }
    
    @Override
    public void setRootDeviceBssid(String rootBssid)
    {
        this.mRootDeviceBssid = rootBssid;
    }
    
    @Override
    public String getRootDeviceBssid()
    {
        return this.mRootDeviceBssid;
    }
    
    @Override
    public List<EspDeviceTimer> getTimerList()
    {
        if (isSupportTimer())
        {
            if (mTimerList == null)
            {
                mTimerList = new Vector<EspDeviceTimer>();
            }
            return mTimerList;
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public boolean isSupportTimer()
    {
        switch (mDeviceType)
        {
            case PLUG:
            case PLUGS:
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public void saveInDB()
    {
        IOTDeviceDBManager iotDeviceDBManager = IOTDeviceDBManager.getInstance();
        int type = this.getDeviceType().getSerial();
        int state = this.getDeviceState().getStateValue();
        iotDeviceDBManager.insertOrReplace(mDeviceId,
            mDeviceKey,
            mBssid,
            type,
            state,
            mIsOwner,
            mDeviceName,
            mRomVersion,
            mLatestRomVersion,
            mTimeStamp,
            mUserId);
    }
    
    @Override
    public void deleteInDB()
    {
        IOTDeviceDBManager iotDeviceDBManager = IOTDeviceDBManager.getInstance();
        iotDeviceDBManager.delete(mDeviceId);
    }
    
    @Override
    public void clear()
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean isSimilar(IEspDevice device)
    {
        // check whether the device is null
        if (device == null)
        {
            return false;
        }
        // the same device is similar of course
        if (this.equals(device))
        {
            return true;
        }
        return this.getBssid().equals(device.getBssid());
    }
    
    @Override
    public boolean isStateEqual(IEspDevice device)
    {
        if (!this.equals(device))
        {
            return false;
        }
        return this.getDeviceState().getStateValue() == device.getDeviceState().getStateValue();
    }
    
    @Override
    public IEspDevice cloneDevice()
    {
        IEspDevice device = null;
        try
        {
            device = (EspDevice)this.clone();
        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
        }
        return device;
    }
    
    @Override
    public void copyDeviceState(IEspDevice device)
    {
        this.mDeviceState.setStateValue(device.getDeviceState().getStateValue());
    }
    
    @Override
    public void copyDeviceRomVersion(IEspDevice deivce)
    {
        IEspDeviceUpgradeParser parser = EspDeviceUpgradeParser.getInstance();
        IEspDeviceUpgradeInfo deviceUpgradeInfoCurrent = parser.parseUpgradeInfo(this.mRomVersion);
        IEspDeviceUpgradeInfo deviceUpgradeInfoNew = parser.parseUpgradeInfo(deivce.getRom_version());
        if (deviceUpgradeInfoNew == null)
        {
            // the rom version don't satisfy the new version format
            return;
        }
        // only the higher version could be copied to low version
        if (deviceUpgradeInfoCurrent == null
            || deviceUpgradeInfoNew.getVersionValue() > deviceUpgradeInfoCurrent.getVersionValue())
        {
            this.mRomVersion = deivce.getRom_version();
        }
        this.mLatestRomVersion = deivce.getLatest_rom_version();
    }
    
    @Override
    public void copyDeviceName(IEspDevice device)
    {
        this.mDeviceName = device.getName();
    }
    
    @Override
    public void copyInetAddress(IEspDevice device)
    {
        this.mInetAddress = device.getInetAddress();
    }
    
    @Override
    public void copyRootDeviceId(IEspDevice device)
    {
        this.mRootDeviceId = device.getRootDeviceId();
    }
    
    @Override
    public void copyRouter(IEspDevice device)
    {
        this.mRouter = device.getRouter();
    }
    
    @Override
    public void copyIsMeshDevice(IEspDevice device)
    {
        this.mIsMeshDevice = device.getIsMeshDevice();
    }
    
    @Override
    public void copyTimestamp(IEspDevice device)
    {
        this.mTimeStamp = device.getTimestamp();
    }
    
    @Override
    public boolean equals(Object o)
    {
        // check the type
        if (o == null || !(o instanceof IEspDevice))
        {
            return false;
        }
        IEspDevice other = (IEspDevice)o;
        return other.getKey().equals(mDeviceKey);
    }
    
    @Override
    public int hashCode()
    {
        return mDeviceKey.hashCode();
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        EspDevice device = (EspDevice)super.clone();
        // deep copy
        EspDeviceState state = (EspDeviceState)device.getDeviceState();
        device.mDeviceState = (IEspDeviceState)state.clone();
        return device;
    }
    
    @Override
    public String toString()
    {
        // return "EspDevice: (mBssid=[" + mBssid + "],mDeviceId=[" + mDeviceId + "],mDeviceKey=[" + mDeviceKey
        // + "],mIsOwner=[" + mIsOwner + "],mDeviceName=[" + mDeviceName + "],mRomVersion=[" + mRomVersion
        // + "],mLatestRomVersion=[" + mLatestRomVersion + "],mTimeStamp=[" + mTimeStamp + "],mUserId=[" + mUserId
        // + "],_isDeviceRefreshed=[" + _isDeviceRefreshed + "],mDeviceType=[" + mDeviceType + "],mDeviceState=["
        // + mDeviceState + "],mInetAddress=[" + mInetAddress + "])";
        return "EspDevice: (mBssid=[" + mBssid + "],mRootDeviceBssid=[" + mRootDeviceBssid + "]mDeviceId=[" + mDeviceId
            + "],mDeviceName=[" + mDeviceName + "],mDeviceState=[" + mDeviceState + "],mIsMeshDevice=[" + mIsMeshDevice
            + "],mRouter=[" + mRouter + "],mInetAddress=[" + mInetAddress + "])";
        
    }
    
    private List<IEspDeviceTreeElement> __getDeviceTreeElementListByRouter(List<IEspDevice> allDeviceList)
    {
        if (!allDeviceList.contains(this))
        {
            throw new IllegalStateException("allDeviceList don't contain current device");
        }
        String router = null;
        String routerTemp = null;
        String parentRouter = null;
        String parentDeviceKey = null;
        boolean hasParent = false;
        boolean hasChild = false;
        int level = -1;
        int currentLevel = -1;
        // get routerList
        List<String> routerList = new ArrayList<String>();
        for (IEspDevice deviceInList : allDeviceList)
        {
            router = deviceInList.getRouter();
            if (router != null)
            {
                routerList.add(router);
            }
        }
        List<String> allChildRouterList = RouterUtil.getAllChildRouterList(routerList, mRouter);
        // all of its child and itself device list, don't forget to add itself
        List<IEspDevice> allChildAndSelfDeviceList = new ArrayList<IEspDevice>();
        for (IEspDevice deviceInList : allDeviceList)
        {
            router = deviceInList.getRouter();
            if (router != null && allChildRouterList.contains(router))
            {
                allChildAndSelfDeviceList.add(deviceInList);
            }
        }
        // don't forget to add itself
        allChildAndSelfDeviceList.add(this);
        // build deviceTreeElementList
        List<IEspDeviceTreeElement> deviceTreeElementList = new ArrayList<IEspDeviceTreeElement>();
        for (IEspDevice deviceInList : allChildAndSelfDeviceList)
        {
            router = deviceInList.getRouter();
            // don't forget to clear dirty info
            parentRouter = null;
            parentDeviceKey = null;
            hasParent = false;
            hasChild = false;
            level = -1;
            if (router != null)
            {
                level = RouterUtil.getRouterLevel(router);
                if (currentLevel == -1 && deviceInList.equals(this))
                {
                    // set current level to set relative level later
                    currentLevel = level;
                }
                parentRouter = RouterUtil.getParentRouter(routerList, router);
                for (IEspDevice deviceInList2 : allChildAndSelfDeviceList)
                {
                    routerTemp = deviceInList2.getRouter();
                    if (routerTemp != null && routerTemp.equals(parentRouter))
                    {
                        parentDeviceKey = deviceInList2.getKey();
                        break;
                    }
                }
                hasParent = parentRouter != null;
                hasChild = !RouterUtil.getDirectChildRouterList(routerList, router).isEmpty();
                IEspDeviceTreeElement deviceTreeElement =
                    new EspDeviceTreeElement(deviceInList, parentDeviceKey, hasParent, hasChild, level);
                deviceTreeElementList.add(deviceTreeElement);
            }
        }
        // set relative level
        for (IEspDeviceTreeElement deviceTreeElemenInList : deviceTreeElementList)
        {
            deviceTreeElemenInList.setRelativeLevel(currentLevel);
        }
        return deviceTreeElementList;
    }
    
    private List<IEspDeviceTreeElement> __getDeviceTreeElementListByBssid(List<IEspDevice> allDeviceList)
    {
        if (!allDeviceList.contains(this))
        {
            throw new IllegalStateException("allDeviceList don't contain current device");
        }
        if (!this.getIsMeshDevice())
        {
            throw new IllegalStateException("the device isn't mesh device");
        }
        // internet tell us the root device id while local tell us the root device bssid
        // fill root device bssid if it is null and root device id isn't -1
        for (IEspDevice outDevice : allDeviceList)
        {
            // only process mesh device
            if (!outDevice.getIsMeshDevice())
            {
                continue;
            }
            // fill root device bssid
            long outRootDeviceId = outDevice.getRootDeviceId();
            if (outDevice.getRootDeviceBssid() == null && outRootDeviceId != -1)
            {
                for (IEspDevice inDevice : allDeviceList)
                {
                    if (outRootDeviceId == inDevice.getId())
                    {
                        String outRootDeviceBssid = inDevice.getBssid();
                        outDevice.setRootDeviceBssid(outRootDeviceBssid);
                        break;
                    }
                }
            }
        }
        
        // define final level is just to make code readable
        final int rootLevel = 1;
        final int childLevel = 2;
        
        // build deviceTreeElementList
        List<IEspDeviceTreeElement> deviceTreeElementList = new ArrayList<IEspDeviceTreeElement>();
        String rootDeviceKey = null;
        boolean isRootHasChild = false;
        
        // root device
        if (this.getBssid().equals(this.getRootDeviceBssid()))
        {
            rootDeviceKey = this.getKey();
            for (IEspDevice device : allDeviceList)
            {
                // check whether the device is valid and isn't this device
                if (!device.getIsMeshDevice() || device.equals(this) || !device.getRootDeviceBssid().equals(mBssid))
                {
                    continue;
                }
                // child device tree element
                IEspDeviceTreeElement child = new EspDeviceTreeElement(device, rootDeviceKey, true, false, childLevel);
                deviceTreeElementList.add(child);
                isRootHasChild = true;
            }
            
            // root device tree element
            IEspDeviceTreeElement root = new EspDeviceTreeElement(this, null, false, isRootHasChild, rootLevel);
            deviceTreeElementList.add(root);
        }
        // non root device
        else
        {
            // root device tree element
            IEspDevice rootDevice = null;
            for (IEspDevice device : allDeviceList)
            {
                if (this.getRootDeviceBssid().equals(device.getBssid()))
                {
                    rootDevice = device;
                    break;
                }
            }
            if (rootDevice == null)
            {
                throw new IllegalStateException();
            }
            // root device's child is this device
            isRootHasChild = true;
            rootDeviceKey = rootDevice.getKey();
            IEspDeviceTreeElement root = new EspDeviceTreeElement(rootDevice, null, false, isRootHasChild, rootLevel);
            deviceTreeElementList.add(root);
            // child device tree element
            IEspDeviceTreeElement child = new EspDeviceTreeElement(this, rootDeviceKey, true, false, childLevel);
            deviceTreeElementList.add(child);
        }
        return deviceTreeElementList;
    }
    
    @Override
    public List<IEspDeviceTreeElement> getDeviceTreeElementList(List<IEspDevice> allDeviceList)
    {
        return __getDeviceTreeElementListByBssid(allDeviceList);
    }
    
    @Override
    public boolean isActivated()
    {
        return mDeviceId > 0 && !TextUtils.isEmpty(mDeviceKey);
    }
    
    @Override
    public List<IEspStatusEspnow> getEspnowStatusList()
    {
        return mEspnowStatusList;
    }
    
}
