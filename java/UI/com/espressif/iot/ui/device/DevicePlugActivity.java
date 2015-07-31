package com.espressif.iot.ui.device;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.type.device.status.EspStatusPlug;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class DevicePlugActivity extends DeviceActivityAbs implements OnClickListener
{
    private IEspDevicePlug mDevicePlug;
    
    protected CheckBox mPlugSwitch;
    private CheckBox mControlChildCB;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mDevicePlug = (IEspDevicePlug)mIEspDevice;
        
        boolean compatibility = isDeviceCompatibility();
        checkHelpModePlug(compatibility);
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
        mControlChildCB.setVisibility(View.GONE); // hide mesh child checkbox
        
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
        
        checkHelpExecuteFinish(command, result);
    }
    
    protected void checkHelpModePlug(boolean compatibility)
    {
    }
    
    protected void checkHelpExecuteFinish(int command, boolean result)
    {
    }
}
