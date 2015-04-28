package com.espressif.iot.ui.configure;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.ScanResult;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.help.statemachine.IEspHelpStateMachine;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.help.HelpStepConfigure;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class DeviceConfigureSettingsDialog implements OnClickListener, OnCancelListener
{
    private DeviceConfigureActivity mActivity;
    
    private IEspDeviceNew mDevice;
    
    private AlertDialog.Builder mBuilder;
    
    private Spinner mWifiSpinner;
    
    private WifiAdapter mWifiAdapter;
    
    private List<ScanResult> mWifiList;
    
    private EditText mWifiPasswordEdt;
    
    private CheckBox mShowPasswordCheck;
    
    private IEspUser mUser;
    
    private IEspHelpStateMachine mHelpMachine;
    
    public DeviceConfigureSettingsDialog(DeviceConfigureActivity activity, IEspDeviceNew device)
    {
        mActivity = activity;
        mDevice = device;
        mBuilder = new AlertDialog.Builder(activity);
        mUser = BEspUser.getBuilder().getInstance();
        mHelpMachine = EspHelpStateMachine.getInstance();
    }
    
    public void show()
    {
        mActivity.setIsShowConfigureDialog(true);
        mActivity.removeRefreshMessage();
        
        final AlertDialog dialog;
        
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.device_configure_settings_dialog, null);
        
        mWifiSpinner = (Spinner)view.findViewById(R.id.wifi_spinner);
        mWifiList = EspBaseApiUtil.scan();
        mActivity.removeConfigureDeviceFromWifiList(mWifiList, mDevice);
        mWifiAdapter = new WifiAdapter();
        mWifiSpinner.setAdapter(mWifiAdapter);
        mWifiSpinner.setOnItemSelectedListener(mWifiSelectedListener);
        
        mWifiPasswordEdt = (EditText)view.findViewById(R.id.wifi_password);
        mShowPasswordCheck = (CheckBox)view.findViewById(R.id.wifi_show_password_check);
        mShowPasswordCheck.setOnCheckedChangeListener(mShowPasswordListener);
        mShowPasswordCheck.setChecked(true);
        
        mBuilder.setTitle(mDevice.getSsid());
        mBuilder.setView(view);
        mBuilder.setPositiveButton(R.string.esp_configure_start, this);
        mBuilder.setOnCancelListener(this);
        dialog = mBuilder.show();
        dialog.setCanceledOnTouchOutside(false);
        
        // Get last configured AP
        String lastBssid = mUser.getLastSelectedApBssid();
        String lastPwd = mUser.getLastSelectedApPassword();
        if (lastBssid != null)
        {
            for (int i = 0; i < mWifiList.size(); i++)
            {
                if (mWifiList.get(i).BSSID.equals(lastBssid))
                {
                    mWifiSpinner.setSelection(i);
                    mWifiPasswordEdt.setText(lastPwd);
                    break;
                }
            }
        }
        
        if (mHelpMachine.isHelpModeConfigure())
        {
            if (mHelpMachine.getCurrentStateOrdinal() == HelpStepConfigure.SCAN_AVAILABLE_AP.ordinal())
            {
                if (mWifiList.isEmpty())
                {
                    mHelpMachine.transformState(false);
                    dialog.dismiss();
                }
                else
                {
                    mHelpMachine.transformState(true);
                }
                
                mActivity.onHelpConfigure();
            }
        }
    }
    
    
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        switch (which)
        {
            case AlertDialog.BUTTON_POSITIVE:
                String bssid;
                String ssid;
                String password;
                WifiCipherType wifiType;
                int wifiSelection = mWifiSpinner.getSelectedItemPosition();
                if (wifiSelection >= 0)
                {
                    String currentSSID = EspBaseApiUtil.getWifiConnectedSsid();
                    if (!TextUtils.isEmpty(currentSSID))
                    {
                        mUser.setLastConnectedSsid(currentSSID);
                    }
                    
                    ScanResult sr = mWifiList.get(wifiSelection);
                    bssid = sr.BSSID;
                    ssid = sr.SSID;
                    password = mWifiPasswordEdt.getText().toString();
                    wifiType = WifiCipherType.getWifiCipherType(sr);
                    //TODO mesh
                    ApInfo apInfo = new ApInfo(bssid, ssid, password, wifiType);
                    new DeviceConfigureProgressDialog(mActivity, mDevice, apInfo).show();
                }
                break;
        }
    }
    
    private Spinner.OnItemSelectedListener mWifiSelectedListener = new Spinner.OnItemSelectedListener()
    {
        
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            ScanResult sr = mWifiList.get(position);
            String password = mUser.getApPassword(sr.BSSID);
            mWifiPasswordEdt.setText(password);
        }
        
        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {
        }
        
    };
    
    private CheckBox.OnCheckedChangeListener mShowPasswordListener = new CheckBox.OnCheckedChangeListener()
    {
        
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            if (isChecked)
            {
                mWifiPasswordEdt.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else
            {
                mWifiPasswordEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
        
    };
    
    private class WifiAdapter extends BaseAdapter
    {
        
        @Override
        public int getCount()
        {
            return mWifiList.size();
        }
        
        @Override
        public Object getItem(int position)
        {
            return mWifiList.get(position);
        }
        
        @Override
        public long getItemId(int position)
        {
            return 0;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                LayoutInflater inflater = mActivity.getLayoutInflater();
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
            }
            ScanResult sr = mWifiList.get(position);
            
            TextView wifiSsidTV = (TextView)convertView.findViewById(android.R.id.text1);
            wifiSsidTV.setText(sr.SSID);
            return convertView;
        }
        
    }

    @Override
    public void onCancel(DialogInterface dialog)
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            if (mHelpMachine.getCurrentStateOrdinal() == HelpStepConfigure.SELECT_CONFIGURED_DEVICE.ordinal())
            {
                mHelpMachine.retry();
                mActivity.onHelpConfigure();
            }
        }
        else
        {
            mActivity.setIsShowConfigureDialog(false);
            mActivity.resetRefreshMessage();
        }
    }
}
