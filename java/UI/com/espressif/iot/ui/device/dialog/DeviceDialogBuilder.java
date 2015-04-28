package com.espressif.iot.ui.device.dialog;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.EspDeviceType;

import android.content.Context;

public class DeviceDialogBuilder
{
    private Context mContext;
    private IEspDevice mDevice;
    
    public DeviceDialogBuilder(Context context, IEspDevice device)
    {
        mContext = context;
        mDevice = device;
    }
    
    public EspDeviceDialogInterface show()
    {
        EspDeviceDialogInterface dialog = create();
        create().show();
        
        return dialog;
    }
    
    public EspDeviceDialogInterface create()
    {
        switch(mDevice.getDeviceType())
        {
            case LIGHT:
                return new DeviceLightDialog(mContext, mDevice);
            case PLUG:
                return new DevicePlugDialog(mContext, mDevice);
            case PLUGS:
                return new DevicePlugsDialog(mContext, mDevice);
            case REMOTE:
                return new DeviceRemoteDialog(mContext, mDevice);
            case ROOT:
                throw new IllegalArgumentException("Please use #createRootDialog(EspDeviceType type)");
            case FLAMMABLE:
            case HUMITURE:
            case VOLTAGE:
            case NEW:
                throw new IllegalArgumentException("Not support such type device");
        }
        
        return null;
    }
    
    public EspDeviceDialogInterface showRootDialog(EspDeviceType type)
    {
        EspDeviceDialogInterface dialog = createRootDialog(type);
        dialog.show();
        
        return dialog;
    }
    
    public EspDeviceDialogInterface createRootDialog(EspDeviceType type)
    {
        if (mDevice.getDeviceType() != EspDeviceType.ROOT)
        {
            throw new IllegalArgumentException("This function only support Root type device");
        }
        
        switch(type)
        {
            case LIGHT:
                return new DeviceLightDialog(mContext, mDevice);
            case PLUG:
                return new DevicePlugDialog(mContext, mDevice);
            case REMOTE:
                return new DeviceRemoteDialog(mContext, mDevice);
            case PLUGS:
                return new DevicePlugsDialog(mContext, mDevice);
                
            case FLAMMABLE:
            case HUMITURE:
            case VOLTAGE:
            case NEW:
            case ROOT:
                throw new IllegalArgumentException("Not support such type dialog: " + type);
        }
        
        return null;
    }
}
