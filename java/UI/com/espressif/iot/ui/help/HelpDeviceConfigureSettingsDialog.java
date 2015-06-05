package com.espressif.iot.ui.help;

import android.view.View;

import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.help.statemachine.IEspHelpStateMachine;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.help.HelpStepConfigure;
import com.espressif.iot.ui.configure.DeviceConfigureSettingsDialog;

public class HelpDeviceConfigureSettingsDialog extends DeviceConfigureSettingsDialog
{
    private IEspHelpStateMachine mHelpMachine;
    private HelpDeviceConfigureActivity mActivity;
    
    public HelpDeviceConfigureSettingsDialog(HelpDeviceConfigureActivity activity, IEspDeviceNew device)
    {
        super(activity, device);
        
        mActivity = activity;
        mHelpMachine = EspHelpStateMachine.getInstance();
    }
    
    @Override
    public void show()
    {
        super.show();
        
        if (mHelpMachine.isHelpModeConfigure())
        {
            mMeshContent.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void checkHelpState()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            if (mHelpMachine.getCurrentStateOrdinal() == HelpStepConfigure.SCAN_AVAILABLE_AP.ordinal())
            {
                if (mWifiList.isEmpty())
                {
                    mHelpMachine.transformState(false);
                    mDialog.dismiss();
                }
                else
                {
                    mHelpMachine.transformState(true);
                }
                
                mActivity.onHelpConfigure();
            }
        }
    }
    
    @Override
    protected boolean checkHelpOnCancel()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            if (mHelpMachine.getCurrentStateOrdinal() == HelpStepConfigure.SELECT_CONFIGURED_DEVICE.ordinal())
            {
                mHelpMachine.retry();
                mActivity.onHelpConfigure();
            }
        }
        
        return true;
    }
}
