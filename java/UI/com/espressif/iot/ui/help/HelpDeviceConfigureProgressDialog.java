package com.espressif.iot.ui.help;

import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.help.statemachine.IEspHelpStateMachine;
import com.espressif.iot.help.ui.IEspHelpUIConfigure;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.help.HelpStepConfigure;
import com.espressif.iot.ui.configure.ApInfo;
import com.espressif.iot.ui.configure.DeviceConfigureProgressDialog;

public class HelpDeviceConfigureProgressDialog extends DeviceConfigureProgressDialog
{
    private HelpDeviceConfigureActivity mActivity;
    private IEspHelpStateMachine mHelpMachine;
    
    public HelpDeviceConfigureProgressDialog(HelpDeviceConfigureActivity activity, IEspDeviceNew device, ApInfo apInfo)
    {
        super(activity, device, apInfo);
        
        mActivity = activity;
        mHelpMachine = EspHelpStateMachine.getInstance();
    }
    
    @Override
    protected void checkHelpState()
    {
        if (mHelpMachine.isHelpModeConfigure()
            && mHelpMachine.getCurrentStateOrdinal() == HelpStepConfigure.SELECT_CONFIGURED_DEVICE.ordinal())
        {
            mHelpMachine.setConnectedApSsid(mApInfo.getSsid());
        }
    }
    
    @Override
    protected void checkHelpActivating()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            mActivity.setResult(IEspHelpUIConfigure.RESULT_HELP_CONFIGURE);
        }
    }
    
    @Override
    protected void checkHelpDeleted()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            HelpStepConfigure step = HelpStepConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
            switch (step)
            {
                case FAIL_CONNECT_DEVICE:
                    mDialog.dismiss();
                    mActivity.onHelpConfigure();
                    break;
                default:
                    break;
            }
        }
    }
}
