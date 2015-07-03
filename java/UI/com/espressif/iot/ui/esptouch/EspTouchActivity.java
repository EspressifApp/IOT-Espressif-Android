package com.espressif.iot.ui.esptouch;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.esptouch.EsptouchTask;
import com.espressif.iot.type.device.esptouch.IEsptouchResult;

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
    private static final String ESPTOUCH_VERSION = "EspTouch v0.3.3";
    
    private static final String SSID_PASSWORD = "ssid_password";
    
    private SharedPreferences mShared;
    private TextView mCurrentSsidTV;
    private Spinner mConfigureSP;
    private EditText mSsidET;
    private EditText mPasswordET;
    private CheckBox mShowPasswordCB;
    private CheckBox mIsSsidHiddenCB;
    private Button mDeletePasswordBtn;
    private Button mConfirmBtn;
    private Spinner mSpinnerTaskCount;
    
    private BaseAdapter mWifiAdapter;
    private volatile List<ScanResult> mScanResultList;
    private volatile List<String> mScanResultSsidList;
    
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
        mSsidET = (EditText)findViewById(R.id.esptouch_ssid);
        mShowPasswordCB = (CheckBox)findViewById(R.id.esptouch_show_pwd);
        mIsSsidHiddenCB = (CheckBox)findViewById(R.id.esptouch_isSsidHidden);
        mDeletePasswordBtn = (Button)findViewById(R.id.esptouch_delete_pwd);
        mConfirmBtn = (Button)findViewById(R.id.esptouch_confirm);
        
        mShowPasswordCB.setOnCheckedChangeListener(this);
        mDeletePasswordBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);
        
        mScanResultList = new ArrayList<ScanResult>();
        mScanResultSsidList = new ArrayList<String>();
        mWifiAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mScanResultSsidList);
        mConfigureSP.setAdapter(mWifiAdapter);
        mConfigureSP.setOnItemSelectedListener(this);
        
        TextView version = (TextView)findViewById(R.id.esptouch_version);
        version.setText(ESPTOUCH_VERSION);
        
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        initSpinner();
    }
    
    private void initSpinner()
    {
        mSpinnerTaskCount = (Spinner) findViewById(R.id.spinnerTaskResultCount);
        int[] spinnerItemsInt = getResources().getIntArray(R.array.esp_touch_taskResultCount);
        int length = spinnerItemsInt.length;
        Integer[] spinnerItemsInteger = new Integer[length];
        for(int i=0;i<length;i++)
        {
            spinnerItemsInteger[i] = spinnerItemsInt[i];
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_list_item_1, spinnerItemsInteger);
        mSpinnerTaskCount.setAdapter(adapter);
        mSpinnerTaskCount.setSelection(1);
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        unregisterReceiver(mReceiver);
    }
    
    private void scanWifi()
    {
        mScanResultList = EspBaseApiUtil.scan();
        mScanResultSsidList.clear();
        for(ScanResult scanResult : mScanResultList)
        {
            mScanResultSsidList.add(scanResult.SSID);
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
            scanWifi();
            mWifiAdapter.notifyDataSetChanged();
            for (int i = 0; i < mScanResultList.size(); i++)
            {
                String ssid = mScanResultList.get(i).SSID;
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
    
    private class ConfigureTask extends AsyncTask<String, Void, List<IEsptouchResult>> implements OnCancelListener
    {
        private Activity mActivity;
        
        private ProgressDialog mDialog;
        
        private EsptouchTask mTask;
        
        private final String mSsid;
        
        private final int mTaskCount;
        
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();
        
        public ConfigureTask(Activity activity, String apSsid, String apBssid, String password, boolean isSsidHidden
            ,int taskCount)
        {
            synchronized(mLock)
            {
                mActivity = activity;
                mSsid = apSsid;
                mTaskCount = taskCount;
                mTask = new EsptouchTask(apSsid, apBssid, password, isSsidHidden, EspTouchActivity.this);
            }
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
        protected List<IEsptouchResult> doInBackground(String... params)
        {
            return mTask.executeForResults(mTaskCount);
        }
        
        @Override
        protected void onPostExecute(List<IEsptouchResult> result)
        {
            mDialog.dismiss();
            
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("Esptouch success, bssid = "
                                + resultInList.getBssid()
                                + ",InetAddress = "
                                + resultInList.getInetAddress()
                                        .getHostAddress() + "\n");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                    Toast.makeText(mActivity, sb.toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mActivity, R.string.esp_esptouch_result_failed, Toast.LENGTH_LONG).show();
                }
            }
        }
        
        @Override
        public void onCancel(DialogInterface dialog)
        {
            synchronized (mLock)
            {
                if (mTask != null)
                {
                    mTask.interrupt();
                    Toast.makeText(mActivity, R.string.esp_esptouch_result_cancel, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    private String scanApBssidBySsid(String apSsid)
    {
        if (TextUtils.isEmpty(apSsid))
        {
            return null;
        }
        String bssid = null;
        for (int retry = 0; bssid == null && retry < 3; retry++)
        {
            scanWifi();
            for (ScanResult scanResult : mScanResultList)
            {
                if (scanResult.SSID.equals(apSsid))
                {
                    bssid = scanResult.BSSID;
                    return bssid;
                }
            }
        }
        return null;
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mConfirmBtn)
        {
            if (!TextUtils.isEmpty(mCurrentSSID))
            {
                String ssid = mSsidET.getText().toString();
                String password = mPasswordET.getText().toString();
                mShared.edit().putString(ssid, password).commit();
                boolean isSsidHidden = mIsSsidHiddenCB.isChecked();
                int taskResultCount = mSpinnerTaskCount.getSelectedItemPosition();
                // find the bssid is scanList
                String bssid = scanApBssidBySsid(ssid);
                if (bssid == null)
                {
                    Toast.makeText(this, getString(R.string.esp_esptouch_cannot_find_ap_hing, ssid), Toast.LENGTH_LONG)
                        .show();
                }
                else
                {
                    new ConfigureTask(this, ssid, bssid, password, isSsidHidden, taskResultCount).execute();
                }
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
        String ssid = mScanResultList.get(position).SSID;
        String password = mShared.getString(ssid, "");
        mPasswordET.setText(password);
        mSsidET.setText(ssid);
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
}
