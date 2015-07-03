package com.espressif.iot.ui.softap_sta_support.help;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.help.IEspHelpUISSSMeshConfigure;
import com.espressif.iot.help.ui.IEspHelpUISSSUseDevice;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.help.HelpStepSSSMeshConfigure;
import com.espressif.iot.type.help.HelpStepSSSUseDevice;
import com.espressif.iot.ui.help.HelpDeviceMeshConfigureDialog;
import com.espressif.iot.ui.softap_sta_support.SSSFragmentConfigure;

import android.os.Bundle;

public class HelpSSSFragmentConfigure extends SSSFragmentConfigure implements IEspHelpUISSSUseDevice,
    IEspHelpUISSSMeshConfigure
{
    private HelpSoftApStaSupportActivity mActivity;
    private EspHelpStateMachine mHelpMachine;
    
    @Override
    protected void showConfigureDialog(IEspDeviceNew device)
    {
        if (mHelpMachine.isHelpModeSSSMeshConfigure())
        {
            HelpStepSSSMeshConfigure step = HelpStepSSSMeshConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
            if (step == HelpStepSSSMeshConfigure.SELECT_SOFTAP)
            {
                mHelpMachine.transformState(true);
                onHelpSSSMeshConfigure();
            }
        }
        
        new HelpDeviceMeshConfigureDialog(getActivity(), device).show();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        mActivity = (HelpSoftApStaSupportActivity)getActivity();
        mHelpMachine = EspHelpStateMachine.getInstance();
    }
    
    @Override
    protected void checkHelpTransformNext(boolean suc, int transformCount)
    {
        if (mHelpMachine.isHelpModeUseSSSDevice())
        {
            HelpStepSSSUseDevice step = HelpStepSSSUseDevice.valueOf(mHelpMachine.getCurrentStateOrdinal());
            if (step == HelpStepSSSUseDevice.FIND_SOFTAP)
            {
                for (int i = 0; i < transformCount; i++)
                {
                    mHelpMachine.transformState(suc);
                }
                onHelpUseSSSDevice();
            }
        }
        else if (mHelpMachine.isHelpModeSSSMeshConfigure())
        {
            HelpStepSSSMeshConfigure step = HelpStepSSSMeshConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
            if (step == HelpStepSSSMeshConfigure.FIND_SOFT_AP)
            {
                for (int i = 0; i < transformCount; i++)
                {
                    mHelpMachine.transformState(suc);
                }
                onHelpSSSMeshConfigure();
            }
        }
    }
    
    @Override
    protected boolean checkHelpModeUse(int which)
    {
        switch(which)
        {
            case OPTION_DIRECT_CONNECT:
                return mHelpMachine.isHelpOn() && !mHelpMachine.isHelpModeUseSSSDevice();
            case OPTION_CONFIGURE:
                return mHelpMachine.isHelpOn() && !mHelpMachine.isHelpModeSSSMeshConfigure();
        }
        
        return false;
    }
    
    @Override
    public void onHelpUseSSSDevice()
    {
        mActivity.clearHelpContainer();
        
        HelpStepSSSUseDevice step = HelpStepSSSUseDevice.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_DEVICE:
                break;
            case START_DIRECT_CONNECT:
                break;
            case FIND_SOFTAP:
                mSoftApList.clear();
                mSoftAPAdapter.notifyDataSetChanged();
                mActivity.highlightHelpView(mListView);
                mActivity.gestureAnimHint(R.string.esp_sss_help_use_device_find_softap_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case FOUND_SOFTAP_FAILED:
                mActivity.highlightHelpView(mListView);
                mHelpMachine.retry();
                mActivity.gestureAnimHint(R.string.esp_sss_help_use_device_found_softap_failed_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case SELECT_SOFTAP:
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_select_softap_msg);
                break;
            case CONNECT_SOFTAP_FAILED:
                mHelpMachine.retry();
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_connect_softap_failed_msg);
                break;
            case SOFTAP_NOT_SUPPORT:
                mHelpMachine.retry();
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_not_support_msg);
                break;
            case DEVICE_SELECT:
                mActivity.onHelpUseSSSDevice();
                break;
            case DEVICE_CONTROL:
                break;
            case DEVICE_CONTROL_FAILED:
                break;
            case SUC:
                break;
        }
        
    }

    @Override
    public void onHelpSSSMeshConfigure()
    {
        mActivity.clearHelpContainer();
        
        HelpStepSSSMeshConfigure step = HelpStepSSSMeshConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case FIND_SOFT_AP:
                mSoftApList.clear();
                mSoftAPAdapter.notifyDataSetChanged();
                mActivity.highlightHelpView(mListView);
                mActivity.gestureAnimHint(R.string.esp_sss_help_mesh_configure_find_softap_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case FOUND_SOFTAP_FAILED:
                mActivity.highlightHelpView(mListView);
                mHelpMachine.retry();
                mActivity.gestureAnimHint(R.string.esp_sss_help_mesh_configure_find_softap_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case SELECT_SOFTAP:
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_mesh_select_softap_msg);
                break;
            case SELCET_MESH_CONFIGURE:
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_mesh_select_mesh_configure_msg);
                break;
            default:
                break;
        }
    }
}
