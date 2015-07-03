package com.espressif.iot.ui.help;

import android.content.Context;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.help.IEspHelpUISSSMeshConfigure;
import com.espressif.iot.help.statemachine.IEspHelpStateMachine;
import com.espressif.iot.help.ui.IEspHelpUIMeshConfigure;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.help.HelpStepMeshConfigure;
import com.espressif.iot.type.help.HelpStepSSSMeshConfigure;
import com.espressif.iot.ui.configure.DeviceMeshConfigureDialog;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.softap_sta_support.help.HelpSoftApStaSupportActivity;

public class HelpDeviceMeshConfigureDialog extends DeviceMeshConfigureDialog implements IEspHelpUISSSMeshConfigure,
    IEspHelpUIMeshConfigure
{
    private IEspHelpStateMachine mHelpMachine;
    
    public HelpDeviceMeshConfigureDialog(Context context, IEspDeviceNew device)
    {
        this(context, device, false);
    }

    public HelpDeviceMeshConfigureDialog(Context context, IEspDeviceNew device, boolean isActivated)
    {
        super(context, device, isActivated);
        
        mHelpMachine = EspHelpStateMachine.getInstance();
    }
    
    @Override
    protected void checkHelpDissmissDialog()
    {
        if (mHelpMachine.isHelpModeSSSMeshConfigure())
        {
            mHelpMachine.transformState(true);
            onHelpSSSMeshConfigure();
        }
        else if (mHelpMachine.isHelpModeMeshConfigure())
        {
            mHelpMachine.transformState(true);
            onHelpMeshConfigure();
        }
    }
    
    @Override
    public void onHelpSSSMeshConfigure()
    {
        HelpSoftApStaSupportActivity activity = (HelpSoftApStaSupportActivity) getContext();
        activity.clearHelpContainer();
        
        HelpStepSSSMeshConfigure step = HelpStepSSSMeshConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case SUC:
                activity.setHelpFrameDark();
                activity.setHelpHintMessage(R.string.esp_sss_help_mesh_configure_suc);
                activity.setHelpButtonVisible(EspActivityAbs.HELP_BUTTON_EXIT, true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onHelpMeshConfigure()
    {
        HelpDeviceConfigureActivity activity = (HelpDeviceConfigureActivity) getContext();
        activity.clearHelpContainer();
        
        HelpStepMeshConfigure step = HelpStepMeshConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case SUC:
                activity.setHelpFrameDark();
                activity.setHelpHintMessage(R.string.esp_help_mesh_configure_suc_msg);
                activity.setHelpButtonVisible(EspActivityAbs.HELP_BUTTON_EXIT, true);
            default:
                break;
        }
    }
}
