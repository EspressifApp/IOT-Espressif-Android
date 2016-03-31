package com.espressif.iot.ui.device;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceTypeResult;
import com.espressif.iot.ui.configure.EspButtonConfigureActivity;
import com.espressif.iot.ui.device.timer.DeviceTimersActivity;
import com.espressif.iot.ui.device.trigger.DeviceTriggerActivity;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.settings.SettingsActivity;
import com.espressif.iot.ui.view.EspPagerAdapter;
import com.espressif.iot.ui.view.EspViewPager;
import com.espressif.iot.ui.view.TreeView;
import com.espressif.iot.ui.view.TreeView.LastLevelItemClickListener;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspDefaults;
import com.espressif.iot.util.EspStrings;
import com.google.zxing.qrcode.ui.QRImageHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public abstract class DeviceActivityAbs extends EspActivityAbs {
    private static final Logger log = Logger.getLogger(DeviceActivityAbs.class);
    
    protected static final int MENU_ID_SHARE_DEVICE = 0x1000;
    protected static final int MENU_ID_DEVICE_TIMERS = 0x1001;
    protected static final int MENU_ID_UPGRADE_LOCAL = 0x1002;
    protected static final int MENU_ID_UPGRADE_ONLINE = 0x1003;
    protected static final int MENU_ID_ESPBUTTON_CONFIGURE = 0x1004;
    protected static final int MENU_ID_TRIGGER = 0x1005;
    
    protected IEspDevice mIEspDevice;
    
    protected IEspUser mUser;
    
    private boolean mDeviceCompatibility;
    
    protected static final int COMMAND_GET = 0;
    protected static final int COMMAND_POST = 1;
    
    protected EspViewPager mPager;
    private List<View> mViewList;
    private View mControlView;
    private View mMeshView;
    private TreeView mTreeView;
    private ImageView mSwapView;
    
    private boolean mShowChildren;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        String deviceKey = intent.getStringExtra(EspStrings.Key.DEVICE_KEY_KEY);
        
        mUser = BEspUser.getBuilder().getInstance();
        IEspDevice device;
        if (sTempDevice != null && deviceKey.equals(sTempDevice.getKey())) {
            device = sTempDevice;
        } else {
            device = mUser.getUserDevice(deviceKey);
        }
        if (device instanceof IEspDeviceSSS) {
            mIEspDevice = BEspDevice.convertSSSToTypeDevice((IEspDeviceSSS)device);
        } else {
            mIEspDevice = device;
        }
        
        SharedPreferences shared = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mShowChildren = mIEspDevice.getIsMeshDevice()
            && intent.getBooleanExtra(EspStrings.Key.DEVICE_KEY_SHOW_CHILDREN, EspDefaults.SHOW_CHILDREN)
            && shared.getBoolean(EspStrings.Key.SETTINGS_KEY_SHOW_MESH_TREE, EspDefaults.SHOW_MESH_TREE);
        
        setTitle(mIEspDevice.getName());
        
        checkDeviceCompatibility();
        
        if (!isDeviceArray())
        {
            setTitleRightIcon(R.drawable.esp_icon_menu_moreoverflow);
        }
        
        initViews();
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        setTempDevice(null);
    }
    
    private void checkDeviceCompatibility()
    {
        switch (mUser.checkDeviceCompatibility(mIEspDevice))
        {
            case COMPATIBILITY:
                mDeviceCompatibility = true;
                break;
            case APK_NEED_UPGRADE:
                showUpgradeApkHintDialog();
                mDeviceCompatibility = false;
                break;
            case DEVICE_NEED_UPGRADE:
                showUpgradeDeviceHintDialog();
                mDeviceCompatibility = false;
                break;
        }
    }
    
    private void initViews()
    {
        setContentView(R.layout.device_ui_container);
        
        mPager = (EspViewPager)findViewById(R.id.device_ui_pager);
        mPager.setInterceptTouchEvent(mShowChildren);
        
        mMeshView = View.inflate(this, R.layout.device_mesh_children_list, null);
        if (mShowChildren)
        {
            initTreeView();

            if (mIEspDevice.getDeviceType() != EspDeviceType.ROOT) {
                mSwapView = new ImageView(this);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
                mSwapView.setLayoutParams(lp);
                mSwapView.setImageResource(R.drawable.esp_icon_swap);
                mSwapView.setBackgroundResource(R.drawable.esp_activity_icon_background);
                mSwapView.setScaleType(ScaleType.CENTER);
                mSwapView.setOnClickListener(mSwapListener);
                setTitleContentView(mSwapView);
            }
        }

        mControlView = initControlView();
        mViewList = new ArrayList<View>();
        mViewList.add(mControlView);
        mViewList.add(mMeshView);
        mPager.setAdapter(new EspPagerAdapter(mViewList));
    }
    
    private void initTreeView()
    {
        mTreeView = (TreeView)mMeshView.findViewById(R.id.mesh_children_list);
        List<IEspDevice> allDeviceList = mUser.getAllDeviceList();
        List<IEspDeviceTreeElement> childList = mIEspDevice.getDeviceTreeElementList(allDeviceList);
        mTreeView.initData(this, childList);
        mTreeView.setLastLevelItemClickCallBack(mTreeViewItemClickListener);
    }
    
    private LastLevelItemClickListener mTreeViewItemClickListener = new LastLevelItemClickListener()
    {
        
        @Override
        public void onLastLevelItemClick(IEspDeviceTreeElement element, int position)
        {
            IEspDevice device = element.getCurrentDevice();
            Class<?> cls = null;
            switch (device.getDeviceType())
            {
                case PLUG:
                    cls = DevicePlugActivity.class;
                    break;
                case LIGHT:
                    cls = DeviceLightActivity.class;
                    break;
                    
                case REMOTE:
                case FLAMMABLE:
                case HUMITURE:
                case VOLTAGE:
                case ROOT:
                case NEW:
                case PLUGS:
                    break;
            }
            
            if (cls != null)
            {
                Intent intent = new Intent(getBaseContext(), cls);
                intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, device.getKey());
                intent.putExtra(EspStrings.Key.DEVICE_KEY_SHOW_CHILDREN, false);
                startActivity(intent);
            }
        }
        
    };
    
    private View.OnClickListener mSwapListener = new View.OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            final int item_control = 0;
            final int item_children = 1;
            int currentItem = mPager.getCurrentItem();
            int targetItem = currentItem == item_control ? item_children : item_control;
            mPager.setCurrentItem(targetItem, true);
        }
    };
    
    protected TreeView getTreeView()
    {
        return mTreeView;
    }
    
    @Override
    protected void onTitleRightIconClick(View rightIcon)
    {
        openOptionsMenu();
    }
    
    /**
     * The device is compatibility or not
     */
    protected boolean isDeviceCompatibility()
    {
        return mDeviceCompatibility;
    }
    
    /**
     * The application APK need upgrade, show the hint dialog
     */
    private void showUpgradeApkHintDialog()
    {
        new AlertDialog.Builder(this).setMessage(R.string.esp_device_dialog_upgrade_apk_message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Activity activity = DeviceActivityAbs.this;
                    activity.finish();
                    activity.startActivity(new Intent(activity, SettingsActivity.class));
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    finish();
                }
            })
            .show();
    }
    
    /**
     * The device need upgrade, show the hint dialog
     */
    private void showUpgradeDeviceHintDialog()
    {
        AlertDialog.Builder builder =
            new AlertDialog.Builder(this).setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    log.debug("Cancel upgrade device hint dialog");
                    finish();
                }
            });
        
        IEspDeviceState state = mIEspDevice.getDeviceState();
        if (state.isStateUpgradingLocal())
        {
            /*
             * The device is upgrading local
             */
            builder.setMessage(R.string.esp_device_dialog_upgrading_local_message);
        }
        else if (state.isStateUpgradingInternet())
        {
            /*
             * The device is upgrading online
             */
            builder.setMessage(R.string.esp_device_dialog_upgrading_online_message);
        }
        else
        {
            /*
             * Check the state and show the upgrade select option
             */
            builder.setMessage(R.string.esp_device_dialog_upgrade_device_message);
            DialogInterface.OnClickListener upgradeListener = new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which)
                    {
                        case DialogInterface.BUTTON_POSITIVE: // upgrade local
                            log.debug("Click upgrade device hint dialog local button");
                            mUser.doActionUpgradeLocal(mIEspDevice);
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEUTRAL: // upgrade online
                            log.debug("Click upgrade device hint dialog online button");
                            mUser.doActionUpgradeInternet(mIEspDevice);
                            finish();
                            break;
                    }
                }
            };
            EspUpgradeDeviceTypeResult upgradeType = mUser.getDeviceUpgradeTypeResult(mIEspDevice);
            log.info("mIEspDevice state = " + state + " ||| " + "Upgrade type = " + upgradeType);
            switch (upgradeType)
            {
                case SUPPORT_ONLINE_LOCAL:
                    if (state.isStateLocal())
                    {
                        builder.setPositiveButton(R.string.esp_device_dialog_upgrade_device_local, upgradeListener);
                    }
                    if (state.isStateInternet())
                    {
                        builder.setNeutralButton(R.string.esp_device_dialog_upgrade_device_online, upgradeListener);
                    }
                    break;
                case SUPPORT_LOCAL_ONLY:
                    if (state.isStateLocal())
                    {
                        builder.setPositiveButton(R.string.esp_device_dialog_upgrade_device_local, upgradeListener);
                    }
                    break;
                case SUPPORT_ONLINE_ONLY:
                    if (state.isStateInternet())
                    {
                        builder.setNeutralButton(R.string.esp_device_dialog_upgrade_device_online, upgradeListener);
                    }
                    break;
                default:
                    break;
            }
        }
        builder.show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (isDeviceArray())
        {
            return super.onCreateOptionsMenu(menu);
        }
        
        if (mIEspDevice.getIsOwner())
        {
            menu.add(Menu.NONE, MENU_ID_SHARE_DEVICE, 0, R.string.esp_device_menu_share)
                .setIcon(R.drawable.esp_menu_icon_share);
        }
        
        if (mIEspDevice.isSupportTimer())
        {
            menu.add(Menu.NONE, MENU_ID_DEVICE_TIMERS, 0, R.string.esp_device_menu_timer);
        }
        
        menu.add(Menu.NONE, MENU_ID_UPGRADE_LOCAL, 0, R.string.esp_device_menu_upgrade_local);
        menu.add(Menu.NONE, MENU_ID_UPGRADE_ONLINE, 0, R.string.esp_device_menu_upgrade_online);
        
        if (mIEspDevice.getIsMeshDevice() && mIEspDevice.getDeviceState().isStateLocal())
        {
            menu.add(Menu.NONE, MENU_ID_ESPBUTTON_CONFIGURE, 0, R.string.esp_device_menu_espbutton_configure);
        }
        
        menu.add(Menu.NONE, MENU_ID_TRIGGER, 10, R.string.esp_device_menu_trigger);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if (isDeviceArray())
        {
            return super.onPrepareOptionsMenu(menu);
        }
        
        log.debug("onPrepareOptionsMenu mIEspDevice state = " + mIEspDevice.getDeviceState());
        switch (mUser.getDeviceUpgradeTypeResult(mIEspDevice))
        {
            case SUPPORT_ONLINE_LOCAL:
                break;
            case SUPPORT_LOCAL_ONLY:
                menu.findItem(MENU_ID_UPGRADE_ONLINE).setEnabled(false);
                break;
            case SUPPORT_ONLINE_ONLY:
                menu.findItem(MENU_ID_UPGRADE_LOCAL).setEnabled(false);
                break;
            case CURRENT_ROM_INVALID:
            case CURRENT_ROM_IS_NEWEST:
            case DEVICE_TYPE_INCONSISTENT:
            case LATEST_ROM_INVALID:
            case NOT_SUPPORT_UPGRADE:
                menu.findItem(MENU_ID_UPGRADE_LOCAL).setEnabled(false);
                menu.findItem(MENU_ID_UPGRADE_ONLINE).setEnabled(false);
                break;
        }
        
        if (!mIEspDevice.getDeviceState().isStateLocal())
        {
            menu.findItem(MENU_ID_UPGRADE_LOCAL).setEnabled(false);
        }
        if (!mIEspDevice.getDeviceState().isStateInternet())
        {
            menu.findItem(MENU_ID_UPGRADE_ONLINE).setEnabled(false);
        }
        if (mIEspDevice.getDeviceState().isStateUpgradingLocal()
            || mIEspDevice.getDeviceState().isStateUpgradingInternet())
        {
            menu.findItem(MENU_ID_UPGRADE_LOCAL).setEnabled(false);
            menu.findItem(MENU_ID_UPGRADE_ONLINE).setEnabled(false);
        }
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_ID_SHARE_DEVICE:
                new GenerateShareKeyTask(this).execute(mIEspDevice.getKey());
                return true;
            case MENU_ID_DEVICE_TIMERS:
                Intent timerIntent = new Intent(this, DeviceTimersActivity.class);
                timerIntent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, mIEspDevice.getKey());
                startActivity(timerIntent);
                return true;
            case MENU_ID_UPGRADE_LOCAL:
                mUser.doActionUpgradeLocal(mIEspDevice);
                finish();
                return true;
            case MENU_ID_UPGRADE_ONLINE:
                mUser.doActionUpgradeInternet(mIEspDevice);
                finish();
                return true;
            case MENU_ID_ESPBUTTON_CONFIGURE:
                Intent configureIntent = new Intent(this, EspButtonConfigureActivity.class);
                String[] deviceKeys = new String[] {mIEspDevice.getKey()};
                configureIntent.putExtra(EspStrings.Key.KEY_ESPBUTTON_DEVICE_KEYS, deviceKeys);
                startActivity(configureIntent);
                return true;
            case MENU_ID_TRIGGER:
                Intent triggerIntent = new Intent(this, DeviceTriggerActivity.class);
                triggerIntent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, mIEspDevice.getKey());
                startActivity(triggerIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showQRCodeDialog(String shareKey)
    {
        String qrUrl = QRImageHelper.createDeviceKeyUrl(shareKey);
        final ImageView QRImage = (ImageView)View.inflate(this, R.layout.qr_code_image, null);
        final Bitmap QRBmp = QRImageHelper.createQRImage(qrUrl, DeviceActivityAbs.this);
        QRImage.setImageBitmap(QRBmp);
        
        AlertDialog dialog = new AlertDialog.Builder(this).setView(QRImage).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(new OnDismissListener()
        {
            
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                QRImage.setImageBitmap(null);
                QRBmp.recycle();
            }
        });
        dialog.show();
    }
    
    private class GenerateShareKeyTask extends AsyncTask<String, Void, String>
    {
        private Activity mActivity;
        
        private ProgressDialog mDialog;
        
        public GenerateShareKeyTask(Activity activity)
        {
            mActivity = activity;
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.esp_device_share_progress_message));
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
        }
        
        @Override
        protected String doInBackground(String... params)
        {
            String ownerDeviceKey = params[0];
            return mUser.doActionGenerateShareKey(ownerDeviceKey);
        }
        
        @Override
        protected void onPostExecute(String result)
        {
            if (result == null)
            {
                // Generate share key from server failed
                mDialog.setMessage(getString(R.string.esp_device_share_result_failed));
                mDialog.setCancelable(true);
                mDialog.setCanceledOnTouchOutside(true);
            }
            else
            {
                // Generate share key from server success
                mDialog.dismiss();
                mDialog = null;
                showQRCodeDialog(result);
            }
        }
    }
    
    /**
     * 
     * @return the Device control View
     */
    abstract protected View initControlView();
    
    /**
     * Run this function in onPreExecute() of DeviceTask when executePost or executeGet
     */
    protected abstract void executePrepare();
    
    /**
     * Run this function in onPostExecute(Result result) of DeviceTask when executePost or executeGet
     * 
     * @param command Whether post or get, one of {@link #COMMAND_GET} and {@link #COMMAND_POST}
     * @param result Whether executePost or executeGet success
     */
    protected abstract void executeFinish(int command, boolean result);
    
    /**
     * Post the status
     * 
     * @param status
     */
    protected void executePost(final IEspDeviceStatus status)
    {
        new DeviceTask(this).execute(status);
    }
    /**
     * Get current status of device
     */
    protected void executeGet()
    {
        new DeviceTask(this).execute();
    }
    
    private class DeviceTask extends AsyncTask<IEspDeviceStatus, Void, Boolean> implements OnDismissListener
    {
        private Activity mActivity;
        
        private ProgressDialog mDialog;
        
        private int mCommand;
        
        public DeviceTask(Activity activity)
        {
            mActivity = activity;
        }
        
        @Override
        protected void onPreExecute()
        {
            executePrepare();
            
            showDialog();
        }
        
        @Override
        protected Boolean doInBackground(IEspDeviceStatus... params)
        {
            if (params.length > 0)
            {
                // execute Post status
                log.debug(mIEspDevice.getName() + " Post status");
                IEspDeviceStatus status = params[0];
                mCommand = COMMAND_POST;
                return mUser.doActionPostDeviceStatus(mIEspDevice, status);
            }
            else
            {
                // execute Get status
                log.debug(mIEspDevice.getName() + " Get status");
                mCommand = COMMAND_GET;
                if (mIEspDevice instanceof IEspDeviceArray)
                {
                    return false;
                }
                else
                {
                    return mUser.doActionGetDeviceStatus(mIEspDevice);
                }
            }
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            log.debug("DeviceTask result = " + result);
            releaseDialog();
            
            executeFinish(mCommand, result);
        }
        
        private void showDialog()
        {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.esp_device_task_dialog_message));
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnDismissListener(this);
            mDialog.show();
        }
        
        private void releaseDialog()
        {
            if (mDialog != null)
            {
                mDialog.dismiss();
                mDialog = null;
            }
        }
        
        @Override
        public void onDismiss(DialogInterface dialog)
        {
            cancel(true);
            mDialog = null;
        }
    }
    
    /**
     * Get the intent which can start device Activity
     * 
     * @param context
     * @param device
     * @return
     */
    public static Intent getDeviceIntent(Context context, IEspDevice device)
    {
        Class<?> cls = getDeviceClass(device);
        if (cls != null)
        {
            Intent intent = new Intent(context, cls);
            intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, device.getKey());
            return intent;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Get device type's function use class
     * 
     * @param device
     * @return
     */
    public static Class<?> getDeviceClass(IEspDevice device)
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
    
    /**
     * The device is normal device or IEspDeviceArray
     * 
     * @return
     */
    protected boolean isDeviceArray()
    {
        return mIEspDevice instanceof IEspDeviceArray;
    }
    
    private static IEspDevice sTempDevice;

    /**
     * Set the temp device for IEspDeviceArray(Group mode etc.) or IEspDeviceSSS(DirectConnect mode etc.)
     * 
     * @param device
     */
    public static void setTempDevice(IEspDevice device) {
        sTempDevice = device;
    }
}
