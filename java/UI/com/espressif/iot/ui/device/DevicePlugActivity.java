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
    
    private CheckBox mPlugSwitch;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mDevicePlug = (IEspDevicePlug)mIEspDevice;
        
        boolean compatibility = isDeviceCompatibility();
        if (compatibility && !isDeviceArray())
        {
            executeGet();
        }
    }
    
    @Override
    protected View initControlView()
    {
        View view = View.inflate(this, R.layout.device_activity_plug, null);
        mPlugSwitch = (CheckBox)view.findViewById(R.id.plug_switch);
        mPlugSwitch.setOnClickListener(this);
        
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
            
            if (isDeviceArray())
            {
                mDevicePlug.setStatusPlug(status);
            }
            
            executePost(status);
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
    }
}
