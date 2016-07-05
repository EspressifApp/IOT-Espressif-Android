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
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.group.IEspGroup;
import com.espressif.iot.model.device.sort.DeviceSortor;
import com.espressif.iot.model.device.sort.DeviceSortor.DeviceSortType;
import com.espressif.iot.model.thread.FinishThread;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.DeviceUtil;
import com.espressif.iot.util.EspDefaults;
import com.espressif.iot.util.EspStrings;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.meetme.android.horizontallistview.HorizontalListView;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class EspMainFragment extends Fragment implements OnSharedPreferenceChangeListener, OnClickListener,
    OnItemClickListener, OnItemLongClickListener, OnRefreshListener<ListView>, OnLongClickListener {
    private static final Logger log = Logger.getLogger(EspMainFragment.class);

    private EspMainActivity mActivity;
    private IEspUser mUser;

    private HorizontalListView mGroupListView;
    private List<IEspGroup> mGroupList;
    private GroupAdapter mGroupAdapter;
    private TextView mAllGroupView;
    private View mAddGroupView;

    private volatile IEspGroup mSelectedGroup;

    private static final int POPMENU_ID_GROUP_RENAME = 0x10;
    private static final int POPMENU_ID_GROUP_DELETE = 0x11;

    private EspDeviceType mFilterDeviceType;
    private boolean mFilterDeviceUsable;

    private PullToRefreshListView mDeviceListView;
    private DeviceAdapter mDeviceAdapter;
    private List<IEspDevice> mAllDeviceList;
    private LinkedBlockingQueue<Object> mDevicesUpdateMsgQueue;
    private UpdateDeviceThread mUpdateDeviceThread;

    private static final int POPUP_ID_DEVICE_RENAME = 0x20;
    private static final int POPUP_ID_DEVICE_DELETE = 0x21;
    private static final int POPUP_ID_DEVICE_ACTIVATE = 0x22;
    private static final int PUPUP_ID_DEVICE_MORE = 0x23;

    private final static int REQUEST_DEVICE = 0x11;

    /**
     * Whether the device refresh task is running
     */
    private boolean mRefreshing;
    private Handler mRefreshHandler;
    private static final int MSG_AUTO_REFRESH = 0;
    private static final int MSG_UPDATE_TEMPDEVICE = 1;

    private SharedPreferences mSettingsShared;

    private SharedPreferences mNewDevicesShared;
    private Set<String> mNewDevicesSet;

    private DeviceSortType mSortType;

    private LocalBroadcastManager mBroadcastManager;

    private boolean mActivityVisible;
    private boolean mIsDevicesUpdatedNecessary = false;
    private boolean mIsDevicesPullRefreshUpdated = false;

    public static final int RESULT_SCAN = 0x10;

    private View mBottomBar;
    private ImageView mAllDeviceBtn;
    private ImageView mActivateDeviceBtn;
    private ImageView mDeleteDeviceBtn;
    private ImageView mMoveDeviceBtn;
    private ImageView mRemoveDeviceBtn;

    private EditText mSearchET;

    private boolean mOnStartRefreshMark = false;

    private static final int SORT_POSITION_DEVICE_NAME = 0;
    private static final int SORT_POSITION_ACTIVATE_TIME = 1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (EspMainActivity)activity;
        mUser = BEspUser.getBuilder().getInstance();

        mSettingsShared = activity.getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mSettingsShared.registerOnSharedPreferenceChangeListener(this);

        mNewDevicesShared =
            activity.getSharedPreferences(EspStrings.Key.NAME_NEW_ACTIVATED_DEVICES, Context.MODE_PRIVATE);
        mNewDevicesShared.registerOnSharedPreferenceChangeListener(this);
        mNewDevicesSet = mUser.getNewActivatedDevices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_esp_fragment, container, false);

        mSearchET = (EditText)view.findViewById(R.id.search_view);
        mSearchET.addTextChangedListener(mSearchWatcher);

        mGroupListView = (HorizontalListView)view.findViewById(R.id.group_list);
        mUser.loadGroupDB();
        mGroupList = mUser.getGroupList();
        mGroupAdapter = new GroupAdapter();
        mGroupListView.setAdapter(mGroupAdapter);
        mGroupListView.setOnItemClickListener(this);
        mGroupListView.setOnItemLongClickListener(this);

        mAllGroupView = (TextView)view.findViewById(R.id.group_all);
        mAllGroupView.setOnClickListener(this);
        mAddGroupView = view.findViewById(R.id.group_add);
        mAddGroupView.setOnClickListener(this);

        mDeviceListView = (PullToRefreshListView)view.findViewById(R.id.devices_list);
        mAllDeviceList = new ArrayList<IEspDevice>();
        mDeviceAdapter = new DeviceAdapter(mAllDeviceList);
        mDeviceListView.setAdapter(mDeviceAdapter);
        mDevicesUpdateMsgQueue = new LinkedBlockingQueue<Object>();
        mUpdateDeviceThread = new UpdateDeviceThread();
        mUpdateDeviceThread.start();
        mDeviceListView.setOnRefreshListener(this);
        mDeviceListView.setOnItemClickListener(this);
        mDeviceListView.getRefreshableView().setOnItemLongClickListener(this);

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

        mRefreshing = false;
        mRefreshHandler = new RefreshHandler(this);
        mActivity.getActionBar().setDisplayShowCustomEnabled(true);
        mActivity.getActionBar().setCustomView(R.layout.widget_progressbar);
        refresh();

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

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBroadcastManager.unregisterReceiver(mGroupReceiver);
        mBroadcastManager.unregisterReceiver(mDeviceReceiver);

        mRefreshHandler.removeMessages(MSG_AUTO_REFRESH);
        mRefreshHandler.removeMessages(MSG_UPDATE_TEMPDEVICE);
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
            mDeviceListView.onRefreshComplete();
            mRefreshing = false;
            mIsDevicesUpdatedNecessary = false;
            mIsDevicesPullRefreshUpdated = false;
        }

        if (mOnStartRefreshMark) {
            mOnStartRefreshMark = false;
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
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        if (refreshView == mDeviceListView) {
            refresh();
        }
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
            showCreateGroupDialog();
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
        }
    }

    private Toast mBottomToast;

    @Override
    public boolean onLongClick(View v) {
        if (v == mAllDeviceBtn || v == mDeleteDeviceBtn || v == mActivateDeviceBtn || v == mMoveDeviceBtn
            || v == mRemoveDeviceBtn) {
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
        if (parent == mGroupListView) {
            IEspGroup group = (IEspGroup)view.getTag();
            if (mSelectedGroup != group) {
                onGroupSelectChange(group);
            }
        } else if (parent == mDeviceListView.getRefreshableView()) {
            IEspDevice device = (IEspDevice)view.getTag();
            gotoUseDevice(device);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mGroupListView) {
            IEspGroup group = (IEspGroup)view.getTag();
            PopupMenu groupMenu = new PopupMenu(mActivity, view);
            Menu menu = groupMenu.getMenu();
            menu.add(Menu.NONE, POPMENU_ID_GROUP_RENAME, 0, R.string.esp_main_group_menu_rename);
            menu.add(Menu.NONE, POPMENU_ID_GROUP_DELETE, 0, R.string.esp_main_group_menu_delete);
            groupMenu.setOnMenuItemClickListener(new OnGroupPopupMenuItemClickListener(group));
            groupMenu.show();

            return true;
        } else if (parent == mDeviceListView.getRefreshableView()) {
            IEspDevice device = (IEspDevice)view.getTag();
            if (!isDeviceEditable(device) || !mUser.isLogin()) {
                setDeviceListCheckMode(true);
                return true;
            }

            PopupMenu deviceMenu = new PopupMenu(mActivity, view);
            deviceMenu.setOnMenuItemClickListener(new OnDevicePopupMenuItemClickListener(device));
            Menu menu = deviceMenu.getMenu();
            if (device.isActivated()) {
                menu.add(Menu.NONE, POPUP_ID_DEVICE_RENAME, 0, R.string.esp_main_device_menu_rename);
                menu.add(Menu.NONE, POPUP_ID_DEVICE_DELETE, 0, R.string.esp_main_device_menu_delete);
            } else {
                menu.add(Menu.NONE, POPUP_ID_DEVICE_ACTIVATE, 0, R.string.esp_main_device_menu_activate);
            }
            menu.add(Menu.NONE, PUPUP_ID_DEVICE_MORE, 0, R.string.esp_main_device_menu_more);
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

    private class GroupAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mGroupList.size();
        }

        @Override
        public IEspGroup getItem(int position) {
            return mGroupList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mGroupList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = View.inflate(mActivity, R.layout.main_group_item, null);
            } else {
                view = convertView;
            }

            IEspGroup group = mGroupList.get(position);
            view.setTag(group);

            TextView text = (TextView)view.findViewById(R.id.group_text);
            text.setText(group.getName());
            Drawable d;
            if (group == mSelectedGroup) {
                d = getResources().getDrawable(R.drawable.group_general_selected);
            } else {
                d = getResources().getDrawable(R.drawable.group_general_normal);
            }
            text.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);

            return view;
        }

    }

    private void showCreateGroupDialog() {
        showEditGroupDialog(null);
    }

    private void showEditGroupDialog(final IEspGroup scene) {
        View view = View.inflate(mActivity, R.layout.edit_dialog, null);
        final EditText edittext = (EditText)view.findViewById(R.id.edit);
        edittext.setHint(R.string.esp_scene_edit_hint);
        final TextView textview = (TextView)view.findViewById(R.id.text);
        textview.setVisibility(View.GONE);
        textview.setText(R.string.esp_scene_duplicate_scene_msg);
        final AlertDialog dialog = new AlertDialog.Builder(mActivity).setView(view)
            .setTitle(scene == null ? getString(R.string.esp_scene_create) : scene.getName())
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String groupName = edittext.getText().toString();
                    editGroup(scene, groupName);
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
                        mSelectedGroup = null;

                        updateDeviceList();
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
        Drawable d;
        if (group == null) {
            d = getResources().getDrawable(R.drawable.group_all_selected);
        } else {
            d = getResources().getDrawable(R.drawable.group_all_normal);
        }
        mAllGroupView.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
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
                            if (!state.isStateLocal() && !state.isStateInternet())
                            {
                                continue;
                            }
                        }

                        deviceList.add(device);
                    }
                }

                new DeviceSortor().sort(deviceList, mSortType);

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
                        mDeviceListView.onRefreshComplete();
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
        if (state.isStateUpgradingInternet() || state.isStateUpgradingLocal()
            || state.isStateActivating()) {
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
                case PUPUP_ID_DEVICE_MORE:
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
                if (device.isActivated() && isDeviceEditable(device)) {
                    mDevices.add(device);
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

    private class DeviceAdapter extends BaseAdapter {
        private List<IEspDevice> mDeviceList;

        private final int mOnlineColor;
        private final int mOfflineColor;

        private LayoutInflater mInflater;

        private boolean mCheckMode;
        private List<IEspDevice> mCheckedDeviceList;

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
            if (convertView == null) {
                view = mInflater.inflate(R.layout.main_device_item, parent, false);
            } else {
                view = convertView;
            }

            IEspDevice device = getItem(position);
            view.setTag(device);

            IEspDeviceState deviceState = device.getDeviceState();

            ImageView icon = (ImageView)view.findViewById(R.id.device_icon);
            int iconRes = DeviceUtil.getDeviceIconRes(device);
            icon.setImageResource(iconRes);

            TextView name = (TextView)view.findViewById(R.id.device_name);
            name.setText(device.getName());
            TextView version = (TextView)view.findViewById(R.id.device_version);
            version.setText(device.getRom_version());
            if (deviceState.isStateOffline()) {
                name.setTextColor(mOfflineColor);
                version.setTextColor(mOfflineColor);
            } else {
                name.setTextColor(mOnlineColor);
                version.setTextColor(mOnlineColor);
            }

            TextView status = (TextView)view.findViewById(R.id.device_status_text1);
            if (device.isActivated()) {
                if (deviceState.isStateInternet()) {
                    status.setBackgroundResource(R.drawable.device_status_cloud_on);
                } else {
                    status.setBackgroundResource(R.drawable.device_status_cloud_off);
                }
            } else {
                status.setBackgroundDrawable(null);
            }
            if (deviceState.isStateUpgradingInternet() || deviceState.isStateUpgradingLocal()) {
                status.setBackgroundResource(R.drawable.device_status_upgrading);
            }

            TextView newActivate = (TextView)view.findViewById(R.id.device_status_text2);
            newActivate.setTextColor(Color.RED);
            newActivate.setText("NEW");
            boolean isNewActivate = mNewDevicesSet.contains(device.getKey());
            newActivate.setVisibility(isNewActivate ? View.VISIBLE : View.GONE);

            CheckBox checked = (CheckBox)view.findViewById(R.id.device_checked);
            checked.setVisibility(mCheckMode ? View.VISIBLE : View.GONE);
            checked.setChecked(mCheckedDeviceList.contains(device));
            checked.setOnCheckedChangeListener(new OnCheckedChangedListener(device));

            return view;
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

        public void addCheckDevice(IEspDevice device) {
            if (mCheckMode && mDeviceList.contains(device) && !mCheckedDeviceList.contains(device)) {
                mCheckedDeviceList.add(device);
                notifyDataSetChanged();
                onDeviceCheckedChanged();
            }
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
    }

    private void onDeviceCheckedChanged() {
        if (mDeviceAdapter.getCheckedCount() == 0) {
            mActivateDeviceBtn.setEnabled(false);
            mDeleteDeviceBtn.setEnabled(false);
            mRemoveDeviceBtn.setEnabled(false);
            mMoveDeviceBtn.setEnabled(false);
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
        }
    }

    private void setDeviceListCheckMode(boolean checkMode) {
        mBottomBar.setVisibility(checkMode ? View.VISIBLE : View.GONE);
        mDeviceListView.setMode(checkMode ? Mode.DISABLED : Mode.PULL_FROM_START);
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

            mActivity.getActionBar().setDisplayShowCustomEnabled(false);

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
                mDeviceListView.onRefreshComplete();

                mRefreshing = false;

                updateGroupList();
            } else if (action.equals(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE)) {
                log.debug("Receive Broadcast DEVICES_ARRIVE_STATEMACHINE");
                mUser.doActionDevicesUpdated(true);
                updateDeviceList();
                mDeviceAdapter.notifyDataSetChanged();
            }
        }

    };
}
