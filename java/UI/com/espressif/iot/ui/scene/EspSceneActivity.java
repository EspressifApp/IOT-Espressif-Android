package com.espressif.iot.ui.scene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.espressif.iot.R;
import com.espressif.iot.db.EspGroupDBManager;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.group.IEspGroup;
import com.espressif.iot.model.group.EspGroupHandler;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.ui.device.DeviceActivityAbs;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.view.DeviceAdapter;
import com.espressif.iot.ui.view.DeviceAdapter.OnEditCheckedChangeListener;
import com.espressif.iot.ui.view.TouchPointMoveLayout;
import com.espressif.iot.ui.view.TouchPointMoveLayout.IntersectsView;
import com.espressif.iot.ui.view.TouchPointMoveLayout.OnTouchMoveListener;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspDefaults;
import com.espressif.iot.util.EspStrings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class EspSceneActivity extends EspActivityAbs implements OnClickListener, OnItemLongClickListener,
    OnItemClickListener, OnMenuItemClickListener, OnEditCheckedChangeListener
{
    private IEspUser mUser;
    
    private List<IEspDevice> mUserDeviceList;
    
    private ListView mDeviceLV;
    private List<IEspDevice> mDeviceList;
    private DeviceAdapter mDeviceAdapter;
    
    private View mSceneContent;
    private ImageView mSceneAddIV;
    private ListView mSceneLV;
    private List<IEspGroup> mSceneList;
    private SceneAdapter mSceneAdapter;
    
    private TouchPointMoveLayout mMoveLayout;
    
    private View mButtonBar;
    private Button mBottomEditBtn;
    private Button mBottomRemoveBtn;
    private Button mBottomControlBtn;
    
    private static final int POPMENU_ID_SYNC_LOCAL = 0x10;
    private static final int POPMENU_ID_RENAME = 0x20;
    private static final int POPMENU_ID_DELETE = 0x21;
    
    private EspGroupHandler mEspGroupHandler;
    private EspGroupDBManager mEspGroupDBManager;
    
    private IEspGroup mSelectedGroup;
    
    private String mTitle;
    
    private LocalBroadcastManager mBroadcastManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.esp_scene_activity);
        
        mUser = BEspUser.getBuilder().getInstance();
        mUserDeviceList = mUser.getAllDeviceList();
        
        mTitle = getString(R.string.esp_scene);
        setTitle(mTitle);
        setTitleRightIcon(R.drawable.esp_icon_menu_moreoverflow);
        
        mMoveLayout = (TouchPointMoveLayout)findViewById(R.id.container);
        mMoveLayout.setOnIntersectsChangeListener(mIntersectsListener);
        
        mDeviceLV = (ListView)findViewById(R.id.device_list);
        mDeviceList = new ArrayList<IEspDevice>();
        mDeviceList.addAll(mUserDeviceList);
        mDeviceAdapter = new DeviceAdapter(this, mDeviceList);
        mDeviceLV.setAdapter(mDeviceAdapter);
        mDeviceLV.setOnItemClickListener(this);
        mDeviceLV.setOnItemLongClickListener(this);
        mDeviceAdapter.setOnEditCheckedChangeListener(this);
        
        mSceneContent = findViewById(R.id.scene_content);
        mSceneAddIV = (ImageView)findViewById(R.id.scene_add_btn);
        mSceneAddIV.setOnClickListener(this);
        mSceneLV = (ListView)findViewById(R.id.scene_list);
        mUser.loadGroupDB();
        mSceneList = mUser.getGroupList();
        mSceneAdapter = new SceneAdapter(this);
        mSceneLV.setAdapter(mSceneAdapter);
        mSceneLV.setOnItemClickListener(this);
        mSceneLV.setOnItemLongClickListener(this);
        
        mButtonBar = findViewById(R.id.scene_button_bar);
        mBottomEditBtn = (Button)findViewById(R.id.scene_device_edit);
        mBottomEditBtn.setOnClickListener(this);
        mBottomControlBtn = (Button)findViewById(R.id.scene_device_control);
        mBottomControlBtn.setOnClickListener(this);
        mBottomRemoveBtn = (Button)findViewById(R.id.scene_device_remove);
        mBottomRemoveBtn.setOnClickListener(this);
        
        mEspGroupHandler = EspGroupHandler.getInstance();
        mEspGroupHandler.call();
        mEspGroupDBManager = EspGroupDBManager.getInstance();
        
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter(EspStrings.Action.CREATE_NEW_CLOUD_GROUP);
        mBroadcastManager.registerReceiver(mReceiver, filter);
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        mBroadcastManager.unregisterReceiver(mReceiver);
    }
    
    private void updateGroupList()
    {
        mUser.loadGroupDB();
        mSceneList.clear();
        mSceneList.addAll(mUser.getGroupList());
        mSceneAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onTitleRightIconClick(View rightIcon)
    {
        PopupMenu popupMenu = new PopupMenu(this, rightIcon);
        
        Menu menu = popupMenu.getMenu();
        boolean canSyncLocal = mUser.isLogin() && mEspGroupDBManager.getUserGroup(null).size() > 0;
        menu.add(Menu.NONE, POPMENU_ID_SYNC_LOCAL, 0, R.string.esp_scene_menu_sync_local).setEnabled(canSyncLocal);
        
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mSceneAddIV)
        {
            showCreateSceneDialog();
        }
        else if (v == mBottomEditBtn)
        {
            boolean editable = !mDeviceAdapter.isEditable();
            mDeviceAdapter.setEditable(editable);
            if (!editable)
            {
                mDeviceAdapter.getEditCheckedDevices().clear();
            }
            updateBottomButtons();
            mDeviceAdapter.notifyDataSetChanged();
        }
        else if (v == mBottomControlBtn)
        {
            checkControllableDevice();
        }
        else if (v == mBottomRemoveBtn)
        {
            showRemoveDeviceDialog();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == mSceneLV)
        {
            mDeviceAdapter.getEditCheckedDevices().clear();
            mDeviceAdapter.setEditable(false);
            updateBottomButtons();
            
            IEspGroup group = mSceneList.get(position);
            mSelectedGroup = mSelectedGroup == group ? null : group;
            boolean groupSelected = mSelectedGroup != null;
            mDeviceList.clear();
            mDeviceList.addAll(groupSelected ? mSelectedGroup.getDeviceList() : mUserDeviceList);
            setTitle(groupSelected ? mSelectedGroup.getName() : mTitle);
            mButtonBar.setVisibility(groupSelected ? View.VISIBLE : View.GONE);
            
            mDeviceAdapter.notifyDataSetChanged();
            mSceneAdapter.notifyDataSetChanged();
        }
        else if (parent == mDeviceLV)
        {
            IEspDevice device = mDeviceList.get(position);
            Intent intent = DeviceActivityAbs.getDeviceIntent(this, device);
            if (intent != null)
            {
                intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, device.getKey());
                startActivity(intent);
            }
        }
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == mDeviceLV)
        {
            float offsetX = mSceneContent.getX();
            float offsetY = mSceneContent.getY() + mSceneAddIV.getHeight();
            int count = mSceneLV.getChildCount();
            List<IntersectsView> views = new ArrayList<IntersectsView>();
            for (int i = 0; i < count; i++)
            {
                View child = mSceneLV.getChildAt(i);
                float x = child.getX() + offsetX;
                float y = child.getY() + offsetY;
                RectF rectf = new RectF(x, y, x + child.getWidth(), y + child.getHeight());
                SceneIntersectsView siv = new SceneIntersectsView(child, rectf);
                views.add(siv);
            }
            
            View moveView = generateBitmapView(view);
            moveView.setTag(view.getTag());
            mMoveLayout.startTouchMove(moveView, view.getX(), view.getY(), views);
            
            return true;
        }
        else if (parent == mSceneLV)
        {
            IEspGroup group = mSceneList.get(position);
            PopupMenu popMenu = new PopupMenu(this, view);
            Menu menu = popMenu.getMenu();
            menu.add(Menu.NONE, POPMENU_ID_RENAME, 0, R.string.esp_scene_popmenu_rename);
            menu.add(Menu.NONE, POPMENU_ID_DELETE, 0, R.string.esp_scene_popmenu_delete);
            popMenu.setOnMenuItemClickListener(new SceneMenuListener(group));
            popMenu.show();
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch (item.getItemId())
        {
            case POPMENU_ID_SYNC_LOCAL:
                mEspGroupDBManager.updateLocalGroupUserKey(mUser.getUserKey());
                updateGroupList();
                mEspGroupHandler.call();
                return true;
        }
        return false;
    }
    
    @Override
    public void onEditCheckedChanged(CheckBox checkBox, IEspDevice device, boolean isChecked)
    {
        updateBottomButtons();
    }
    
    private void updateBottomButtons()
    {
        boolean editable = mDeviceAdapter.isEditable();
        int titleRes = editable ? R.string.esp_scene_menu_edit_cancel : R.string.esp_scene_menu_edit;
        mBottomEditBtn.setText(titleRes);
        
        boolean hasDeviceSelected = mDeviceAdapter.getEditCheckedDevices().size() > 0;
        mBottomControlBtn.setEnabled(hasDeviceSelected);
        mBottomRemoveBtn.setEnabled(hasDeviceSelected);
    }
    
    private Bitmap generateViewBitmap(View view)
    {
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        view.draw(canvas);
        
        return bmp;
    }
    
    private View generateBitmapView(View view)
    {
        ImageView iv = new ImageView(this);
        LayoutParams lp = new LayoutParams(view.getWidth(), view.getHeight());
        iv.setLayoutParams(lp);
        iv.setScaleType(ScaleType.CENTER_INSIDE);
        iv.setImageBitmap(generateViewBitmap(view));
        iv.setBackgroundColor(getResources().getColor(R.color.esp_scene_move_view_background));
        
        return iv;
    }
    
    private OnTouchMoveListener mIntersectsListener = new OnTouchMoveListener()
    {
        
        @Override
        public void onIntersectsChanged(View moveView, View intersectsView)
        {
            IEspGroup group = intersectsView == null ? null : (IEspGroup)intersectsView.getTag();
            if (group != null)
            {
                System.out.println("group id = " + group.getId());
            }
            mSceneAdapter.setDeviceSelectedGroup(group);
            mSceneAdapter.notifyDataSetChanged();
        }

        @Override
        public void onTouchMoveEnd(View moveView, View intersectsView)
        {
            if (intersectsView != null)
            {
                IEspDevice device = (IEspDevice)moveView.getTag();
                IEspGroup group = (IEspGroup)intersectsView.getTag();
                mUser.doActionGroupDeviceMoveInto(device, group);
                mSceneAdapter.setDeviceSelectedGroup(null);
                updateGroupList();
                Toast.makeText(getBaseContext(), R.string.esp_scene_move_device_into_toast, Toast.LENGTH_SHORT).show();
            }
        }

    };
    
    private class SceneIntersectsView implements IntersectsView
    {
        private View mView;
        private RectF mRectF;
        
        public SceneIntersectsView(View view, RectF rectF)
        {
            mView = view;
            mRectF = rectF;
        }
        
        @Override
        public View getView()
        {
            return mView;
        }

        @Override
        public RectF getRectF()
        {
            return mRectF;
        }
        
    }
    
    private class SceneAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        
        private IEspGroup mDeviceIntersectsGroup;
        
        private final int COLOR_NORMAL;
        private final int COLOR_INTERSECTS;
        private final int COLOR_SELECTED;
        
        public SceneAdapter(Context context)
        {
            COLOR_NORMAL = Color.TRANSPARENT;
            COLOR_INTERSECTS = getResources().getColor(R.color.esp_scene_background_intersects);
            COLOR_SELECTED = getResources().getColor(R.color.esp_scene_background_selected);
            mInflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount()
        {
            return mSceneList.size();
        }

        @Override
        public IEspGroup getItem(int position)
        {
            return mSceneList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return mSceneList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = mInflater.inflate(R.layout.esp_scene_list_item, parent, false);
            }
            
            IEspGroup group = getItem(position);
            convertView.setTag(group);
            if (group == mDeviceIntersectsGroup)
            {
                convertView.setBackgroundColor(COLOR_INTERSECTS);
            }
            else if (group == mSelectedGroup)
            {
                convertView.setBackgroundColor(COLOR_SELECTED);
            }
            else
            {
                convertView.setBackgroundColor(COLOR_NORMAL);
            }
            
            TextView title = (TextView)convertView.findViewById(R.id.text);
            title.setText(group.getName());
            
            return convertView;
        }
        
        public void setDeviceSelectedGroup(IEspGroup group)
        {
            mDeviceIntersectsGroup = group;
        }
    }
    
    private class SceneMenuListener implements OnMenuItemClickListener
    {
        private IEspGroup mGroup;
        
        public SceneMenuListener(IEspGroup group)
        {
            mGroup = group;
        }
        
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch (item.getItemId())
            {
                case POPMENU_ID_RENAME:
                    showEditSceneDialog(mGroup);
                    return true;
                case POPMENU_ID_DELETE:
                    showDeleteSceneDialog(mGroup);
                    return true;
            }
            return false;
        }
        
    }
    
    private void showCreateSceneDialog()
    {
        showEditSceneDialog(null);
    }
    
    private void showEditSceneDialog(final IEspGroup scene)
    {
        View view = View.inflate(this, R.layout.edit_dialog, null);
        final EditText edittext = (EditText)view.findViewById(R.id.edit);
        edittext.setHint(R.string.esp_scene_edit_hint);
        final TextView textview = (TextView)view.findViewById(R.id.text);
        textview.setVisibility(View.GONE);
        textview.setText(R.string.esp_scene_duplicate_scene_msg);
        final AlertDialog dialog =
            new AlertDialog.Builder(this).setView(view)
                .setTitle(scene == null ? getString(R.string.esp_scene_create) : scene.getName())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String groupName = edittext.getText().toString();
                        editGroup(scene, groupName);
                    }
                })
                .show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        edittext.addTextChangedListener(new TextWatcher()
        {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }
            
            @Override
            public void afterTextChanged(Editable s)
            {
                boolean duplicateName = false;
                for (IEspGroup group : mSceneList)
                {
                    if (s.toString().equals(group.getName()))
                    {
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
    
    private void editGroup(final IEspGroup scene, String groupName)
    {
        if (scene == null)
        {
            mUser.doActionGroupCreate(groupName);
        }
        else
        {
            mUser.doActionGroupRename(scene, groupName);
        }
        
        updateGroupList();
    }
    
    private void showDeleteSceneDialog(final IEspGroup group)
    {
        new AlertDialog.Builder(this).setTitle(R.string.esp_scene_delete_dialog_title)
            .setMessage(getString(R.string.esp_scene_delete_dialog_msg, group.getName()))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    mUser.doActionGroupDelete(group);
                    
                    updateGroupList();
                }
            })
            .show();
    }
    
    private void showRemoveDeviceDialog()
    {
        new AlertDialog.Builder(this).setMessage(R.string.esp_scene_remove_dialog_msg)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    for (IEspDevice device : mDeviceAdapter.getEditCheckedDevices())
                    {
                        mUser.doActionGroupDeviceRemove(device, mSelectedGroup);
                        mSelectedGroup.removeDevice(device);
                        mDeviceList.remove(device);
                    }
                    mDeviceAdapter.notifyDataSetChanged();
                }
            })
            .show();
    }
    
    private void checkControllableDevice()
    {
        Set<EspDeviceType> deviceTypes = new HashSet<EspDeviceType>();
        for (IEspDevice device : mDeviceAdapter.getEditCheckedDevices())
        {
            EspDeviceType type = device.getDeviceType();
            if (type == EspDeviceType.LIGHT || type == EspDeviceType.PLUG)
            {
                deviceTypes.add(type);
            }
        }
        if (deviceTypes.size() == 0)
        {
            Toast.makeText(this, R.string.esp_scene_no_controllable_device_type_msg, Toast.LENGTH_SHORT).show();
        }
        else if (deviceTypes.size() == 1)
        {
            EspDeviceType deviceType = deviceTypes.iterator().next();
            boolean allSameType = true;
            for (IEspDevice device : mDeviceAdapter.getEditCheckedDevices())
            {
                if (device.getDeviceType() != deviceType)
                {
                    allSameType = false;
                    break;
                }
            }
            if (allSameType)
            {
                gotoSelectedDeviceArray(deviceType);
            }
            else
            {
                showDeviceArraySelectDialog(deviceTypes);
            }
        }
        else
        {
            showDeviceArraySelectDialog(deviceTypes);
        }
    }
    
    private void showDeviceArraySelectDialog(Set<EspDeviceType> deviceTypes)
    {
        final String[] chars = new String[deviceTypes.size()];
        int i = 0;
        for (EspDeviceType type : deviceTypes)
        {
            chars[i] = type.toString();
            i++;
        }
        new AlertDialog.Builder(this).setItems(chars, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                EspDeviceType type = EspDeviceType.getEspTypeEnumByString(chars[which]);
                gotoSelectedDeviceArray(type);
            }
        })
        .show();
    }
    
    private void gotoSelectedDeviceArray(EspDeviceType type)
    {
        IEspDeviceArray deviceArray = BEspDevice.createDeviceArray(type);
        for (IEspDevice device : mDeviceAdapter.getEditCheckedDevices())
        {
            IEspDeviceState state = device.getDeviceState();
            if (device.getDeviceType() == type && (state.isStateInternet() || state.isStateLocal()))
            {
                deviceArray.addDevice(device);
            }
        }
        
        if (deviceArray.getDeviceList().size() > 0)
        {
            Intent intent = DeviceActivityAbs.getDeviceIntent(getBaseContext(), deviceArray);
            intent.putExtra(EspStrings.Key.DEVICE_KEY_SHOW_CHILDREN, false);
            intent.putExtra(EspStrings.Key.DEVICE_KEY_TEMP_DEVICE, true);
            DeviceActivityAbs.TEMP_DEVICE = deviceArray;
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, R.string.esp_secen_no_online_device_msg, Toast.LENGTH_SHORT).show();
        }
    }
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (action.equals(EspStrings.Action.CREATE_NEW_CLOUD_GROUP))
            {
                long oldId = intent.getLongExtra(EspStrings.Key.KEY_GROUP_ID_OLD, EspDefaults.GROUP_ID_OLD);
                long newId = intent.getLongExtra(EspStrings.Key.KEY_GROUP_ID_NEW, EspDefaults.GROUP_ID_NEW);
                updateGroupId(oldId, newId);
            }
        }
        
    };
    
    private void updateGroupId(long oldId, long newId)
    {
        for (IEspGroup group : mSceneList)
        {
            if (group.getId() == oldId)
            {
                group.setId(newId);
                return;
            }
        }
    }
}
