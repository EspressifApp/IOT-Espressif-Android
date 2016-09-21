package com.espressif.iot.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.net.proxy.EspProxyServerImpl;
import com.espressif.iot.base.net.udp.UdpServer;
import com.espressif.iot.db.EspGroupDBManager;
import com.espressif.iot.esppush.EspPushUtils;
import com.espressif.iot.model.device.cache.EspDeviceCache;
import com.espressif.iot.model.device.statemachine.EspDeviceStateMachineHandler;
import com.espressif.iot.model.group.EspGroupHandler;
import com.espressif.iot.ui.configure.DeviceEspTouchActivity;
import com.espressif.iot.ui.login.LoginActivity;
import com.espressif.iot.ui.main.EspDrawerFragmentBase.NavigationDrawerCallbacks;
import com.espressif.iot.ui.settings.SettingsActivity;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspDefaults;
import com.espressif.iot.util.EspStrings;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.widget.DrawerLayout;

public class EspMainActivity extends Activity implements NavigationDrawerCallbacks {
    private static final Logger log = Logger.getLogger(EspMainActivity.class);

    private IEspUser mUser;

    private EspMainFragment mMainFragment;
    private EspDrawerFragmentLeft mDrawerFragmentLeft;
    private EspDrawerFragmentRight mDrawerFragmentRight;

    public static final int REQUEST_LOGIN = 0x10;
    public static final int REQUEST_ESPTOUCH = 0x11;

    private SharedPreferences mSettingsShared;

    private EspGroupHandler mEspGroupHandler;
    private EspGroupDBManager mEspGroupDBManager;

    private static final int MENU_ID_SYNC_GROUP = 0x01;
    private static final int MENU_ID_SORT_DEVICE = 0x02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_esp_activity);

        mUser = BEspUser.getBuilder().getInstance();
        mSettingsShared = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mEspGroupHandler = EspGroupHandler.getInstance();
        mEspGroupDBManager = EspGroupDBManager.getInstance();

        prepare();

        if (!mUser.isLogin()) {
            Toast.makeText(this, R.string.esp_main_not_login_msg, Toast.LENGTH_LONG).show();
        }

        FragmentManager fm = getFragmentManager();
        DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);

        mDrawerFragmentLeft = (EspDrawerFragmentLeft)fm.findFragmentById(R.id.navigation_drawer_left);
        mDrawerFragmentLeft.setUp(R.id.navigation_drawer_left, dl);
        mDrawerFragmentLeft.checkLoginStatus();

        mDrawerFragmentRight = (EspDrawerFragmentRight)fm.findFragmentById(R.id.navigation_drawer_right);
        mDrawerFragmentRight.setUp(R.id.navigation_drawer_right, dl);

        mMainFragment = new EspMainFragment();
        fm.beginTransaction().replace(R.id.container, mMainFragment).commit();
    }

    private void prepare() {
        // Start local proxy server
        EspProxyServerImpl.getInstance().start();
        // Clear cache list
        EspDeviceCache.getInstance().clear();
        // Call group handler
        mEspGroupHandler.call();
        // Start UDP server
        UdpServer.INSTANCE.open();

        boolean autoLogin = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE)
            .getBoolean(EspStrings.Key.KEY_AUTO_LOGIN, EspDefaults.AUTO_LOGIN);
        
        if (autoLogin) {
            mUser.doActionUserLoginDB();
        } else {
            mUser.doActionUserLoginGuest();
        }
        if (mUser.isLogin()) {
            if (mSettingsShared.getBoolean(EspStrings.Key.SETTINGS_KEY_ESPPUSH, EspDefaults.ESPPUSH_ON)) {
                EspPushUtils.startPushService(this);
            } else {
                EspPushUtils.stopPushService(this);
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(Fragment fragment, int id) {
        if (fragment == mDrawerFragmentLeft) {
            switch (id) {
                case R.id.drawer_item_login:
                    startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
                    break;
                case R.id.drawer_item_adddevice:
                    startActivityForResult(new Intent(this, DeviceEspTouchActivity.class), REQUEST_ESPTOUCH);
                    break;
                case R.id.drawer_item_settings:
                    startActivity(new Intent(this, SettingsActivity.class));
                    break;
                case R.id.drawer_item_help:
                    // TODO
                    break;
                case R.id.drawer_item_logout:
                    logout();
                    break;
            }
        } else if (fragment == mDrawerFragmentRight) {
            if (id < 0) {
                mMainFragment.filterDeviceUsable(mDrawerFragmentRight.isFilterDeviceUsable());
            } else {
                mMainFragment.filterDeviceType(mDrawerFragmentRight.getCheckedDeviceType());
            }
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            log.debug("Login result = " + resultCode);
            if (resultCode == RESULT_OK) {
                onLogin();
            }
        } else if (requestCode == REQUEST_ESPTOUCH) {
            log.debug("ESPTouch result = " + resultCode);
            mMainFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (mMainFragment.onBackPressed()) {
            return;
        }

        new AlertDialog.Builder(this).setMessage(R.string.esp_main_exit_message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new FinishTask(EspMainActivity.this).execute();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mDrawerFragmentLeft.isDrawerOpen()) {
            boolean canSyncLocal = mUser.isLogin() && mEspGroupDBManager.getUserGroup(null).size() > 0;
            menu.add(Menu.NONE, MENU_ID_SYNC_GROUP, 0, R.string.esp_scene_menu_sync_local).setEnabled(canSyncLocal);

            menu.add(Menu.NONE, MENU_ID_SORT_DEVICE, 0, R.string.esp_main_menu_sort_device);

            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case MENU_ID_SYNC_GROUP:
                mEspGroupDBManager.updateLocalGroupUserKey(mUser.getUserKey());
                mMainFragment.updateGroupList();
                mEspGroupHandler.call();
                return true;
            case MENU_ID_SORT_DEVICE:
                mMainFragment.showSortItems();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FinishTask extends AsyncTask<Void, Void, Void> {
        private EspMainActivity mActivity;
        private ProgressDialog mDialog;

        public FinishTask(EspMainActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(mActivity.getString(R.string.esp_main_exiting));
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mMainFragment.finish();
            EspBaseApiUtil.cancelAllTask();
            EspDeviceStateMachineHandler.getInstance().cancelAllTasks();
            EspGroupHandler.getInstance().finish();
            EspProxyServerImpl.getInstance().stop();
            UdpServer.INSTANCE.close();
            mUser.clearUserDeviceLists();
            EspUpgradeHelper.INSTANCE.clear();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
            mActivity.finish();
        }
    }

    private void onLogin() {
        mSettingsShared.edit().putBoolean(EspStrings.Key.KEY_AUTO_LOGIN, true).apply();
        invalidateOptionsMenu();

        mDrawerFragmentLeft.checkLoginStatus();
        mMainFragment.updateGroupList();
        getActionBar().setDisplayShowCustomEnabled(true);
        mMainFragment.markOnStartRefresh();

        if (mSettingsShared.getBoolean(EspStrings.Key.SETTINGS_KEY_ESPPUSH, EspDefaults.ESPPUSH_ON)) {
            EspPushUtils.startPushService(this);
        } else {
            EspPushUtils.stopPushService(this);
        }
    }

    private void logout() {
        mUser.doActionUserLogout();
        mSettingsShared.edit().putBoolean(EspStrings.Key.KEY_AUTO_LOGIN, false).apply();
        EspPushUtils.stopPushService(this);
        EspUpgradeHelper.INSTANCE.clear();

        mDrawerFragmentLeft.checkLoginStatus();
        mMainFragment.updateGroupList();
        mMainFragment.updateDeviceList();
        getActionBar().setDisplayShowCustomEnabled(true);
        mMainFragment.refresh();
    }
}
