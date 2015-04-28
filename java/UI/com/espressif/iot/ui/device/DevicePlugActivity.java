package com.espressif.iot.ui.device;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.help.ui.IEspHelpUIUsePlug;
import com.espressif.iot.type.device.status.EspStatusPlug;
import com.espressif.iot.type.help.HelpStepUsePlug;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class DevicePlugActivity extends DeviceActivityAbs implements OnClickListener, IEspHelpUIUsePlug
{
    private IEspDevicePlug mDevicePlug;
    
    private CheckBox mPlugSwitch;
    private CheckBox mControlChildCB;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mDevicePlug = (IEspDevicePlug)mIEspDevice;
        
        boolean compatibility = isDeviceCompatibility();
        if (mHelpMachine.isHelpModeUsePlug())
        {
            mHelpMachine.transformState(compatibility);
            onHelpUsePlug();
        }
        if (compatibility)
        {
            executeGet();
        }
    }
    
    @Override
    protected View initControlView()
    {
        View view = getLayoutInflater().inflate(R.layout.device_activity_plug, null);
        mPlugSwitch = (CheckBox)view.findViewById(R.id.plug_switch);
        mPlugSwitch.setOnClickListener(this);
        mControlChildCB = (CheckBox)view.findViewById(R.id.control_child_cb);
        mControlChildCB.setVisibility(mIEspDevice.getIsMeshDevice() ? View.VISIBLE : View.GONE);
        
        return view;
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mPlugSwitch)
        {
            boolean isOn = mPlugSwitch.isChecked();
            EspStatusPlug status = new EspStatusPlug();
            status.setIsOn(isOn);
            
            if (mIEspDevice.getIsMeshDevice() && mControlChildCB.isChecked())
            {
                executePost(status, true);
            }
            else
            {
                executePost(status);
            }
        }
    }
    
    @Override
    protected void executePrepare()
    {
        
    }
    
    @Override
    protected void executeFinish(int command, boolean result)
    {
        boolean isOn = mDevicePlug.getStatusPlug().isOn();
        mPlugSwitch.setChecked(isOn);
        
        if (mHelpMachine.isHelpModeUsePlug() && command == COMMAND_POST)
        {
            mHelpMachine.transformState(true);
            onHelpUsePlug();
        }
    }
    
    @Override
    public void onHelpUsePlug()
    {
        clearHelpContainer();
        
        HelpStepUsePlug step = HelpStepUsePlug.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case START_USE_HELP:
            case FAIL_FOUND_PLUG:
            case FIND_ONLINE:
            case NO_PLUG_ONLINE:
            case PLUG_SELECT:
                break;
                
            case PLUG_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case PLUG_CONTROL:
                highlightHelpView(mPlugSwitch);
                setHelpHintMessage(R.string.esp_help_use_plug_tap_icon_msg);
                break;
            case PLUG_CONTROL_FAILED:
                highlightHelpView(mPlugSwitch);
                setHelpHintMessage(R.string.esp_help_use_plug_control_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_plug_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
}
