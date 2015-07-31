package com.espressif.iot.ui.configure;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.db.IOTApDBManager;
import com.espressif.iot.db.greenrobot.daos.ApDB;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class DeviceEspTouchActivity extends EspActivityAbs implements OnCheckedChangeListener, OnClickListener
{
    private IOTApDBManager mIOTApDBManager;
    
    private WifiManager mWifiManager;
    
    private static final String ESPTOUCH_VERSION = "v0.3.4";
    
    private TextView mSsidTV;
    private EditText mPasswordEdT;
    private CheckBox mShowPwdCB;
    private CheckBox mIsHideSsidCB;
    private CheckBox mActivateCB;
    private Button mConfirmBtn;
    private TextView mWifiHintTV;
    private TextView mEspTouchVersionTV;
    
    private IEspUser mUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.esp_esptouch);
        
        mIOTApDBManager = IOTApDBManager.getInstance();
        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mUser = BEspUser.getBuilder().getInstance();
        
        String ssid = EspBaseApiUtil.getWifiConnectedSsid();
        if (ssid == null)
        {
            ssid = "";
        }
        mSsidTV = (TextView)findViewById(R.id.ssid);
        mSsidTV.setText(getString(R.string.esp_sss_esptouch_ssid, ssid));
        
        final String bssid = getConnectionBssid();
        mPasswordEdT = (EditText)findViewById(R.id.password);
        String password = getCurrentWifiPassword(bssid);
        mPasswordEdT.setText(password);
        
        mShowPwdCB = (CheckBox)findViewById(R.id.show_password);
        mShowPwdCB.setOnCheckedChangeListener(this);
        
        mIsHideSsidCB = (CheckBox)findViewById(R.id.is_hidden);
        mActivateCB = (CheckBox)findViewById(R.id.activate);
        if (!mUser.isLogin())
        {
            mActivateCB.setChecked(false);
            mActivateCB.setVisibility(View.GONE);
        }
        
        boolean isWifiConnected = EspBaseApiUtil.isWifiConnected();
        mConfirmBtn = (Button)findViewById(R.id.comfirm_btn);
        mConfirmBtn.setOnClickListener(this);
        mConfirmBtn.setEnabled(isWifiConnected);
        
        mWifiHintTV = (TextView)findViewById(R.id.wifi_connect_hint);
        mWifiHintTV.setVisibility(isWifiConnected ? View.GONE : View.VISIBLE);
        
        mEspTouchVersionTV = (TextView)findViewById(R.id.esptouch_version);
        mEspTouchVersionTV.setText(getString(R.string.esp_esptouch_version, ESPTOUCH_VERSION));
        
        setTitle(R.string.esp_configure_add);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        IntentFilter wifiFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, wifiFilter);
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        
        unregisterReceiver(mWifiReceiver);
    }
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (buttonView == mShowPwdCB)
        {
            if (isChecked)
            {
                mPasswordEdT.setInputType(InputType_PASSWORD_VISIBLE);
            }
            else
            {
                mPasswordEdT.setInputType(InputType_PAssWORD_NORMAL);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v == mConfirmBtn)
        {
            doEspTouch();
        }
    }
    
    private String getConnectionBssid()
    {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null)
        {
            return wifiInfo.getBSSID();
        }
        
        return null;
    }
    
    private String getCurrentWifiPassword(String currentBssid)
    {
        List<ApDB> apDBList = mIOTApDBManager.getAllApDBList();
        for (ApDB ap : apDBList)
        {
            if (ap.getBssid().equals(currentBssid))
            {
                return ap.getPassword();
            }
        }
        
        return "";
    }
    
    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            boolean isWifiConnected = EspBaseApiUtil.isWifiConnected();
            mConfirmBtn.setEnabled(isWifiConnected);
            mWifiHintTV.setVisibility(isWifiConnected ? View.GONE : View.VISIBLE);
            
            String ssid = EspBaseApiUtil.getWifiConnectedSsid();
            String password = "";
            if (ssid == null)
            {
                ssid = "";
            }
            else
            {
                String bssid = getConnectionBssid();
                password = getCurrentWifiPassword(bssid);
            }
            mSsidTV.setText(getString(R.string.esp_sss_esptouch_ssid, ssid));
            mPasswordEdT.setText(password);
        }
        
    };
    
    private void doEspTouch()
    {
        final String bssid = getConnectionBssid();
        final String ssid = EspBaseApiUtil.getWifiConnectedSsid();
        final String password = mPasswordEdT.getText().toString();
        mIOTApDBManager.insertOrReplace(bssid, ssid, password);
        
        final ProgressDialog dialog = new ProgressDialog(this);
        final AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>()
        {
            @Override
            protected Boolean doInBackground(Void... params)
            {
                return mUser.addDevicesSyn(ssid, bssid, password, mIsHideSsidCB.isChecked(), mActivateCB.isChecked());
            }
            
            @Override
            protected void onPostExecute(Boolean result)
            {
                toastEspTouchResult(result);
                dialog.dismiss();
            }
        };
        
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.esp_configure_configuring));
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            getString(android.R.string.cancel),
            new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    mUser.cancelAllAddDevices();
                    task.cancel(true);
                }
            });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                esptouchOver();
            }
        });
        dialog.show();
        
        task.execute();
    }
    
    private void esptouchOver()
    {
        setResult(Activity.RESULT_OK);
        finish();
    }
    
    private void toastEspTouchResult(boolean suc)
    {
        int msgRes = suc ? R.string.esp_configure_result_success : R.string.esp_configure_result_failed;
        Toast.makeText(this, msgRes, Toast.LENGTH_LONG).show();
    }
}
