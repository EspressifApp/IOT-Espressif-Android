package com.espressif.iot.ui.main;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.device.builder.BEspDeviceRoot;
import com.espressif.iot.esppush.EspPushService;
import com.espressif.iot.esppush.EspPushUtils;
import com.espressif.iot.model.device.cache.EspDeviceCache;
import com.espressif.iot.model.device.sort.DeviceSortor;
import com.espressif.iot.model.device.sort.DeviceSortor.DeviceSortType;
import com.espressif.iot.model.device.statemachine.EspDeviceStateMachineHandler;
import com.espressif.iot.model.group.EspGroupHandler;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.ui.configure.DeviceConfigureActivity;
import com.espressif.iot.ui.configure.DeviceEspTouchActivity;
import com.espressif.iot.ui.device.DeviceActivityAbs;
import com.espressif.iot.ui.login.LoginActivity;
import com.espressif.iot.ui.scene.EspSceneActivity;
import com.espressif.iot.ui.settings.SettingsActivity;
import com.espressif.iot.ui.view.DeviceAdapter;
import com.espressif.iot.ui.view.DeviceAdapter.OnEditCheckedChangeListener;
import com.espressif.iot.ui.view.menu.IEspBottomMenu;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspDefaults;
import com.espressif.iot.util.EspStrings;
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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EspUIActivity extends EspActivityAbs implements OnRefreshListener<ListView>,
    OnSharedPreferenceChangeListener, OnItemClickListener, OnItemLongClickListener, OnClickListener,
    OnEditCheckedChangeListener, OnCheckedChangeListener, OnItemSelectedListener
{
    private static final Logger log = Logger.getLogger(EspUIActivity.class);
    
    protected IEspUser mUser;
    
    private static final int MENU_ID_ADD_DEVICE = 1;
    private static final int MENU_ID_EDIT = 4;
    private static final int MENU_ID_SCENE = 5;
    
    protected List<IEspDevice> mAllDeviceList;
    protected PullToRefreshListView mDeviceListView;
    private DeviceAdapter mDeviceAdapter;
    
    private static final int POPUPMENU_ID_RENAME = 1;
    private static final int POPUPMENU_ID_DELETE = 2;
    private static final int POPUPMENU_ID_ACTIVATE = 3;
    
    /**
     * Whether the refresh task is running
     */
    private boolean mRefreshing;
    private Handler mAutoRefreshHandler;
    private static final int MSG_AUTO_REFRESH = 0;
    
    private SharedPreferences mShared;
    
    /**
     * This activity is in the foreground or background
     */
    private boolean mActivityVisible;
    private boolean mIsDevicesUpdatedNecessary = false;
    private boolean mIsDevicesPullRefreshUpdated = false;
    
    private View mEditBar;
    private Button mSelectAllBtn;
    private Button mDeleteSelectedBtn;
    
    private Set<IEspDevice> mEditCheckedDevices;
    
    protected View mConfigureBtn;
    
    protected final static int REQUEST_HELP = 0x10;
    protected final static int REQUEST_DEVICE = 0x11;
    private static final int REQUEST_ESPTOUCH = 0x12;
    private static final int REQUEST_SETTINGS = 0x13;
    
    private LocalBroadcastManager mBraodcastManager;
    
    private IEspDevice mVirtuaMeshRoot;
    
    private ToggleButton mDeviceVisibleCB;
    
    private Spinner mDeviceSortSpinner;
    private DeviceSortType mSortType;
    private static final int POSITION_SORT_DEVICE_NAME = 0;
    private static final int POSITION_SORT_ACTIVATE_TIME = 1;
    
    private SharedPreferences mNewDevicesShared;
    private Set<String> mNewDevicesSet;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.esp_ui_activity);
        
        mUser = BEspUser.getBuilder().getInstance();
        mShared = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mShared.registerOnSharedPreferenceChangeListener(this);
        
        mNewDevicesShared = getSharedPreferences(EspStrings.Key.NAME_NEW_ACTIVATED_DEVICES, Context.MODE_PRIVATE);
        mNewDevicesShared.registerOnSharedPreferenceChangeListener(this);
        mNewDevicesSet = mUser.getNewActivatedDevices();
        
        // Clear cache list
        EspDeviceCache.getInstance().clear();
        
        // Init top panel
        mDeviceVisibleCB = (ToggleButton)findViewById(R.id.device_visible_check);
        mDeviceVisibleCB.setOnCheckedChangeListener(this);
        
        mSortType = DeviceSortType.DEVICE_NAME;
        mDeviceSortSpinner = (Spinner)findViewById(R.id.device_sort_spinner);
        String[] sortOptions = getResources().getStringArray(R.array.esp_ui_device_sort);
        ArrayAdapter<String> sortAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortOptions);
        mDeviceSortSpinner.setAdapter(sortAdapter);
        mDeviceSortSpinner.setOnItemSelectedListener(this);
        
        // Init device list
        mVirtuaMeshRoot = BEspDeviceRoot.getBuilder().getVirtualMeshRoot();
        mDeviceListView = (PullToRefreshListView)findViewById(R.id.devices_list);
        mAllDeviceList = new Vector<IEspDevice>();
        updateDeviceList();
        mDeviceAdapter = new EspDeviceAdapter(this, mAllDeviceList);
        mDeviceListView.setAdapter(mDeviceAdapter);
        mDeviceListView.setOnRefreshListener(this);
        mDeviceListView.setOnItemClickListener(this);
        mDeviceListView.getRefreshableView().setOnItemLongClickListener(this);
        
        // Init edit bar
        mEditBar = findViewById(R.id.edit_bar);
        mSelectAllBtn = (Button)findViewById(R.id.select_all_btn);
        mSelectAllBtn.setOnClickListener(this);
        mDeleteSelectedBtn = (Button)findViewById(R.id.delete_selected_btn);
        mDeleteSelectedBtn.setOnClickListener(this);
        mEditCheckedDevices = mDeviceAdapter.getEditCheckedDevices();
        mDeviceAdapter.setOnEditCheckedChangeListener(this);
        
        mAutoRefreshHandler = new AutoRefreshHandler(this);
        // Get auto refresh settings data
        long autoRefreshTime =
            mShared.getLong(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_REFRESH, EspDefaults.AUTO_REFRESH_DEVICE_TIME);
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
        setTitleLeftIcon(0);
        setTitleRightIcon(R.drawable.esp_menu_icon_settings);
        setTitleProgressing();
        
        mRefreshing = false;
        if (mUser.isLogin())
        {
            refresh();
        }
        else
        {
            Toast.makeText(this, R.string.esp_ui_not_login_msg, Toast.LENGTH_LONG).show();
            scanSta();
        }
        
        EspGroupHandler.getInstance().call();
        
        if (mUser.isLogin())
        {
            if (mShared.getBoolean(EspStrings.Key.SETTINGS_KEY_ESPPUSH, EspDefaults.STORE_LOG))
            {
                EspPushUtils.startPushService(this);
            }
            else
            {
                EspPushUtils.stopPushService(this);
            }
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
            if (mIsDevicesPullRefreshUpdated)
            {
                mUser.doActionDevicesUpdated(false);
            }
            mUser.doActionDevicesUpdated(true);
        }
        // when the UI is showed, show the newest device list need the follow two sentences
        updateDeviceList();
        mDeviceAdapter.notifyDataSetChanged();
        if (mIsDevicesUpdatedNecessary)
        {
            mDeviceListView.onRefreshComplete();
            mRefreshing = false;
            mIsDevicesUpdatedNecessary = false;
            mIsDevicesPullRefreshUpdated = false;
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
        mNewDevicesShared.unregisterOnSharedPreferenceChangeListener(this);
        mBraodcastManager.unregisterReceiver(mReciever);
        EspBaseApiUtil.cancelAllTask();
        EspDeviceStateMachineHandler.getInstance().cancelAllTasks();
        EspGroupHandler.getInstance().finish();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_ESPTOUCH)
        {
            if (resultCode == RESULT_OK)
            {
                updateDeviceList();
                return;
            }
        }
        else if (requestCode == REQUEST_SETTINGS)
        {
            if (resultCode == SettingsActivity.RESULT_CODE_LOGOUT)
            {
                // cancel all task in thread pool before the activity is finished
                mUser.doActionUserLogout();
                EspPushService.stop(this);
                EspBaseApiUtil.cancelAllTask();
                EspDeviceStateMachineHandler.getInstance().cancelAllTasks();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected void onCreateBottomItems(IEspBottomMenu bottomMenu)
    {
        mConfigureBtn =
            bottomMenu.addBottomItem(MENU_ID_ADD_DEVICE, R.drawable.esp_icon_add, R.string.esp_ui_menu_add_device);
        bottomMenu.addBottomItem(MENU_ID_SCENE, R.drawable.esp_menu_icon_scene, R.string.esp_ui_menu_scene);
        bottomMenu.addBottomItem(MENU_ID_EDIT, R.drawable.esp_menu_icon_edit, R.string.esp_ui_menu_edit);
    }
    
    @Override
    protected void onBottomItemClick(View v, int itemId)
    {
        switch (itemId)
        {
            case MENU_ID_ADD_DEVICE:
                startActivityForResult(new Intent(this, DeviceEspTouchActivity.class), REQUEST_ESPTOUCH);
                break;
            case MENU_ID_SCENE:
                startActivity(new Intent(this, EspSceneActivity.class));
                break;
            case MENU_ID_EDIT:
                boolean bottomBarEnable = mEditBar.getVisibility() == View.VISIBLE;
                setEditBarEnable(!bottomBarEnable);
                break;
        }
    }
    
    private void setEditBarEnable(boolean enable)
    {
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
            long autoTime = mShared.getLong(key, EspDefaults.AUTO_REFRESH_DEVICE_TIME);
            if (autoTime > 0)
            {
                sendAutoRefreshMessage(autoTime);
            }
        }
        else if (key.equals(EspStrings.Key.SETTINGS_KEY_SHOW_MESH_TREE))
        {
            updateDeviceList();
        }
        else if (key.equals(mUser.getUserKey()))
        {
            mNewDevicesSet = mUser.getNewActivatedDevices();
            mDeviceAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    protected void onTitleRightIconClick(View rightIcon)
    {
        startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
    }
    
    @Override
    public void onRefresh(PullToRefreshBase<ListView> view)
    {
        if (view == mDeviceListView)
        {
            if (mUser.isLogin())
            {
                refresh();
            }
            else
            {
                scanSta();
            }
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
    
    private void scanSta()
    {
        if (!mRefreshing)
        {
            mUser.doActionRefreshStaDevices(false);
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
        mAllDeviceList.clear();
        boolean hasMeshDevice = false;
        List<IEspDevice> list = mUser.getAllDeviceList();
        for (int i = 0; i < list.size(); i++)
        {
            IEspDevice device = list.get(i);
            IEspDeviceState state = device.getDeviceState();
            if (!state.isStateDeleted())
            {
                if (device.getIsMeshDevice())
                {
                    hasMeshDevice = true;
                }
                if (!mDeviceVisibleCB.isChecked())
                {
                    if (!state.isStateLocal() && !state.isStateInternet())
                    {
                        continue;
                    }
                }
                
                mAllDeviceList.add(device);
            }
        }
        
        new DeviceSortor().sort(mAllDeviceList, mSortType);
        
        boolean showMeshTree =
            mShared.getBoolean(EspStrings.Key.SETTINGS_KEY_SHOW_MESH_TREE, EspDefaults.SHOW_MESH_TREE);
        if (hasMeshDevice && showMeshTree)
        {
            mVirtuaMeshRoot.setName("Mesh Root");
            mAllDeviceList.add(0, mVirtuaMeshRoot);
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
                
                mRefreshing = false;
                
                if (mIsDevicesUpdatedNecessary)
                {
                    mIsDevicesPullRefreshUpdated = true;
                }
            }
            else if (action.equals(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE))
            {
                log.debug("Receive Broadcast DEVICES_ARRIVE_STATEMACHINE");
                mUser.doActionDevicesUpdated(true);
                updateDeviceList();
                mDeviceAdapter.notifyDataSetChanged();
                
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
             * If all devices are selected, cancel all the select. else select all devices.
             */
            boolean allSelected = true;
            for (IEspDevice device : mAllDeviceList)
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
                mEditCheckedDevices.addAll(mAllDeviceList);
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (buttonView == mDeviceVisibleCB)
        {
            updateDeviceList();
            mDeviceAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        switch(position)
        {
            case POSITION_SORT_DEVICE_NAME:
                mSortType = DeviceSortType.DEVICE_NAME;
                break;
            case POSITION_SORT_ACTIVATE_TIME:
                mSortType = DeviceSortType.ACTIVATED_TIME;
                break;
        }
        
        updateDeviceList();
        mDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == mDeviceListView.getRefreshableView())
        {
            IEspDevice device = (IEspDevice)view.getTag();
            gotoUseDevice(device);
        }
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == mDeviceListView.getRefreshableView())
        {
            IEspDevice device = (IEspDevice)view.getTag();
            
            if (device.isActivated())
            {
                if (!isDeviceEditable(device))
                {
                    Toast.makeText(this, R.string.esp_ui_edit_forbidden_toast, Toast.LENGTH_SHORT).show();
                    return true;
                }
                
                PopupMenu popupMenu = new PopupMenu(this, view);
                Menu menu = popupMenu.getMenu();
                menu.add(Menu.NONE, POPUPMENU_ID_RENAME, 0, R.string.esp_ui_device_popupmenu_rename);
                menu.add(Menu.NONE, POPUPMENU_ID_DELETE, 0, R.string.esp_ui_device_popupmenu_delete);
                popupMenu.setOnMenuItemClickListener(new ListItemMenuListener(this, device));
                popupMenu.show();
                
                return true;
            }
            else
            {
                if (mUser.isLogin() && device.getDeviceType() != EspDeviceType.ROOT)
                {
                    PopupMenu popupMenu = new PopupMenu(this, view);
                    Menu menu = popupMenu.getMenu();
                    menu.add(Menu.NONE, POPUPMENU_ID_ACTIVATE, 0, R.string.esp_configure_activate);
                    popupMenu.setOnMenuItemClickListener(new ListItemMenuListener(this, device));
                    popupMenu.show();
                }
                return true;
                
            }
        }
        
        return false;
    }
    
    @Override
    public void onEditCheckedChanged(CheckBox checkBox, IEspDevice device, boolean isChecked)
    {
        mDeleteSelectedBtn.setEnabled(!mEditCheckedDevices.isEmpty());
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
        
        Class<?> _class = DeviceActivityAbs.getDeviceClass(device);
        if (_class != null)
        {
            Intent intent = new Intent(this, _class);
            intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, device.getKey());
            startActivityForResult(intent, REQUEST_DEVICE);
            mUser.deleteNewActivatedDevice(device.getKey());
            return true;
        }
        
        return false;
    }
    
    private boolean isDeviceEditable(IEspDevice device)
    {
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
    
    private class EspDeviceAdapter extends DeviceAdapter
    {

        public EspDeviceAdapter(Activity activity, List<IEspDevice> list)
        {
            super(activity, list);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            convertView = super.getView(position, convertView, parent);
            
            IEspDevice device = (IEspDevice)convertView.getTag();
            
            TextView contentTV = (TextView)convertView.findViewById(R.id.content_text);
            contentTV.setTextColor(Color.RED);
            contentTV.setText("NEW");
            boolean newActivated = mNewDevicesSet.contains(device.getKey());
            contentTV.setVisibility(newActivated ? View.VISIBLE : View.GONE);
            
            return convertView;
        }
        
    }
    
    private class ListItemMenuListener implements PopupMenu.OnMenuItemClickListener
    {
        private IEspDevice mItemDevice;
        private Context mContext;
        
        public ListItemMenuListener(Context context, IEspDevice device)
        {
            mContext = context;
            mItemDevice = device;
        }
        
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch(item.getItemId())
            {
                case POPUPMENU_ID_RENAME:
                    showRenameDialog();
                    return true;
                case POPUPMENU_ID_DELETE:
                    showDeleteDialog();
                    return true;
                case POPUPMENU_ID_ACTIVATE:
                    activateDevice();
                    return true;
            }
            return false;
        }
        
        private void showRenameDialog()
        {
            final EditText nameEdit = new EditText(mContext);
            nameEdit.setSingleLine();
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            nameEdit.setLayoutParams(lp);
            new AlertDialog.Builder(mContext).setView(nameEdit)
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
            new AlertDialog.Builder(mContext).setTitle(mItemDevice.getName())
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
        
        private void activateDevice()
        {
            final ProgressDialog dialog = new ProgressDialog(mContext);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.esp_ui_status_activating));
            dialog.show();
            new AsyncTask<Void, Void, Boolean>()
            {
                
                @Override
                protected Boolean doInBackground(Void... params)
                {
                    boolean isSuc = mUser.addDeviceSyn((IEspDeviceSSS)mItemDevice);
                    mUser.doActionRefreshStaDevices(true);
                    return isSuc;
                }
                
                protected void onPostExecute(Boolean result)
                {
                    int msgRes = result ? R.string.esp_ui_device_activate_suc : R.string.esp_ui_device_activate_failed;
                    Toast.makeText(mContext, msgRes, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    updateDeviceList();
                    mDeviceAdapter.notifyDataSetChanged();
                }
            }.execute();
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
                        finish();
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
    
    protected void checkHelpConfigure()
    {
    }
    
    protected boolean checkHelpClickDeviceType(EspDeviceType type)
    {
        return false;
    }
}
