package com.espressif.iot.ui.device.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.EspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;

public class DevicePlugDialog extends DeviceDialogAbs implements View.OnClickListener
{
    private CheckBox mPlugCB;
    private CheckBox mControlChildCB;
    
    public DevicePlugDialog(Context context, IEspDevice device)
    {
        super(context, device);
    }
    
    @Override
    protected View getContentView(LayoutInflater inflater)
    {
        View view = inflater.inflate(R.layout.device_activity_plug, null);
        
        mPlugCB = (CheckBox)view.findViewById(R.id.plug_switch);
        mPlugCB.setOnClickListener(this);
        mControlChildCB = (CheckBox)view.findViewById(R.id.control_child_cb);
        mControlChildCB.setVisibility(mDevice.getIsMeshDevice() ? View.VISIBLE : View.GONE);
        if (mDevice.getDeviceType() == EspDeviceType.ROOT)
        {
            mControlChildCB.setChecked(true);
            mControlChildCB.setVisibility(View.GONE);
        }
//        mControlChildCB.setVisibility(View.GONE); // hide mesh child checkbox
        
        return view;
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mPlugCB)
        {
            IEspStatusPlug status = new EspStatusPlug();
            status.setIsOn(mPlugCB.isChecked());
            new StatusTask(mControlChildCB.isChecked()).execute(status);
        }
    }
    
    @Override
    protected void onExecuteEnd(boolean suc)
    {
        if (mDevice.getDeviceType() == EspDeviceType.PLUG)
        {
            IEspStatusPlug status;
            if (mDevice instanceof IEspDeviceSSS)
            {
                status = (IEspStatusPlug)((IEspDeviceSSS)mDevice).getDeviceStatus();
            }
            else
            {
                status = ((IEspDevicePlug)mDevice).getStatusPlug();
            }
            mPlugCB.setChecked(status.isOn());
        }
    }
    
}
