package com.espressif.iot.ui.configure;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.ScanResult;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.widget.adapter.WifiAdapter;
import com.espressif.iot.util.BSSIDUtil;

public class DeviceSoftAPConfigureSettingsDialog extends DeviceSoftAPConfigureDialogAbs
    implements OnClickListener, OnCancelListener
{
    private AlertDialog mDialog;
    
    private Spinner mWifiSpinner;
    private WifiAdapter mWifiAdapter;
    private List<ScanResult> mWifiList;
    
    private EditText mWifiPasswordEdt;
    private CheckBox mShowPasswordCheck;
    
    public DeviceSoftAPConfigureSettingsDialog(DeviceSoftAPConfigureActivity activity, IEspDeviceNew device)
    {
        super(activity, device);
    }
    
    public void show()
    {
        stopAutoRefresh();
        
        View view = View.inflate(mActivity, R.layout.device_configure_select_dialog, null);
        
        mWifiSpinner = (Spinner)view.findViewById(R.id.wifi_spinner);
        mWifiList = mUser.scanApList(true);
        mWifiAdapter = new WifiAdapter(mActivity, mWifiList);
        mWifiAdapter.addFilter(BSSIDUtil.restoreSoftApBSSID(mDevice.getBssid()));
        mWifiSpinner.setAdapter(mWifiAdapter);
        mWifiSpinner.setOnItemSelectedListener(mWifiSelectedListener);
        
        mWifiPasswordEdt = (EditText)view.findViewById(R.id.wifi_password);
        mShowPasswordCheck = (CheckBox)view.findViewById(R.id.wifi_show_password_check);
        mShowPasswordCheck.setOnCheckedChangeListener(mShowPasswordListener);
        mShowPasswordCheck.setChecked(true);
        
        mDialog =
            new AlertDialog.Builder(mActivity).setTitle(mDevice.getSsid())
                .setView(view)
                .setPositiveButton(R.string.esp_configure_start, this)
                .create();
        mDialog.setOnCancelListener(this);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        
        // Get last configured AP
        String lastBssid = mUser.getLastSelectedApBssid();
        String lastPwd = mUser.getLastSelectedApPassword();
        if (lastBssid != null)
        {
            for (int i = 0; i < mWifiList.size(); i++)
            {
                if (mWifiList.get(i).BSSID.equals(lastBssid))
                {
                    // find last configured AP
                    mWifiSpinner.setSelection(i);
                    mWifiPasswordEdt.setText(lastPwd);
                    break;
                }
            }
        }
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        switch (which)
        {
            case AlertDialog.BUTTON_POSITIVE:
                configure();
                break;
        }
    }
    
    private void configure()
    {
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
            
            ApInfo apInfo = new ApInfo(bssid, ssid, password, wifiType);
            mActivity.showConfigureProgressDialog(mDevice, apInfo);
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
                mWifiPasswordEdt.setInputType(EspActivityAbs.InputType_PASSWORD_VISIBLE);
            }
            else
            {
                mWifiPasswordEdt.setInputType(EspActivityAbs.InputType_PASSWORD_NORMAL);
            }
        }
        
    };
    
    @Override
    public void onCancel(DialogInterface dialog)
    {
        resetAutoRefresh();
    }
}
