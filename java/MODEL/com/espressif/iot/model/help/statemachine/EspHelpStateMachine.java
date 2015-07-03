package com.espressif.iot.model.help.statemachine;

import org.apache.log4j.Logger;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.help.statemachine.IEspHelpStateMachine;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpType;

public class EspHelpStateMachine implements IEspHelpStateMachine, IEspSingletonObject
{
    private final static Logger log = Logger.getLogger(EspHelpStateMachine.class);
    
    private boolean mIsOn;
    
    private HelpType mType;
    
    private String mDeviceBssid;
    
    private int mCurrentStateOrdinal;
    
    private String mApSsid;
    
    /*
     * Singleton lazy initialization start
     */
    private EspHelpStateMachine()
    {
        mIsOn = false;
        mType = null;
        mDeviceBssid = null;
        mApSsid = null;
        mCurrentStateOrdinal = -1;
        mDeviceSelection = -1;
    }
    
    private static class InstanceHolder
    {
        static EspHelpStateMachine instance = new EspHelpStateMachine();
    }
    
    public static EspHelpStateMachine getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public boolean isHelpOn()
    {
        return this.mIsOn;
    }
    
    private void __setIsHelpOn()
    {
        this.mIsOn = true;
    }
    
    private void __setIsHelpOff()
    {
        this.mIsOn = false;
    }
    
    @Override
    public void setConnectedApSsid(String apSsid)
    {
        this.mApSsid = apSsid;
    }
    
    @Override
    public String getConnectedApSsid()
    {
        return this.mApSsid;
    }
    
    @Override
    public void transformState(boolean isSuc)
    {
        log.info("##transformState(isSuc=[" + isSuc + "]):");
        log.info("old getCurretStateInDetailed(): " + getCurretStateInDetailed());
        IEspHelpHandler handler = __getHandler();
        this.mCurrentStateOrdinal = handler.getNextStateOrdinal(this.mCurrentStateOrdinal, isSuc);
        log.info("new getCurretStateInDetailed(): " + getCurretStateInDetailed());
    }
    
    @Override
    public void transformState(Enum<?> state)
    {
        mCurrentStateOrdinal = state.ordinal();
    }
    
    @Override
    public void start(HelpType type)
    {
        log.info("##start(type=[" + type + "])");
        this.mType = type;
        this.mCurrentStateOrdinal = 0;
        __setIsHelpOn();
    }
    
    @Override
    public void retry()
    {
        log.info("##retry():");
        log.info("old getCurretStateInDetailed(): " + getCurretStateInDetailed());
        IEspHelpHandler handler = __getHandler();
        this.mCurrentStateOrdinal = handler.getRetryStateOrdinal(mCurrentStateOrdinal);
        log.info("new getCurretStateInDetailed(): " + getCurretStateInDetailed());
    }
    
    @Override
    public void exit()
    {
        log.info("##exit():");
        this.mDeviceBssid = null;
        this.mType = null;
        this.mApSsid = null;
        this.mCurrentStateOrdinal = -1;
        __setIsHelpOff();
    }
    
    @Override
    public HelpType getCurrentType()
    {
        return this.mType;
    }
    
    @Override
    public int getCurrentStateOrdinal()
    {
        if (this.mCurrentStateOrdinal == -1)
        {
            throw new IllegalStateException("mCurrentStateOrdinal = -1");
        }
        else
        {
            return this.mCurrentStateOrdinal;
        }
    }
    
    @Override
    public String getCurretStateInDetailed()
    {
        IEspHelpHandler handler = __getHandler();
        if (handler != null)
        {
            return handler.getStateInDetailed(mCurrentStateOrdinal);
        }
        else
        {
            throw new IllegalStateException("can't find the handler");
        }
    }
    
    @Override
    public String __getCurrentDeviceBssid()
    {
        return this.mDeviceBssid;
    }
    
    @Override
    public void __setCurrentDeviceBssid(String bssid)
    {
        this.mDeviceBssid = bssid;
    }
    
    private IEspHelpHandler __getHandler()
    {
        switch (mType)
        {
            case CONFIGURE:
                return EspHelpConfigureHandler.getInstance();
            case MESH_CONFIGURE:
                return EspHelpMeshConfigureHandler.getInstance();
            case USAGE_PLUG:
                return EspHelpUsePlugHandler.getInstance();
            case USAGE_PLUGS:
                return EspHelpUsePlugsHandler.getInstance();
            case USAGE_LIGHT:
                return EspHelpUseLightHandler.getInstance();
            case USAGE_HUMITURE:
                return EspHelpUseHumitureHandler.getInstance();
            case USAGE_VOLTAGE:
                return EspHelpUseVoltageHandler.getInstance();
            case USAGE_FLAMMABLE:
                return EspHelpUseFlammableHandler.getInstance();
            case USAGE_REMOTE:
                return EspHelpUseRemoteHandler.getInstance();
            case UPGRADE_LOCAL:
                return EspHelpUpgradeLocalHandler.getInstance();
            case UPGRADE_ONLINE:
                return EspHelpUpgradeOnlineHandler.getInstance();
            case SSS_USAGE_DEVICE:
                return EspHelpSSSUseDeviceHandler.getInstance();
            case SSS_UPGRADE:
                return EspHelpSSSUpgradeHandler.getInstance();
            case SSS_MESH_CONFIGURE:
                return EspHelpSSSMeshConfigureHandler.getInstance();
        }
        return null;
    }
    
    private int mDeviceSelection;
    
    @Override
    public void setDeviceSelection(int selection)
    {
        mDeviceSelection = selection;
    }
    
    @Override
    public int getDeviceSelection()
    {
        return mDeviceSelection;
    }
    
    @Override
    public int resetDeviceSelection()
    {
        mDeviceSelection = -1;
        return mDeviceSelection;
    }
    
    @Override
    public boolean isHelpModeConfigure()
    {
        return (isHelpOn() && getCurrentType() == HelpType.CONFIGURE);
    }
    
    @Override
    public boolean isHelpModeUsePlug()
    {
        return (isHelpOn() && getCurrentType() == HelpType.USAGE_PLUG);
    }
    
    @Override
    public boolean isHelpModeUseLight()
    {
        return (isHelpOn() && getCurrentType() == HelpType.USAGE_LIGHT);
    }
    
    @Override
    public boolean isHelpModeUseHumiture()
    {
        return (isHelpOn() && getCurrentType() == HelpType.USAGE_HUMITURE);
    }
    
    @Override
    public boolean isHelpModeUseFlammable()
    {
        return (isHelpOn() && getCurrentType() == HelpType.USAGE_FLAMMABLE);
    }
    
    @Override
    public boolean isHelpModeUseVoltage()
    {
        return (isHelpOn() && getCurrentType() == HelpType.USAGE_VOLTAGE);
    }
    
    @Override
    public boolean isHelpModeUseRemote()
    {
        return (isHelpOn() && getCurrentType() == HelpType.USAGE_REMOTE);
    }
    
    @Override
    public boolean isHelpModeUsePlugs()
    {
        return (isHelpOn() && getCurrentType() == HelpType.USAGE_PLUGS);
    }
    
    @Override
    public boolean isHelpModeUpgradeLocal()
    {
        return (isHelpOn() && getCurrentType() == HelpType.UPGRADE_LOCAL);
    }
    
    @Override
    public boolean isHelpModeUpgradeOnline()
    {
        return (isHelpOn() && getCurrentType() == HelpType.UPGRADE_ONLINE);
    }
    
    @Override
    public boolean isHelpModeUseSSSDevice()
    {
        return (isHelpOn() && getCurrentType() == HelpType.SSS_USAGE_DEVICE);
    }
    
    @Override
    public boolean isHelpModeSSSUpgrade()
    {
        return (isHelpOn() && getCurrentType() == HelpType.SSS_UPGRADE);
    }

    @Override
    public boolean isHelpModeSSSMeshConfigure()
    {
        return (isHelpOn() && getCurrentType() == HelpType.SSS_MESH_CONFIGURE);
    }

    @Override
    public boolean isHelpModeMeshConfigure()
    {
        return (isHelpOn() && getCurrentType() == HelpType.MESH_CONFIGURE);
    }
}
