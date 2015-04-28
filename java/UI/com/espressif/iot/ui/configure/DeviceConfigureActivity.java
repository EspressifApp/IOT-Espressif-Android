package com.espressif.iot.ui.configure;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.help.ui.IEspHelpUIConfigure;
import com.espressif.iot.type.device.DeviceInfo;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.help.HelpStepConfigure;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.ui.EspActivityAbs;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.EspStrings;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceConfigureActivity extends EspActivityAbs implements OnItemClickListener,
    OnRefreshListener<ListView>, OnItemLongClickListener, OnSharedPreferenceChangeListener, IEspHelpUIConfigure
{
    private final Logger log = Logger.getLogger(DeviceConfigureActivity.class);
    
    private IEspUser mUser;
    
    private PullToRefreshListView mSoftApListView;
    
    /**
     * There is a header in PullToRefreshListView, so the list items are behind header.
     */
    private final int LIST_HEADER_COUNT = 1;
    
    private List<IEspDeviceNew> mSoftApList;
    
    private SoftApAdapter mSoftApAdapter;
    
    private Handler mHandler;
    
    private SharedPreferences mShared;
    private int mAutoConfigureValue;
    
    public static final int DEFAULT_AUTO_CONFIGRUE_VALUE = -30;
    
    /**
     * When show the dialogs, pause the auto refresh handler
     */
    private boolean mShowConfigureDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.device_configure_activity);
        
        mUser = BEspUser.getBuilder().getInstance();
        mShared = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mShared.registerOnSharedPreferenceChangeListener(this);
        mAutoConfigureValue =
            mShared.getInt(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_CONFIGURE, DEFAULT_AUTO_CONFIGRUE_VALUE);
        
        mSoftApListView = (PullToRefreshListView)findViewById(R.id.softap_list);
        mSoftApListView.setOnItemClickListener(this);
        mSoftApListView.getRefreshableView().setOnItemLongClickListener(this);
        mSoftApList = new Vector<IEspDeviceNew>();
        mSoftApAdapter = new SoftApAdapter(this);
        mSoftApListView.setAdapter(mSoftApAdapter);
        mSoftApListView.setOnRefreshListener(this);
        
        mShowConfigureDialog = false;
        mHandler = new ListHandler(this);
        
        setTitle(R.string.esp_configure_title);
        setTitleLeftIcon(R.drawable.esp_icon_back);
        
        if (mHelpMachine.isHelpModeConfigure())
        {
            mSoftApList.clear();
            mSoftApAdapter.notifyDataSetInvalidated();
            
            onHelpConfigure();
        }
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        if (!mHelpMachine.isHelpModeConfigure() && !mShowConfigureDialog)
        {
            sendRefreshMessage();
        }
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        
        removeRefreshMessage();
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        mShared.unregisterOnSharedPreferenceChangeListener(this);
    }
    
    private void refreshSoftApList()
    {
        mSoftApList.clear();
        mSoftApList.addAll(mUser.scanSoftapDeviceList());
        sortDeviceByRssi(mSoftApList);
        mSoftApAdapter.notifyDataSetChanged();
        mSoftApListView.onRefreshComplete();
        
        if (mAutoConfigureValue < 0 && mSoftApList.size() > 0)
        {
            IEspDeviceNew device = mSoftApList.get(0);
            if (device.getRssi() >= mAutoConfigureValue && device.getRssi() < 0)
            {
                autoConfigure(device);
            }
        }
        
        if (mHelpMachine.isHelpModeConfigure())
        {
            if (mHelpMachine.getCurrentStateOrdinal() <= HelpStepConfigure.FAIL_DISCOVER_IOT_DEVICES.ordinal())
            {
                mHelpMachine.retry();
                onHelpConfigure();
            }
        }
    }
    
    private void autoConfigure(IEspDeviceNew device)
    {
        List<ScanResult> wifis = EspBaseApiUtil.scan();
        removeConfigureDeviceFromWifiList(wifis, device);
        sortWifiByRssi(wifis);
        
        List<String[]> configuredAps = mUser.getConfiguredAps();
        for (ScanResult wifi : wifis)
        {
            
            for (String[] configuredAp : configuredAps)
            {
                if (wifi.BSSID.equals(configuredAp[0]))
                {
                    String bssid = wifi.BSSID;
                    String ssid = wifi.SSID;
                    String password = configuredAp[2];
                    WifiCipherType wifiType = WifiCipherType.getWifiCipherType(wifi);
                    ApInfo apInfo = new ApInfo(bssid, ssid, password, wifiType);
                    new DeviceConfigureProgressDialog(this, device, apInfo).show();
                    return;
                }
            }
            
            if (isConfigured(wifi.BSSID))
            {
                String bssid = wifi.BSSID;
                String ssid = wifi.SSID;
                String password = IEspDevice.DEFAULT_MESH_PASSWORD;
                WifiCipherType wifiType = WifiCipherType.getWifiCipherType(wifi);
                ApInfo apInfo = new ApInfo(bssid, ssid, password, wifiType);
                if (!TextUtils.isEmpty(password))
                {
                    new DeviceConfigureProgressDialog(this, device, apInfo).show();
                    return;
                }
            }
        }
        
        showConfigureSettingsDialog(device);
    }
    
    public void setIsShowConfigureDialog(boolean value)
    {
        mShowConfigureDialog = value;
    }
    
    private void sortDeviceByRssi(List<IEspDeviceNew> list)
    {
        Comparator<IEspDeviceNew> comparatro = new Comparator<IEspDeviceNew>()
        {
            
            @Override
            public int compare(IEspDeviceNew lhs, IEspDeviceNew rhs)
            {
                Integer lRssi = lhs.getRssi();
                Integer rRssi = rhs.getRssi();
                return rRssi.compareTo(lRssi);
            }
            
        };
        
        Collections.sort(list, comparatro);
    }
    
    private void sortWifiByRssi(List<ScanResult> list)
    {
        Comparator<ScanResult> comparatro = new Comparator<ScanResult>()
        {
            
            @Override
            public int compare(ScanResult lhs, ScanResult rhs)
            {
                Integer lRssi = lhs.level;
                Integer rRssi = rhs.level;
                return rRssi.compareTo(lRssi);
            }
            
        };
        
        Collections.sort(list, comparatro);
    }
    
    /**
     * The softAP can't connect itself, so remove itself from WIFI list
     * 
     * @param wifiList
     * @param device
     */
    public void removeConfigureDeviceFromWifiList(List<ScanResult> wifiList, IEspDeviceNew device)
    {
        for (int i = 0; i < wifiList.size(); i++)
        {
            ScanResult sr = wifiList.get(i);
            if (sr.BSSID.equals(device.getBssid()))
            {
                wifiList.remove(sr);
            }
        }
    }
    
    private boolean isConfigured(String bssid)
    {
        List<IEspDevice> list = mUser.getDeviceList();
        for (IEspDevice userDevice : list)
        {
            if (userDevice.getDeviceState().isStateDeleted())
            {
                continue;
            }
            if (BSSIDUtil.isEqualIgnore2chars(userDevice.getBssid(), bssid))
            {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void onRefresh(PullToRefreshBase<ListView> view)
    {
        sendRefreshMessage();
    }
    
    private static final int MSG_REFRESH_LIST = 0;
    
    private void sendRefreshMessage()
    {
        if (mHandler.hasMessages(MSG_REFRESH_LIST))
        {
            mHandler.removeMessages(MSG_REFRESH_LIST);
        }
        mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
    }
    
    public void resetRefreshMessage()
    {
        if (mHandler.hasMessages(MSG_REFRESH_LIST))
        {
            mHandler.removeMessages(MSG_REFRESH_LIST);
        }
        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_LIST, 8000);
    }
    
    public void removeRefreshMessage()
    {
        if (mHandler.hasMessages(MSG_REFRESH_LIST))
        {
            mHandler.removeMessages(MSG_REFRESH_LIST);
        }
    }
    
    private static class ListHandler extends Handler
    {
        private WeakReference<DeviceConfigureActivity> mActivity;
        
        public ListHandler(DeviceConfigureActivity activity)
        {
            mActivity = new WeakReference<DeviceConfigureActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            DeviceConfigureActivity activity = mActivity.get();
            if (activity == null)
            {
                System.out.println("DeviceConfigureActivity ListHandler handleMessage activity is null");
                return;
            }
            
            switch (msg.what)
            {
                case MSG_REFRESH_LIST:
                    sendEmptyMessageDelayed(MSG_REFRESH_LIST, 5000);
                    
                    mActivity.get().refreshSoftApList();
                    break;
            }
        }
    }
    
    private class SoftApAdapter extends BaseAdapter
    {
        
        private Activity mActivity;
        
        public SoftApAdapter(Activity activity)
        {
            mActivity = activity;
        }
        
        @Override
        public int getCount()
        {
            return mSoftApList.size();
        }
        
        @Override
        public Object getItem(int position)
        {
            return mSoftApList.get(position);
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
                convertView = inflater.inflate(R.layout.device_layout, parent, false);
            }
            
            IEspDeviceNew device = mSoftApList.get(position);
            
            TextView deviceNameTV = (TextView)convertView.findViewById(R.id.device_name);
            deviceNameTV.setText(device.getSsid());
            ImageView deviceIconIV = (ImageView)convertView.findViewById(R.id.device_icon);
            deviceIconIV.setImageResource(R.drawable.esp_wifi_signal);
            deviceIconIV.getDrawable().setLevel(WifiManager.calculateSignalLevel(device.getRssi(), 5));
            TextView deviceRssiTV = (TextView)convertView.findViewById(R.id.device_status_text);
            deviceRssiTV.setText("RSSI: " + device.getRssi());
            
            ImageView meshIcon = (ImageView)convertView.findViewById(R.id.device_status);
            boolean isMesh = device.getIsMeshDevice();
            if (isMesh)
            {
                meshIcon.setBackgroundResource(R.drawable.esp_icon_mesh);
            }
            else
            {
                meshIcon.setBackgroundResource(0);
            }
            
            TextView contentTV = (TextView)convertView.findViewById(R.id.content_text);
            if (isConfigured(device.getBssid()))
            {
                contentTV.setText("Configured");
            }
            else
            {
                contentTV.setText("");
            }
            
            return convertView;
        }
        
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (!EspBaseApiUtil.isWifiEnabled())
        {
            Toast.makeText(this, R.string.esp_configure_wifi_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        IEspDeviceNew device = mSoftApList.get(position - LIST_HEADER_COUNT);
        showConfigureSettingsDialog(device);
    }
    
    private void showConfigureSettingsDialog(IEspDeviceNew device)
    {
        new DeviceConfigureSettingsDialog(this, device).show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        final IEspDeviceNew device = mSoftApList.get(position - LIST_HEADER_COUNT);
        new AlertDialog.Builder(this).setTitle(device.getName())
            .setMessage(R.string.esp_configure_softap_connect_message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    showSoftApDialog(device);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        return true;
    }
    
    private void showSoftApDialog(final IEspDeviceNew device)
    {
        final View view = getLayoutInflater().inflate(R.layout.device_softap_dialog, null);
        final AlertDialog dialog =
            new AlertDialog.Builder(this).setTitle(device.getName())
                .setView(view)
                .setPositiveButton(R.string.esp_configure_softap_close, new DialogInterface.OnClickListener()
                {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Get device type from saved view tag
                        Object tag = view.getTag();
                        log.info("The closed softap dialog view tag = " + tag);
                        if (tag != null)
                        {
                            EspDeviceType type = (EspDeviceType)tag;
                            mUser.doActionDeviceSleepRebootLocal(type);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false)
                .show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
        
        final String connectedSSID = EspBaseApiUtil.getWifiConnectedSsid();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                log.info("Reconnect wifi ssid = " + connectedSSID);
                EspBaseApiUtil.enableConnected(connectedSSID);
            }
        });
        
        new ConnectSoftApTask(dialog, view).execute(device);
    }
    
    private class ConnectSoftApTask extends AsyncTask<IEspDeviceNew, Void, DeviceInfo>
    {
        
        private AlertDialog mDialog;
        
        private View mView;
        
        public ConnectSoftApTask(AlertDialog dialog, View view)
        {
            mDialog = dialog;
            mView = view;
        }
        
        @Override
        protected DeviceInfo doInBackground(IEspDeviceNew... params)
        {
            IEspDeviceNew device = params[0];
            return mUser.doActionDeviceNewGetInfo(device);
        }
        
        @Override
        protected void onPostExecute(DeviceInfo result)
        {
            mView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
            mView.findViewById(R.id.device_info_container).setVisibility(View.VISIBLE);
            TextView versionTV = (TextView)mView.findViewById(R.id.device_version);
            TextView typeTV = (TextView)mView.findViewById(R.id.device_type);
            TextView authorizedTV = (TextView)mView.findViewById(R.id.device_authorized);
            if (result == null)
            {
                String failedStr = getString(R.string.esp_configure_softap_getinfo_failed);
                versionTV.setText(getString(R.string.esp_configure_softap_version, failedStr));
                typeTV.setText(getString(R.string.esp_configure_softap_type, failedStr));
                authorizedTV.setText(getString(R.string.esp_configure_softap_authorized, failedStr));
            }
            else
            {
                // Save the device type in view tag
                mView.setTag(EspDeviceType.getEspTypeEnumByString(result.getType()));
                
                versionTV.setText(getString(R.string.esp_configure_softap_version, result.getVersion()));
                typeTV.setText(getString(R.string.esp_configure_softap_type, result.getType()));
                String authorizedStr;
                if (result.isAuthorized())
                {
                    authorizedStr = getString(R.string.esp_configure_softap_authorized_yes);
                }
                else
                {
                    authorizedStr = getString(R.string.esp_configure_softap_authorized_no);
                }
                authorizedTV.setText(getString(R.string.esp_configure_softap_authorized, authorizedStr));
                
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            }
            
            mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_CONFIGURE))
        {
            mAutoConfigureValue = sharedPreferences.getInt(key, 0);
        }
    }
    
    @Override
    protected void onTitleLeftIconClick()
    {
        onBackPressed();
    }
    
    @Override
    public void onHelpConfigure()
    {
        highlightHelpView(mSoftApListView);
        
        HelpStepConfigure step = HelpStepConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_CONFIGURE_HELP:
                break;
            case DISCOVER_IOT_DEVICES:
                mHelpMachine.transformState(!mSoftApList.isEmpty());
                onHelpConfigure();
                break;
            case FAIL_DISCOVER_IOT_DEVICES:
                hintDiscoverSoftAP();
                break;
            case SCAN_AVAILABLE_AP:
                setHelpHintMessage(R.string.esp_help_configure_select_softap_msg);
                break;
            case FAIL_DISCOVER_AP:
                setHelpHintMessage(R.string.esp_help_configure_discover_wifi_msg);
                mHelpMachine.retry();
                break;
            case SELECT_CONFIGURED_DEVICE:
                break;
            case FAIL_CONNECT_DEVICE:
                setHelpHintMessage(R.string.esp_help_configure_connect_device_failed_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
                
            case DEVICE_IS_ACTIVATING:
            case FAIL_ACTIVATE:
            case FAIL_CONNECT_AP:
            case SUC:
                // Process in EspUIActivity, not here.
                break;
        }
    }
    
    private void hintDiscoverSoftAP()
    {
        ImageView hintView = new ImageView(this);
        hintView.setScaleType(ScaleType.CENTER_INSIDE);
        hintView.setImageResource(R.drawable.esp_pull_to_refresh_hint);
        FrameLayout.LayoutParams lp =
            new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mHelpContainer.addView(hintView, lp);
        
        setHelpHintMessage(R.string.esp_help_configure_discover_softap_msg);
        
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.esp_pull_to_refresh_hint);
        hintView.startAnimation(anim);
    }
    
    @Override
    public void onExitHelpMode()
    {
        setResult(RESULT_EXIT_HELP_MODE);
        finish();
    }
    
    @Override
    public void onHelpRetryClick()
    {
        onHelpConfigure();
    }
}
