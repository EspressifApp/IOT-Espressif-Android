package com.espressif.iot.ui.configure;

import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public abstract class DeviceConfigureDialogAbs
{
    protected IEspUser mUser;
    protected DeviceConfigureActivity mActivity;
    protected IEspDeviceNew mDevice;
    
    public DeviceConfigureDialogAbs(DeviceConfigureActivity activity, IEspDeviceNew device)
    {
        mUser = BEspUser.getBuilder().getInstance();
        mActivity = activity;
        mDevice = device;
    }
    
    public abstract void show();
    
    protected void stopAutoRefresh()
    {
        mActivity.setIsShowConfigureDialog(true);
        mActivity.removeRefreshMessage();
    }
    
    protected void resetAutoRefresh()
    {
        mActivity.setIsShowConfigureDialog(false);
        mActivity.resetRefreshMessage();
    }
}
