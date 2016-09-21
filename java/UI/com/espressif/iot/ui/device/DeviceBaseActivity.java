package com.espressif.iot.ui.device;

import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceTypeResult;
import com.espressif.iot.ui.configure.EspButtonConfigureActivity;
import com.espressif.iot.ui.device.timer.DeviceTimersActivity;
import com.espressif.iot.ui.device.trigger.DeviceTriggerActivity;
import com.espressif.iot.ui.main.EspUpgradeHelper;
import com.espressif.iot.ui.settings.SettingsActivity;
import com.espressif.iot.ui.task.GenerateShareKeyTask;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

public abstract class DeviceBaseActivity extends Activity {
    private static final Logger log = Logger.getLogger(DeviceBaseActivity.class);

    protected IEspDevice mDevice;
    protected IEspUser mUser;
    protected EspUpgradeHelper mEspUpgradeHelper;

    protected static final int MENU_ID_SHARE_DEVICE = 0x1000;
    protected static final int MENU_ID_DEVICE_TIMERS = 0x1001;
    protected static final int MENU_ID_UPGRADE_LOCAL = 0x1002;
    protected static final int MENU_ID_UPGRADE_ONLINE = 0x1003;
    protected static final int MENU_ID_ESPBUTTON_CONFIGURE = 0x1004;
    protected static final int MENU_ID_TRIGGER = 0x1005;

    private boolean mDeviceCompatibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mUser = BEspUser.getBuilder().getInstance();
        mEspUpgradeHelper = EspUpgradeHelper.INSTANCE;
        mDevice = getDevice(intent);

        checkDeviceCompatibility();
    }

    private IEspDevice getDevice(Intent intent) {
        IEspDevice device = null;
        String deviceKey = intent.getStringExtra(EspStrings.Key.DEVICE_KEY_KEY);
        if (!TextUtils.isEmpty(deviceKey)) {
            device = mUser.getUserDevice(deviceKey);
        }

        if (device == null) {
            IOTAddress iotAddress = intent.getParcelableExtra(EspStrings.Key.DEVICE_KEY_IOTADDRESS);
            if (iotAddress != null) {
                device = BEspDevice.getInstance().createStaDevice(iotAddress);
            }
        }

        if (device == null) {
            String deviceTypeStr = intent.getStringExtra(EspStrings.Key.DEVICE_KEY_TYPE);
            if (!TextUtils.isEmpty(deviceTypeStr)) {
                EspDeviceType deviceType = EspDeviceType.getEspTypeEnumByString(deviceTypeStr);
                IEspDeviceArray deviceArray = BEspDevice.createDeviceArray(deviceType);
                String[] deviceKeys = intent.getStringArrayExtra(EspStrings.Key.DEVICE_KEY_KEY_ARRAY);
                List<IEspDevice> devices = mUser.getUserDevices(deviceKeys);
                for (IEspDevice d : devices) {
                    deviceArray.addDevice(d);
                }

                device = deviceArray;
            }
        }

        return device;
    }

    private void initBaseMenuItems(Menu menu) {
        // Share device menu
        if (mDevice.getIsOwner()) {
            menu.add(Menu.NONE, MENU_ID_SHARE_DEVICE, 0, R.string.esp_device_menu_share)
                .setIcon(R.drawable.esp_menu_icon_share);
        }

        // Timer menu
        if (mDevice.isSupportTimer()) {
            menu.add(Menu.NONE, MENU_ID_DEVICE_TIMERS, 0, R.string.esp_device_menu_timer);
        }

        // Trigger menu
        if (mDevice.isSupportTrigger()) {
            menu.add(Menu.NONE, MENU_ID_TRIGGER, 0, R.string.esp_device_menu_trigger);
        }

        // Upgrade menu
        boolean upgradeLocalEnable = true;
        boolean upgradeOnlineEnable = true;
        switch (mUser.getDeviceUpgradeTypeResult(mDevice)) {
            case SUPPORT_ONLINE_LOCAL:
                break;
            case SUPPORT_LOCAL_ONLY:
                upgradeOnlineEnable = false;
                break;
            case SUPPORT_ONLINE_ONLY:
                upgradeLocalEnable = false;
                break;
            case CURRENT_ROM_INVALID:
            case CURRENT_ROM_IS_NEWEST:
            case DEVICE_TYPE_INCONSISTENT:
            case LATEST_ROM_INVALID:
            case NOT_SUPPORT_UPGRADE:
                upgradeLocalEnable = false;
                upgradeOnlineEnable = false;
                break;
        }
        if (!mDevice.getDeviceState().isStateLocal()) {
            upgradeLocalEnable = false;
        }
        if (!mDevice.getDeviceState().isStateInternet()) {
            upgradeOnlineEnable = false;
        }
        if (mDevice.getDeviceState().isStateUpgradingLocal() || mDevice.getDeviceState().isStateUpgradingInternet()) {
            upgradeLocalEnable = false;
            upgradeOnlineEnable = false;
        }
        menu.add(Menu.NONE, MENU_ID_UPGRADE_LOCAL, 0, R.string.esp_device_menu_upgrade_local)
            .setEnabled(upgradeLocalEnable);
        menu.add(Menu.NONE, MENU_ID_UPGRADE_ONLINE, 0, R.string.esp_device_menu_upgrade_online)
            .setEnabled(upgradeOnlineEnable);

        // EspButton menu
        if (mDevice.getIsMeshDevice() && mDevice.getDeviceState().isStateLocal()) {
            menu.add(Menu.NONE, MENU_ID_ESPBUTTON_CONFIGURE, 0, R.string.esp_device_menu_espbutton_configure);
        }
    }

    protected boolean onSelectMenuItem(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_SHARE_DEVICE:
                new GenerateShareKeyTask(this).execute(mDevice.getKey());
                return true;
            case MENU_ID_DEVICE_TIMERS:
                Intent timerIntent = new Intent(this, DeviceTimersActivity.class);
                timerIntent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, mDevice.getKey());
                startActivity(timerIntent);
                return true;
            case MENU_ID_UPGRADE_LOCAL:
                mEspUpgradeHelper.addDevice(mDevice, EspUpgradeHelper.UpgradeDevice.UPGRADE_TYPE_LOCAL);
                mEspUpgradeHelper.checkUpgradingDevices();
                finish();
                return true;
            case MENU_ID_UPGRADE_ONLINE:
                mEspUpgradeHelper.addDevice(mDevice, EspUpgradeHelper.UpgradeDevice.UPGRADE_TYPE_INTERNET);
                mEspUpgradeHelper.checkUpgradingDevices();
                finish();
                return true;
            case MENU_ID_ESPBUTTON_CONFIGURE:
                Intent configureIntent = new Intent(this, EspButtonConfigureActivity.class);
                String[] deviceKeys = new String[] {mDevice.getKey()};
                configureIntent.putExtra(EspStrings.Key.KEY_ESPBUTTON_DEVICE_KEYS, deviceKeys);
                startActivity(configureIntent);
                return true;
            case MENU_ID_TRIGGER:
                Intent triggerIntent = new Intent(this, DeviceTriggerActivity.class);
                triggerIntent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, mDevice.getKey());
                startActivity(triggerIntent);
                return true;
        }

        return false;
    }

    protected void onCreateMenuItems(Menu menu) {
    }

    public void createDeviceMenuItems(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        Menu menu = popupMenu.getMenu();
        initBaseMenuItems(menu);
        onCreateMenuItems(menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onSelectMenuItem(item);
            }
        });
        popupMenu.show();
    }

    /**
     * The device is normal device or IEspDeviceArray
     * 
     * @return
     */
    protected boolean isDeviceArray() {
        return mDevice instanceof IEspDeviceArray;
    }

    /**
     * The device is compatibility or not
     */
    protected boolean isDeviceCompatibility() {
        return mDeviceCompatibility;
    }

    private void checkDeviceCompatibility() {
        switch (mUser.checkDeviceCompatibility(mDevice)) {
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

    /**
     * The application APK need upgrade, show the hint dialog
     */
    private void showUpgradeApkHintDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.esp_device_dialog_upgrade_apk_message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Activity activity = DeviceBaseActivity.this;
                    activity.finish();
                    activity.startActivity(new Intent(activity, SettingsActivity.class));
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            })
            .show();
    }

    /**
     * The device need upgrade, show the hint dialog
     */
    private void showUpgradeDeviceHintDialog() {
        AlertDialog.Builder builder =
            new AlertDialog.Builder(this).setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    log.debug("Cancel upgrade device hint dialog");
                    finish();
                }
            });

        IEspDeviceState state = mDevice.getDeviceState();
        if (state.isStateUpgradingLocal()) {
            /*
             * The device is upgrading local
             */
            builder.setMessage(R.string.esp_device_dialog_upgrading_local_message);
        } else if (state.isStateUpgradingInternet()) {
            /*
             * The device is upgrading online
             */
            builder.setMessage(R.string.esp_device_dialog_upgrading_online_message);
        } else {
            /*
             * Check the state and show the upgrade select option
             */
            builder.setMessage(R.string.esp_device_dialog_upgrade_device_message);
            DialogInterface.OnClickListener upgradeListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE: // upgrade local
                            log.debug("Click upgrade device hint dialog local button");
                            mEspUpgradeHelper.addDevice(mDevice, EspUpgradeHelper.UpgradeDevice.UPGRADE_TYPE_LOCAL);
                            mEspUpgradeHelper.checkUpgradingDevices();
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEUTRAL: // upgrade online
                            log.debug("Click upgrade device hint dialog online button");
                            mEspUpgradeHelper.addDevice(mDevice, EspUpgradeHelper.UpgradeDevice.UPGRADE_TYPE_INTERNET);
                            mEspUpgradeHelper.checkUpgradingDevices();
                            finish();
                            break;
                    }
                }
            };
            EspUpgradeDeviceTypeResult upgradeType = mUser.getDeviceUpgradeTypeResult(mDevice);
            log.info("mIEspDevice state = " + state + " ||| " + "Upgrade type = " + upgradeType);
            switch (upgradeType) {
                case SUPPORT_ONLINE_LOCAL:
                    if (state.isStateLocal()) {
                        builder.setPositiveButton(R.string.esp_device_dialog_upgrade_device_local, upgradeListener);
                    }
                    if (state.isStateInternet()) {
                        builder.setNeutralButton(R.string.esp_device_dialog_upgrade_device_online, upgradeListener);
                    }
                    break;
                case SUPPORT_LOCAL_ONLY:
                    if (state.isStateLocal()) {
                        builder.setPositiveButton(R.string.esp_device_dialog_upgrade_device_local, upgradeListener);
                    }
                    break;
                case SUPPORT_ONLINE_ONLY:
                    if (state.isStateInternet()) {
                        builder.setNeutralButton(R.string.esp_device_dialog_upgrade_device_online, upgradeListener);
                    }
                    break;
                default:
                    break;
            }
        }
        builder.show();
    }
}
