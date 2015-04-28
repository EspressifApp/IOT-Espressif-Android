package com.espressif.iot.ui.configure;

import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.help.statemachine.IEspHelpStateMachine;
import com.espressif.iot.help.ui.IEspHelpUIConfigure;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.help.HelpStepConfigure;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.EspStrings;

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

public class DeviceConfigureProgressDialog implements OnDismissListener, OnCancelListener
{
    private final Logger log = Logger.getLogger(DeviceConfigureProgressDialog.class);
    
    private IEspUser mUser;
    
    private DeviceConfigureActivity mActivity;
    
    private IEspDeviceNew mDevice;
    
    private ApInfo mApInfo;
    
    private ProgressDialog mDialog;
    
    private IEspHelpStateMachine mHelpMachine;
    
    private LocalBroadcastManager mBroadcastManager;
    
    public DeviceConfigureProgressDialog(DeviceConfigureActivity activity, IEspDeviceNew device, ApInfo apInfo)
    {
        mUser = BEspUser.getBuilder().getInstance();
        mActivity = activity;
        mDevice = device;
        mApInfo = apInfo;
        
        mBroadcastManager = LocalBroadcastManager.getInstance(mActivity);
        
        mHelpMachine = EspHelpStateMachine.getInstance();
    }
    
    public void show()
    {
        mActivity.setIsShowConfigureDialog(true);
        mActivity.removeRefreshMessage();
        
        IntentFilter filter = new IntentFilter(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE);
        mBroadcastManager.registerReceiver(mReceiver, filter);
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(mActivity.getString(R.string.esp_configure_configuring));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnDismissListener(this);
        mDialog.setOnCancelListener(this);
        mDialog.show();
        if (mHelpMachine.isHelpModeConfigure()
            && mHelpMachine.getCurrentStateOrdinal() == HelpStepConfigure.SELECT_CONFIGURED_DEVICE.ordinal())
        {
            mHelpMachine.setConnectedApSsid(mApInfo.ssid);
        }
        
        mUser.saveApInfoInDB(mApInfo.bssid, mApInfo.ssid, mApInfo.password, mDevice.getBssid());
        mUser.doActionConfigure(mDevice, mApInfo.ssid, mApInfo.type, mApInfo.password);
    }
    
    @Override
    public void onDismiss(DialogInterface dialog)
    {
        Log.i("DeviceConfigureProgressDialog",
            "@@@@@@@@@@@@@@@@@@@@@@@@@@@@onDismiss@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        
        mBroadcastManager.unregisterReceiver(mReceiver);
        
        mActivity.setIsShowConfigureDialog(false);
        mActivity.resetRefreshMessage();
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
                if (mHelpMachine.isHelpModeConfigure())
                {
                    mActivity.setResult(IEspHelpUIConfigure.RESULT_HELP_CONFIGURE);
                }
                
                // Configure success
                mDialog.setMessage(mActivity.getString(R.string.esp_configure_result_success));
                mDialog.dismiss();
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
                
                if (mHelpMachine.isHelpModeConfigure())
                {
                    HelpStepConfigure step = HelpStepConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
                    log.info("Receive help step = " + step);
                    switch (step)
                    {
                        case FAIL_CONNECT_DEVICE:
                            mDialog.dismiss();
                            mActivity.onHelpConfigure();
                            break;
                        default:
                            break;
                    }
                }
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
