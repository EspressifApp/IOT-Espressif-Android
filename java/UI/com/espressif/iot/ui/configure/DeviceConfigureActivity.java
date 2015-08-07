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
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.type.device.DeviceInfo;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.ui.device.dialog.DeviceDialogBuilder;
import com.espressif.iot.ui.device.dialog.EspDeviceDialogInterface;
import com.espressif.iot.ui.main.EspActivityAbs;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceConfigureActivity extends EspActivityAbs implements OnItemClickListener,
    OnRefreshListener<ListView>, OnItemLongClickListener, OnSharedPreferenceChangeListener
{
    private final Logger log = Logger.getLogger(DeviceConfigureActivity.class);
    
    private IEspUser mUser;
    
    /**
     * There is a header in PullToRefreshListView, so the list items are behind header.
     */
    private final int LIST_HEADER_COUNT = PullToRefreshListView.DEFAULT_HEADER_COUNT;
    
    protected PullToRefreshListView mSoftApListView;
    protected List<IEspDeviceNew> mSoftApList;
    protected BaseAdapter mSoftApAdapter;
    
    private Handler mHandler;
    
    private SharedPreferences mShared;
    private int mAutoConfigureValue;
    
    public static final int DEFAULT_AUTO_CONFIGRUE_VALUE = -30;
    
    protected static final int POPMENU_ID_ACTIVATE = 0;
    protected static final int POPMENU_ID_DIRECT_CONNECT = 1;
    
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
//        mSoftApListView.getRefreshableView().setOnItemLongClickListener(this);
        mSoftApList = new Vector<IEspDeviceNew>();
        mSoftApAdapter = new SoftApAdapter(this);
        mSoftApListView.setAdapter(mSoftApAdapter);
        mSoftApListView.setOnRefreshListener(this);
        
        mShowConfigureDialog = false;
        mHandler = new ListHandler(this);
        
        setTitle(R.string.esp_configure_title);
        
        checkHelpModeConfigureClear();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        if (!mShowConfigureDialog && !checkHelpModeOn())
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
        
        if (!mHelpMachine.isHelpOn() && mAutoConfigureValue < 0 && mSoftApList.size() > 0)
        {
            IEspDeviceNew device = mSoftApList.get(0);
            if (device.getRssi() >= mAutoConfigureValue && device.getRssi() < 0)
            {
                autoConfigure(device);
            }
        }
        
        checkHelpModeConfigureRetry();
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
                    showConfigureProgressDialog(device, apInfo);
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
                return;
            }
        }
    }
    
    protected boolean isConfigured(String bssid)
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
                    
                    activity.refreshSoftApList();
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
            
            IEspDeviceNew deviceNew = mSoftApList.get(position);
            
            TextView deviceNameTV = (TextView)convertView.findViewById(R.id.device_name);
            String displayText = deviceNew.getSsid();
            // check whether the device is configured
            List<IEspDevice> deviceList = BEspUser.getBuilder().getInstance().getDeviceList();
            for (IEspDevice deviceInList : deviceList)
            {
                // when device activating fail, it will be stored in local db and be activating state,
                // at this situation, we don't let the displayText change to device name
                if (deviceInList.getDeviceState().isStateActivating())
                {
                    continue;
                }
                if (deviceNew.getBssid().equals(deviceInList.getBssid()))
                {
                    displayText = deviceInList.getName();
                    break;
                }
            }
            deviceNameTV.setText(displayText);
            ImageView deviceIconIV = (ImageView)convertView.findViewById(R.id.device_icon);
            deviceIconIV.setImageResource(R.drawable.esp_wifi_signal);
            deviceIconIV.getDrawable().setLevel(WifiManager.calculateSignalLevel(deviceNew.getRssi(), 5));
            TextView deviceRssiTV = (TextView)convertView.findViewById(R.id.device_status_text);
            deviceRssiTV.setText("RSSI: " + deviceNew.getRssi());
            
            ImageView meshIcon = (ImageView)convertView.findViewById(R.id.device_status);
            boolean isMesh = deviceNew.getIsMeshDevice();
            if (isMesh)
            {
                meshIcon.setBackgroundResource(R.drawable.esp_icon_mesh);
            }
            else
            {
                meshIcon.setBackgroundResource(0);
            }
            
            TextView contentTV = (TextView)convertView.findViewById(R.id.content_text);
            if (isConfigured(deviceNew.getBssid()))
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
        
        PopupMenu popMenu = new PopupMenu(this, view);
        Menu menu = popMenu.getMenu();
        if (mUser.isLogin())
        {
            menu.add(Menu.NONE, POPMENU_ID_ACTIVATE, 0, R.string.esp_configure_activate);
        }
        menu.add(Menu.NONE, POPMENU_ID_DIRECT_CONNECT, 0, R.string.esp_configure_direct_connect);
        popMenu.setOnMenuItemClickListener(new SoftAPPopMenuItemClickListener(device));
        popMenu.show();
    }
    
    private class SoftAPPopMenuItemClickListener implements OnMenuItemClickListener
    {
        private IEspDeviceNew mPopDevice;
        
        public SoftAPPopMenuItemClickListener(IEspDeviceNew device)
        {
            mPopDevice = device;
        }
        
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            if (checkHelpPopMenuItemClick(item.getItemId()))
            {
                return true;
            }
            
            switch(item.getItemId())
            {
                case POPMENU_ID_ACTIVATE:
                    showConfigureSettingsDialog(mPopDevice);
                    return true;
                case POPMENU_ID_DIRECT_CONNECT:
                    showDirectConnectProgressDialog(mPopDevice);
                    return true;
            }
            return false;
        }
        
    }
    
    protected void showConfigureSettingsDialog(IEspDeviceNew device)
    {
        new DeviceConfigureSettingsDialog(this, device).show();
    }
    
    public void showConfigureProgressDialog(IEspDeviceNew device, ApInfo apInfo)
    {
        new DeviceConfigureProgressDialog(this, device, apInfo).show(); 
    }
    
    public void showDirectConnectProgressDialog(IEspDeviceNew device)
    {
        new DeviceDirectConnectProgressDialog(this, device).show();
    }
    
    public void showLocalDeviceDialog(IEspDeviceSSS device, final String cacheSsid)
    {
        setIsShowConfigureDialog(true);
        removeRefreshMessage();
        
        EspDeviceDialogInterface deviceDialog = new DeviceDialogBuilder(this, device).show();
        deviceDialog.setOnDissmissedListener(new EspDeviceDialogInterface.OnDissmissedListener()
        {
            
            @Override
            public void onDissmissed(DialogInterface dialog)
            {
                if (!TextUtils.isEmpty(cacheSsid))
                {
                    EspBaseApiUtil.enableConnected(cacheSsid);
                }
                
                setIsShowConfigureDialog(false);
                resetRefreshMessage();
            }
        });
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
            return mUser.doActionDeviceNewConnect(device);
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
    
    protected void checkHelpModeConfigureClear() {
    }
    
    protected void checkHelpModeConfigureRetry() {
    }
    
    protected boolean checkHelpModeOn()
    {
        return false;
    }
    
    protected boolean checkHelpPopMenuItemClick(int itemId)
    {
        return false;
    }
}
