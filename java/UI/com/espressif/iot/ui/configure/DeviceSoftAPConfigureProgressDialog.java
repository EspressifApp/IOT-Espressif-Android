package com.espressif.iot.ui.configure;

import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

public class DeviceSoftAPConfigureProgressDialog extends DeviceSoftAPConfigureDialogAbs
    implements OnDismissListener, OnCancelListener
{
    private final Logger log = Logger.getLogger(DeviceSoftAPConfigureProgressDialog.class);
    
    private ApInfo mApInfo;
    
    private ProgressDialog mDialog;
    
    private LocalBroadcastManager mBroadcastManager;
    
    public DeviceSoftAPConfigureProgressDialog(DeviceSoftAPConfigureActivity activity, IEspDeviceNew device, ApInfo apInfo)
    {
        super(activity, device);
        
        mApInfo = apInfo;
        mBroadcastManager = LocalBroadcastManager.getInstance(mActivity);
    }
    
    public void show()
    {
        stopAutoRefresh();
        
        IntentFilter filter = new IntentFilter(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE);
        mBroadcastManager.registerReceiver(mReceiver, filter);
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(mActivity.getString(R.string.esp_configure_configuring));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnDismissListener(this);
        mDialog.setOnCancelListener(this);
        mDialog.show();
        
        mUser.saveApInfoInDB(mApInfo.bssid, mApInfo.ssid, mApInfo.password, mDevice.getBssid());
        mUser.doActionConfigure(mDevice, mApInfo.ssid, mApInfo.type, mApInfo.password);
    }
    
    @Override
    public void onDismiss(DialogInterface dialog)
    {
        Log.i("DeviceConfigureProgressDialog",
            "@@@@@@@@@@@@@@@@@@@@@@@@@@@@onDismiss@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        
        mBroadcastManager.unregisterReceiver(mReceiver);
        
        resetAutoRefresh();
    }
    
    @Override
    public void onCancel(DialogInterface dialog)
    {
        Log.i("DeviceConfigureProgressDialog",
            "@@@@@@@@@@@@@@@@@@@@@@@@@@@@onCancel@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        
        mDevice.cancel(true);
    }
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        
        @Override
        public void onReceive(Context context, Intent intent)
        {
            log.error("onReceive = " + intent.getAction());
            log.error("Configure bc before " + mDevice);
            mUser.doActionDevicesUpdated(true);
            log.error("Configure bc after " + mDevice);
            
            if (mDevice.getDeviceState().isStateActivating())
            {
                log.debug("mDevice.getDeviceState().isStateActivating()");
                
                // Configure success
                mDialog.setMessage(mActivity.getString(R.string.esp_configure_result_success));
                mDialog.dismiss();
                mActivity.setResult(Activity.RESULT_OK);
                mActivity.finish();
                
                connectAP();
                log.debug("mDevice.getDeviceState().isStateActivating()");
            }
            else if (mDevice.getDeviceState().isStateDeleted())
            {
                // Configure failed
                mDialog.setMessage(mActivity.getString(R.string.esp_configure_result_failed));
                mDialog.setCancelable(true);
                mDialog.setCanceledOnTouchOutside(true);
            }
        }
        
        private void connectAP()
        {
            List<IEspDevice> userDeviceList = mUser.getDeviceList();
            for (IEspDevice device: userDeviceList)
            {
                if (BSSIDUtil.isEqualIgnore2chars(device.getBssid(), mApInfo.bssid))
                {
                    // connect last ap
                    String lastSSID = mUser.getLastConnectedSsid();
                    if (!TextUtils.isEmpty(lastSSID))
                    {
                        EspBaseApiUtil.enableConnected(lastSSID);
                    }
                    return;
                }
            }
            // Connect the configured wifi
            EspBaseApiUtil.enableConnected(mApInfo.ssid, mApInfo.type, mApInfo.password);
        }
    };
}
