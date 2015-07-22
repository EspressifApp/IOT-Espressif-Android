package com.espressif.iot.ui.device;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceRoot;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.help.ui.IEspHelpUIUpgradeLocal;
import com.espressif.iot.help.ui.IEspHelpUIUpgradeOnline;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.help.HelpStepUpgradeLocal;
import com.espressif.iot.type.help.HelpStepUpgradeOnline;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceTypeResult;
import com.espressif.iot.ui.device.dialog.DeviceDialogBuilder;
import com.espressif.iot.ui.device.timer.DeviceTimersActivity;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.settings.SettingsActivity;
import com.espressif.iot.ui.view.EspPagerAdapter;
import com.espressif.iot.ui.view.EspViewPager;
import com.espressif.iot.ui.view.TreeView;
import com.espressif.iot.ui.view.TreeView.LastLevelItemClickListener;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;
import com.google.zxing.qrcode.ui.CreateQRImageHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public abstract class DeviceActivityAbs extends EspActivityAbs implements IEspHelpUIUpgradeLocal,
    IEspHelpUIUpgradeOnline
{
    private static final Logger log = Logger.getLogger(DeviceActivityAbs.class);
    
    protected static final int MENU_ID_SHARE_DEVICE = 0x1000;
    
    protected static final int MENU_ID_DEVICE_TIMERS = 0x1001;
    
    protected static final int MENU_ID_UPGRADE_LOCAL = 0x1002;
    
    protected static final int MENU_ID_UPGRADE_ONLINE = 0x1003;
    
    protected IEspDevice mIEspDevice;
    
    protected IEspUser mUser;
    
    private boolean mDeviceCompatibility;
    
    protected static final int COMMAND_GET = 0;
    
    protected static final int COMMAND_POST = 1;
    
    private HelpHandler mHelpHandler;
    
    protected EspViewPager mPager;
    
    private List<View> mViewList;
    
    private View mControlView;
    
    private View mMeshView;
    
    private TreeView mTreeView;
    
    private ImageView mSwapView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        String deviceKey = intent.getStringExtra(EspStrings.Key.DEVICE_KEY_KEY);
        
        mUser = BEspUser.getBuilder().getInstance();
        IEspDevice device = mUser.getUserDevice(deviceKey);
        if (device instanceof IEspDeviceSSS)
        {
            mIEspDevice = BEspDevice.convertSSSToTypeDevice((IEspDeviceSSS)device);
        }
        else
        {
            mIEspDevice = device;
        }
        
        setTitle(mIEspDevice.getName());
        
        checkDeviceCompatibility();
        
        setTitleRightIcon(R.drawable.esp_icon_menu_moreoverflow);
        
        checkHelpHandlerInit();
        
        initViews();
    }
    
    private void checkDeviceCompatibility()
    {
        switch (mUser.checkDeviceCompatibility(mIEspDevice))
        {
            case COMPATIBILITY:
                mDeviceCompatibility = true;
                
                checkHelpDeviceCompatibility();
                break;
            case APK_NEED_UPGRADE:
                showUpgradeApkHintDialog();
                mDeviceCompatibility = false;
                break;
            case DEVICE_NEED_UPGRADE:
                showUpgradeDeviceHintDialog();
                mDeviceCompatibility = false;
                
                checkHelpDeviceNeedUpgrade();
                break;
        }
    }
    
    private void initViews()
    {
        setContentView(R.layout.device_ui_container);
        
        mPager = (EspViewPager)findViewById(R.id.device_ui_pager);
        mPager.setInterceptTouchEvent(mIEspDevice.getIsMeshDevice());
        
        mMeshView = getLayoutInflater().inflate(R.layout.device_mesh_children_list, null);
        if (mIEspDevice.getIsMeshDevice())
        {
            initTreeView();
            
            mSwapView = new ImageView(this);
            ViewGroup.LayoutParams lp =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mSwapView.setLayoutParams(lp);
            mSwapView.setImageResource(R.drawable.esp_icon_swap);
            mSwapView.setBackgroundResource(R.drawable.esp_activity_icon_background);
            mSwapView.setScaleType(ScaleType.CENTER);
            mSwapView.setOnClickListener(mSwapListener);
            setTitleContentView(mSwapView);
        }
        
        if (mIEspDevice.getDeviceType() == EspDeviceType.ROOT) // hide mesh child control
        {
            setTitleContentView(null);
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
        List<IEspDeviceTreeElement> childList;
        if (mIEspDevice.getDeviceState().isStateLocal())
        {
            childList = getLocalTreeElementList();
        }
        else
        {
            childList = getInternetTreeElementList();
        }
        mTreeView.initData(this, childList);
        mTreeView.setLastLevelItemClickCallBack(mTreeViewItemClickListener);
    }
    
    private List<IEspDeviceTreeElement> getTreeElementList(IEspDeviceState state)
    {
        List<IEspDevice> userDevices = new ArrayList<IEspDevice>();
        userDevices.addAll(mUser.getAllDeviceList());
        
        // create list with root device
        List<List<IEspDevice>> lists = new ArrayList<List<IEspDevice>>();
        // add root device in lists[x][0]
        for (IEspDevice device : userDevices)
        {
            if (state.isStateInternet())
            {
                if (device.getRootDeviceId() == device.getId() && device.getDeviceState().isStateInternet())
                {
                    List<IEspDevice> childDevices = new ArrayList<IEspDevice>();
                    childDevices.add(device);
                    
                    lists.add(childDevices);
                }
            }
            else if (state.isStateLocal())
            {
                if (device.getIsMeshDevice() && device.getDeviceState().isStateLocal()
                    && device.getBssid().equals(device.getRootDeviceBssid()))
                {
                    List<IEspDevice> childDevices = new ArrayList<IEspDevice>();
                    childDevices.add(device);
                    
                    lists.add(childDevices);
                }
            }
        }
        
        // Add device in it's root device list
        for (IEspDevice device : userDevices)
        {
            // Check state
            if (!EspDeviceState.checkValidWithNecessaryStates(device.getDeviceState(), state))
            {
                continue;
            }
            
            // add devices into their root device(lists[x][0])
            for (List<IEspDevice> list : lists)
            {
                IEspDevice rootDevice = list.get(0);
                // Not device itself and same rootDeviceId
                // Internet device use rootDeviceId
                if (state.isStateInternet())
                {
                    if (rootDevice.getId() != device.getId() && rootDevice.getId() == device.getRootDeviceId())
                    {
                        list.add(device);
                        break;
                    }
                }
                // Local device use rootDeviceBssid
                else if (state.isStateLocal())
                {
                    if (rootDevice.getId() != device.getId()
                        && rootDevice.getBssid().equals(device.getRootDeviceBssid()))
                    {
                        list.add(device);
                        break;
                    }
                }
            }
        }
        
        List<IEspDeviceTreeElement> childList = new ArrayList<IEspDeviceTreeElement>();
        if (mIEspDevice instanceof IEspDeviceRoot)
        {
            // Virtual root device, show all mesh devices
            for (List<IEspDevice> list : lists)
            {
                IEspDevice rootDevice = list.get(0);
                // add root list into result
                childList.addAll(rootDevice.getDeviceTreeElementList(list));
            }
        }
        else
        {
            // Real device, only show it's child mesh devices
            for (List<IEspDevice> list : lists)
            {
                for (IEspDevice device : list)
                {
                    if (device.equals(mIEspDevice))
                    {
                        childList.addAll(device.getDeviceTreeElementList(list));
                        return childList;
                    }
                }
            }
        }
        
        return childList;
    }
    
    private List<IEspDeviceTreeElement> getLocalTreeElementList()
    {
        return getTreeElementList(EspDeviceState.LOCAL);
    }
    
    private List<IEspDeviceTreeElement> getInternetTreeElementList()
    {
        return getTreeElementList(EspDeviceState.INTERNET);
    }
    
    private LastLevelItemClickListener mTreeViewItemClickListener = new LastLevelItemClickListener()
    {
        
        @Override
        public void onLastLevelItemClick(IEspDeviceTreeElement element, int position)
        {
            IEspDevice device = element.getCurrentDevice();
            switch (device.getDeviceType())
            {
                case PLUG:
                case LIGHT:
                case REMOTE:
                    new DeviceDialogBuilder(DeviceActivityAbs.this, device).show();
                    break;
                
                case FLAMMABLE:
                case HUMITURE:
                case VOLTAGE:
                case ROOT:
                case NEW:
                case PLUGS:
                    break;
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
    protected void onTitleRightIconClick()
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
        AlertDialog dialog = builder.show();
        
        checkHelpShowHintDialog(dialog);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
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
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
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
        
        checkHelpOnPreOptionMenu(menu);
        
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
                checkHelpOnSelectUpgradeLocal();
                
                mUser.doActionUpgradeLocal(mIEspDevice);
                finish();
                return true;
            case MENU_ID_UPGRADE_ONLINE:
                checkHelpOnSelectUpgradeOnline();
                
                mUser.doActionUpgradeInternet(mIEspDevice);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showQRCodeDialog(String shareKey)
    {
        final ImageView QRImage = (ImageView)getLayoutInflater().inflate(R.layout.qr_code_image, null);
        final Bitmap QRBmp = CreateQRImageHelper.createQRImage(shareKey, DeviceActivityAbs.this);
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
    
    protected void executePost(final IEspDeviceStatus status, boolean broadcast)
    {
        new DeviceTask(this, broadcast).execute(status);
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
        
        private Boolean mBroadcast;
        
        public DeviceTask(Activity activity)
        {
            mActivity = activity;
            mBroadcast = null;
        }
        
        public DeviceTask(Activity activity, boolean broadcast)
        {
            mActivity = activity;
            mBroadcast = broadcast;
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
                if (mBroadcast == null)
                {
                    return mUser.doActionPostDeviceStatus(mIEspDevice, status);
                }
                else
                {
                    return mUser.doActionPostDeviceStatus(mIEspDevice, status, mBroadcast);
                }
            }
            else
            {
                // execute Get status
                log.debug(mIEspDevice.getName() + " Get status");
                mCommand = COMMAND_GET;
                return mUser.doActionGetDeviceStatus(mIEspDevice);
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
    
    // *************About help below***********//
    @Override
    public void onExitHelpMode()
    {
        setResult(RESULT_EXIT_HELP_MODE);
        finish();
    }
    
    @Override
    public void onHelpUpgradeLocal()
    {
        clearHelpContainer();
        
        HelpStepUpgradeLocal step = HelpStepUpgradeLocal.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case UPGRADING:
                highlightHelpView(findViewById(R.id.right_icon));
                setHelpHintMessage(R.string.esp_help_upgrade_local_click_upgrade_msg);
                break;
            default:
                break;
        }
    }
    
    @Override
    public void onHelpUpgradeOnline()
    {
        clearHelpContainer();
        
        HelpStepUpgradeOnline step = HelpStepUpgradeOnline.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case UPGRADING:
                highlightHelpView(findViewById(R.id.right_icon));
                setHelpHintMessage(R.string.esp_help_upgrade_online_click_upgrade_msg);
                break;
            default:
                break;
        }
    }
    
    private static class HelpHandler extends Handler
    {
        
        private WeakReference<DeviceActivityAbs> mActivity;
        
        public HelpHandler(DeviceActivityAbs activity)
        {
            mActivity = new WeakReference<DeviceActivityAbs>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            DeviceActivityAbs activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
            
            switch (msg.what)
            {
                case RESULT_HELP_UPGRADE_LOCAL:
                    activity.onHelpUpgradeLocal();
                    break;
                case RESULT_HELP_UPGRADE_ONLINE:
                    activity.onHelpUpgradeOnline();
                    break;
            }
        }
    }
    
    private void checkHelpHandlerInit()
    {
        mHelpHandler = new HelpHandler(this);
        if (mHelpMachine.isHelpModeUpgradeLocal())
        {
            mHelpHandler.sendEmptyMessageDelayed(RESULT_HELP_UPGRADE_LOCAL, 300);
        }
        else if (mHelpMachine.isHelpModeUpgradeOnline())
        {
            mHelpHandler.sendEmptyMessageDelayed(RESULT_HELP_UPGRADE_ONLINE, 300);
        }
    }
    
    private void checkHelpDeviceCompatibility()
    {
        if (mHelpMachine.isHelpModeUpgradeLocal() || mHelpMachine.isHelpModeUpgradeOnline())
        {
            mHelpMachine.transformState(true);
        }
    }
    
    private void checkHelpDeviceNeedUpgrade()
    {
        if (mHelpMachine.isHelpModeUpgradeLocal() || mHelpMachine.isHelpModeUpgradeOnline())
        {
            mHelpMachine.exit();
            setResult(RESULT_EXIT_HELP_MODE);
        }
    }
    
    private void checkHelpShowHintDialog(AlertDialog dialog)
    {
        if (mHelpMachine.isHelpModeUpgradeLocal())
        {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
        }
        else if (mHelpMachine.isHelpModeUpgradeOnline())
        {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }
    
    private void checkHelpOnPreOptionMenu(Menu menu)
    {
        if (mHelpMachine.isHelpModeUpgradeLocal())
        {
            setHelpMenuItem(menu, MENU_ID_UPGRADE_LOCAL);
        }
        else if (mHelpMachine.isHelpModeUpgradeOnline())
        {
            setHelpMenuItem(menu, MENU_ID_UPGRADE_ONLINE);
        }
    }
    
    private void setHelpMenuItem(Menu menu, int itemId)
    {
        for (int i = 0; i < menu.size(); i++)
        {
            MenuItem item = menu.getItem(i);
            if (item.getItemId() != itemId)
            {
                item.setEnabled(false);
            }
        }
    }
    
    private void checkHelpOnSelectUpgradeLocal()
    {
        if (mHelpMachine.isHelpModeUpgradeLocal())
        {
            mHelpMachine.transformState(true);
            setResult(RESULT_HELP_UPGRADE_LOCAL);
        }
    }
    
    private void checkHelpOnSelectUpgradeOnline()
    {
        if (mHelpMachine.isHelpModeUpgradeOnline())
        {
            mHelpMachine.transformState(true);
            setResult(RESULT_HELP_UPGRADE_ONLINE);
        }
    }
    // *************About help up***********//
}
