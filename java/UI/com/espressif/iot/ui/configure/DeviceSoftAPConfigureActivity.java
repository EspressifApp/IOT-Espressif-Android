package com.espressif.iot.ui.configure;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.ui.device.DeviceActivityAbs;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.EspDefaults;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceSoftAPConfigureActivity extends EspActivityAbs
    implements OnSharedPreferenceChangeListener, OnRefreshListener {
    private IEspUser mUser;
    
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mSoftApListView;
    private List<IEspDeviceNew> mSoftApList;
    private SoftApAdapter mSoftApAdapter;
    
    private Handler mHandler;
    
    private SharedPreferences mShared;
    
    private int mAutoConfigureValue;
    
    private static final int POPMENU_ID_ACTIVATE = 0;
    private static final int POPMENU_ID_DIRECT_CONNECT = 1;
    
    /**
     * When show the dialogs, pause the auto refresh handler
     */
    private boolean mShowConfigureDialog;
    
    private String mCacheSSID;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_configure_activity);

        mUser = BEspUser.getBuilder().getInstance();
        mShared = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mShared.registerOnSharedPreferenceChangeListener(this);
        mAutoConfigureValue =
            mShared.getInt(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_CONFIGURE, EspDefaults.AUTO_CONFIGRUE_RSSI);

        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeResources(R.color.esp_actionbar_color);
        mRefreshLayout.setOnRefreshListener(this);

        mSoftApListView = (RecyclerView)findViewById(R.id.softap_list);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        
        mSoftApListView.setLayoutManager(llm);
        mSoftApList = new Vector<IEspDeviceNew>();
        mSoftApAdapter = new SoftApAdapter(this);
        mSoftApListView.setAdapter(mSoftApAdapter);

        mShowConfigureDialog = false;
        mHandler = new ListHandler(this);

        setTitle(R.string.esp_configure_title);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        if (!mShowConfigureDialog)
        {
            sendRefreshMessage();
        }
        
        if (!TextUtils.isEmpty(mCacheSSID))
        {
            EspBaseApiUtil.enableConnected(mCacheSSID);
            mCacheSSID = null;
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
        mHandler.removeMessages(MSG_REFRESH_LIST);
    }
    
    private void refreshSoftApList()
    {
        mSoftApList.clear();
        mSoftApList.addAll(mUser.scanSoftapDeviceList());
        sortDeviceByRssi(mSoftApList);
        mSoftApAdapter.notifyDataSetChanged();
        mRefreshLayout.setRefreshing(false);
        
        if (mAutoConfigureValue < 0 && mSoftApList.size() > 0)
        {
            IEspDeviceNew device = mSoftApList.get(0);
            if (device.getRssi() >= mAutoConfigureValue && device.getRssi() < 0)
            {
                autoConfigure(device);
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
                    new DeviceSoftAPConfigureProgressDialog(this, device, apInfo).show();
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
    
    /**
     * Whether the WIFI is configured ESP device
     * 
     * @param bssid of the WIFI
     * @return
     */
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
    public void onRefresh() {
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
        private WeakReference<DeviceSoftAPConfigureActivity> mActivity;
        
        public ListHandler(DeviceSoftAPConfigureActivity activity)
        {
            mActivity = new WeakReference<DeviceSoftAPConfigureActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            DeviceSoftAPConfigureActivity activity = mActivity.get();
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
    
    private class Holder extends RecyclerView.ViewHolder {
        IEspDeviceNew softAp;

        TextView deviceName;
        ImageView deviceIcon;
        TextView deviceRssi;
        ImageView meshIcon;
        TextView content;

        public Holder(View itemView) {
            super(itemView);

            deviceName = (TextView)itemView.findViewById(R.id.device_name);
            deviceIcon = (ImageView)itemView.findViewById(R.id.device_icon);
            deviceIcon.setImageResource(R.drawable.esp_wifi_signal);
            deviceRssi = (TextView)itemView.findViewById(R.id.device_status_text);
            meshIcon = (ImageView)itemView.findViewById(R.id.device_status1);
            content = (TextView)itemView.findViewById(R.id.content_text);

            itemView.setOnClickListener(new OnItemClickListener());
        }

        private class OnItemClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                if (!EspBaseApiUtil.isWifiEnabled()) {
                    Toast.makeText(v.getContext(), R.string.esp_configure_wifi_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                IEspDeviceNew device = softAp;

                PopupMenu popMenu = new PopupMenu(v.getContext(), v);
                Menu menu = popMenu.getMenu();
                if (mUser.isLogin()) {
                    menu.add(Menu.NONE, POPMENU_ID_ACTIVATE, 0, R.string.esp_configure_activate);
                }
                menu.add(Menu.NONE, POPMENU_ID_DIRECT_CONNECT, 0, R.string.esp_configure_direct_connect);
                popMenu.setOnMenuItemClickListener(new SoftAPPopMenuItemClickListener(device));
                popMenu.show();
            }

        }
    }

    private class SoftApAdapter extends RecyclerView.Adapter<Holder> {
        private Activity mActivity;
        private LayoutInflater mInflater;

        public SoftApAdapter(Activity activity) {
            mActivity = activity;
            mInflater = mActivity.getLayoutInflater();
        }

        @Override
        public int getItemCount() {
            return mSoftApList.size();
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            IEspDeviceNew softAp = mSoftApList.get(position);
            holder.softAp = softAp;

            String displayText = softAp.getSsid();
            // check whether the device is configured
            List<IEspDevice> deviceList = BEspUser.getBuilder().getInstance().getDeviceList();
            for (IEspDevice deviceInList : deviceList) {
                // when device activating fail, it will be stored in local db and be activating state,
                // at this situation, we don't let the displayText change to device name
                if (deviceInList.getDeviceState().isStateActivating()) {
                    continue;
                }
                if (softAp.getBssid().equals(deviceInList.getBssid())) {
                    displayText = deviceInList.getName();
                    break;
                }
            }
            holder.deviceName.setText(displayText);

            holder.deviceIcon.getDrawable().setLevel(WifiManager.calculateSignalLevel(softAp.getRssi(), 5));

            holder.deviceRssi.setText("RSSI: " + softAp.getRssi());

            boolean isMesh = softAp.getIsMeshDevice();
            if (isMesh) {
                holder.meshIcon.setBackgroundResource(R.drawable.esp_icon_mesh);
            } else {
                holder.meshIcon.setBackgroundResource(0);
            }

            if (isConfigured(softAp.getBssid())) {
                holder.content.setText("Configured");
            } else {
                holder.content.setText("");
            }
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.device_layout, parent, false);
            Holder holder = new Holder(view);
            return holder;
        }

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
            switch (item.getItemId())
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
    
    private void showConfigureSettingsDialog(IEspDeviceNew device)
    {
        new DeviceSoftAPConfigureSettingsDialog(this, device).show();
    }
    
    public void showConfigureProgressDialog(IEspDeviceNew device, ApInfo apInfo)
    {
        new DeviceSoftAPConfigureProgressDialog(this, device, apInfo).show();
    }
    
    private void showDirectConnectProgressDialog(IEspDeviceNew device)
    {
        new DeviceDirectConnectProgressDialog(this, device).show();
    }
    
    public void showLocalDevice(IOTAddress iotAddress, final String cacheSsid)
    {
        Class<?> cls = DeviceActivityAbs.getLocalDeviceClass(iotAddress.getDeviceTypeEnum());
        if (cls != null)
        {
            mCacheSSID = cacheSsid;
            Intent intent = new Intent(getBaseContext(), cls);
            intent.putExtra(EspStrings.Key.DEVICE_KEY_IOTADDRESS, iotAddress);
            intent.putExtra(EspStrings.Key.DEVICE_KEY_SHOW_CHILDREN, false);
            startActivity(intent);
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_CONFIGURE))
        {
            mAutoConfigureValue = sharedPreferences.getInt(key, EspDefaults.AUTO_CONFIGRUE_RSSI);
        }
    }
}
