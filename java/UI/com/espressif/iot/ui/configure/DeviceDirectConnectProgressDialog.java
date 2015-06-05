package com.espressif.iot.ui.configure;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.type.device.DeviceInfo;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.BSSIDUtil;

public class DeviceDirectConnectProgressDialog extends DeviceConfigureDialogAbs implements
    DialogInterface.OnCancelListener
{
    private ProgressDialog mDialog;
    
    private String mCacheSsid;
    
    private static final int RESULT_FAILED = -1;
    private static final int RESULT_NOT_SUPPORT = -2;
    private static final int RESULT_FIND_STA_FAILED = -3;
    private static final int RESULT_SUC = 0;
    
    private ConnectTask mConnectTask;
    
    public DeviceDirectConnectProgressDialog(DeviceConfigureActivity activity, IEspDeviceNew device)
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
        private IEspDeviceSSS mDeviceSSS;
        
        @Override
        protected Integer doInBackground(Void... params)
        {
            DeviceInfo deviceInfo = mUser.doActionDeviceNewConnect(mDevice);
            if (isCancelled())
            {
                // check cancel
                return RESULT_FAILED;
            }
            
            if (deviceInfo == null)
            {
                return RESULT_FAILED;
            }
            else if (deviceInfo.isTypeUnknow())
            {
                return RESULT_NOT_SUPPORT;
            }
            else
            {
                mDeviceSSS = getStaDevice(mDevice.getBssid());
                if (mDeviceSSS != null)
                {
                    return RESULT_SUC;
                }
                else
                {
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
                    mActivity.showLocalDeviceDialog(mDeviceSSS, mCacheSsid);
                    break;
            }
        }
        
        private IEspDeviceSSS getStaDevice(String bssid)
        {
            for (int i = 0; i < 5; i++)
            {
                IOTAddress ia = EspBaseApiUtil.discoverDevice(BSSIDUtil.restoreSoftApBSSID(bssid));
                if (ia != null)
                {
                    ia.setSSID(BSSIDUtil.genDeviceNameByBSSID(bssid));
                    return BEspDevice.createSSSDevice(ia);
                }
            }
            
            return null;
        }
    }
    
}
