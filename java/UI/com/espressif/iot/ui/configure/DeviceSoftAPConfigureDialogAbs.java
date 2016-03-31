package com.espressif.iot.ui.configure;

import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public abstract class DeviceSoftAPConfigureDialogAbs
{
    protected IEspUser mUser;
    protected DeviceSoftAPConfigureActivity mActivity;
    protected IEspDeviceNew mDevice;
    
    public DeviceSoftAPConfigureDialogAbs(DeviceSoftAPConfigureActivity activity, IEspDeviceNew device)
    {
        mUser = BEspUser.getBuilder().getInstance();
        mActivity = activity;
        mDevice = device;
    }
    
    public abstract void show();
    
    /**
     * Stop auto refresh SoftAP list
     */
    protected void stopAutoRefresh()
    {
        mActivity.setIsShowConfigureDialog(true);
        mActivity.removeRefreshMessage();
    }
    
    /**
     * Restart auto refresh SoftAP list
     */
    protected void resetAutoRefresh()
    {
        mActivity.setIsShowConfigureDialog(false);
        mActivity.resetRefreshMessage();
    }
}
