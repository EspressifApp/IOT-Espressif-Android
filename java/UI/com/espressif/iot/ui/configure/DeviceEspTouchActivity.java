package com.espressif.iot.ui.configure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.db.IOTApDBManager;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.object.db.IApDB;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.EspStrings;
import com.google.zxing.qrcode.ui.ShareCaptureActivity;

public class DeviceEspTouchActivity extends EspActivityAbs implements OnCheckedChangeListener, OnClickListener,
    OnMenuItemClickListener
{
    private IOTApDBManager mIOTApDBManager;
    
    private WifiManager mWifiManager;
    private LocalBroadcastManager mBraodcastManager;
    
    private static final String ESPTOUCH_VERSION = "v0.3.4.3";
    
    private TextView mSsidTV;
    private EditText mPasswordEdT;
    private CheckBox mShowPwdCB;
    private CheckBox mIsHideSsidCB;
    private CheckBox mActivateCB;
    private CheckBox mMultipleDevicesCB;
    private Button mConfirmBtn;
    private TextView mWifiHintTV;
    private TextView mEspTouchVersionTV;
    private View mEsptouchContentView;
    private TextView mEsptouchContentCountTV;
    private Button mEsptouchContentDoneBtn;
    
    private IEspUser mUser;
    
    private static final int POPUPMENU_ID_GET_SHARE = 1;
    private static final int POPUPMENU_ID_SOFTAP_CONFIGURE = 2;
    
    private List<String> mEsptouchDeviceBssidList = new ArrayList<String>();
    private AtomicInteger mEsptouchDeivceRegisterCount = new AtomicInteger();
    private List<String> mRegisteredDeviceBssidList = new ArrayList<String>();
    private List<String> mRegisteredDeviceNameList = new ArrayList<String>();
    
    private static final int REQUEST_SOFTAP_CONFIGURE = 10;
    private static final int REQUEST_GET_SHARED = 12;
    
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
        mSsidTV.setText(getString(R.string.esp_esptouch_ssid, ssid));
        
        final String bssid = getConnectionBssid();
        mPasswordEdT = (EditText)findViewById(R.id.password);
        String password = getCurrentWifiPassword(bssid);
        mPasswordEdT.setText(password);
        
        mShowPwdCB = (CheckBox)findViewById(R.id.show_password);
        mShowPwdCB.setOnCheckedChangeListener(this);
        
        mIsHideSsidCB = (CheckBox)findViewById(R.id.is_hidden);
        mActivateCB = (CheckBox)findViewById(R.id.activate);
        mMultipleDevicesCB = (CheckBox)findViewById(R.id.multiple_devices);
        
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
        setTitleRightIcon(R.drawable.esp_icon_menu_moreoverflow);
        
        mEsptouchContentView = View.inflate(this, R.layout.esptouch_dialog_content, null);
        mEsptouchContentCountTV = (TextView)mEsptouchContentView.findViewById(R.id.esptouch_tv_count);
        mEsptouchContentDoneBtn = (Button)mEsptouchContentView.findViewById(R.id.esptouch_btn_done);
        
        // register Receiver
        mBraodcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter(EspStrings.Action.ESPTOUCH_CONTACTING_SERVER);
        filter.addAction(EspStrings.Action.ESPTOUCH_DEVICE_FOUND);
        filter.addAction(EspStrings.Action.ESPTOUCH_DEVICE_REGISTERED);
        filter.addAction(EspStrings.Action.ESPTOUCH_REGISTERING_DEVICES);
        mBraodcastManager.registerReceiver(mEsptouchReceiver, filter);
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
    protected void onDestroy()
    {
        super.onDestroy();
        mBraodcastManager.unregisterReceiver(mEsptouchReceiver);
    }
    
    @Override
    protected void onTitleRightIconClick(View rightIcon)
    {
        PopupMenu popupMenu = new PopupMenu(this, rightIcon);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, POPUPMENU_ID_GET_SHARE, 0, R.string.esp_esptouch_menu_get_share);
        menu.add(Menu.NONE, POPUPMENU_ID_SOFTAP_CONFIGURE, 0, R.string.esp_esptouch_menu_softap_configure);
//        menu.add(Menu.NONE, POPUPMENU_ID_BROWSER_CONFIGURE, 0, R.string.esp_esptouch_menu_browser_configure);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
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
                mPasswordEdT.setInputType(InputType_PASSWORD_NORMAL);
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
    
    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch(item.getItemId())
        {
            case POPUPMENU_ID_GET_SHARE:
                startActivityForResult(new Intent(this, ShareCaptureActivity.class), REQUEST_GET_SHARED);
                return true;
            case POPUPMENU_ID_SOFTAP_CONFIGURE:
                startActivityForResult(new Intent(this, DeviceSoftAPConfigureActivity.class), REQUEST_SOFTAP_CONFIGURE);
                return true;
        }
        return false;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case REQUEST_SOFTAP_CONFIGURE:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
            case REQUEST_GET_SHARED:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
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
        List<IApDB> apDBList = mIOTApDBManager.getAllApDBList();
        for (IApDB ap : apDBList)
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
            mSsidTV.setText(getString(R.string.esp_esptouch_ssid, ssid));
            mPasswordEdT.setText(password);
        }
        
    };
    
    private BroadcastReceiver mEsptouchReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            final Resources resources = getResources();
            String text = null;
            if(action.equals(EspStrings.Action.ESPTOUCH_DEVICE_FOUND))
            {
                int quantity = mEsptouchDeviceBssidList.size();
                text = resources.getQuantityString(R.plurals.esp_esptouch_device_found, quantity, quantity);
                mEsptouchContentCountTV.setText(text);
            }
            else if (action.equals(EspStrings.Action.ESPTOUCH_CONTACTING_SERVER))
            {
                // when contacting server, make done button gone
                mEsptouchContentDoneBtn.setVisibility(View.GONE);
                text = resources.getString(R.string.esp_esptouch_contacting_server);
                mEsptouchContentCountTV.setText(text);
            }
            else if(action.equals(EspStrings.Action.ESPTOUCH_REGISTERING_DEVICES))
            {
                text = resources.getString(R.string.esp_esptouch_registering_devices);
                mEsptouchContentCountTV.setText(text);
            }
            else if(action.equals(EspStrings.Action.ESPTOUCH_DEVICE_REGISTERED))
            {
                String deviceBssid = intent.getStringExtra(EspStrings.Key.DEVICE_BSSID);
                mRegisteredDeviceBssidList.add(deviceBssid);
                String deviceName = intent.getStringExtra(EspStrings.Key.DEVICE_NAME);
                mRegisteredDeviceNameList.add(deviceName);
                int quantity = mEsptouchDeivceRegisterCount.incrementAndGet();
                text = resources.getQuantityString(R.plurals.esp_esptouch_device_registered, quantity, quantity);
                mEsptouchContentCountTV.setText(text);
            }
        }
        
    };
    
    private IEsptouchListener mEsptouchListener = new IEsptouchListener()
    {
        
        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result)
        {
            runOnUiThread(new Runnable()
            {
                
                @Override
                public void run()
                {
                    String bssid = BSSIDUtil.restoreBSSID(result.getBssid());
                    mEsptouchDeviceBssidList.add(bssid);
                    Context context = EspApplication.sharedInstance().getApplicationContext();
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                    Intent intent = new Intent(EspStrings.Action.ESPTOUCH_DEVICE_FOUND);
                    broadcastManager.sendBroadcast(intent);
                }
                
            });
        }
    };
    
    private List<String> filterConfigureDeviceNameList()
    {
        List<String> deviceNameList = new ArrayList<String>();
        
        for (int i = 0; i < mRegisteredDeviceBssidList.size(); ++i)
        {
            String deviceBssid = mRegisteredDeviceBssidList.get(i);
            if (mEsptouchDeviceBssidList.contains(deviceBssid))
            {
                String deviceName = mRegisteredDeviceNameList.get(i);
                deviceNameList.add(deviceName);
                mEsptouchDeviceBssidList.remove(deviceBssid);
                break;
            }
        }
        
        return deviceNameList;
    }
    
    private void doEspTouch()
    {
        mRegisteredDeviceBssidList.clear();
        mRegisteredDeviceNameList.clear();
        mEsptouchDeivceRegisterCount.set(0);
        mEsptouchDeviceBssidList.clear();
        final String bssid = getConnectionBssid();
        final String ssid = EspBaseApiUtil.getWifiConnectedSsid();
        final String password = mPasswordEdT.getText().toString();
        mIOTApDBManager.insertOrReplace(bssid, ssid, password);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        
        final AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>()
        {
            @Override
            protected void onPreExecute()
            {
                boolean isMultipleDevices = mMultipleDevicesCB.isChecked();
                if(!isMultipleDevices)
                {
                    mEsptouchContentDoneBtn.setVisibility(View.GONE);
                }
            }
            
            @Override
            protected Boolean doInBackground(Void... params)
            {
                boolean isMultipleDevices = mMultipleDevicesCB.isChecked();
                boolean isSsidHidden = mIsHideSsidCB.isChecked();
                boolean requiredActivate = mActivateCB.isChecked();
                if (isMultipleDevices)
                    return mUser.addDevicesSyn(ssid, bssid, password, isSsidHidden, requiredActivate, mEsptouchListener);
                else
                    return mUser.addDeviceSyn(ssid, bssid, password, isSsidHidden, requiredActivate, mEsptouchListener);
            }
            
            @Override
            protected void onPostExecute(Boolean result)
            {
                dialog.dismiss();
                // show dialog, add device list
                List<String> deviceNameList = filterConfigureDeviceNameList();
                List<String> configuredDeviceBssidList = mEsptouchDeviceBssidList;
                if (deviceNameList.isEmpty() && configuredDeviceBssidList.isEmpty()) {
                    toastEspTouchResult(false);
                }
                String title = getString(R.string.esp_esptouch_result_title);
                StringBuilder message = new StringBuilder();
                message.append(getString(R.string.esp_esptouch_message_cloud_title, deviceNameList.size()));
                for (String deviceName : deviceNameList)
                {
                    message.append(deviceName);
                    message.append("\n");
                }
                
                message.append("\n");
                message.append(getString(R.string.esp_esptouch_message_wifi_title, configuredDeviceBssidList.size()));
                for (String deviceBssid : configuredDeviceBssidList)
                {
                    message.append(BSSIDUtil.genDeviceNameByBSSID(deviceBssid));
                    message.append("\n");
                }
                
                new AlertDialog.Builder(DeviceEspTouchActivity.this).setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            esptouchOver();
                        }
                    })
                    .show();
            }
        };
        
        dialog.setCancelable(false);
        dialog.setView(mEsptouchContentView);
        mEsptouchContentDoneBtn.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                mUser.doneAllAddDevices();
            }
        });
        final Resources resources = getResources();
        final String text = resources.getString(R.string.esp_esptouch_discovering_devices);
        mEsptouchContentCountTV.setText(text);
        dialog.setTitle(getString(R.string.esp_configure_configuring));
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            getString(android.R.string.cancel),
            new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    mUser.cancelAllAddDevices();
                    task.cancel(true);
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
