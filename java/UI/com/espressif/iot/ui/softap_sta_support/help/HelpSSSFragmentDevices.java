package com.espressif.iot.ui.softap_sta_support.help;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.help.ui.IEspHelpUISSSUpgrade;
import com.espressif.iot.help.ui.IEspHelpUISSSUseDevice;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.help.HelpStepSSSUpgrade;
import com.espressif.iot.type.help.HelpStepSSSUseDevice;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.softap_sta_support.SSSFragmentDevices;

import android.os.Bundle;
import android.widget.Toast;

public class HelpSSSFragmentDevices extends SSSFragmentDevices implements IEspHelpUISSSUseDevice, IEspHelpUISSSUpgrade
{
    private HelpSoftApStaSupportActivity mActivity;
    private EspHelpStateMachine mHelpMachine;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        mActivity = (HelpSoftApStaSupportActivity)getActivity();
        mHelpMachine = EspHelpStateMachine.getInstance();
    }
    
    @Override
    protected void checkHelpOnPostScanSta()
    {
        if (mHelpMachine.isHelpModeUseSSSDevice())
        {
            if (mStaList.isEmpty())
            {
                mHelpMachine.transformState(false);
            }
            else
            {
                mHelpMachine.transformState(HelpStepSSSUseDevice.DEVICE_SELECT);
            }
            
            onHelpUseSSSDevice();
        }
        else if (mHelpMachine.isHelpModeSSSUpgrade())
        {
            mHelpMachine.transformState(!mStaList.isEmpty());
            onHelpUpgradeSSSDevice();
        }
    }
    
    @Override
    protected boolean checkHelpOnItemClick(IEspDeviceSSS device)
    {
        if (mHelpMachine.isHelpOn() && device.getDeviceType() == EspDeviceType.ROOT)
        {
            return true;
        }
        
        if (mHelpMachine.isHelpOn() && !mHelpMachine.isHelpModeUseSSSDevice())
        {
            return true;
        }
        else if (mHelpMachine.isHelpModeUseSSSDevice())
        {
            mHelpMachine.transformState(true);
            onHelpUseSSSDevice();
            return false;
        }
        
        return false;
    }
    
    @Override
    protected boolean checkHelpOnItemLongClick()
    {
        if (mHelpMachine.isHelpOn() && !mHelpMachine.isHelpModeSSSUpgrade())
        {
            return true;
        }
        
        return false;
    }
    
    @Override
    protected void checkHelpUpgrade()
    {
        if (mHelpMachine.isHelpModeSSSUpgrade())
        {
            mHelpMachine.transformState(true);
            onHelpUpgradeSSSDevice();
        }
    }
    
    @Override
    public void onHelpUseSSSDevice()
    {
        mActivity.clearHelpContainer();
        
        HelpStepSSSUseDevice step = HelpStepSSSUseDevice.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                mStaList.clear();
                mStaAdapter.notifyDataSetChanged();
                mActivity.highlightHelpView(mListView);
                mActivity.gestureAnimHint(R.string.esp_sss_help_use_device_find_device_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case FAIL_FOUND_DEVICE:
                mHelpMachine.retry();
                mActivity.highlightHelpView(mListView);
                mActivity.gestureAnimHint(R.string.esp_sss_help_use_device_found_device_failed_msg,
                    R.anim.esp_pull_to_refresh_hint);
                mActivity.setHelpButtonVisible(EspActivityAbs.HELP_BUTTON_EXIT, true);
                mActivity.setHelpButtonVisible(EspActivityAbs.HELP_BUTTON_NEXT, true);
                break;
            case START_DIRECT_CONNECT:
                break;
            case FIND_SOFTAP:
                break;
            case FOUND_SOFTAP_FAILED:
                break;
            case SELECT_SOFTAP:
                break;
            case CONNECT_SOFTAP_FAILED:
                break;
            case SOFTAP_NOT_SUPPORT:
                break;
            case DEVICE_SELECT:
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_select_msg);
                break;
            case DEVICE_CONTROL:
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_control_msg);
                break;
            case DEVICE_CONTROL_FAILED:
                break;
            case SUC:
                break;
        }
        
    }
    
    @Override
    public void onHelpUpgradeSSSDevice()
    {
        mActivity.clearHelpContainer();
        
        HelpStepSSSUpgrade step = HelpStepSSSUpgrade.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_HELP:
                mStaList.clear();
                mStaAdapter.notifyDataSetChanged();
                mActivity.highlightHelpView(mListView);
                mActivity.gestureAnimHint(R.string.esp_sss_help_upgrade_find_device_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case FOUND_DEVICE_FAILED:
                mHelpMachine.retry();
                mActivity.highlightHelpView(mListView);
                mActivity.gestureAnimHint(R.string.esp_sss_help_upgrade_found_device_failed_msg,
                    R.anim.esp_pull_to_refresh_hint);
                mActivity.setHelpButtonVisible(EspActivityAbs.HELP_BUTTON_EXIT, true);
                break;
            case SELECT_DEVICE:
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_upgrade_select_device_msg);
                break;
            case SUC:
                mHelpMachine.exit();
                mActivity.onExitHelpMode();
                Toast.makeText(mActivity, R.string.esp_sss_help_upgrade_success_msg, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
