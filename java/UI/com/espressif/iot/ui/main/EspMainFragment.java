package com.espressif.iot.ui.main;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.net.udp.UdpServer;
import com.espressif.iot.base.net.udp.UdpServer.OnLightTwinkleListener;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.device.builder.BEspDeviceRoot;
import com.espressif.iot.group.IEspGroup;
import com.espressif.iot.model.device.sort.DeviceSortor;
import com.espressif.iot.model.device.sort.DeviceSortor.DeviceSortType;
import com.espressif.iot.model.thread.FinishThread;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.ui.device.light.LightUtils;
import com.espressif.iot.ui.group.EspGroupEditActivity;
import com.espressif.iot.ui.task.DeviceStatusTask;
import com.espressif.iot.ui.widget.view.ColorView;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.DeviceUtil;
import com.espressif.iot.util.EspDefaults;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class EspMainFragment extends Fragment implements OnSharedPreferenceChangeListener, OnClickListener,
    OnItemClickListener, OnItemLongClickListener, OnLongClickListener, OnCheckedChangeListener, OnRefreshListener {
    private static final Logger log = Logger.getLogger(EspMainFragment.class);

    private EspMainActivity mActivity;
    private IEspUser mUser;

    private RecyclerView mGroupListView;
    private List<IEspGroup> mGroupList;
    private GroupAdapter mGroupAdapter;
    private TextView mAllGroupView;
    private View mAddGroupView;

    private volatile IEspGroup mSelectedGroup;

    private static final int POPMENU_ID_GROUP_RENAME = 0x10;
    private static final int POPMENU_ID_GROUP_DELETE = 0x11;

    private EspDeviceType mFilterDeviceType;
    private boolean mFilterDeviceUsable;

    private SwipeRefreshLayout mDeviceRefreshLayout;
    private ListView mDeviceListView;
    private DeviceAdapter mDeviceAdapter;
    private List<IEspDevice> mAllDeviceList;
    private LinkedBlockingQueue<Object> mDevicesUpdateMsgQueue;
    private UpdateDeviceThread mUpdateDeviceThread;

    private static final int POPUP_ID_DEVICE_RENAME = 0x20;
    private static final int POPUP_ID_DEVICE_DELETE = 0x21;
    private static final int POPUP_ID_DEVICE_ACTIVATE = 0x22;
    private static final int POPUP_ID_DEVICE_MORE = 0x30;

    private final static int REQUEST_DEVICE = 0x11;
    private final static int REQUEST_GROUP_CREATE = 0x12;

    public static final int RESULT_SCAN = 0x10;
    public static final int RESULT_UPGRADE_LOCAL = 0x11;
    public static final int RESULT_UPGRADE_INTERNET = 0x12;

    /**
     * Whether the device refresh task is running
     */
    private boolean mRefreshing;
    private Handler mRefreshHandler;
    private static final int MSG_AUTO_REFRESH = 0;
    private static final int MSG_UPDATE_TEMPDEVICE = 1;
    private static final int MSG_TWINKLE = 2;
    private static final int MSG_LAYOUT_REFRESH = 3;

    private SharedPreferences mSettingsShared;

    private SharedPreferences mNewDevicesShared;
    private Set<String> mNewDevicesSet;

    private DeviceSortType mSortType;

    private LocalBroadcastManager mBroadcastManager;

    private boolean mActivityVisible;
    private boolean mIsDevicesUpdatedNecessary = false;
    private boolean mIsDevicesPullRefreshUpdated = false;

    private View mBottomBar;
    private ImageView mAllDeviceBtn;
    private ImageView mActivateDeviceBtn;
    private ImageView mDeleteDeviceBtn;
    private ImageView mMoveDeviceBtn;
    private ImageView mRemoveDeviceBtn;
    private ImageView mUpgradeDeviceBtn;

    private RecyclerView mRecyclerView;
    private ColorAdapter mColorAdapter;
    private List<Integer> mColorList;
    private CheckBox mDevicesSwitch;

    private EditText mSearchET;

    private boolean mOnStartRefreshMark = false;

    private static final int SORT_POSITION_DEVICE_NAME = 0;
    private static final int SORT_POSITION_ACTIVATE_TIME = 1;

    private boolean mShowMesh;

    private DeviceStatusTask mLightTask;

    private EspUpgradeHelper mEspUpgradeHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (EspMainActivity)activity;
        mUser = BEspUser.getBuilder().getInstance();

        mSettingsShared = activity.getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mSettingsShared.registerOnSharedPreferenceChangeListener(this);
        mShowMesh = mSettingsShared.getBoolean(EspStrings.Key.SETTINGS_KEY_SHOW_MESH_TREE, EspDefaults.SHOW_MESH_TREE);

        mNewDevicesShared =
            activity.getSharedPreferences(EspStrings.Key.NAME_NEW_ACTIVATED_DEVICES, Context.MODE_PRIVATE);
        mNewDevicesShared.registerOnSharedPreferenceChangeListener(this);
        mNewDevicesSet = mUser.getNewActivatedDevices();

        mEspUpgradeHelper = EspUpgradeHelper.INSTANCE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_esp_fragment, container, false);

        mSearchET = (EditText)view.findViewById(R.id.search_view);
        mSearchET.addTextChangedListener(mSearchWatcher);

        mGroupListView = (RecyclerView)view.findViewById(R.id.group_list);
        LinearLayoutManager groupllm = new LinearLayoutManager(mActivity, RecyclerView.HORIZONTAL, false);
        mGroupListView.setLayoutManager(groupllm);
        mUser.loadGroupDB();
        mGroupList = mUser.getGroupList();
        mGroupAdapter = new GroupAdapter();
        mGroupListView.setAdapter(mGroupAdapter);

        mAllGroupView = (TextView)view.findViewById(R.id.group_all);
        mAllGroupView.setOnClickListener(this);
        mAddGroupView = view.findViewById(R.id.group_add);
        mAddGroupView.setOnClickListener(this);

        mDeviceRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.devices_refresh_layout);
        mDeviceRefreshLayout.setColorSchemeResources(R.color.esp_actionbar_color);
        mDeviceRefreshLayout.setOnRefreshListener(this);
        mDeviceListView = (ListView)view.findViewById(R.id.devices_list);
        mAllDeviceList = new ArrayList<IEspDevice>();
        mDeviceAdapter = new DeviceAdapter(mAllDeviceList);
        mDeviceListView.setAdapter(mDeviceAdapter);
        mDevicesUpdateMsgQueue = new LinkedBlockingQueue<Object>();
        mUpdateDeviceThread = new UpdateDeviceThread();
        mUpdateDeviceThread.start();
        mDeviceListView.setOnItemClickListener(this);
        mDeviceListView.setOnItemLongClickListener(this);

        mSortType = DeviceSortType.DEVICE_NAME;

        mBottomBar = view.findViewById(R.id.bottom_bar);
        mAllDeviceBtn = (ImageView)view.findViewById(R.id.select_all_device_btn);
        mAllDeviceBtn.setOnClickListener(this);
        mAllDeviceBtn.setOnLongClickListener(this);
        mActivateDeviceBtn = (ImageView)view.findViewById(R.id.activate_device_btn);
        mActivateDeviceBtn.setOnClickListener(this);
        mActivateDeviceBtn.setOnLongClickListener(this);
        mActivateDeviceBtn.setEnabled(false);
        mDeleteDeviceBtn = (ImageView)view.findViewById(R.id.delete_device_btn);
        mDeleteDeviceBtn.setOnClickListener(this);
        mDeleteDeviceBtn.setOnLongClickListener(this);
        mDeleteDeviceBtn.setEnabled(false);
        mMoveDeviceBtn = (ImageView)view.findViewById(R.id.move_device_btn);
        mMoveDeviceBtn.setOnClickListener(this);
        mMoveDeviceBtn.setOnLongClickListener(this);
        mMoveDeviceBtn.setEnabled(false);
        mRemoveDeviceBtn = (ImageView)view.findViewById(R.id.remove_device_btn);
        mRemoveDeviceBtn.setOnClickListener(this);
        mRemoveDeviceBtn.setOnLongClickListener(this);
        mRemoveDeviceBtn.setEnabled(false);
        mUpgradeDeviceBtn = (ImageView)view.findViewById(R.id.upgrade_device_btn);
        mUpgradeDeviceBtn.setOnClickListener(this);
        mUpgradeDeviceBtn.setOnLongClickListener(this);
        mUpgradeDeviceBtn.setEnabled(false);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(mActivity);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(llm);
        mColorList = getColorList();
        mColorAdapter = new ColorAdapter();
        mRecyclerView.setAdapter(mColorAdapter);
        mDevicesSwitch = (CheckBox)view.findViewById(R.id.device_array_switch);
        mDevicesSwitch.setOnCheckedChangeListener(this);

        mRefreshing = false;
        mRefreshHandler = new RefreshHandler(this);
        mRefreshHandler.sendEmptyMessage(MSG_LAYOUT_REFRESH);
        // mActivity.getActionBar().setDisplayShowCustomEnabled(true);
        // mActivity.getActionBar().setCustomView(R.layout.widget_progressbar);
        // refresh();

        // Get auto refresh settings data
        long autoRefreshTime = mSettingsShared.getLong(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_REFRESH,
            EspDefaults.AUTO_REFRESH_DEVICE_TIME);
        if (autoRefreshTime > 0) {
            sendAutoRefreshMessage(autoRefreshTime);
        }

        mBroadcastManager = LocalBroadcastManager.getInstance(mActivity);
        IntentFilter groupFilter = new IntentFilter(EspStrings.Action.CREATE_NEW_CLOUD_GROUP);
        mBroadcastManager.registerReceiver(mGroupReceiver, groupFilter);

        IntentFilter deviceFilter = new IntentFilter(EspStrings.Action.UI_REFRESH_LOCAL_DEVICES);
        deviceFilter.addAction(EspStrings.Action.DEVICES_ARRIVE_PULLREFRESH);
        deviceFilter.addAction(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE);
        mBroadcastManager.registerReceiver(mDeviceReceiver, deviceFilter);

        UdpServer.INSTANCE.registerOnLightTwinkleListener(mLightTwinkleListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBroadcastManager.unregisterReceiver(mGroupReceiver);
        mBroadcastManager.unregisterReceiver(mDeviceReceiver);

        UdpServer.INSTANCE.unRegisterOnLightTwinkleListener(mLightTwinkleListener);

        mRefreshHandler.removeMessages(MSG_AUTO_REFRESH);
        mRefreshHandler.removeMessages(MSG_UPDATE_TEMPDEVICE);
        mRefreshHandler.removeMessages(MSG_TWINKLE);
        mRefreshHandler.removeMessages(MSG_LAYOUT_REFRESH);
    }

    @Override
    public void onStart() {
        super.onStart();

        mActivityVisible = true;
        // onReceive(Context context, Intent intent) need all of the four sentences
        if (mIsDevicesUpdatedNecessary) {
            if (mIsDevicesPullRefreshUpdated) {
                mUser.doActionDevicesUpdated(false);
            }
            mUser.doActionDevicesUpdated(true);
        }
        // when the UI is showed, show the newest device list need the follow two sentences
        updateDeviceList();
        if (mIsDevicesUpdatedNecessary) {
            mDeviceRefreshLayout.setRefreshing(false);
            mRefreshing = false;
            mIsDevicesUpdatedNecessary = false;
            mIsDevicesPullRefreshUpdated = false;
        }

        if (mOnStartRefreshMark) {
            mOnStartRefreshMark = false;
            mDeviceRefreshLayout.setRefreshing(true);
            refresh();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        mActivityVisible = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void finish() {
        mUpdateDeviceThread.setRun(false);
        mUpdateDeviceThread.finish();
    }

    public boolean onBackPressed() {
        if (mDeviceAdapter.isCheckMode()) {
            setDeviceListCheckMode(false);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EspMainActivity.REQUEST_ESPTOUCH) {
            if (resultCode == Activity.RESULT_OK) {
                updateDeviceList();
            } else if (resultCode == RESULT_SCAN) {
                refresh();
            }
        } else if (requestCode == REQUEST_GROUP_CREATE) {
            if (resultCode == Activity.RESULT_OK) {
                updateGroupList();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (sharedPreferences == mSettingsShared) {
            if (key.equals(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_REFRESH)) {
                // The auto refresh settings changed
                if (mRefreshHandler.hasMessages(MSG_AUTO_REFRESH)) {
                    mRefreshHandler.removeMessages(MSG_AUTO_REFRESH);
                }
                long autoTime = mSettingsShared.getLong(key, EspDefaults.AUTO_REFRESH_DEVICE_TIME);
                if (autoTime > 0) {
                    sendAutoRefreshMessage(autoTime);
                }
            } else if (key.equals(EspStrings.Key.SETTINGS_KEY_SHOW_MESH_TREE)) {
                mShowMesh = mSettingsShared.getBoolean(key, EspDefaults.SHOW_MESH_TREE);
                updateDeviceList();
            }
        } else if (sharedPreferences == mNewDevicesShared) {
            if (key.equals(mUser.getUserKey())) {
                mNewDevicesSet = mUser.getNewActivatedDevices();
                mDeviceAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void onClick(View v) {
        if (v == mAllGroupView) {
            if (mSelectedGroup != null) {
                onGroupSelectChange(null);
            }
        } else if (v == mActivateDeviceBtn) {
            activateDevices(mDeviceAdapter.getCheckedDeviceList());
        } else if (v == mAddGroupView) {
            startActivityForResult(new Intent(mActivity, EspGroupEditActivity.class), REQUEST_GROUP_CREATE);
        } else if (v == mAllDeviceBtn) {
            if (mDeviceAdapter.getCheckedCount() == mDeviceAdapter.getCount()) {
                mDeviceAdapter.clearCheckedDevices();
            } else {
                mDeviceAdapter.checkAllDevices();
            }
        } else if (v == mDeleteDeviceBtn) {
            showDeleteDevicesDialog(mDeviceAdapter.getCheckedDeviceList());
        } else if (v == mMoveDeviceBtn) {
            if (mDeviceAdapter.getCheckedCount() > 0) {
                showMoveDeviceIntoGroupDialog();
            }
        } else if (v == mRemoveDeviceBtn) {
            if (mSelectedGroup != null && mDeviceAdapter.getCheckedCount() > 0) {
                showRemoveDeviceFromGroupDialog();
            }
        } else if (v == mUpgradeDeviceBtn) {
            upgradeDevice(mDeviceAdapter.getCheckedDeviceList());
            setDeviceListCheckMode(false);
        }
    }

    private Toast mBottomToast;

    @Override
    public boolean onLongClick(View v) {
        if (v == mAllDeviceBtn || v == mDeleteDeviceBtn || v == mActivateDeviceBtn || v == mMoveDeviceBtn
            || v == mRemoveDeviceBtn || v == mUpgradeDeviceBtn) {
            if (mBottomToast != null) {
                mBottomToast.cancel();
            }
            mBottomToast = Toast.makeText(mActivity, v.getContentDescription(), Toast.LENGTH_LONG);
            mBottomToast.show();

            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mDeviceListView) {
            IEspDevice device = ((DeviceHolder)view.getTag()).device;
            gotoUseDevice(device);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mDeviceListView) {
            IEspDevice device = ((DeviceHolder)view.getTag()).device;
            if (!isDeviceEditable(device)) {
                setDeviceListCheckMode(true);
                return true;
            }

            PopupMenu deviceMenu = new PopupMenu(mActivity, view);
            deviceMenu.setOnMenuItemClickListener(new OnDevicePopupMenuItemClickListener(device));
            Menu menu = deviceMenu.getMenu();
            menu.add(Menu.NONE, POPUP_ID_DEVICE_RENAME, 0, R.string.esp_main_device_menu_rename);
            if (mUser.isLogin()) {
                menu.add(Menu.NONE, POPUP_ID_DEVICE_DELETE, 0, R.string.esp_main_device_menu_delete);
                if (!device.isActivated()) {
                    menu.add(Menu.NONE, POPUP_ID_DEVICE_ACTIVATE, 0, R.string.esp_main_device_menu_activate);
                }
            }
            menu.add(Menu.NONE, POPUP_ID_DEVICE_MORE, 0, R.string.esp_main_device_menu_more);
            deviceMenu.show();

            return true;
        }
        return false;
    }

    public void markOnStartRefresh() {
        mOnStartRefreshMark = true;
    }

    private TextWatcher mSearchWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateDeviceList();
        }
    };

    private class GroupHolder extends RecyclerView.ViewHolder {
        IEspGroup group;
        View view;
        TextView text;

        public GroupHolder(View itemView) {
            super(itemView);

            view = itemView;
            text = (TextView)itemView.findViewById(R.id.group_text);

            itemView.setOnClickListener(new OnItemClickListener());
            itemView.setOnLongClickListener(new OnItemLongClickListener());
        }

        private class OnItemClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                if (mSelectedGroup != group) {
                    onGroupSelectChange(group);
                }
            }

        }

        private class OnItemLongClickListener implements View.OnLongClickListener {

            @Override
            public boolean onLongClick(View v) {
                PopupMenu groupMenu = new PopupMenu(mActivity, v);
                Menu menu = groupMenu.getMenu();
                menu.add(Menu.NONE, POPMENU_ID_GROUP_RENAME, 0, R.string.esp_main_group_menu_rename);
                menu.add(Menu.NONE, POPMENU_ID_GROUP_DELETE, 0, R.string.esp_main_group_menu_delete);
                groupMenu.setOnMenuItemClickListener(new OnGroupPopupMenuItemClickListener(group));
                groupMenu.show();

                return true;
            }

        }
    }

    private class GroupAdapter extends RecyclerView.Adapter<GroupHolder> {

        private LayoutInflater mInflater;

        public GroupAdapter() {
            mInflater = mActivity.getLayoutInflater();
        }

        @Override
        public int getItemCount() {
            return mGroupList.size();
        }

        @Override
        public GroupHolder onCreateViewHolder(ViewGroup group, int viewType) {
            View view = mInflater.inflate(R.layout.main_group_item, group, false);
            GroupHolder holder = new GroupHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(GroupHolder holder, int position) {
            IEspGroup group = mGroupList.get(position);
            holder.group = group;

            holder.text.setText(group.getName());
            Drawable d = getResources().getDrawable(group.getType().getIconRes());
            holder.text.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
            if (group == mSelectedGroup) {
                holder.view.setBackgroundResource(R.drawable.esp_activity_icon_background_pressed);
            } else {
                holder.view.setBackgroundResource(0);
            }
        }
    }

    private void showEditGroupDialog(final IEspGroup group) {
        View view = View.inflate(mActivity, R.layout.edit_dialog, null);
        final EditText edittext = (EditText)view.findViewById(R.id.edit);
        edittext.setHint(R.string.esp_scene_edit_hint);
        final TextView textview = (TextView)view.findViewById(R.id.text);
        textview.setVisibility(View.GONE);
        textview.setText(R.string.esp_scene_duplicate_scene_msg);
        final AlertDialog dialog = new AlertDialog.Builder(mActivity).setView(view)
            .setTitle(group == null ? getString(R.string.esp_scene_create) : group.getName())
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String groupName = edittext.getText().toString();
                    editGroup(group, groupName);
                }
            })
            .show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        edittext.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean duplicateName = false;
                for (IEspGroup group : mGroupList) {
                    if (s.toString().equals(group.getName())) {
                        duplicateName = true;
                        break;
                    }
                }
                textview.setVisibility(duplicateName ? View.VISIBLE : View.GONE);

                Button pButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                boolean btnEnable = (!TextUtils.isEmpty(s)) && (!duplicateName);
                pButton.setEnabled(btnEnable);
            }
        });
    }

    private void editGroup(final IEspGroup group, String groupName) {
        if (group == null) {
            mUser.doActionGroupCreate(groupName);
        } else {
            mUser.doActionGroupRename(group, groupName);
        }

        updateGroupList();
    }

    private void showDeleteGroupDialog(final IEspGroup group) {
        new AlertDialog.Builder(mActivity).setTitle(R.string.esp_scene_delete_dialog_title)
            .setMessage(getString(R.string.esp_scene_delete_dialog_msg, group.getName()))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mUser.doActionGroupDelete(group);

                    if (mSelectedGroup == group) {
                        onGroupSelectChange(null);
                    }
                    updateGroupList();
                }
            })
            .show();
    }

    private void showRemoveDeviceFromGroupDialog() {
        new AlertDialog.Builder(mActivity).setMessage(R.string.esp_scene_remove_dialog_msg)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<IEspDevice> checkedDevices = mDeviceAdapter.getCheckedDeviceList();
                    mUser.doActionGroupDeviceRemove(checkedDevices, mSelectedGroup);
                    for (IEspDevice device : checkedDevices) {
                        mSelectedGroup.removeDevice(device);
                        mAllDeviceList.remove(device);
                        mDeviceAdapter.removeCheckDevice(device);
                    }
                    mDeviceAdapter.notifyDataSetChanged();
                }
            })
            .show();
    }

    private void showMoveDeviceIntoGroupDialog() {
        String[] groupNames = new String[mGroupList.size()];
        for (int i = 0; i < mGroupList.size(); i++) {
            groupNames[i] = mGroupList.get(i).getName();
        }
        new AlertDialog.Builder(mActivity).setTitle(R.string.esp_main_device_move_dialog_title)
            .setItems(groupNames, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<IEspDevice> checkedDevices = mDeviceAdapter.getCheckedDeviceList();
                    IEspGroup group = mGroupList.get(which);
                    mUser.doActionGroupDeviceMoveInto(checkedDevices, group);
                    updateGroupList();
                }
            })
            .show();
    }

    private class OnGroupPopupMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private IEspGroup mGroup;

        public OnGroupPopupMenuItemClickListener(IEspGroup group) {
            mGroup = group;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case POPMENU_ID_GROUP_RENAME:
                    showEditGroupDialog(mGroup);
                    return true;
                case POPMENU_ID_GROUP_DELETE:
                    showDeleteGroupDialog(mGroup);
                    return true;
            }
            return false;
        }

    }

    private void sendAutoRefreshMessage(Long autoRefreshTime) {
        log.debug("send Auto Refresh Message Delayed " + autoRefreshTime);
        Message msg = Message.obtain();
        msg.what = MSG_AUTO_REFRESH;
        msg.obj = autoRefreshTime;
        mRefreshHandler.sendMessageDelayed(msg, autoRefreshTime);
    }

    public void updateGroupList() {
        mUser.loadGroupDB();
        mGroupList.clear();
        mGroupList.addAll(mUser.getGroupList());
        if (mSelectedGroup != null) {
            for (IEspGroup group : mGroupList) {
                if (group.getId() == mSelectedGroup.getId()) {
                    mSelectedGroup = group;
                    break;
                }
            }
        }
        mGroupAdapter.notifyDataSetChanged();
    }

    private BroadcastReceiver mGroupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(EspStrings.Action.CREATE_NEW_CLOUD_GROUP)) {
                long oldId = intent.getLongExtra(EspStrings.Key.KEY_GROUP_ID_OLD, EspDefaults.GROUP_ID_OLD);
                long newId = intent.getLongExtra(EspStrings.Key.KEY_GROUP_ID_NEW, EspDefaults.GROUP_ID_NEW);
                updateGroupId(oldId, newId);
            }
        }

    };

    private void updateGroupId(long oldId, long newId) {
        for (IEspGroup group : mGroupList) {
            if (group.getId() == oldId) {
                group.setId(newId);
                return;
            }
        }
    }

    private void onGroupSelectChange(IEspGroup group) {
        mSelectedGroup = group;
        if (group == null) {
            mAllGroupView.setBackgroundResource(R.drawable.esp_activity_icon_background_pressed);
        } else {
            mAllGroupView.setBackgroundResource(0);
        }
        mGroupAdapter.notifyDataSetChanged();
        updateDeviceList();
        mDeviceAdapter.clearCheckedDevices();
    }

    public synchronized void updateDeviceList() {
        if (mDevicesUpdateMsgQueue.size() < 1) {
            mDevicesUpdateMsgQueue.add(new Object());
        }
    }

    private class UpdateDeviceThread extends FinishThread {
        private volatile boolean run = true;

        public void setRun(boolean run) {
            this.run = run;
        }

        @Override
        public void execute() {
            while (run) {
                try {
                    mDevicesUpdateMsgQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                final String nameFilter = mSearchET.getText().toString();
                final EspDeviceType typeFilter = mFilterDeviceType;
                final boolean usableFilter = mFilterDeviceUsable;
                final List<IEspDevice> deviceList = new ArrayList<IEspDevice>();
                List<IEspDevice> list = mUser.getAllDeviceList();
                IEspGroup group = mSelectedGroup;
                List<IEspDevice> groupDevices = new ArrayList<IEspDevice>();
                if (group != null) {
                    groupDevices.addAll(group.getDeviceList());
                }
                for (int i = 0; i < list.size(); i++) {
                    IEspDevice device = list.get(i);
                    IEspDeviceState state = device.getDeviceState();
                    if (!state.isStateDeleted()) {
                        if (group != null) {
                            if (!groupDevices.contains(device)) {
                                continue;
                            }
                        }
                        if (!TextUtils.isEmpty(nameFilter)) {
                            if (!device.getName().contains(nameFilter)) {
                                continue;
                            }
                        }
                        if (typeFilter != null) {
                            if (device.getDeviceType() != typeFilter) {
                                continue;
                            }
                        }
                        if (usableFilter) {
                            if (!state.isStateLocal() && !state.isStateInternet()) {
                                continue;
                            }
                        }

                        deviceList.add(device);
                    }
                }

                new DeviceSortor().sort(deviceList, mSortType);

                if (mShowMesh) {
                    IEspDevice rootDevice = BEspDeviceRoot.getBuilder().getVirtualMeshRoot();
                    deviceList.add(0, rootDevice);
                }

                if (mRefreshHandler != null) {
                    mRefreshHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAllDeviceList.clear();
                            mAllDeviceList.addAll(deviceList);
                            mDeviceAdapter.notifyDataSetChanged();
                        }
                    });
                }
            } // end while

        }

    }

    public void filterDeviceType(EspDeviceType filter) {
        mFilterDeviceType = filter;
        updateDeviceList();
    }

    public void filterDeviceUsable(boolean filter) {
        mFilterDeviceUsable = filter;
        updateDeviceList();
    }

    public void showSortItems() {
        String[] sortItems = getResources().getStringArray(R.array.esp_main_device_sort);
        new AlertDialog.Builder(mActivity).setTitle(R.string.esp_main_sort_title)
            .setItems(sortItems, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case SORT_POSITION_DEVICE_NAME:
                            mSortType = DeviceSortType.DEVICE_NAME;

                            break;
                        case SORT_POSITION_ACTIVATE_TIME:
                            mSortType = DeviceSortType.ACTIVATED_TIME;
                            break;
                    }

                    updateDeviceList();
                }
            })
            .show();
    }

    /**
     * Do refresh devices action
     */
    public void refresh() {
        if (mUser.isLogin()) {
            scanAll();
        } else {
            scanSta();
        }
    }

    private void scanAll() {
        if (!checkDeviceUpgradeWhenRefresh()) {
            return;
        }

        if (!mRefreshing) {
            mRefreshing = true;
            mUser.doActionRefreshDevices();
        }

        if (!isNetworkAvailabale()) {
            Toast.makeText(mActivity, R.string.esp_main_network_enable_msg, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailabale() {
        ConnectivityManager cm = (ConnectivityManager)mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return false;
        } else {
            return info.isAvailable();
        }
    }

    private void scanSta() {
        if (!checkDeviceUpgradeWhenRefresh()) {
            return;
        }

        if (!mRefreshing) {
            mRefreshing = true;
            mUser.doActionRefreshStaDevices(false);
        }
    }

    /**
     * Check whether device is upgrading when refreshing
     * 
     * @return true mean no device is upgrading, permit refresh devices. false mean at least one device is upgrading,
     *         forbid refresh devices
     */
    private boolean checkDeviceUpgradeWhenRefresh() {
        for (IEspDevice device : mAllDeviceList) {
            IEspDeviceState state = device.getDeviceState();
            if (state.isStateUpgradingInternet() || state.isStateUpgradingLocal()) {
                mRefreshHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mDeviceRefreshLayout.setRefreshing(false);
                    }
                }, 3000);

                return false;
            }
        }

        return true;
    }

    /**
     * 
     * @param device
     * @return true go to use device success, false for some reasons the device can't use
     */
    private boolean gotoUseDevice(IEspDevice device) {
        IEspDeviceState state = device.getDeviceState();
        if (state.isStateUpgradingInternet() || state.isStateUpgradingLocal()) {
            return false;
        }

        List<IEspDevice> upgradingDevices = mEspUpgradeHelper.getUpgradingDevices();
        if (upgradingDevices.contains(device)) {
            return false;
        }

        Class<?> _class = DeviceUtil.getDeviceClass(device);
        if (_class != null) {
            Intent intent = new Intent(mActivity, _class);
            intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, device.getKey());
            startActivityForResult(intent, REQUEST_DEVICE);
            mUser.deleteNewActivatedDevice(device.getKey());
            return true;
        }

        return false;
    }

    private boolean isDeviceEditable(IEspDevice device) {
        if (device.getDeviceType() == EspDeviceType.ROOT) {
            return false;
        }

        IEspDeviceState state = device.getDeviceState();
        if (state.isStateUpgradingInternet() || state.isStateUpgradingLocal() || state.isStateActivating()) {
            return false;
        }

        return true;
    }

    private class OnDevicePopupMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        public IEspDevice mDevice;

        public OnDevicePopupMenuItemClickListener(IEspDevice device) {
            mDevice = device;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case POPUP_ID_DEVICE_RENAME:
                    showRenameDeviceDialog(mDevice);
                    return true;
                case POPUP_ID_DEVICE_DELETE:
                    showDeleteDeviceDialog(mDevice);
                    return true;
                case POPUP_ID_DEVICE_ACTIVATE:
                    activateDevice(mDevice);
                    return true;
                case POPUP_ID_DEVICE_MORE:
                    setDeviceListCheckMode(true);
                    return true;
            }
            return false;
        }

    }

    private void showRenameDeviceDialog(final IEspDevice device) {
        new RenameDeviceDialog(device).show();
    }

    private class RenameDeviceDialog {
        private IEspDevice mDevice;

        private EditText mEdit;
        private AlertDialog mDialog;
        private Button mConfirmBtn;

        public RenameDeviceDialog(IEspDevice device) {
            mDevice = device;

            mEdit = new EditText(mActivity);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mEdit.setLayoutParams(lp);
            mEdit.setSingleLine();

            mDialog = new AlertDialog.Builder(mActivity).setView(mEdit)
                .setTitle(mDevice.getName())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = mEdit.getText().toString();
                        mUser.doActionRename(mDevice, newName);
                    }
                })
                .create();
        }

        public void show() {
            mDialog.show();
            mConfirmBtn = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            mConfirmBtn.setEnabled(false);
            mEdit.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    mConfirmBtn.setEnabled(!TextUtils.isEmpty(s));
                }
            });
        }
    }

    private void showDeleteDeviceDialog(final IEspDevice device) {
        new AlertDialog.Builder(mActivity).setTitle(device.getName())
            .setMessage(R.string.esp_main_delete_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mUser.doActionDelete(device);
                }
            })
            .show();
    }

    private void showDeleteDevicesDialog(final List<IEspDevice> list) {
        new AlertDialog.Builder(mActivity).setTitle(R.string.esp_main_edit_delete_selected)
            .setMessage(R.string.esp_main_edit_delete_sellected_message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new DeleteDevicesTask(list).execute();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void activateDevice(final IEspDevice device) {
        new ActivateDevicesTask(device).execute();
    }

    private void activateDevices(final List<IEspDevice> devices) {
        new ActivateDevicesTask(devices).execute();
    }

    private class ActivateDevicesTask extends AsyncTask<Void, Void, Boolean> {
        private IEspDevice mDevice = null;
        private List<IEspDevice> mDevices = null;

        private ProgressDialog mDialog;

        public ActivateDevicesTask(IEspDevice device) {
            mDevice = device;
        }

        public ActivateDevicesTask(List<IEspDevice> devices) {
            mDevices = devices;
            for (int i = mDevices.size() - 1; i >= 0; i--) {
                IEspDevice device = mDevices.get(i);
                if (device.getDeviceType() == EspDeviceType.ROOT) {
                    mDevices.remove(i);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setCancelable(false);
            mDialog.setMessage(getString(R.string.esp_main_device_activating));
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            if (mDevice != null) {
                result = mUser.addDeviceSyn(mDevice);
                mUser.doActionRefreshStaDevices(true);
            }
            if (mDevices != null) {
                List<IEspDevice> failedDevices = mUser.addDevicesSync(mDevices);
                if (failedDevices == null) {
                    result = false;
                } else {
                    result = failedDevices.size() == 0;
                }
                mUser.doActionRefreshStaDevices(true);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            int msgRes = result ? R.string.esp_main_device_activate_suc : R.string.esp_main_device_activate_failed;
            Toast.makeText(mActivity, msgRes, Toast.LENGTH_LONG).show();
            mDialog.dismiss();
            mDialog = null;
            updateDeviceList();
        }
    }

    private class DeleteDevicesTask extends AsyncTask<Void, Void, Boolean> {
        private Collection<IEspDevice> mDevices;
        private boolean mHasEneditableDevice;
        private ProgressDialog mDialog;

        private List<IEspDevice> mCheckedDevices;

        public DeleteDevicesTask(List<IEspDevice> checkedDevices) {
            mCheckedDevices = checkedDevices;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.esp_device_task_dialog_message));
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            // Filter devices can't be deleted
            mHasEneditableDevice = false;
            mDevices = new HashSet<IEspDevice>();
            for (IEspDevice device : mCheckedDevices) {
                if (isDeviceEditable(device)) {
                    if (device.isActivated()) {
                        mDevices.add(device);
                    } else if (device.getDeviceState().isStateOffline()) {
                        // The device is not activated, and it is offline, permit delete
                        mDevices.add(device);
                    } else {
                        mHasEneditableDevice = true;
                    }
                } else {
                    mHasEneditableDevice = true;
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mUser.doActionDelete(mDevices);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mDialog.dismiss();
            mDevices.clear();

            if (mHasEneditableDevice) {
                Toast.makeText(mActivity, R.string.esp_main_edit_has_eneditable_device_message, Toast.LENGTH_LONG)
                    .show();
            }
        }
    }

    private void upgradeDevice(List<IEspDevice> devices) {
        for (int i = devices.size() - 1; i >= 0; i--) {
            IEspDevice device = devices.get(i);
            IEspDeviceState state = device.getDeviceState();
            if (!state.isStateLocal() && !state.isStateInternet()) {
                devices.remove(i);
                continue;
            }
            if (device.getDeviceType() == EspDeviceType.ROOT) {
                devices.remove(i);
                continue;
            }

            switch (mUser.getDeviceUpgradeTypeResult(device)) {
                case SUPPORT_LOCAL_ONLY:
                    if (!state.isStateLocal()) {
                        devices.remove(i);
                        continue;
                    }
                    break;
                case SUPPORT_ONLINE_ONLY:
                    if (!state.isStateInternet()) {
                        devices.remove(i);
                        continue;
                    }
                    break;
                case SUPPORT_ONLINE_LOCAL:
                    break;

                case CURRENT_ROM_INVALID:
                case CURRENT_ROM_IS_NEWEST:
                case DEVICE_TYPE_INCONSISTENT:
                case LATEST_ROM_INVALID:
                case NOT_SUPPORT_UPGRADE:
                    devices.remove(i);
                    continue;
            }
        }

        log.debug("upgrade devices size = " + devices.size());
        mEspUpgradeHelper.addDevices(devices);
        mEspUpgradeHelper.checkUpgradingDevices();
        mDeviceAdapter.notifyDataSetChanged();
    }

    private class DeviceHolder {
        IEspDevice device;
        ImageView icon;
        ImageView iconCover;
        TextView name;
        TextView version;
        TextView status;
        TextView newActivate;
        CheckBox checked;
        ImageView rssi;
    }

    private class DeviceAdapter extends BaseAdapter {
        private List<IEspDevice> mDeviceList;

        private final int mOnlineColor;
        private final int mOfflineColor;

        private LayoutInflater mInflater;

        private boolean mCheckMode;
        private List<IEspDevice> mCheckedDeviceList;

        private String[] mTwinkleBssids;

        public DeviceAdapter(List<IEspDevice> deviceList) {
            mDeviceList = deviceList;

            mOnlineColor = mActivity.getResources().getColor(R.color.esp_device_online);
            mOfflineColor = mActivity.getResources().getColor(R.color.esp_device_offline);

            mInflater = mActivity.getLayoutInflater();

            mCheckMode = false;
            mCheckedDeviceList = new ArrayList<IEspDevice>();
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public IEspDevice getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mDeviceList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            DeviceHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.main_device_item, parent, false);

                holder = new DeviceHolder();
                holder.icon = (ImageView)view.findViewById(R.id.device_icon);
                holder.iconCover = (ImageView)view.findViewById(R.id.device_icon_cover);
                holder.name = (TextView)view.findViewById(R.id.device_name);
                holder.version = (TextView)view.findViewById(R.id.device_version);
                holder.status = (TextView)view.findViewById(R.id.device_status_text1);
                holder.newActivate = (TextView)view.findViewById(R.id.device_status_text2);
                holder.checked = (CheckBox)view.findViewById(R.id.device_checked);
                holder.rssi = (ImageView)view.findViewById(R.id.device_status_img1);

                view.setTag(holder);
            } else {
                view = convertView;
                holder = (DeviceHolder)view.getTag();
            }

            IEspDevice device = getItem(position);
            holder.device = device;

            IEspDeviceState deviceState = device.getDeviceState();

            int iconRes = DeviceUtil.getDeviceIconRes(device);
            holder.icon.setImageResource(iconRes);

            if (device.getDeviceType() == EspDeviceType.LIGHT) {
                holder.iconCover.setVisibility(View.VISIBLE);
                holder.iconCover.setImageResource(R.drawable.device_light_cover);
            } else {
                holder.iconCover.setVisibility(View.GONE);
                holder.iconCover.setImageDrawable(null);
            }

            holder.name.setText(device.getName());
            String verStr = device.getRom_version();
            holder.version.setText(verStr);

            holder.version.setVisibility(TextUtils.isEmpty(verStr) ? View.GONE : View.VISIBLE);
            if (deviceState.isStateOffline()) {
                holder.name.setTextColor(mOfflineColor);
                holder.version.setTextColor(mOfflineColor);
            } else {
                holder.name.setTextColor(mOnlineColor);
                holder.version.setTextColor(mOnlineColor);
            }
            checkLight(holder);

            holder.status.setText("");
            if (device.isActivated()) {
                if (deviceState.isStateInternet()) {
                    holder.status.setBackgroundResource(R.drawable.device_status_cloud_on);
                } else {
                    holder.status.setBackgroundResource(R.drawable.device_status_cloud_off);
                }
            } else {
                holder.status.setBackgroundResource(0);
            }
            if (deviceState.isStateUpgradingInternet() || deviceState.isStateUpgradingLocal()) {
                holder.status.setBackgroundResource(R.drawable.device_status_upgrading);
            } else {
                List<IEspDevice> upgradingDevices = mEspUpgradeHelper.getUpgradingDevices();
                if (upgradingDevices.contains(device)) {
                    holder.status.setBackgroundResource(0);
                    holder.status.setText(R.string.esp_main_waiting_for_upgrading);
                }
            }

            holder.newActivate.setTextColor(Color.RED);
            holder.newActivate.setText("NEW");
            boolean isNewActivate = mNewDevicesSet.contains(device.getKey());
            holder.newActivate.setVisibility(isNewActivate ? View.VISIBLE : View.GONE);

            holder.checked.setVisibility(mCheckMode ? View.VISIBLE : View.GONE);
            holder.checked.setChecked(mCheckedDeviceList.contains(device));
            holder.checked.setOnCheckedChangeListener(new OnCheckedChangedListener(device));

            int rssiValue = device.getRssi();
            if (rssiValue == IEspDevice.RSSI_NULL) {
                holder.rssi.setVisibility(View.GONE);
            } else {
                holder.rssi.setVisibility(View.VISIBLE);
                holder.rssi.getDrawable().setLevel(getRssiLevel(rssiValue));
            }

            boolean twinkle = false;
            if (mTwinkleBssids != null) {
                for (String bssid : mTwinkleBssids) {
                    if (bssid.equals(device.getBssid())) {
                        twinkle = true;
                        break;
                    }
                }
            }
            if (twinkle) {
                view.setBackgroundColor(Color.RED);
            } else {
                view.setBackgroundResource(0);
            }

            return view;
        }

        private int getRssiLevel(int rssi) {
            if (rssi >= -65) {
                return 3;
            } else if (rssi < -65 && rssi >= -75) {
                return 2;
            } else if (rssi < -75 && rssi >= -85) {
                return 1;
            } else {
                return 0;
            }
        }

        private void checkLight(DeviceHolder holder) {
            if (holder.device instanceof IEspDeviceLight) {
                IEspStatusLight statusLight = ((IEspDeviceLight)holder.device).getStatusLight();
                IEspDeviceState state = holder.device.getDeviceState();
                if (state.isStateOffline()) {
                    holder.icon.setBackgroundColor(Color.TRANSPARENT);
                    holder.iconCover.setVisibility(View.GONE);
                } else {
                    holder.iconCover.setVisibility(View.VISIBLE);
                    if (statusLight.getStatus() != IEspStatusLight.STATUS_NULL) {
                        int color = statusLight.getCurrentColor();
                        holder.icon.setBackgroundColor(color);
                    } else {
                        holder.icon.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            } else {
                holder.icon.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        public void setCheckMode(boolean check) {
            mCheckMode = check;
            notifyDataSetChanged();
        }

        public boolean isCheckMode() {
            return mCheckMode;
        }

        private class OnCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {
            public IEspDevice mDevice;

            public OnCheckedChangedListener(IEspDevice device) {
                mDevice = device;
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!mCheckedDeviceList.contains(mDevice)) {
                        mCheckedDeviceList.add(mDevice);
                    }
                } else {
                    mCheckedDeviceList.remove(mDevice);
                }
                onDeviceCheckedChanged();
            }

        }

        public int getCheckedCount() {
            return mCheckedDeviceList.size();
        }

        public List<IEspDevice> getCheckedDeviceList() {
            List<IEspDevice> result = new ArrayList<IEspDevice>();
            result.addAll(mCheckedDeviceList);
            return result;
        }

        public void removeCheckDevice(IEspDevice device) {
            if (!mCheckedDeviceList.contains(device)) {
                mCheckedDeviceList.remove(device);
                notifyDataSetChanged();
                onDeviceCheckedChanged();
            }
        }

        public void checkAllDevices() {
            if (mCheckMode) {
                mCheckedDeviceList.clear();
                mCheckedDeviceList.addAll(mDeviceList);
                notifyDataSetChanged();
                onDeviceCheckedChanged();
            }
        }

        public void clearCheckedDevices() {
            if (!mCheckedDeviceList.isEmpty()) {
                mCheckedDeviceList.clear();
                notifyDataSetChanged();
                onDeviceCheckedChanged();
            }
        }

        public void clearTwinkleBssids() {
            mTwinkleBssids = null;
            notifyDataSetChanged();
        }

        public void twinkle(String[] bssids) {
            mTwinkleBssids = bssids;
            notifyDataSetChanged();
            mRefreshHandler.removeMessages(MSG_TWINKLE);
            mRefreshHandler.sendEmptyMessageDelayed(MSG_TWINKLE, 1000);
        }
    }

    private void onDeviceCheckedChanged() {
        if (mDeviceAdapter.getCheckedCount() == 0) {
            mActivateDeviceBtn.setEnabled(false);
            mDeleteDeviceBtn.setEnabled(false);
            mRemoveDeviceBtn.setEnabled(false);
            mMoveDeviceBtn.setEnabled(false);
            mUpgradeDeviceBtn.setEnabled(false);
        } else {
            List<IEspDevice> checkedDevices = mDeviceAdapter.getCheckedDeviceList();
            boolean hasUnactivatedDevice = false;
            for (IEspDevice device : checkedDevices) {
                if (!device.isActivated()) {
                    hasUnactivatedDevice = true;
                    break;
                }
            }
            mActivateDeviceBtn.setEnabled(hasUnactivatedDevice && mUser.isLogin());
            mDeleteDeviceBtn.setEnabled(true);
            mRemoveDeviceBtn.setEnabled(mSelectedGroup != null);
            mMoveDeviceBtn.setEnabled(true);
            mUpgradeDeviceBtn.setEnabled(true);
        }
    }

    private void setDeviceListCheckMode(boolean checkMode) {
        mBottomBar.setVisibility(checkMode ? View.VISIBLE : View.GONE);
        mDeviceRefreshLayout.setEnabled(checkMode ? false : true);
        if (!checkMode) {
            mDeviceAdapter.clearCheckedDevices();
        }
        mDeviceAdapter.setCheckMode(checkMode);
    }

    private static class RefreshHandler extends Handler {
        private WeakReference<EspMainFragment> mFragment;

        public RefreshHandler(EspMainFragment fragment) {
            mFragment = new WeakReference<EspMainFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            EspMainFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }

            switch (msg.what) {
                case MSG_AUTO_REFRESH:
                    if (fragment.mActivityVisible) {
                        fragment.refresh();
                    }
                    Long autoTime = (Long)msg.obj;
                    fragment.sendAutoRefreshMessage(autoTime);
                    break;
                case MSG_LAYOUT_REFRESH:
                    fragment.mDeviceRefreshLayout.setRefreshing(true);
                    fragment.refresh();
                    break;
                case MSG_UPDATE_TEMPDEVICE:
                    fragment.updateDeviceList();
                    break;
                case MSG_TWINKLE:
                    fragment.mDeviceAdapter.clearTwinkleBssids();
                    break;
            }
        }
    }

    private BroadcastReceiver mDeviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(EspStrings.Action.UI_REFRESH_LOCAL_DEVICES)) {
                if (!mRefreshHandler.hasMessages(MSG_UPDATE_TEMPDEVICE)) {
                    mRefreshHandler.sendEmptyMessage(MSG_UPDATE_TEMPDEVICE);
                }

                return;
            }

            // for EspDeviceStateMachine check the state valid before and after device's state transformation
            // so when the user is using device, we don't like to make the state changed by pull refresh before
            // the user tap the device into device using Activity.
            //
            // for example, device A is LOCAL and INTERNET, user pull refresh, before
            // the refresh finished, the user tap the device into the using activity choosing UPGRADE LOCAL,device
            // refresh result(INTERNET) arrived, if the device state is changed to INTERNET, it will throw
            // IllegalStateException for UPGRADE LOCAL require LOCAL state sometimes.
            // onStart() will handle the device state transformation when the Activity visible again.
            if (!mActivityVisible) {
                log.debug("Receive Broadcast but invisible so ignore");
                mIsDevicesUpdatedNecessary = true;
                if (action.equals(EspStrings.Action.DEVICES_ARRIVE_PULLREFRESH)) {
                    mIsDevicesPullRefreshUpdated = true;
                }
                return;
            }

            if (action.equals(EspStrings.Action.DEVICES_ARRIVE_PULLREFRESH)) {
                log.debug("Receive Broadcast DEVICES_ARRIVE_PULLREFRESH");
                // Refresh list
                mUser.doActionDevicesUpdated(false);

                updateDeviceList();
                mDeviceRefreshLayout.setRefreshing(false);

                mRefreshing = false;

                updateGroupList();
            } else if (action.equals(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE)) {
                log.debug("Receive Broadcast DEVICES_ARRIVE_STATEMACHINE");
                mUser.doActionDevicesUpdated(true);
                updateDeviceList();
                mEspUpgradeHelper.checkUpgradingDevices();
                mDeviceAdapter.notifyDataSetChanged();
            }
        }

    };

    private class ColorHolder extends RecyclerView.ViewHolder {
        ColorView colorView;

        int color;

        public ColorHolder(View itemView) {
            super(itemView);

            colorView = (ColorView)itemView.findViewById(R.id.color_view);
            colorView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    executeLightTask(IEspStatusLight.STATUS_COLOR, color);
                }
            });
            colorView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    IEspDeviceArray deviceArray = generateSelectedDeviceArray();
                    if (deviceArray != null) {
                        EspDeviceType type = deviceArray.getDeviceType();
                        List<IEspDevice> devices = deviceArray.getDeviceList();
                        String[] keys = new String[devices.size()];
                        for (int i = 0; i < devices.size(); i++) {
                            keys[i] = devices.get(i).getKey();
                        }

                        Intent intent = DeviceUtil.getDeviceIntent(mActivity, deviceArray);
                        intent.putExtra(EspStrings.Key.DEVICE_KEY_TYPE, type.toString());
                        intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY_ARRAY, keys);
                        intent.putExtra(EspStrings.Key.DEVICE_KEY_SHOW_CHILDREN, false);
                        mActivity.startActivity(intent);
                    }
                    return true;
                }
            });
        }

    }

    private class ColorAdapter extends RecyclerView.Adapter<ColorHolder> {

        @Override
        public int getItemCount() {
            return mColorList.size();
        }

        @Override
        public void onBindViewHolder(ColorHolder holder, int position) {
            int color = mColorList.get(position);
            holder.color = color;
            holder.colorView.setColor(color);
        }

        @Override
        public ColorHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
            View view = View.inflate(mActivity, R.layout.main_bottom_color_item, null);
            ColorHolder vh = new ColorHolder(view);
            return vh;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mDevicesSwitch) {
            int status = isChecked ? IEspStatusLight.STATUS_ON : IEspStatusLight.STATUS_OFF;
            executeLightTask(status);
        }
    }

    private List<Integer> getColorList() {
        int[] colorArray = mActivity.getResources().getIntArray(R.array.esp_light_colors);
        List<Integer> result = new ArrayList<Integer>();
        for (int color : colorArray) {
            result.add(color);
        }
        return result;
    }

    private IEspDeviceArray generateSelectedDeviceArray() {
        List<IEspDevice> devices = mDeviceAdapter.getCheckedDeviceList();
        IEspDeviceArray mLightArray = null;
        IEspDeviceArray mPlugArray = null;
        for (IEspDevice device : devices) {
            IEspDeviceState state = device.getDeviceState();
            EspDeviceType type = device.getDeviceType();
            if (state.isStateLocal() || state.isStateInternet()) {
                if (type == EspDeviceType.LIGHT) {
                    if (mLightArray == null) {
                        mLightArray = BEspDevice.createDeviceArray(EspDeviceType.LIGHT);
                    }
                    mLightArray.addDevice(device);
                } else if (type == EspDeviceType.PLUG) {
                    if (mPlugArray == null) {
                    }
                }
            }
        }

        return mLightArray;
    }

    private void executeLightTask(int status, int... colors) {
        IEspDeviceArray lightArray = generateSelectedDeviceArray();
        if (lightArray == null) {
            return;
        }

        IEspStatusLight statusLight = null;
        switch (status) {
            case IEspStatusLight.STATUS_OFF:
            case IEspStatusLight.STATUS_ON:
                statusLight = LightUtils.generateStatus(status);
                break;
            case IEspStatusLight.STATUS_COLOR:
            case IEspStatusLight.STATUS_BRIGHT:
                int c = colors[0];
                statusLight = LightUtils.generateStatus(status, Color.red(c), Color.green(c), Color.blue(c));
                break;
        }

        if (mLightTask != null) {
            mLightTask.cancel(true);
        }
        mLightTask = new DeviceStatusTask(lightArray);
        mLightTask.execute(statusLight);
        List<IEspDevice> lights = lightArray.getDeviceList();
        updateLightStatus(lights, statusLight);
    }

    private void updateLightStatus(List<IEspDevice> lights, IEspStatusLight newStatus) {
        for (IEspDevice device : lights) {
            IEspDeviceLight light = (IEspDeviceLight)device;
            IEspStatusLight currentStatus = light.getStatusLight();
            int status = newStatus.getStatus();
            currentStatus.setStatus(status);
            switch (status) {
                case IEspStatusLight.STATUS_OFF:
                case IEspStatusLight.STATUS_ON:
                    break;
                case IEspStatusLight.STATUS_COLOR:
                    currentStatus.setRed(newStatus.getRed());
                    currentStatus.setGreen(newStatus.getGreen());
                    currentStatus.setBlue(newStatus.getBlue());
                    break;
                case IEspStatusLight.STATUS_BRIGHT:
                    currentStatus.setWhite(newStatus.getWhite());
                    break;
            }
        }
        updateDeviceList();
    }

    private OnLightTwinkleListener mLightTwinkleListener = new OnLightTwinkleListener() {

        @Override
        public void onTwinkle(final String[] bssids) {
            boolean twinkle = false;
            ListView listView = mDeviceListView;
            for (int i = 0; i < mAllDeviceList.size(); i++) {
                IEspDevice device = mAllDeviceList.get(i);
                for (String bssid : bssids) {
                    if (device.getBssid().equals(bssid)) {
                        twinkle = true;
                        listView.setSelection(i);
                        break;
                    }
                }
                if (twinkle) {
                    break;
                }
            }
            if (twinkle) {
                mRefreshHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mDeviceAdapter.twinkle(bssids);
                    }
                });
            }
        }
    };
}
