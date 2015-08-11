package com.espressif.iot.ui.main;

import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.device.builder.BEspDeviceRoot;
import com.espressif.iot.model.device.statemachine.EspDeviceStateMachineHandler;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.ui.configure.DeviceConfigureActivity;
import com.espressif.iot.ui.configure.DeviceEspTouchActivity;
import com.espressif.iot.ui.configure.WifiConfigureActivity;
import com.espressif.iot.ui.device.DeviceFlammableActivity;
import com.espressif.iot.ui.device.DeviceHumitureActivity;
import com.espressif.iot.ui.device.DeviceLightActivity;
import com.espressif.iot.ui.device.DevicePlugActivity;
import com.espressif.iot.ui.device.DevicePlugsActivity;
import com.espressif.iot.ui.device.DeviceRemoteActivity;
import com.espressif.iot.ui.device.DeviceRootRouterActivity;
import com.espressif.iot.ui.device.DeviceVoltageActivity;
import com.espressif.iot.ui.login.LoginActivity;
import com.espressif.iot.ui.settings.SettingsActivity;
import com.espressif.iot.ui.view.EspPagerAdapter;
import com.espressif.iot.ui.view.EspViewPager;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.EspStrings;
import com.google.zxing.qrcode.ui.ShareCaptureActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class EspUIActivity extends EspActivityAbs implements OnRefreshListener<ListView>,
    OnSharedPreferenceChangeListener, OnItemClickListener, OnItemLongClickListener, OnClickListener,
    OnCheckedChangeListener, OnPageChangeListener
{
    private static final Logger log = Logger.getLogger(EspUIActivity.class);
    
    protected IEspUser mUser;
    
    private static final int MENU_ID_GET_SHARE = 0;
    private static final int MENU_ID_CONFIGURE = 1;
    private static final int MENU_ID_SETTINGS = 2;
    private static final int MENU_ID_LOGOUT = 3;
    private static final int MENU_ID_EDIT = 4;
    private static final int MENU_ID_WIFI = 5;
    
    protected PullToRefreshListView mDeviceListView;
    private DeviceAdapter mDeviceAdapter;
    protected List<IEspDevice> mDeviceList;
    
    /**
     * Whether the refresh task is running
     */
    private boolean mRefreshing;
    
    private SharedPreferences mShared;
    
    private Handler mAutoRefreshHandler;
    private static final int MSG_AUTO_REFRESH = 0;
    
    /**
     * This activity is in the foreground or background
     */
    private boolean mActivityVisible;
    
    private boolean mIsDevicesUpdatedNecessary = false;
    
    private View mEditBar;
    private Button mSelectAllBtn;
    private Button mDeleteSelectedBtn;
    private Set<IEspDevice> mEditCheckedDevices;
    
    protected View mConfigureBtn;
    
    protected final static int REQUEST_HELP = 0x10;
    protected final static int REQUEST_DEVICE = 0x11;
    private static final int REQUEST_ESPTOUCH = 0x12;
    
    private LocalBroadcastManager mBraodcastManager;
    
    private IEspDevice mVirtuaMeshRoot;
    
    private PullToRefreshListView mStaListView;
    private List<IEspDeviceSSS> mStaList;
    private StaAdapter mStaAdapter;
    
    private boolean mStaScanning;
    
    private static final int PAGE_ACTIVATED_DEVICES = 0;
    private static final int PAGE_STA_DEVICES = 1;
    
    private RadioGroup mTabs;
    private EspViewPager mPager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.esp_ui_activity);
        
        mUser = BEspUser.getBuilder().getInstance();
        mShared = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mShared.registerOnSharedPreferenceChangeListener(this);
        
        // Init device list
        mVirtuaMeshRoot = BEspDeviceRoot.getBuilder().getVirtualMeshRoot();
        mDeviceListView = (PullToRefreshListView)getLayoutInflater().inflate(R.layout.esp_ui_devices_list, null);
        mDeviceList = new Vector<IEspDevice>();
        updateDeviceList();
        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceListView.setAdapter(mDeviceAdapter);
        mDeviceListView.setOnRefreshListener(this);
        mDeviceListView.setOnItemClickListener(this);
        mDeviceListView.getRefreshableView().setOnItemLongClickListener(this);
        
        mStaListView = (PullToRefreshListView)getLayoutInflater().inflate(R.layout.esp_ui_devices_list, null);
        mStaList = new Vector<IEspDeviceSSS>();
        mStaAdapter = new StaAdapter();
        mStaListView.setAdapter(mStaAdapter);
        mStaListView.setOnRefreshListener(this);
        mStaListView.setOnItemClickListener(this);
        mStaListView.getRefreshableView().setOnItemLongClickListener(this);
        
        // Init edit bar
        mEditBar = findViewById(R.id.edit_bar);
        mSelectAllBtn = (Button)findViewById(R.id.select_all_btn);
        mSelectAllBtn.setOnClickListener(this);
        mDeleteSelectedBtn = (Button)findViewById(R.id.delete_selected_btn);
        mDeleteSelectedBtn.setOnClickListener(this);
        mEditCheckedDevices = new HashSet<IEspDevice>();
        
        // Init Pager
        mPager = (EspViewPager)findViewById(R.id.esp_ui_pager);
        mPager.setInterceptTouchEvent(true);
        List<View> viewList = new ArrayList<View>();
        viewList.add(mDeviceListView);
        viewList.add(mStaListView);
        EspPagerAdapter pagerAdapter = new EspPagerAdapter(viewList);
        mPager.setAdapter(pagerAdapter);
        mPager.setOnPageChangeListener(this);
        
        mTabs = (RadioGroup)findViewById(R.id.pager_tab_group);
        mTabs.setOnCheckedChangeListener(this);
        
        mAutoRefreshHandler = new AutoRefreshHandler(this);
        // Get auto refresh settings data
        long autoRefreshTime = mShared.getLong(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_REFRESH, 0);
        if (autoRefreshTime > 0)
        {
            sendAutoRefreshMessage(autoRefreshTime);
        }
        
        // register Receiver
        mBraodcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter(EspStrings.Action.DEVICES_ARRIVE_PULLREFRESH);
        filter.addAction(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE);
        filter.addAction(EspStrings.Action.LOGIN_NEW_ACCOUNT);
        mBraodcastManager.registerReceiver(mReciever, filter);
        
        // Init title bar
        setTitle(R.string.esp_ui_title);
        setTitleLeftIcon(R.drawable.esp_icon_add);
        setTitleProgressing();
        
        mRefreshing = false;
        mStaScanning = false;
        if (mUser.isLogin())
        {
            refresh();
        }
        else
        {
            scanSta();
        }
    }
    
    private void setTitleProgressing()
    {
        ProgressBar progressbar = new ProgressBar(this);
        int progressPadding = getResources().getDimensionPixelSize(R.dimen.esp_activity_ui_progress_padding);
        setTitleContentView(progressbar, progressPadding, progressPadding, progressPadding, progressPadding);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        
        mActivityVisible = true;
        // onReceive(Context context, Intent intent) need all of the four sentences
        if (mIsDevicesUpdatedNecessary)
        {
            mUser.doActionDevicesUpdated(false);
            mUser.doActionDevicesUpdated(true);
        }
        // when the UI is showed, show the newest device list need the follow two sentences
        updateDeviceList();
        mDeviceAdapter.notifyDataSetChanged();
        updateStaList();
        mStaAdapter.notifyDataSetChanged();
        if (mIsDevicesUpdatedNecessary)
        {
            mDeviceListView.onRefreshComplete();
            mStaListView.onRefreshComplete();
            mStaScanning = false;
            mRefreshing = false;
            mIsDevicesUpdatedNecessary = false;
        }
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        checkAccountLogin();
    }
    
    private void checkAccountLogin()
    {
        if (mUser.isLogin())
        {
            mPager.setInterceptTouchEvent(true);
            mTabs.setVisibility(View.VISIBLE);
        }
        else
        {
            mPager.setInterceptTouchEvent(false);
            mPager.setCurrentItem(PAGE_STA_DEVICES);
            mTabs.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mActivityVisible = false;
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mShared.unregisterOnSharedPreferenceChangeListener(this);
        mBraodcastManager.unregisterReceiver(mReciever);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_ESPTOUCH)
        {
            if (resultCode == RESULT_OK)
            {
                updateDeviceList();
                updateStaList();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected void onCreateBottomItems(IEspBottomBar bottombar)
    {
        mConfigureBtn =
            bottombar.addBottomItem(MENU_ID_CONFIGURE,
                R.drawable.esp_menu_icon_configure,
                R.string.esp_ui_menu_configure);
        bottombar.addBottomItem(MENU_ID_GET_SHARE, R.drawable.esp_menu_icon_camera, R.string.esp_ui_menu_get_share);
        bottombar.addBottomItem(MENU_ID_SETTINGS, R.drawable.esp_menu_icon_settings, R.string.esp_ui_menu_settings);
        bottombar.addBottomItem(MENU_ID_EDIT, R.drawable.esp_menu_icon_edit, R.string.esp_ui_menu_edit);
        bottombar.addBottomItem(MENU_ID_WIFI, R.drawable.esp_menu_icon_wifi, R.string.esp_ui_menu_wifi);
        bottombar.addBottomItem(MENU_ID_LOGOUT, R.drawable.esp_menu_icon_logout, R.string.esp_ui_menu_logout);
    }
    
    @Override
    protected void onBottomItemClick(View v, int itemId)
    {
        switch (itemId)
        {
            case MENU_ID_GET_SHARE:
                startActivity(new Intent(this, ShareCaptureActivity.class));
                break;
            case MENU_ID_CONFIGURE:
                gotoConfigure();
                break;
            case MENU_ID_SETTINGS:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case MENU_ID_LOGOUT:
                // cancel all task in thread pool before the activity is finished
                EspBaseApiUtil.cancelAllTask();
                EspDeviceStateMachineHandler.getInstance().cancelAllTasks();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case MENU_ID_WIFI:
                startActivity(new Intent(this, WifiConfigureActivity.class));
                break;
            case MENU_ID_EDIT:
                boolean bottomBarEnable = mEditBar.getVisibility() == View.VISIBLE;
                setEditBarEnable(!bottomBarEnable);
                break;
        }
    }
    
    private void setEditBarEnable(boolean enable)
    {
        if (enable)
        {
            mPager.setCurrentItem(PAGE_ACTIVATED_DEVICES, true);
        }
        mEditBar.setVisibility(enable ? View.VISIBLE : View.GONE);
        mDeleteSelectedBtn.setEnabled(false);
        mDeviceListView.setMode(enable ? Mode.DISABLED : Mode.PULL_FROM_START);
        mEditCheckedDevices.clear();
        mDeviceAdapter.setEditable(enable);
        mDeviceAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_REFRESH))
        {
            // The auto refresh settings changed
            if (mAutoRefreshHandler.hasMessages(MSG_AUTO_REFRESH))
            {
                mAutoRefreshHandler.removeMessages(MSG_AUTO_REFRESH);
            }
            long autoTime = mShared.getLong(key, 0);
            if (autoTime > 0)
            {
                sendAutoRefreshMessage(autoTime);
            }
        }
    }
    
    @Override
    protected void onTitleLeftIconClick()
    {
        startActivityForResult(new Intent(this, DeviceEspTouchActivity.class), REQUEST_ESPTOUCH);
    }
    
    @Override
    public void onRefresh(PullToRefreshBase<ListView> view)
    {
        if (view == mDeviceListView)
        {
            refresh();
        }
        else if (view == mStaListView)
        {
            scanSta();
        }
    }
    
    private void sendAutoRefreshMessage(Long autoRefreshTime)
    {
        log.debug("send Auto Refresh Message Delayed " + autoRefreshTime);
        Message msg = new Message();
        msg.what = MSG_AUTO_REFRESH;
        msg.obj = autoRefreshTime;
        mAutoRefreshHandler.sendMessageDelayed(msg, autoRefreshTime);
    }
    
    /**
     * Do refresh devices action
     */
    private void refresh()
    {
        if (!mRefreshing)
        {
            mRefreshing = true;
            mUser.doActionRefreshDevices();
        }
        
        if (!isNetworkAvailabale())
        {
            Toast.makeText(this, R.string.esp_ui_network_enable_msg, Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean isNetworkAvailabale()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null)
        {
            return false;
        }
        else
        {
            return info.isAvailable();
        }
    }
    
    private void updateDeviceList()
    {
        mDeviceList.clear();
        boolean hasMeshDevice = false;
        List<IEspDevice> list = mUser.getDeviceList();
        for (int i = 0; i < list.size(); i++)
        {
            IEspDevice device = list.get(i);
            if (!device.getDeviceState().isStateDeleted())
            {
                if (device.getIsMeshDevice())
                {
                    hasMeshDevice = true;
                }
                mDeviceList.add(device);
            }
        }
        sortDevicesByState(mDeviceList);
        
        if (hasMeshDevice)
        {
            mVirtuaMeshRoot.setName("Mesh Root");
            mDeviceList.add(0, mVirtuaMeshRoot);
        }
    }
    
    private void sortDevicesByState(List<IEspDevice> devices)
    {
        Comparator<IEspDevice> comparator = new Comparator<IEspDevice>()
        {
            
            @Override
            public int compare(IEspDevice lhs, IEspDevice rhs)
            {
                // Sort by state
                Integer l = getStateCompareValue(lhs);
                int result = l.compareTo(getStateCompareValue(rhs));
                
                // Same state, sort by device name
                if (result == 0)
                {
                    String lName = lhs.getName().toUpperCase(Locale.US);
                    String rName = rhs.getName().toUpperCase(Locale.US);
                    if(lName.equals(rName))
                    {
                        result = 0;
                    }
                    else
                    {
                        // order device by it name
                        List<String> lrNameList = new ArrayList<String>();
                        lrNameList.add(lName);
                        lrNameList.add(rName);
                        Collections.sort(lrNameList, Collator.getInstance(Locale.getDefault()));
                        if (lrNameList.get(0).equals(lName))
                        {
                            result = -1;
                        }
                        else
                        {
                            result = 1;
                        }
                    }
                    
                }
                
                // Same device name, sort by bssid
                if (result == 0)
                {
                    result = lhs.getBssid().compareTo(rhs.getBssid());
                }
                
                return result;
            }
        };
        
        Collections.sort(devices, comparator);
    }
    
    private int getStateCompareValue(IEspDevice device)
    {
        switch (device.getDeviceState().getDeviceState())
        {
            case ACTIVATING:
                return 1;
                
            case UPGRADING_INTERNET:
            case UPGRADING_LOCAL:
                return 2;
                
            case CONFIGURING:
                return 3;
                
            case INTERNET:
            case LOCAL:
                return 4;
                
            case OFFLINE:
                return 5;
                
            default:
                return 10;
            
        }
    }
    
    private class DeviceAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        
        private boolean mEditable = false;
        
        public DeviceAdapter(Activity activity)
        {
            mInflater = activity.getLayoutInflater();
        }
        
        @Override
        public int getCount()
        {
            return mDeviceList.size();
        }
        
        @Override
        public Object getItem(int position)
        {
            return mDeviceList.get(position);
        }
        
        @Override
        public long getItemId(int position)
        {
            return mDeviceList.get(position).getId();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final IEspDevice device = mDeviceList.get(position);
            
            if (convertView == null)
            {
                convertView = mInflater.inflate(R.layout.device_layout, null);
            }
            
            convertView.setTag(device);
            
            // Set icon
            ImageView iconIV = (ImageView)convertView.findViewById(R.id.device_icon);
            iconIV.setBackgroundResource(R.drawable.esp_device_icon_general);
            
            // Set device name
            TextView nameTV = (TextView)convertView.findViewById(R.id.device_name);
            nameTV.setText(device.getName());
            
            // Set state
            IEspDeviceState state = device.getDeviceState();
            
            TextView statusTV = (TextView)convertView.findViewById(R.id.device_status_text);
            switch (state.getDeviceState())
            {
                case UPGRADING_LOCAL:
                    statusTV.setText(R.string.esp_ui_status_upgrading_local);
                    break;
                case UPGRADING_INTERNET:
                    statusTV.setText(R.string.esp_ui_status_upgrading_online);
                    break;
                case OFFLINE:
                    statusTV.setText(R.string.esp_ui_status_offline);
                    break;
                case NEW:
                case ACTIVATING:
                    statusTV.setText(R.string.esp_ui_status_activating);
                    break;
                case LOCAL:
                    statusTV.setText(R.string.esp_ui_status_local);
                    break;
                case INTERNET:
                    statusTV.setText(R.string.esp_ui_status_online);
                    break;
                
                case CLEAR:
                case CONFIGURING:
                case DELETED:
                case RENAMED:
                    // shouldn't goto here
                    log.error("EspUIActivity getView status wrong: " + device);
                    statusTV.setText(state.getDeviceState().toString());
                    break;
            }
            statusTV.append(" | " + device.getDeviceType());
            
            ImageView statusIV = (ImageView)convertView.findViewById(R.id.device_status);
            if (state.isStateInternet() || state.isStateLocal())
            {
                statusIV.setBackgroundResource(R.drawable.esp_device_status_online);
            }
            else
            {
                statusIV.setBackgroundResource(R.drawable.esp_device_status_offline);
            }
            
            final CheckBox editCB = (CheckBox)convertView.findViewById(R.id.edit_check);
            editCB.setChecked(mEditCheckedDevices.contains(device) ? true : false);
            editCB.setOnClickListener(new View.OnClickListener()
            {
                
                @Override
                public void onClick(View v)
                {
                    boolean isChecked = editCB.isChecked();
                    if (isChecked)
                    {
                        mEditCheckedDevices.add(device);
                    }
                    else
                    {
                        mEditCheckedDevices.remove(device);
                    }
                    
                    mDeleteSelectedBtn.setEnabled(!mEditCheckedDevices.isEmpty());
                }
            });
            
            if (mEditable)
            {
                statusIV.setVisibility(View.GONE);
                editCB.setVisibility(View.VISIBLE);
            }
            else
            {
                statusIV.setVisibility(View.VISIBLE);
                editCB.setVisibility(View.GONE);
            }
            
            return convertView;
        }
        
        public void setEditable(boolean editable)
        {
            mEditable = editable;
        }
    }
    
    private void scanSta()
    {
        if (!mStaScanning && !mRefreshing)
        {
            mUser.doActionRefreshStaDevices(false);
        }
    }
    
    private void updateStaList()
    {
        List<IEspDeviceSSS> stas = mUser.getStaDeviceList();
        mStaList.clear();
        mStaList.addAll(stas);
    }
    
    private class StaAdapter extends BaseAdapter
    {
        
        @Override
        public int getCount()
        {
            return mStaList.size();
        }
        
        @Override
        public Object getItem(int position)
        {
            return mStaList.get(position);
        }
        
        @Override
        public long getItemId(int position)
        {
            return mStaList.get(position).getId();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            IEspDeviceSSS device = mStaList.get(position);
            
            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
            }
            
            convertView.setTag(device);
            
            TextView nameTV = (TextView)convertView.findViewById(android.R.id.text1);
            TextView typeTV = (TextView)convertView.findViewById(android.R.id.text2);
            
            String name;
            if (device.getName() == null) {
                name = BSSIDUtil.genDeviceNameByBSSID(device.getBssid());
            }
            else
            {
                name = device.getName();
            }
            
            nameTV.setText(name);
            typeTV.setText(device.getDeviceType().toString());
            
            return convertView;
        }
        
    }
    
    private BroadcastReceiver mReciever = new BroadcastReceiver()
    {
        
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (action.equals(EspStrings.Action.LOGIN_NEW_ACCOUNT))
            {
                setTitleProgressing();
                refresh();
                return;
            }
            
            setTitleContentView(null);
            
            // for EspDeviceStateMachine check the state valid before and after device's state transformation
            // so when the user is using device, we don't like to make the state changed by pull refresh before
            // the user tap the device into device using Activity.
            //
            // for example, device A is LOCAL and INTERNET, user pull refresh, before
            // the refresh finished, the user tap the device into the using activity choosing UPGRADE LOCAL,device
            // refresh result(INTERNET) arrived, if the device state is changed to INTERNET, it will throw
            // IllegalStateException for UPGRADE LOCAL require LOCAL state sometimes.
            // onStart() will handle the device state transformation when the Activity visible again.
            if (!mActivityVisible)
            {
                log.debug("Receive Broadcast but invisible so ignore");
                mIsDevicesUpdatedNecessary = true;
                return;
            }
            if (action.equals(EspStrings.Action.DEVICES_ARRIVE_PULLREFRESH))
            {
                log.debug("Receive Broadcast DEVICES_ARRIVE_PULLREFRESH");
                // Refresh list
                mUser.doActionDevicesUpdated(false);
                
                updateDeviceList();
                mDeviceAdapter.notifyDataSetChanged();
                mDeviceListView.onRefreshComplete();
                
                updateStaList();
                mStaAdapter.notifyDataSetChanged();
                mStaListView.onRefreshComplete();
                
                mRefreshing = false;
                mStaScanning = false;
            }
            else if (action.equals(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE))
            {
                log.debug("Receive Broadcast DEVICES_ARRIVE_STATEMACHINE");
                mUser.doActionDevicesUpdated(true);
                updateDeviceList();
                mDeviceAdapter.notifyDataSetChanged();
                
                updateStaList();
                mStaAdapter.notifyDataSetChanged();
                
                checkHelpConfigure();
            }
        }
        
    };
    
    private static class AutoRefreshHandler extends Handler
    {
        private WeakReference<EspUIActivity> mActivity;
        
        public AutoRefreshHandler(EspUIActivity activity)
        {
            mActivity = new WeakReference<EspUIActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            EspUIActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
            
            switch (msg.what)
            {
                case MSG_AUTO_REFRESH:
                    log.debug("handleMessage MSG_AUTO_REFRESH");
                    // Send refresh message every settings time
                    if (activity.mActivityVisible)
                    {
                        activity.refresh();
                    }
                    
                    long autoTime = (Long)msg.obj;
                    activity.sendAutoRefreshMessage(autoTime);
                    break;
            }
        }
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mSelectAllBtn)
        {
            /*
             * If all devices are selected, cancel all the select.
             * else select all devices.
             */
            boolean allSelected = true;
            for (IEspDevice device : mDeviceList)
            {
                if (mEditCheckedDevices.contains(device))
                {
                    continue;
                }
                else
                {
                    allSelected = false;
                    break;
                }
            }
            if (allSelected)
            {
                mEditCheckedDevices.clear();
            }
            else
            {
                mEditCheckedDevices.addAll(mDeviceList);
            }
            
            mDeleteSelectedBtn.setEnabled(!mEditCheckedDevices.isEmpty());
            mDeviceAdapter.notifyDataSetChanged();
        }
        else if (v == mDeleteSelectedBtn)
        {
            new AlertDialog.Builder(this).setTitle(R.string.esp_ui_edit_delete_selected)
                .setMessage(R.string.esp_ui_edit_delete_sellected_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new DeleteDevicesTask().execute();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == mDeviceListView.getRefreshableView())
        {
            IEspDevice device = (IEspDevice)view.getTag();
            gotoUseDevice(device);
        }
        else if (parent == mStaListView.getRefreshableView())
        {
            IEspDeviceSSS deviceSSS = (IEspDeviceSSS)view.getTag();
            gotoUseDevice(deviceSSS);
        }
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == mDeviceListView.getRefreshableView())
        {
            IEspDevice device = (IEspDevice)view.getTag();
            if (!isDeviceEditable(device))
            {
                Toast.makeText(this, R.string.esp_ui_edit_forbidden_toast, Toast.LENGTH_SHORT).show();
                return true;
            }
            
            new AlertDialog.Builder(this).setItems(R.array.esp_ui_device_dialog_items, new ListItemDialogListener(device))
                .show();
            
            return true;
        }
        else if (parent == mStaListView.getRefreshableView())
        {
            final IEspDeviceSSS deviceSSS = (IEspDeviceSSS)view.getTag();
            PopupMenu popupMenu = new PopupMenu(this, view);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, 0, R.string.esp_configure_activate);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    activateDevice(deviceSSS);
                    return true;
                }
            });
            if (mUser.isLogin())
            {
                popupMenu.show();
            }
        }
        
        return false;

    }
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        switch(checkedId)
        {
            case R.id.tab_activated_device:
                mPager.setCurrentItem(PAGE_ACTIVATED_DEVICES);
                break;
            case R.id.tab_sta_device:
                mPager.setCurrentItem(PAGE_STA_DEVICES);
                break;
        }
    }
    
    @Override
    public void onPageSelected(int selection)
    {
        switch (selection)
        {
            case PAGE_ACTIVATED_DEVICES:
                mTabs.check(R.id.tab_activated_device);
                break;
            case PAGE_STA_DEVICES:
                mTabs.check(R.id.tab_sta_device);
                break;
        }
    }
    
    @Override
    public void onPageScrollStateChanged(int arg0)
    {
    }
    
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2)
    {
    }

    protected void gotoConfigure()
    {
        Intent intent = new Intent(this, DeviceConfigureActivity.class);
        startActivity(intent);
    }
    
    /**
     * 
     * @param device
     * @return true go to use device success, false for some reasons the device can't use
     */
    protected boolean gotoUseDevice(IEspDevice device)
    {
        IEspDeviceState state = device.getDeviceState();
        if (state.isStateUpgradingInternet() || state.isStateUpgradingLocal())
        {
            return false;
        }
        
        if (checkHelpClickDeviceType(device.getDeviceType()))
        {
            // The help mode is on, but not the clicked device type help
            return false;
        }
        
        Class<?> _class = getDeviceClass(device);
        if (_class != null)
        {
            Intent intent = new Intent(this, _class);
            intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, device.getKey());
            startActivityForResult(intent, REQUEST_DEVICE);
            return true;
        }
        
        return false;
    }
    
    protected Class<?> getDeviceClass(IEspDevice device)
    {
        IEspDeviceState state = device.getDeviceState();
        Class<?> _class = null;
        switch (device.getDeviceType())
        {
            case PLUG:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    _class = DevicePlugActivity.class;
                }
                break;
            case LIGHT:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    _class = DeviceLightActivity.class;
                }
                break;
            case FLAMMABLE:
                if (state.isStateInternet() || state.isStateOffline())
                {
                    _class = DeviceFlammableActivity.class;
                }
                break;
            case HUMITURE:
                if (state.isStateInternet() || state.isStateOffline())
                {
                    _class = DeviceHumitureActivity.class;
                }
                break;
            case VOLTAGE:
                if (state.isStateInternet() || state.isStateOffline())
                {
                    _class = DeviceVoltageActivity.class;
                }
                break;
            case REMOTE:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    _class = DeviceRemoteActivity.class;
                }
                break;
            case PLUGS:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    _class = DevicePlugsActivity.class;
                }
                break;
            case ROOT:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    _class = DeviceRootRouterActivity.class;
                }
                break;
            case NEW:
                log.warn("Click on NEW device, it shouldn't happen");
                break;
        }
        
        return _class;
    }
    
    private boolean isDeviceEditable(IEspDevice device) {
        if (device.getDeviceType() == EspDeviceType.ROOT)
        {
            return false;
        }
        
        IEspDeviceState state = device.getDeviceState();
        if (state.isStateUpgradingInternet() || state.isStateUpgradingLocal() || state.isStateActivating())
        {
            return false;
        }
        
        return true;
    }
    
    private class ListItemDialogListener implements DialogInterface.OnClickListener
    {
        private final int ITEM_RENAME_POSITION = 0;
        
        private final int ITEM_DELETE_POSITION = 1;
        
        private IEspDevice mItemDevice;
        
        public ListItemDialogListener(IEspDevice device)
        {
            mItemDevice = device;
        }
        
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case ITEM_RENAME_POSITION:
                    showRenameDialog();
                    break;
                case ITEM_DELETE_POSITION:
                    showDeleteDialog();
                    break;
            }
        }
        
        private void showRenameDialog()
        {
            Context context = EspUIActivity.this;
            final EditText nameEdit = new EditText(context);
            nameEdit.setSingleLine();
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            nameEdit.setLayoutParams(lp);
            new AlertDialog.Builder(context).setView(nameEdit)
                .setTitle(mItemDevice.getName())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String newName = nameEdit.getText().toString();
                        mUser.doActionRename(mItemDevice, newName);
                    }
                    
                })
                .show();
        }
        
        private void showDeleteDialog()
        {
            Context context = EspUIActivity.this;
            new AlertDialog.Builder(context).setTitle(mItemDevice.getName())
                .setMessage(R.string.esp_ui_delete_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mUser.doActionDelete(mItemDevice);
                    }
                })
                .show();
        }
    }
    
    private class DeleteDevicesTask extends AsyncTask<Void, Void, Boolean>
    {
        private Collection<IEspDevice> mDevices;
        
        private boolean mHasEneditableDevice;
        
        private ProgressDialog mDialog;
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(EspUIActivity.this);
            mDialog.setMessage(getString(R.string.esp_device_task_dialog_message));
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            
            // Filter devices can't be deleted
            mHasEneditableDevice = false;
            mDevices = new HashSet<IEspDevice>();
            for (IEspDevice device : mEditCheckedDevices)
            {
                if (isDeviceEditable(device))
                {
                    mDevices.add(device);
                }
                else
                {
                    mHasEneditableDevice = true;
                    continue;
                }
            }
            
            setEditBarEnable(false);
        }
        
        @Override
        protected Boolean doInBackground(Void... params)
        {
            mUser.doActionDelete(mDevices);
            return true;
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            mDialog.dismiss();
            mDevices.clear();
            
            if (mHasEneditableDevice)
            {
                Toast.makeText(EspUIActivity.this,
                    R.string.esp_ui_edit_has_eneditable_device_message,
                    Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    public void onBackPressed()
    {
        if (mEditBar.getVisibility() == View.VISIBLE)
        {
            // Exit edit mode
            setEditBarEnable(false);
        }
        else if (!mHelpMachine.isHelpOn())
        {
            new AlertDialog.Builder(this).setMessage(R.string.esp_ui_exit_message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    System.exit(0);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        }
        else
        {
            super.onBackPressed();
        }
    }
    
    private void activateDevice(final IEspDeviceSSS device)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.esp_ui_status_activating));
        dialog.show();
        new AsyncTask<Void, Void, Boolean>()
        {

            @Override
            protected Boolean doInBackground(Void... params)
            {
                boolean isSuc = mUser.addDeviceSyn(device);
                mUser.doActionRefreshStaDevices(true);
                return isSuc;
            }
            
            protected void onPostExecute(Boolean result) {
                toastActivateResult(result);
                dialog.dismiss();
                updateDeviceList();
                mDeviceAdapter.notifyDataSetChanged();
                updateStaList();
                mStaAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
    
    private void toastActivateResult(boolean suc)
    {
        int msgRes = suc ? R.string.esp_ui_device_activate_suc : R.string.esp_ui_device_activate_failed;
        Toast.makeText(this, msgRes, Toast.LENGTH_LONG).show();
    }
    
    protected void checkHelpConfigure(){
    }
    
    protected boolean checkHelpClickDeviceType(EspDeviceType type){
        return false;
    }

}
