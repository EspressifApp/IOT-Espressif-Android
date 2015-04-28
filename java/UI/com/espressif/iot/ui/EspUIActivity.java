package com.espressif.iot.ui;

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
import com.espressif.iot.device.builder.BEspDeviceRoot;
import com.espressif.iot.help.ui.IEspHelpUIConfigure;
import com.espressif.iot.help.ui.IEspHelpUIUpgradeLocal;
import com.espressif.iot.help.ui.IEspHelpUIUpgradeOnline;
import com.espressif.iot.help.ui.IEspHelpUIUseFlammable;
import com.espressif.iot.help.ui.IEspHelpUIUseHumiture;
import com.espressif.iot.help.ui.IEspHelpUIUseLight;
import com.espressif.iot.help.ui.IEspHelpUIUsePlug;
import com.espressif.iot.help.ui.IEspHelpUIUsePlugs;
import com.espressif.iot.help.ui.IEspHelpUIUseRemote;
import com.espressif.iot.help.ui.IEspHelpUIUseVoltage;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.help.HelpStepConfigure;
import com.espressif.iot.type.help.HelpStepUpgradeLocal;
import com.espressif.iot.type.help.HelpStepUpgradeOnline;
import com.espressif.iot.type.help.HelpStepUseFlammable;
import com.espressif.iot.type.help.HelpStepUseHumiture;
import com.espressif.iot.type.help.HelpStepUseLight;
import com.espressif.iot.type.help.HelpStepUsePlug;
import com.espressif.iot.type.help.HelpStepUsePlugs;
import com.espressif.iot.type.help.HelpStepUseRemote;
import com.espressif.iot.type.help.HelpStepUseVoltage;
import com.espressif.iot.type.help.HelpType;
import com.espressif.iot.ui.configure.DeviceConfigureActivity;
import com.espressif.iot.ui.configure.WifiConfigureActivity;
import com.espressif.iot.ui.device.DeviceFlammableActivity;
import com.espressif.iot.ui.device.DeviceHumitureActivity;
import com.espressif.iot.ui.device.DeviceLightActivity;
import com.espressif.iot.ui.device.DevicePlugActivity;
import com.espressif.iot.ui.device.DevicePlugsActivity;
import com.espressif.iot.ui.device.DeviceRemoteActivity;
import com.espressif.iot.ui.device.DeviceRootRouterActivity;
import com.espressif.iot.ui.device.DeviceVoltageActivity;
import com.espressif.iot.ui.help.HelpActivity;
import com.espressif.iot.ui.settings.SettingsActivity;
import com.espressif.iot.ui.welcome.LoginActivity;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
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
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EspUIActivity extends EspActivityAbs implements OnRefreshListener<ListView>,
    OnSharedPreferenceChangeListener, OnItemClickListener, OnItemLongClickListener, OnClickListener,
    IEspHelpUIConfigure, IEspHelpUIUsePlug, IEspHelpUIUseLight, IEspHelpUIUseHumiture, IEspHelpUIUseFlammable,
    IEspHelpUIUseVoltage ,IEspHelpUIUseRemote, IEspHelpUIUsePlugs, IEspHelpUIUpgradeLocal, IEspHelpUIUpgradeOnline
{
    private static final Logger log = Logger.getLogger(EspUIActivity.class);
    
    private IEspUser mUser;
    
    private static final int MENU_ID_GET_SHARE = 0;
    private static final int MENU_ID_CONFIGURE = 1;
    private static final int MENU_ID_SETTINGS = 2;
    private static final int MENU_ID_LOGOUT = 3;
    private static final int MENU_ID_EDIT = 4;
    private static final int MENU_ID_WIFI = 5;
    
    /**
     * There is a header in PullToRefreshListView, so the list items are behind header.
     */
    private final int LIST_HEADER_COUNT = 1;
    
    private PullToRefreshListView mDeviceListView;
    private DeviceAdapter mDeviceAdapter;
    private List<IEspDevice> mDeviceList;
    
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
    
    private final static int REQUEST_HELP = 0;
    private final static int REQUEST_CONFIGURE = 1;
    private final static int REQUEST_DEVICE = 2;
    
    private final static int HELP_FIND_UPGRADE_LOCAL = 1;
    private final static int HELP_FIND_UPGRADE_ONLINE = 2;
    
    private LocalBroadcastManager mBraodcastManager;
    
    private IEspDevice mLocalRoot;
    private IEspDevice mInternetRoot;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.esp_ui_activity);
        
        mUser = BEspUser.getBuilder().getInstance();
        mShared = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mShared.registerOnSharedPreferenceChangeListener(this);
        
        // Init device list
        mLocalRoot = BEspDeviceRoot.getBuilder().getLocalRoot();
        mInternetRoot = BEspDeviceRoot.getBuilder().getInternetRoot();
        mDeviceListView = (PullToRefreshListView)findViewById(R.id.devices_list);
        mDeviceList = new Vector<IEspDevice>();
        updateDeviceList();
        mDeviceAdapter = new DeviceAdapter(this);
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
        mEditCheckedDevices = new HashSet<IEspDevice>();
        
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
        mBraodcastManager.registerReceiver(mReciever, filter);
        
        ProgressBar progressbar = new ProgressBar(this);
        int progressPadding = getResources().getDimensionPixelSize(R.dimen.esp_activity_ui_progress_padding);
        setTitleContentView(progressbar, progressPadding, progressPadding, progressPadding, progressPadding);
        mRefreshing = false;
        refresh();
        
        setTitle(R.string.app_name);
        setTitleRightIcon(R.drawable.esp_icon_help);
        mHelpHandler = new UseDeviceHelpHandler(this);
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
        if (mIsDevicesUpdatedNecessary)
        {
            mDeviceListView.onRefreshComplete();
            mRefreshing = false;
            mIsDevicesUpdatedNecessary = false;
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
    
    private View mConfigureBtn;
    
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
                if (mHelpMachine.isHelpModeConfigure())
                {
                    mHelpMachine.transformState(true);
                }
                gotoConfigure();
                break;
            case MENU_ID_SETTINGS:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case MENU_ID_LOGOUT:
                EspBaseApiUtil.cancelAllTask();
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
    public void onRefresh(PullToRefreshBase<ListView> view)
    {
        refresh();
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
    }
    
    private void updateDeviceList()
    {
        mDeviceList.clear();
        boolean hasMeshDevice = false;
        List<IEspDevice> list = mUser.getDeviceList();
        for (int i = 0; i < list.size(); i++)
        {
            IEspDevice device = list.get(i);
            if (device.getIsMeshDevice())
            {
                hasMeshDevice = true;
            }
            if (!device.getDeviceState().isStateDeleted())
            {
                mDeviceList.add(device);
            }
        }
        
        if (hasMeshDevice)
        {
            if (EspBaseApiUtil.isWifiConnected())
            {
                mLocalRoot.setName(EspBaseApiUtil.getWifiConnectedSsid());
                mDeviceList.add(0, mLocalRoot);
            }
            if (EspBaseApiUtil.isNetworkAvailable())
            {
                mInternetRoot.setName("Internet Root Router");
                mDeviceList.add(0, mInternetRoot);
            }
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
                    log.warn("EspUIActivity getView status wrong");
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
    
    private BroadcastReceiver mReciever = new BroadcastReceiver()
    {
        
        @Override
        public void onReceive(Context context, Intent intent)
        {
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
            final String action = intent.getAction();
            if (action.equals(EspStrings.Action.DEVICES_ARRIVE_PULLREFRESH))
            {
                log.debug("Receive Broadcast DEVICES_ARRIVE_PULLREFRESH");
                // Refresh list
                mUser.doActionDevicesUpdated(false);
                
                updateDeviceList();
                mDeviceAdapter.notifyDataSetChanged();
                
                mDeviceListView.onRefreshComplete();
                mRefreshing = false;
            }
            else if (action.equals(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE))
            {
                log.debug("Receive Broadcast DEVICES_ARRIVE_STATEMACHINE");
                mUser.doActionDevicesUpdated(true);
                updateDeviceList();
                mDeviceAdapter.notifyDataSetChanged();
                
                if (mHelpMachine.isHelpModeConfigure())
                {
                    onHelpConfigure();
                }
            }
            
            setTitleContentView(null);
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
        IEspDevice device = mDeviceList.get(position - LIST_HEADER_COUNT);
        gotoUseDevice(device);
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (mHelpMachine.isHelpOn())
        {
            return true;
        }
        
        IEspDevice device = mDeviceList.get(position - LIST_HEADER_COUNT);
        if (!isDeviceEditable(device))
        {
            Toast.makeText(this, R.string.esp_ui_edit_forbidden_toast, Toast.LENGTH_SHORT).show();
            return true;
        }
        
        new AlertDialog.Builder(this).setItems(R.array.esp_ui_device_dialog_items, new ListItemDialogListener(device))
            .show();
        
        return true;
    }
    
    private void gotoConfigure()
    {
        Intent intent = new Intent(this, DeviceConfigureActivity.class);
        startActivityForResult(intent, REQUEST_CONFIGURE);
    }
    
    /**
     * 
     * @param device
     * @return true go to use device success, false for some reasons the device can't use
     */
    private boolean gotoUseDevice(IEspDevice device)
    {
        IEspDeviceState state = device.getDeviceState();
        if (state.isStateUpgradingInternet() || state.isStateUpgradingLocal())
        {
            return false;
        }
        
        if (mHelpMachine.isHelpOn() && !onHelpDeviceClick(device.getDeviceType()))
        {
            // The help mode is on, but not the clicked device type help
            return false;
        }
        
        String deviceKey = device.getKey();
        switch (device.getDeviceType())
        {
            case PLUG:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    Intent IPlug = new Intent(this, DevicePlugActivity.class);
                    IPlug.putExtra(EspStrings.Key.DEVICE_KEY_KEY, deviceKey);
                    startActivityForResult(IPlug, REQUEST_DEVICE);
                    return true;
                }
                break;
            case LIGHT:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    Intent ILight = new Intent(this, DeviceLightActivity.class);
                    ILight.putExtra(EspStrings.Key.DEVICE_KEY_KEY, deviceKey);
                    startActivityForResult(ILight, REQUEST_DEVICE);
                    return true;
                }
                break;
            case FLAMMABLE:
                if (state.isStateInternet() || state.isStateOffline())
                {
                    Intent IFlammable = new Intent(this, DeviceFlammableActivity.class);
                    IFlammable.putExtra(EspStrings.Key.DEVICE_KEY_KEY, deviceKey);
                    startActivityForResult(IFlammable, REQUEST_DEVICE);
                    return true;
                }
                break;
            case HUMITURE:
                if (state.isStateInternet() || state.isStateOffline())
                {
                    Intent IHumiture = new Intent(this, DeviceHumitureActivity.class);
                    IHumiture.putExtra(EspStrings.Key.DEVICE_KEY_KEY, deviceKey);
                    startActivityForResult(IHumiture, REQUEST_DEVICE);
                    return true;
                }
                break;
            case VOLTAGE:
                if (state.isStateInternet() || state.isStateOffline())
                {
                    Intent IVoltage = new Intent(this, DeviceVoltageActivity.class);
                    IVoltage.putExtra(EspStrings.Key.DEVICE_KEY_KEY, deviceKey);
                    startActivityForResult(IVoltage, REQUEST_DEVICE);
                    return true;
                }
                break;
            case REMOTE:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    Intent IRemote = new Intent(this, DeviceRemoteActivity.class);
                    IRemote.putExtra(EspStrings.Key.DEVICE_KEY_KEY, deviceKey);
                    startActivityForResult(IRemote, REQUEST_DEVICE);
                    return true;
                }
                break;
            case PLUGS:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    Intent iPlugs = new Intent(this, DevicePlugsActivity.class);
                    iPlugs.putExtra(EspStrings.Key.DEVICE_KEY_KEY, deviceKey);
                    startActivityForResult(iPlugs, REQUEST_DEVICE);
                    return true;
                }
                break;
            case ROOT:
                if (state.isStateInternet() || state.isStateLocal())
                {
                    Intent iRootRouter = new Intent(this, DeviceRootRouterActivity.class);
                    iRootRouter.putExtra(EspStrings.Key.DEVICE_KEY_KEY, deviceKey);
                    startActivity(iRootRouter);
                    return true;
                }
                break;
            case NEW:
                log.warn("Click on NEW device, it shouldn't happen");
                break;
        }
        
        return false;
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
        if(mHelpMachine.isHelpOn())
        {
            // show exit help mode dialog
            super.onBackPressed();
        }
        else if (mEditBar.getVisibility() == View.VISIBLE)
        {
            // Exit edit mode
            setEditBarEnable(false);
        }
        else
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
    }
    
    @Override
    protected void onTitleRightIconClick()
    {
        startActivityForResult(new Intent(this, HelpActivity.class), REQUEST_HELP);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_HELP)
        {
            mHelpHandler.sendEmptyMessage(resultCode);
        }
        else if (requestCode == REQUEST_CONFIGURE)
        {
            switch(resultCode)
            {
                case RESULT_HELP_CONFIGURE:
                    onHelpConfigure();
                    break;
                case RESULT_EXIT_HELP_MODE:
                    onExitHelpMode();
                    break;
            }
        }
        else if (requestCode == REQUEST_DEVICE)
        {
            switch (resultCode)
            {
                case RESULT_EXIT_HELP_MODE:
                    onExitHelpMode();
                    break;
                case RESULT_HELP_UPGRADE_LOCAL:
                    onHelpUpgradeLocal();
                    break;
                case RESULT_HELP_UPGRADE_ONLINE:
                    onHelpUpgradeOnline();
                    break;
            }
        }
    }
    
    private Handler mHelpHandler;
    
    private static class UseDeviceHelpHandler extends Handler
    {
        private WeakReference<EspUIActivity> mActivity;
        
        public UseDeviceHelpHandler(EspUIActivity activity)
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
            
            switch(msg.what)
            {
                case RESULT_HELP_CONFIGURE:
                    mHelpMachine.start(HelpType.CONFIGURE);
                    activity.onHelpConfigure();
                    break;
                case RESULT_HELP_USE_PLUG:
                    mHelpMachine.start(HelpType.USAGE_PLUG);
                    activity.onHelpUsePlug();
                    break;
                case RESULT_HELP_USE_LIGHT:
                    mHelpMachine.start(HelpType.USAGE_LIGHT);
                    activity.onHelpUseLight();
                    break;
                case RESULT_HELP_USE_HUMITURE:
                    mHelpMachine.start(HelpType.USAGE_HUMITURE);
                    activity.onHelpUseHumiture();
                    break;
                case RESULT_HELP_USE_FLAMMABLE:
                    mHelpMachine.start(HelpType.USAGE_FLAMMABLE);
                    activity.onHelpUseFlammable();
                    break;
                case RESULT_HELP_USE_VOLTAGE:
                    mHelpMachine.start(HelpType.USAGE_VOLTAGE);
                    activity.onHelpUseVoltage();
                    break;
                case RESULT_HELP_USE_REMOTE:
                    mHelpMachine.start(HelpType.USAGE_REMOTE);
                    activity.onHelpUseRemote();
                    break;
                case RESULT_HELP_USE_PLUGS:
                    mHelpMachine.start(HelpType.USAGE_PLUGS);
                    activity.onHelpUsePlugs();
                    break;
                case RESULT_HELP_UPGRADE_LOCAL:
                    mHelpMachine.start(HelpType.UPGRADE_LOCAL);
                    activity.onHelpUpgradeLocal();
                    break;
                case RESULT_HELP_UPGRADE_ONLINE:
                    mHelpMachine.start(HelpType.UPGRADE_ONLINE);
                    activity.onHelpUpgradeOnline();
                    break;
            }
        }
    }
    
    @Override
    public void onHelpConfigure()
    {
        clearHelpContainer();
        
        HelpStepConfigure step = HelpStepConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_CONFIGURE_HELP:
                hintConfigureStart();
                break;
            case DEVICE_IS_ACTIVATING:
                setHelpHintMessage(R.string.esp_help_configure_device_activating_msg);
                break;
            case FAIL_CONNECT_AP:
                setHelpHintMessage(R.string.esp_help_configure_connect_ap_failed_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case FAIL_ACTIVATE:
                setHelpHintMessage(R.string.esp_help_configure_device_activate_failed_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case SUC:
                setHelpHintMessage(R.string.esp_help_configure_success_msg);
                break;
            default:
                // Not process here
                break;
        }
    }
    
    private void hintConfigureStart()
    {
        highlightHelpView(mConfigureBtn);
        
        int statusBarHeight = getStatusBarHeight();
        
        int[] conLocation = new int[2];
        mConfigureBtn.getLocationInWindow(conLocation);
        Rect rect =
            new Rect(conLocation[0], conLocation[1] - statusBarHeight, conLocation[0] + mConfigureBtn.getWidth(),
                conLocation[1] + mConfigureBtn.getHeight() - statusBarHeight);
        
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(rect.width(), rect.height());
        ImageView hintBtn = new ImageView(this);
        hintBtn.setScaleType(ScaleType.CENTER);
        hintBtn.setImageResource(R.drawable.esp_help_hint);
        mHelpContainer.addView(hintBtn, lp);
        hintBtn.setX(rect.left);
        hintBtn.setY(rect.top);
    }
    
    @Override
    public void onHelpUsePlug()
    {
        clearHelpContainer();
        
        HelpStepUsePlug step = HelpStepUsePlug.valueOf(mHelpMachine.getCurrentStateOrdinal());
        log.debug("onHelpUsePlug() Plug step = " + step);
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.PLUG);
                onHelpUsePlug();
                break;
            case FAIL_FOUND_PLUG:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_plug_fail_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                useDeviceHelpFindOnline(EspDeviceType.PLUG);
                onHelpUsePlug();
                break;
            case NO_PLUG_ONLINE:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_use_plug_no_online_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case PLUG_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_plug_selcet_msg);
                break;
            case PLUG_NOT_COMPATIBILITY:
            case PLUG_CONTROL:
            case PLUG_CONTROL_FAILED:
            case SUC:
                break;
        }
    }
    
    @Override
    public void onHelpUsePlugs()
    {
        clearHelpContainer();
        
        HelpStepUsePlugs step = HelpStepUsePlugs.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.PLUGS);
                onHelpUsePlugs();
                break;
            case FAIL_FOUND_PLUGS:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_plugs_fail_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                useDeviceHelpFindOnline(EspDeviceType.PLUGS);
                onHelpUsePlugs();
                break;
            case NO_PLUGS_ONLINE:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_use_plugs_no_online_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case PLUGS_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_plugs_selcet_msg);
                break;
            case PLUGS_CONTROL:
            case PLUGS_CONTROL_FAILED:
            case PLUGS_NOT_COMPATIBILITY:
            case SUC:
                break;
            
        }
    }
    
    @Override
    public void onHelpUseLight()
    {
        clearHelpContainer();
        
        HelpStepUseLight step = HelpStepUseLight.valueOf(mHelpMachine.getCurrentStateOrdinal());
        log.debug("onHelpUsePlug() Light step = " + step);
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.LIGHT);
                onHelpUseLight();
                break;
            case FAIL_FOUND_LIGHT:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_light_fail_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                useDeviceHelpFindOnline(EspDeviceType.LIGHT);
                onHelpUseLight();
                break;
            case NO_LIGHT_ONLINE:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_use_light_no_online_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case LIGHT_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_light_select_msg);
                break;
            case LIGHT_NOT_COMPATIBILITY:
            case LIGHT_CONTROL:
            case LIGHT_CONTROL_FAILED:
            case SUC:
                // not process here
                break;
        }
    }
    
    @Override
    public void onHelpUseHumiture()
    {
        clearHelpContainer();
        
        HelpStepUseHumiture step = HelpStepUseHumiture.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.HUMITURE);
                onHelpUseHumiture();
                break;
            case FAIL_FOUND_HUMITURE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_humiture_faile_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case HUMITURE_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_humiture_select_msg);
                break;

            case HUMITURE_NOT_COMPATIBILITY:
                break;
            case PULL_DOWN_TO_REFRESH:
                break;
            case GET_DATA_FAILED:
                break;
            case SELECT_DATE:
                break;
            case SELECT_DATE_FAILED:
                break;
            case SUC:
                break;
        }
    }
    
    @Override
    public void onHelpUseFlammable()
    {
        clearHelpContainer();
        
        HelpStepUseFlammable step = HelpStepUseFlammable.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.FLAMMABLE);
                onHelpUseFlammable();
                break;
            case FAIL_FOUND_FLAMMABLE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_flammable_faile_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FLAMMABLE_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_flammable_select_msg);
                break;
                
            case FLAMMABLE_NOT_COMPATIBILITY:
                break;
            case GET_DATA_FAILED:
                break;
            case PULL_DOWN_TO_REFRESH:
                break;
            case SELECT_DATE:
                break;
            case SELECT_DATE_FAILED:
                break;
            case SUC:
                break;
        }
    }
    
    @Override
    public void onHelpUseVoltage()
    {
        clearHelpContainer();
        
        HelpStepUseVoltage step = HelpStepUseVoltage.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.VOLTAGE);
                onHelpUseFlammable();
                break;
            case FAIL_FOUND_VOLTAGE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_voltage_faile_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case VOLTAGE_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_voltage_select_msg);
                break;
                
            case VOLTAGE_NOT_COMPATIBILITY:
                break;
            case GET_DATA_FAILED:
                break;
            case PULL_DOWN_TO_REFRESH:
                break;
            case SELECT_DATE:
                break;
            case SELECT_DATE_FAILED:
                break;
            case SUC:
                break;
        }
    }

    
    @Override
    public void onHelpUseRemote()
    {
        clearHelpContainer();
        
        HelpStepUseRemote step = HelpStepUseRemote.valueOf(mHelpMachine.getCurrentStateOrdinal());
        log.error("step = " + step);
        switch (step)
        {
            case START_USE_HELP:
                useDeviceHelpStart(EspDeviceType.REMOTE);
                onHelpUseRemote();
                break;
            case FAIL_FOUND_REMOTE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_remote_fail_found_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                useDeviceHelpFindOnline(EspDeviceType.REMOTE);
                onHelpUseRemote();
                break;
            case NO_REMOTE_ONLINE:
                highlightHelpView(mDeviceListView);
                setHelpHintMessage(R.string.esp_help_use_remote_no_online_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            case REMOTE_SELECT:
                useDeviceHelpHintSelect(R.string.esp_help_use_remote_select_msg);
                break;
            
            case REMOTE_NOT_COMPATIBILITY:
                break;
            case REMOTE_CONTROL:
                break;
            case REMOTE_CONTROL_FAILED:
                break;
            case SUC:
                break;
        }
    }
    
    /**
     * When click device on help mode, check whether is this device type help mode
     * @param type
     * @return
     */
    private boolean onHelpDeviceClick(EspDeviceType type)
    {
        if (mHelpMachine.isHelpModeUpgradeLocal() || mHelpMachine.isHelpModeUpgradeOnline())
        {
            return true;
        }
        
        switch(type)
        {
            case PLUG:
                return mHelpMachine.isHelpModeUsePlug();
            case LIGHT:
                return mHelpMachine.isHelpModeUseLight();
            case HUMITURE:
                return mHelpMachine.isHelpModeUseHumiture();
            case FLAMMABLE:
                return mHelpMachine.isHelpModeUseFlammable();
            case VOLTAGE:
                return mHelpMachine.isHelpModeUseVoltage();
            case REMOTE:
                return mHelpMachine.isHelpModeUseRemote();
            case PLUGS:
                return mHelpMachine.isHelpModeUsePlugs();
            case ROOT:
                break;
            case NEW:
                break;
        }
        
        return false;
    }
    
    /**
     * Start use device help, find this type of device in user device list
     * @param type
     */
    private void useDeviceHelpStart(EspDeviceType type)
    {
        for (int i = 0; i < mDeviceList.size(); i++)
        {
            IEspDevice device = mDeviceList.get(i);
            if (device.getDeviceType() == type)
            {
                if (type == EspDeviceType.FLAMMABLE || type == EspDeviceType.HUMITURE)
                {
                    mHelpMachine.setDeviceSelection(i + LIST_HEADER_COUNT);
                }
                mHelpMachine.transformState(true);
                return;
            }
        }
        
        mHelpMachine.transformState(false);
    }
    
    /**
     * Find online device in user device list
     * @param type
     */
    private void useDeviceHelpFindOnline(EspDeviceType type)
    {
        for (int i = 0; i < mDeviceList.size(); i++)
        {
            IEspDevice device = mDeviceList.get(i);
            if (device.getDeviceType() == type)
            {
                IEspDeviceState state = device.getDeviceState();
                if (state.isStateLocal() || state.isStateInternet())
                {
                    mHelpMachine.setDeviceSelection(i + LIST_HEADER_COUNT);
                    mHelpMachine.transformState(true);
                    return;
                }
            }
        }
        
        mHelpMachine.resetDeviceSelection();
        mHelpMachine.transformState(false);
    }
    
    /**
     * Hint user click the device
     * @param hintMsgRes
     */
    private void useDeviceHelpHintSelect(int hintMsgRes)
    {
        int selection = mHelpMachine.getDeviceSelection();
        mHelpMachine.resetDeviceSelection();
        mDeviceListView.getRefreshableView().setSelection(selection);
        highlightHelpView(mDeviceListView);
        setHelpHintMessage(hintMsgRes);
    }
    
    @Override
    public void onHelpUpgradeLocal()
    {
        clearHelpContainer();
        
        HelpStepUpgradeLocal step = HelpStepUpgradeLocal.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_UPGRADE_LOCAL:
                mHelpMachine.transformState(findHelpUpgradDevice(HELP_FIND_UPGRADE_LOCAL, false) != null);
                onHelpUpgradeLocal();
                break;
            case NO_DEVICE_NEED_UPGRADE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_upgrade_local_no_device_need_upgrade_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_LOCAL:
                mHelpMachine.transformState(findHelpUpgradDevice(HELP_FIND_UPGRADE_LOCAL, true) != null);
                onHelpUpgradeLocal();
                break;
            case NO_DEVICE_LOCAL:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_upgrade_local_no_device_local_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FOUND_LOCAL:
                IEspDevice device = findHelpUpgradDevice(HELP_FIND_UPGRADE_LOCAL, true);
                log.info("device name = " + device.getName());
                gotoUseDevice(device);
                mHelpMachine.transformState(true);
                break;
            
            case CHECK_COMPATIBILITY:
                break;
            case UPGRADING:
                break;
            case UPGRADE_FAILED:
                Toast.makeText(this, R.string.esp_help_upgrade_local_failed_msg, Toast.LENGTH_LONG).show();
                mHelpMachine.exit();
                onExitHelpMode();
                break;
            case SUC:
                Toast.makeText(this, R.string.esp_help_upgrade_local_success_msg, Toast.LENGTH_LONG).show();
                mHelpMachine.exit();
                onExitHelpMode();
                break;
        }
    }
    
    @Override
    public void onHelpUpgradeOnline()
    {
        clearHelpContainer();
        
        HelpStepUpgradeOnline step = HelpStepUpgradeOnline.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_UPGRADE_ONLINE:
                mHelpMachine.transformState(findHelpUpgradDevice(HELP_FIND_UPGRADE_ONLINE, false) != null);
                onHelpUpgradeOnline();
                break;
            case NO_DEVICE_NEED_UPGRADE:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_upgrade_online_no_device_need_upgrade_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FIND_ONLINE:
                mHelpMachine.transformState(findHelpUpgradDevice(HELP_FIND_UPGRADE_ONLINE, true) != null);
                onHelpUpgradeOnline();
                break;
            case NO_DEVICE_LOCAL:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_upgrade_online_no_device_local_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
            case FOUND_ONLINE:
                gotoUseDevice(findHelpUpgradDevice(HELP_FIND_UPGRADE_ONLINE, true));
                mHelpMachine.transformState(true);
                break;
            
            case CHECK_COMPATIBILITY:
                break;
            case UPGRADING:
                break;
            case UPGRADE_FAILED:
                Toast.makeText(this, R.string.esp_help_upgrade_online_failed_msg, Toast.LENGTH_LONG).show();
                mHelpMachine.exit();
                onExitHelpMode();
                break;
            case SUC:
                Toast.makeText(this, R.string.esp_help_upgrade_online_success_msg, Toast.LENGTH_LONG).show();
                mHelpMachine.exit();
                onExitHelpMode();
                break;
        }
    }
    
    /**
     * Find device need upgrade
     * 
     * @param upgradeType @see {@value #HELP_FIND_UPGRADE_LOCAL} & {@value #HELP_FIND_UPGRADE_ONLINE}
     * @return
     */
    private IEspDevice findHelpUpgradDevice(int upgradeType, boolean checkState)
    {
        for (int i = 0; i < mDeviceList.size(); i++)
        {
            IEspDevice device = mDeviceList.get(i);
            
            if (upgradeType == HELP_FIND_UPGRADE_LOCAL)
            {
                switch(mUser.getDeviceUpgradeTypeResult(device))
                {
                    case SUPPORT_ONLINE_LOCAL:
                    case SUPPORT_LOCAL_ONLY:
                        if (!checkState)
                        {
                            return device;
                        }
                        
                        if (device.getDeviceState().isStateLocal())
                        {
                            return device;
                        }
                        
                        break;
                    default:
                        break;
                }
            }
            else if (upgradeType == HELP_FIND_UPGRADE_ONLINE)
            {
                switch(mUser.getDeviceUpgradeTypeResult(device))
                {
                    case SUPPORT_ONLINE_LOCAL:
                    case SUPPORT_ONLINE_ONLY:
                        if (!checkState)
                        {
                            return device;
                        }
                        
                        if (device.getDeviceState().isStateInternet())
                        {
                            return device;
                        }
                        
                        break;
                    default:
                        break;
                }
            }
        }

        return null;
    }
    
    @Override
    public void onExitHelpMode()
    {
        clearHelpContainer();
    }
    
    @Override
    public void onHelpRetryClick()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            onHelpConfigure();
        }
        else if (mHelpMachine.isHelpModeUsePlug())
        {
            onHelpUsePlug();
        }
        else if (mHelpMachine.isHelpModeUseLight())
        {
            onHelpUseLight();
        }
        else if (mHelpMachine.isHelpModeUseRemote())
        {
            onHelpUseRemote();
        }
    }

}
