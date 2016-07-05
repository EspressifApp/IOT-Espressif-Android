package com.espressif.iot.ui.configure;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.BSSIDUtil;

public class DeviceDirectConnectProgressDialog extends DeviceSoftAPConfigureDialogAbs implements
    DialogInterface.OnCancelListener
{
    private ProgressDialog mDialog;
    
    private String mCacheSsid;
    
    private static final int RESULT_FAILED = -1;
    private static final int RESULT_NOT_SUPPORT = -2;
    private static final int RESULT_FIND_STA_FAILED = -3;
    private static final int RESULT_SUC = 0;
    
    private ConnectTask mConnectTask;
    
    public DeviceDirectConnectProgressDialog(DeviceSoftAPConfigureActivity activity, IEspDeviceNew device)
    {
        super(activity, device);
    }
    
    @Override
    public void show()
    {
        stopAutoRefresh();
        
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(mActivity.getString(R.string.esp_configure_direct_connecting));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnCancelListener(this);
        mDialog.show();
        
        mCacheSsid = EspBaseApiUtil.getWifiConnectedSsid();
        
        mConnectTask = new ConnectTask();
        mConnectTask.execute();
    }
    
    @Override
    public void onCancel(DialogInterface dialog)
    {
        if (mConnectTask != null)
        {
            mConnectTask.cancel(true);
        }
        
        if (!TextUtils.isEmpty(mCacheSsid))
        {
            EspBaseApiUtil.enableConnected(mCacheSsid);
        }
        
        resetAutoRefresh();
    }
    
    private class ConnectTask extends AsyncTask<Void, Void, Integer>
    {
        private IOTAddress mIOTAddress;
        
        @Override
        protected Integer doInBackground(Void... params)
        {
            IOTAddress deviceIOTAddress = mUser.doActionDeviceNewConnect(mDevice);
            if (isCancelled())
            {
                // check cancel
                return RESULT_FAILED;
            }
            
            if (deviceIOTAddress == null)
            {
                return RESULT_FAILED;
            }
            else if (!deviceIOTAddress.getDeviceTypeEnum().isLocalSupport())
            {
                return RESULT_NOT_SUPPORT;
            }
            else
            {
                mIOTAddress = getIOTAddress(mDevice.getBssid());
                if (mIOTAddress != null) {
                    return RESULT_SUC;
                } else {
                    return RESULT_FIND_STA_FAILED;
                }
            }
        }
        
        @Override
        protected void onPostExecute(Integer result)
        {
            switch (result)
            {
                case RESULT_FAILED:
                    Toast.makeText(mActivity, R.string.esp_configure_direct_result_failed, Toast.LENGTH_LONG).show();
                    mDialog.cancel();
                    break;
                case RESULT_NOT_SUPPORT:
                    Toast.makeText(mActivity, R.string.esp_configure_direct_result_not_support, Toast.LENGTH_LONG)
                        .show();
                    mDialog.cancel();
                    break;
                case RESULT_FIND_STA_FAILED:
                    Toast.makeText(mActivity, R.string.esp_configure_direct_result_not_found, Toast.LENGTH_LONG).show();
                    mDialog.cancel();
                    break;
                case RESULT_SUC:
                    Toast.makeText(mActivity, R.string.esp_configure_direct_result_suc, Toast.LENGTH_LONG).show();
                    mDialog.dismiss();
                    mActivity.showLocalDevice(mIOTAddress, mCacheSsid);
                    break;
            }
        }
        
        private IOTAddress getIOTAddress(String bssid)
        {
            for (int i = 0; i < 5; i++)
            {
                IOTAddress ia = EspBaseApiUtil.discoverDevice(BSSIDUtil.restoreSoftApBSSID(bssid));
                if (ia != null)
                {
                    ia.setSSID(BSSIDUtil.genDeviceNameByBSSID(bssid));
                    return ia;
                }
            }
            
            return null;
        }
    }
    
}
