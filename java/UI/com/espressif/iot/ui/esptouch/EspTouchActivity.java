package com.espressif.iot.ui.esptouch;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.esptouch.EsptouchTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EspTouchActivity extends Activity implements OnClickListener, OnCheckedChangeListener,
    OnItemSelectedListener
{
    private static final String SSID_PASSWORD = "ssid_password";
    
    private SharedPreferences mShared;
    private TextView mCurrentSsidTV;
    private Spinner mConfigureSP;
    private EditText mPasswordET;
    private CheckBox mShowPasswordCB;
    private Button mDeletePasswordBtn;
    private Button mConfirmBtn;
    
    private BaseAdapter mWifiAdapter;
    private List<String> mWifiList;
    
    private String mCurrentSSID;
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
            {
                updateCurrentConnectionInfo();
            }
        }
        
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.esptouch_activity);
        
        mShared = getSharedPreferences(SSID_PASSWORD, Context.MODE_PRIVATE);
        
        mCurrentSsidTV = (TextView)findViewById(R.id.esptouch_current_ssid);
        mConfigureSP = (Spinner)findViewById(R.id.esptouch_configure_wifi);
        mPasswordET = (EditText)findViewById(R.id.esptouch_pwd);
        mShowPasswordCB = (CheckBox)findViewById(R.id.esptouch_show_pwd);
        mDeletePasswordBtn = (Button)findViewById(R.id.esptouch_delete_pwd);
        mConfirmBtn = (Button)findViewById(R.id.esptouch_confirm);
        
        mShowPasswordCB.setOnCheckedChangeListener(this);
        mDeletePasswordBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);
        
        mWifiList = new ArrayList<String>();
        mWifiAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mWifiList);
        mConfigureSP.setAdapter(mWifiAdapter);
        mConfigureSP.setOnItemSelectedListener(this);
        
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        unregisterReceiver(mReceiver);
    }
    
    private void scanWifi(List<String> list)
    {
        list.clear();
        List<ScanResult> wifis = EspBaseApiUtil.scan();
        for (ScanResult sr : wifis)
        {
            list.add(sr.SSID);
        }
    }
    
    private void updateCurrentConnectionInfo()
    {
        mCurrentSSID = EspBaseApiUtil.getWifiConnectedSsid();
        if (mCurrentSSID == null)
        {
            mCurrentSSID = "";
        }
        mCurrentSsidTV.setText(getString(R.string.esp_esptouch_current_ssid, mCurrentSSID));
        
        if (!TextUtils.isEmpty(mCurrentSSID))
        {
            scanWifi(mWifiList);
            mWifiAdapter.notifyDataSetChanged();
            for (int i = 0; i < mWifiList.size(); i++)
            {
                String ssid = mWifiList.get(i);
                if (ssid.equals(mCurrentSSID))
                {
                    mConfigureSP.setSelection(i);
                    break;
                }
            }
        }
        else
        {
            mPasswordET.setText("");
        }
    }
    
    private class ConfigureTask extends AsyncTask<String, Void, Boolean> implements OnCancelListener
    {
        private Activity mActivity;
        
        private ProgressDialog mDialog;
        
        private EsptouchTask mTask;
        
        private final String mSsid;
        
        private final String mPassword;
        
        private boolean mCancelled;
        
        public ConfigureTask(Activity activity, String apSsid, String password)
        {
            mActivity = activity;
            
            mCancelled = false;
            mSsid = apSsid;
            mPassword = password;
            mTask = new EsptouchTask(mSsid, mPassword, EspTouchActivity.this);
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.esp_esptouch_configure_message, mSsid));
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnCancelListener(this);
            mDialog.show();
        }
        
        @Override
        protected Boolean doInBackground(String... params)
        {
            return mTask.execute();
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            mDialog.dismiss();
            
            int toastMsg;
            if (result)
            {
                toastMsg = R.string.esp_esptouch_result_suc;
            }
            else if (mCancelled)
            {
                toastMsg = R.string.esp_esptouch_result_cancel;
            }
            else if (mCurrentSSID.equals(mSsid))
            {
                toastMsg = R.string.esp_esptouch_result_failed;
            }
            else
            {
                toastMsg = R.string.esp_esptouch_result_over;
            }
            
            Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
        }
        
        @Override
        public void onCancel(DialogInterface dialog)
        {
            if (mTask != null)
            {
                mCancelled = true;
                mTask.interrupt();
            }
        }
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mConfirmBtn)
        {
            if (!TextUtils.isEmpty(mCurrentSSID))
            {
                String ssid = mConfigureSP.getSelectedItem().toString();
                String password = mPasswordET.getText().toString();
                mShared.edit().putString(ssid, password).commit();
                
                new ConfigureTask(this, ssid, password).execute();
            }
            else
            {
                Toast.makeText(this, R.string.esp_esptouch_connection_hint, Toast.LENGTH_LONG).show();
            }
        }
        else if (v == mDeletePasswordBtn)
        {
            String selectionSSID = mConfigureSP.getSelectedItem().toString();
            if (!TextUtils.isEmpty(selectionSSID))
            {
                mShared.edit().remove(selectionSSID).commit();
                mPasswordET.setText("");
            }
        }
    }
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (buttonView == mShowPasswordCB)
        {
            if (isChecked)
            {
                mPasswordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else
            {
                mPasswordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        String ssid = mWifiList.get(position);
        String password = mShared.getString(ssid, "");
        mPasswordET.setText(password);
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
}
