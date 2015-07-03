package com.espressif.iot.ui.help;

import java.lang.ref.WeakReference;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.help.ui.IEspHelpUIConfigure;
import com.espressif.iot.help.ui.IEspHelpUIMeshConfigure;
import com.espressif.iot.help.ui.IEspHelpUIUpgradeLocal;
import com.espressif.iot.help.ui.IEspHelpUIUpgradeOnline;
import com.espressif.iot.help.ui.IEspHelpUIUseFlammable;
import com.espressif.iot.help.ui.IEspHelpUIUseHumiture;
import com.espressif.iot.help.ui.IEspHelpUIUseLight;
import com.espressif.iot.help.ui.IEspHelpUIUsePlug;
import com.espressif.iot.help.ui.IEspHelpUIUsePlugs;
import com.espressif.iot.help.ui.IEspHelpUIUseRemote;
import com.espressif.iot.help.ui.IEspHelpUIUseVoltage;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.help.HelpStepConfigure;
import com.espressif.iot.type.help.HelpStepMeshConfigure;
import com.espressif.iot.type.help.HelpStepUpgradeLocal;
import com.espressif.iot.type.help.HelpStepUpgradeOnline;
import com.espressif.iot.type.help.HelpStepUseFlammable;
import com.espressif.iot.type.help.HelpStepUseHumiture;
import com.espressif.iot.type.help.HelpStepUseLight;
import com.espressif.iot.type.help.HelpStepUsePlug;
import com.espressif.iot.type.help.HelpStepUsePlugs;
import com.espressif.iot.type.help.HelpStepUseRemote;
import com.espressif.iot.type.help.HelpStepUseVoltage;
import com.espressif.iot.type.help.HelpType;
import com.espressif.iot.ui.device.DeviceRootRouterActivity;
import com.espressif.iot.ui.main.EspUIActivity;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class HelpEspUIActivity extends EspUIActivity implements IEspHelpUIConfigure, IEspHelpUIUsePlug,
    IEspHelpUIUseLight, IEspHelpUIUseHumiture, IEspHelpUIUseFlammable, IEspHelpUIUseVoltage, IEspHelpUIUseRemote,
    IEspHelpUIUsePlugs, IEspHelpUIUpgradeLocal, IEspHelpUIUpgradeOnline, IEspHelpUIMeshConfigure
{
    private static final Logger log = Logger.getLogger(HelpEspUIActivity.class);
    
    private final static int HELP_FIND_UPGRADE_LOCAL = 1;
    private final static int HELP_FIND_UPGRADE_ONLINE = 2;
    
    private final static int REQUEST_CONFIGURE = 0x20;
        
    private HelpHandler mHelpHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setTitleRightIcon(R.drawable.esp_icon_help);
        mHelpHandler = new HelpHandler(this);
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (mHelpMachine.isHelpOn())
        {
            return true;
        }
        
        return super.onItemLongClick(parent, view, position, id);
    }
    
    @Override
    protected Class<?> getDeviceClass(IEspDevice device)
    {
        IEspDeviceState state = device.getDeviceState();
        Class<?> _class = null;
        switch (device.getDeviceType())
        {
            case PLUG:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    _class = HelpDevicePlugActivity.class;
                }
                break;
            case LIGHT:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    _class = HelpDeviceLightActivity.class;
                }
                break;
            case FLAMMABLE:
                if (state.isStateInternet() || state.isStateOffline())
                {
                    _class = HelpDeviceFlammableActivity.class;
                }
                break;
            case HUMITURE:
                if (state.isStateInternet() || state.isStateOffline())
                {
                    _class = HelpDeviceHumitureActivity.class;
                }
                break;
            case VOLTAGE:
                if (state.isStateInternet() || state.isStateOffline())
                {
                    _class = HelpDeviceVoltageActivity.class;
                }
                break;
            case REMOTE:
                if (state.isStateInternet() || state.isStateLocal())
                {
                }
                break;
            case PLUGS:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    _class = HelpDevicePlugsActivity.class;
                }
                break;
            case ROOT:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    _class = DeviceRootRouterActivity.class;
                }
                break;
            case NEW:
                log.warn("Click on NEW device, it shouldn't happen");
                break;
        }
        
        return _class;
    }
    
    @Override
    protected void gotoConfigure()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            mHelpMachine.transformState(true);
        }
        else if (mHelpMachine.isHelpModeMeshConfigure())
        {
            mHelpMachine.transformState(true);
        }
        
        Intent intent = new Intent(this, HelpDeviceConfigureActivity.class);
        startActivityForResult(intent, REQUEST_CONFIGURE);
    }
    
    @Override
    protected void checkHelpConfigure()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            mHelpHandler.sendEmptyMessage(RESULT_HELP_CONFIGURE);
        }
    }
    
    @Override
    protected boolean checkHelpClickDeviceType(EspDeviceType type)
    {
        return mHelpMachine.isHelpOn() && !onHelpDeviceClick(type);
    }
    
    @Override
    protected void onTitleRightIconClick()
    {
        startActivityForResult(new Intent(this, HelpActivity.class), REQUEST_HELP);
    }
    
    @Override
    public void onExitHelpMode()
    {
        clearHelpContainer();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_HELP:
            case REQUEST_CONFIGURE:
            case REQUEST_DEVICE:
                mHelpHandler.sendEmptyMessage(resultCode);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private static class HelpHandler extends Handler
    {
        private WeakReference<HelpEspUIActivity> mActivity;
        
        public HelpHandler(HelpEspUIActivity activity)
        {
            mActivity = new WeakReference<HelpEspUIActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            HelpEspUIActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
            
            switch (msg.what)
            {
                case RESULT_EXIT_HELP_MODE:
                    activity.onExitHelpMode();
                    break;
                case RESULT_HELP_CONFIGURE:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.CONFIGURE);
                    }
                    activity.onHelpConfigure();
                    break;
                case RESULT_HELP_MESH_CONFIGURE:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.MESH_CONFIGURE);
                    }
                    activity.onHelpMeshConfigure();
                    break;
                case RESULT_HELP_USE_PLUG:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.USAGE_PLUG);
                    }
                    activity.onHelpUsePlug();
                    break;
                case RESULT_HELP_USE_LIGHT:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.USAGE_LIGHT);
                    }
                    activity.onHelpUseLight();
                    break;
                case RESULT_HELP_USE_HUMITURE:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.USAGE_HUMITURE);
                    }
                    activity.onHelpUseHumiture();
                    break;
                case RESULT_HELP_USE_FLAMMABLE:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.USAGE_FLAMMABLE);
                    }
                    activity.onHelpUseFlammable();
                    break;
                case RESULT_HELP_USE_VOLTAGE:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.USAGE_VOLTAGE);
                    }
                    activity.onHelpUseVoltage();
                    break;
                case RESULT_HELP_USE_REMOTE:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.USAGE_REMOTE);
                    }
                    activity.onHelpUseRemote();
                    break;
                case RESULT_HELP_USE_PLUGS:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.USAGE_PLUGS);
                    }
                    activity.onHelpUsePlugs();
                    break;
                case RESULT_HELP_UPGRADE_LOCAL:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.UPGRADE_LOCAL);
                    }
                    activity.onHelpUpgradeLocal();
                    break;
                case RESULT_HELP_UPGRADE_ONLINE:
                    if (!mHelpMachine.isHelpOn())
                    {
                        mHelpMachine.start(HelpType.UPGRADE_ONLINE);
                    }
                    activity.onHelpUpgradeOnline();
                    break;
            }
        }
    }
    
    @Override
    public void onHelpConfigure()
    {
        clearHelpContainer();
        
        HelpStepConfigure step = HelpStepConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_CONFIGURE_HELP:
                hintConfigureStart();
                break;
            case DEVICE_IS_ACTIVATING:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_configure_device_activating_msg);
                break;
            case FAIL_CONNECT_AP:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_configure_connect_ap_failed_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case FAIL_ACTIVATE:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_configure_device_activate_failed_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case SUC:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_configure_success_msg);
                break;
            default:
                // Not process here
                break;
        }
    }
    
    private void hintConfigureStart()
    {
        highlightHelpView(mConfigureBtn);
        
        int statusBarHeight = getStatusBarHeight();
        
        int[] conLocation = new int[2];
        mConfigureBtn.getLocationInWindow(conLocation);
        Rect rect =
            new Rect(conLocation[0], conLocation[1] - statusBarHeight, conLocation[0] + mConfigureBtn.getWidth(),
                conLocation[1] + mConfigureBtn.getHeight() - statusBarHeight);
        
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(rect.width(), rect.height());
        ImageView hintBtn = new ImageView(this);
        hintBtn.setScaleType(ScaleType.CENTER);
        hintBtn.setImageResource(R.drawable.esp_help_hint);
        mHelpContainer.addView(hintBtn, lp);
        hintBtn.setX(rect.left);
        hintBtn.setY(rect.top);
    }
    
    @Override
    public void onHelpUsePlug()
    {
        clearHelpContainer();
        
        HelpStepUsePlug step = HelpStepUsePlug.valueOf(mHelpMachine.getCurrentStateOrdinal());
        log.debug("onHelpUsePlug() Plug step = " + step);
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.PLUG);
                onHelpUsePlug();
                break;
            case FAIL_FOUND_PLUG:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_plug_fail_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                useDeviceHelpFindOnline(EspDeviceType.PLUG);
                onHelpUsePlug();
                break;
            case NO_PLUG_ONLINE:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_use_plug_no_online_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case PLUG_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_plug_selcet_msg);
                break;
            case PLUG_NOT_COMPATIBILITY:
            case PLUG_CONTROL:
            case PLUG_CONTROL_FAILED:
            case SUC:
                break;
        }
    }
    
    @Override
    public void onHelpUsePlugs()
    {
        clearHelpContainer();
        
        HelpStepUsePlugs step = HelpStepUsePlugs.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.PLUGS);
                onHelpUsePlugs();
                break;
            case FAIL_FOUND_PLUGS:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_plugs_fail_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                useDeviceHelpFindOnline(EspDeviceType.PLUGS);
                onHelpUsePlugs();
                break;
            case NO_PLUGS_ONLINE:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_use_plugs_no_online_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case PLUGS_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_plugs_selcet_msg);
                break;
            case PLUGS_CONTROL:
            case PLUGS_CONTROL_FAILED:
            case PLUGS_NOT_COMPATIBILITY:
            case SUC:
                break;
            
        }
    }
    
    @Override
    public void onHelpUseLight()
    {
        clearHelpContainer();
        
        HelpStepUseLight step = HelpStepUseLight.valueOf(mHelpMachine.getCurrentStateOrdinal());
        log.debug("onHelpUsePlug() Light step = " + step);
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.LIGHT);
                onHelpUseLight();
                break;
            case FAIL_FOUND_LIGHT:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_light_fail_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                useDeviceHelpFindOnline(EspDeviceType.LIGHT);
                onHelpUseLight();
                break;
            case NO_LIGHT_ONLINE:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_use_light_no_online_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case LIGHT_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_light_select_msg);
                break;
            case LIGHT_NOT_COMPATIBILITY:
            case LIGHT_CONTROL:
            case LIGHT_CONTROL_FAILED:
            case SUC:
                // not process here
                break;
        }
    }
    
    @Override
    public void onHelpUseHumiture()
    {
        clearHelpContainer();
        
        HelpStepUseHumiture step = HelpStepUseHumiture.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.HUMITURE);
                onHelpUseHumiture();
                break;
            case FAIL_FOUND_HUMITURE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_humiture_faile_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case HUMITURE_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_humiture_select_msg);
                break;

            case HUMITURE_NOT_COMPATIBILITY:
                break;
            case PULL_DOWN_TO_REFRESH:
                break;
            case GET_DATA_FAILED:
                break;
            case SELECT_DATE:
                break;
            case SELECT_DATE_FAILED:
                break;
            case SUC:
                break;
        }
    }
    
    @Override
    public void onHelpUseFlammable()
    {
        clearHelpContainer();
        
        HelpStepUseFlammable step = HelpStepUseFlammable.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.FLAMMABLE);
                onHelpUseFlammable();
                break;
            case FAIL_FOUND_FLAMMABLE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_flammable_faile_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FLAMMABLE_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_flammable_select_msg);
                break;
                
            case FLAMMABLE_NOT_COMPATIBILITY:
                break;
            case GET_DATA_FAILED:
                break;
            case PULL_DOWN_TO_REFRESH:
                break;
            case SELECT_DATE:
                break;
            case SELECT_DATE_FAILED:
                break;
            case SUC:
                break;
        }
    }
    
    @Override
    public void onHelpUseVoltage()
    {
        clearHelpContainer();
        
        HelpStepUseVoltage step = HelpStepUseVoltage.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.VOLTAGE);
                onHelpUseFlammable();
                break;
            case FAIL_FOUND_VOLTAGE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_voltage_faile_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case VOLTAGE_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_voltage_select_msg);
                break;
                
            case VOLTAGE_NOT_COMPATIBILITY:
                break;
            case GET_DATA_FAILED:
                break;
            case PULL_DOWN_TO_REFRESH:
                break;
            case SELECT_DATE:
                break;
            case SELECT_DATE_FAILED:
                break;
            case SUC:
                break;
        }
    }

    
    @Override
    public void onHelpUseRemote()
    {
        clearHelpContainer();
        
        HelpStepUseRemote step = HelpStepUseRemote.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.REMOTE);
                onHelpUseRemote();
                break;
            case FAIL_FOUND_REMOTE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_remote_fail_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                useDeviceHelpFindOnline(EspDeviceType.REMOTE);
                onHelpUseRemote();
                break;
            case NO_REMOTE_ONLINE:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_use_remote_no_online_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case REMOTE_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_remote_select_msg);
                break;
            
            case REMOTE_NOT_COMPATIBILITY:
                break;
            case REMOTE_CONTROL:
                break;
            case REMOTE_CONTROL_FAILED:
                break;
            case SUC:
                break;
        }
    }
    
    /**
     * When click device on help mode, check whether is this device type help mode
     * @param type
     * @return
     */
    private boolean onHelpDeviceClick(EspDeviceType type)
    {
        if (mHelpMachine.isHelpModeUpgradeLocal() || mHelpMachine.isHelpModeUpgradeOnline())
        {
            return true;
        }
        
        switch(type)
        {
            case PLUG:
                return mHelpMachine.isHelpModeUsePlug();
            case LIGHT:
                return mHelpMachine.isHelpModeUseLight();
            case HUMITURE:
                return mHelpMachine.isHelpModeUseHumiture();
            case FLAMMABLE:
                return mHelpMachine.isHelpModeUseFlammable();
            case VOLTAGE:
                return mHelpMachine.isHelpModeUseVoltage();
            case REMOTE:
                return mHelpMachine.isHelpModeUseRemote();
            case PLUGS:
                return mHelpMachine.isHelpModeUsePlugs();
            case ROOT:
                break;
            case NEW:
                break;
        }
        
        return false;
    }
    
    /**
     * Start use device help, find this type of device in user device list
     * @param type
     */
    private void useDeviceHelpStart(EspDeviceType type)
    {
        for (int i = 0; i < mDeviceList.size(); i++)
        {
            IEspDevice device = mDeviceList.get(i);
            if (device.getDeviceType() == type)
            {
                if (type == EspDeviceType.FLAMMABLE || type == EspDeviceType.HUMITURE)
                {
                    mHelpMachine.setDeviceSelection(i + PullToRefreshListView.DEFAULT_HEADER_COUNT);
                }
                mHelpMachine.transformState(true);
                return;
            }
        }
        
        mHelpMachine.transformState(false);
    }
    
    /**
     * Find online device in user device list
     * @param type
     */
    private void useDeviceHelpFindOnline(EspDeviceType type)
    {
        for (int i = 0; i < mDeviceList.size(); i++)
        {
            IEspDevice device = mDeviceList.get(i);
            if (device.getDeviceType() == type)
            {
                IEspDeviceState state = device.getDeviceState();
                if (state.isStateLocal() || state.isStateInternet())
                {
                    mHelpMachine.setDeviceSelection(i + PullToRefreshListView.DEFAULT_HEADER_COUNT);
                    mHelpMachine.transformState(true);
                    return;
                }
            }
        }
        
        mHelpMachine.resetDeviceSelection();
        mHelpMachine.transformState(false);
    }
    
    /**
     * Hint user click the device
     * @param hintMsgRes
     */
    private void useDeviceHelpHintSelect(int hintMsgRes)
    {
        int selection = mHelpMachine.getDeviceSelection();
        mHelpMachine.resetDeviceSelection();
        mDeviceListView.getRefreshableView().setSelection(selection);
        highlightHelpView(mDeviceListView);
        setHelpHintMessage(hintMsgRes);
    }
    
    @Override
    public void onHelpUpgradeLocal()
    {
        clearHelpContainer();
        
        HelpStepUpgradeLocal step = HelpStepUpgradeLocal.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_UPGRADE_LOCAL:
                mHelpMachine.transformState(findHelpUpgradDevice(HELP_FIND_UPGRADE_LOCAL, false) != null);
                onHelpUpgradeLocal();
                break;
            case NO_DEVICE_NEED_UPGRADE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_upgrade_local_no_device_need_upgrade_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_LOCAL:
                mHelpMachine.transformState(findHelpUpgradDevice(HELP_FIND_UPGRADE_LOCAL, true) != null);
                onHelpUpgradeLocal();
                break;
            case NO_DEVICE_LOCAL:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_upgrade_local_no_device_local_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FOUND_LOCAL:
                IEspDevice device = findHelpUpgradDevice(HELP_FIND_UPGRADE_LOCAL, true);
                gotoUseDevice(device);
                mHelpMachine.transformState(true);
                break;
            
            case CHECK_COMPATIBILITY:
                break;
            case UPGRADING:
                break;
            case UPGRADE_FAILED:
                Toast.makeText(this, R.string.esp_help_upgrade_local_failed_msg, Toast.LENGTH_LONG).show();
                mHelpMachine.exit();
                onExitHelpMode();
                break;
            case SUC:
                Toast.makeText(this, R.string.esp_help_upgrade_local_success_msg, Toast.LENGTH_LONG).show();
                mHelpMachine.exit();
                onExitHelpMode();
                break;
        }
    }
    
    @Override
    public void onHelpUpgradeOnline()
    {
        clearHelpContainer();
        
        HelpStepUpgradeOnline step = HelpStepUpgradeOnline.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_UPGRADE_ONLINE:
                mHelpMachine.transformState(findHelpUpgradDevice(HELP_FIND_UPGRADE_ONLINE, false) != null);
                onHelpUpgradeOnline();
                break;
            case NO_DEVICE_NEED_UPGRADE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_upgrade_online_no_device_need_upgrade_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                mHelpMachine.transformState(findHelpUpgradDevice(HELP_FIND_UPGRADE_ONLINE, true) != null);
                onHelpUpgradeOnline();
                break;
            case NO_DEVICE_LOCAL:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_upgrade_online_no_device_local_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FOUND_ONLINE:
                gotoUseDevice(findHelpUpgradDevice(HELP_FIND_UPGRADE_ONLINE, true));
                mHelpMachine.transformState(true);
                break;
            
            case CHECK_COMPATIBILITY:
                break;
            case UPGRADING:
                break;
            case UPGRADE_FAILED:
                Toast.makeText(this, R.string.esp_help_upgrade_online_failed_msg, Toast.LENGTH_LONG).show();
                mHelpMachine.exit();
                onExitHelpMode();
                break;
            case SUC:
                Toast.makeText(this, R.string.esp_help_upgrade_online_success_msg, Toast.LENGTH_LONG).show();
                mHelpMachine.exit();
                onExitHelpMode();
                break;
        }
    }
    
    /**
     * Find device need upgrade
     * 
     * @param upgradeType @see {@value #HELP_FIND_UPGRADE_LOCAL} & {@value #HELP_FIND_UPGRADE_ONLINE}
     * @return
     */
    private IEspDevice findHelpUpgradDevice(int upgradeType, boolean checkState)
    {
        for (int i = 0; i < mDeviceList.size(); i++)
        {
            IEspDevice device = mDeviceList.get(i);
            
            if (upgradeType == HELP_FIND_UPGRADE_LOCAL)
            {
                switch(mUser.getDeviceUpgradeTypeResult(device))
                {
                    case SUPPORT_ONLINE_LOCAL:
                    case SUPPORT_LOCAL_ONLY:
                        if (!checkState)
                        {
                            return device;
                        }
                        
                        if (device.getDeviceState().isStateLocal())
                        {
                            return device;
                        }
                        
                        break;
                    default:
                        break;
                }
            }
            else if (upgradeType == HELP_FIND_UPGRADE_ONLINE)
            {
                switch(mUser.getDeviceUpgradeTypeResult(device))
                {
                    case SUPPORT_ONLINE_LOCAL:
                    case SUPPORT_ONLINE_ONLY:
                        if (!checkState)
                        {
                            return device;
                        }
                        
                        if (device.getDeviceState().isStateInternet())
                        {
                            return device;
                        }
                        
                        break;
                    default:
                        break;
                }
            }
        }

        return null;
    }
    
    @Override
    public void onHelpRetryClick()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            onHelpConfigure();
        }
        else if (mHelpMachine.isHelpModeUsePlug())
        {
            onHelpUsePlug();
        }
        else if (mHelpMachine.isHelpModeUseLight())
        {
            onHelpUseLight();
        }
        else if (mHelpMachine.isHelpModeUseRemote())
        {
            onHelpUseRemote();
        }
    }
    
    @Override
    public void onHelpMeshConfigure()
    {
        clearHelpContainer();
        
        HelpStepMeshConfigure step = HelpStepMeshConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case START:
                hintConfigureStart();
                break;
            default:
                break;
        }
    }
}
