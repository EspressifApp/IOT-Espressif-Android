package com.espressif.iot.ui.settings;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.base.net.rest.EspHttpDownloadUtil.ProgressUpdateListener;
import com.espressif.iot.log.LogConfigurator;
import com.espressif.iot.log.ReadLogTask;
import com.espressif.iot.type.upgrade.EspUpgradeApkResult;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.ui.configure.DeviceConfigureActivity;
import com.espressif.iot.ui.main.LoginTask;
import com.espressif.iot.ui.main.RegisterActivity;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener
{
    private final Logger log = Logger.getLogger(getClass());
    
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_ACCOUNT_REGISTER = "account_register";
    private static final String KEY_ACCOUNT_AUTO_LOGIN = "account_auto_login";
    private static final String KEY_AUTO_REFRESH_DEVICE = "device_auto_refresh";
    private static final String KEY_AUTO_CONFIGURE_DEVICE = "device_auto_configure";
    private static final String KEY_VERSION_NAME = "version_name";
    private static final String KEY_VERSION_UPGRADE = "version_upgrade";
    private static final String KEY_VERSION_LOG = "version_log";
    private static final String KEY_STORE_LOG = "store_log";
    private static final String KEY_READ_LOG = "read_log";
    private static final String KEY_CLEAR_LOG = "clear_log";
    
    private static final String DEFAULT_VERSION_LOG_URL = "file:///android_asset/html/en_us/update.html";
    /**
     * The url for WebView
     */
    private static final String VERSION_LOG_URL = "file:///android_asset/html/%locale/update.html";
    /**
     * The path for AssetManager
     */
    private static final String VERSION_LOG_PATH = "html/%locale/update.html";
    
    private static final int REQUEST_REGISTER = 1000;
    
    private Preference mAccountPre;
    private Preference mAccountRegisterPre;
    private CheckBoxPreference mAutoLoginPre;
    private ListPreference mAutoRefreshDevicePre;
    private ListPreference mAutoConfigureDevicePre;
    private Preference mVersionNamePre;
    private Preference mVersionUpgradePre;
    private Preference mVersionLogPre;
    private CheckBoxPreference mStoreLogPre;
    private Preference mReadLogPre;
    private Preference mClearLogPre;
    
    private IEspUser mUser;
    
    private UpgradeApkTask mUpgradeApkTask;
    
    private AlertDialog mLogDialog;
    private List<String> mLogList;
    private ArrayAdapter<String> mLogAdapter;
    
    private SharedPreferences mShared;
    
    private LocalBroadcastManager mBroadcastManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settings);
        
        mUser = BEspUser.getBuilder().getInstance();
        mShared = getActivity().getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        mBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        
        // About Account
        mAccountPre = findPreference(KEY_ACCOUNT);
        mAccountRegisterPre = findPreference(KEY_ACCOUNT_REGISTER);
        if (mUser.isLogin())
        {
            String userEmail = mUser.getUserEmail();
            mAccountPre.setTitle(userEmail);
            
            getPreferenceScreen().removePreference(mAccountRegisterPre);
        }
        else
        {
            mAccountPre.setTitle(R.string.esp_settings_account_not_login);
            mAccountPre.setSummary(R.string.esp_settings_account_not_login_summary);
        }
        
        mAutoLoginPre = (CheckBoxPreference)findPreference(KEY_ACCOUNT_AUTO_LOGIN);
        mAutoLoginPre.setChecked(mUser.isAutoLogin());
        mAutoLoginPre.setOnPreferenceChangeListener(this);
        
        // About Device
        mAutoRefreshDevicePre = (ListPreference)findPreference(KEY_AUTO_REFRESH_DEVICE);
        String autoRefreshTime = "" + mShared.getLong(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_REFRESH, 0);
        mAutoRefreshDevicePre.setValue(autoRefreshTime);
        mAutoRefreshDevicePre.setSummary(mAutoRefreshDevicePre.getEntry());
        mAutoRefreshDevicePre.setOnPreferenceChangeListener(this);
        
        mAutoConfigureDevicePre = (ListPreference)findPreference(KEY_AUTO_CONFIGURE_DEVICE);
        int defaultAutoConfigureValue = DeviceConfigureActivity.DEFAULT_AUTO_CONFIGRUE_VALUE;
        String autoConfigureValue =
            "" + mShared.getInt(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_CONFIGURE, defaultAutoConfigureValue);
        mAutoConfigureDevicePre.setValue(autoConfigureValue);
        mAutoConfigureDevicePre.setSummary(mAutoConfigureDevicePre.getEntry());
        mAutoConfigureDevicePre.setOnPreferenceChangeListener(this);
        
        // About Version
        mVersionNamePre = findPreference(KEY_VERSION_NAME);
        String versionName = EspApplication.sharedInstance().getVersionName();
        mVersionNamePre.setSummary(versionName);
        
        mVersionUpgradePre = findPreference(KEY_VERSION_UPGRADE);
        if (mVersionUpgradePre != null && EspApplication.GOOGLE_PALY_VERSION)
        {
            getPreferenceScreen().removePreference(mVersionUpgradePre);
        }
        
        mVersionLogPre = findPreference(KEY_VERSION_LOG);
        
        // About DEBUG
        mStoreLogPre = (CheckBoxPreference)findPreference(KEY_STORE_LOG);
        mStoreLogPre.setOnPreferenceChangeListener(this);
        mReadLogPre = findPreference(KEY_READ_LOG);
        boolean store = mShared.getBoolean(EspStrings.Key.SETTINGS_KEY_STORE_LOG, false);
        mStoreLogPre.setChecked(store);
        mClearLogPre = findPreference(KEY_CLEAR_LOG);
        
        mLogList = new ArrayList<String>();
        mLogAdapter =
            new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mLogList);
        mLogDialog = new AlertDialog.Builder(getActivity()).setAdapter(mLogAdapter, null).create();
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        
        if (mUpgradeApkTask != null)
        {
            mUpgradeApkTask.cancel(true);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_REGISTER)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                String email = data.getStringExtra(EspStrings.Key.REGISTER_NAME_EMAIL);
                String password = data.getStringExtra(EspStrings.Key.REGISTER_NAME_PASSWORD);
                
                login(email, password);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
        if (preference == mVersionUpgradePre)
        {
            updateApk();
            return true;
        }
        else if (preference == mVersionLogPre)
        {
            showLogDialog();
            return true;
        }
        else if (preference == mReadLogPre)
        {
            readLog();
            return true;
        }
        else if (preference == mClearLogPre)
        {
            clearLog();
            return true;
        }
        else if (preference == mAccountPre)
        {
            if (!mUser.isLogin())
            {
                showLoginDialog();
                return true;
            }
        }
        else if (preference == mAccountRegisterPre)
        {
            Intent i = new Intent(getActivity(), RegisterActivity.class);
            startActivityForResult(i, REQUEST_REGISTER);
            return true;
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        if (preference == mAutoLoginPre)
        {
            boolean autoLogin = (Boolean)newValue;
            mUser.saveUserInfoInDB(false, autoLogin);
            return true;
        }
        else if (preference == mAutoRefreshDevicePre)
        {
            String time = newValue.toString();
            mAutoRefreshDevicePre.setValue(time);
            mAutoRefreshDevicePre.setSummary(mAutoRefreshDevicePre.getEntry());
            mShared.edit().putLong(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_REFRESH, Long.parseLong(time)).commit();
            return true;
        }
        else if (preference == mAutoConfigureDevicePre)
        {
            String value = newValue.toString();
            mAutoConfigureDevicePre.setValue(value);
            mAutoConfigureDevicePre.setSummary(mAutoConfigureDevicePre.getEntry());
            mShared.edit().putInt(EspStrings.Key.SETTINGS_KEY_DEVICE_AUTO_CONFIGURE, Integer.parseInt(value)).commit();
            return true;
        }
        else if (preference == mStoreLogPre)
        {
            onStoreLogChanged((Boolean) newValue);
            return true;
        }
        
        return false;
    }
    
    private void updateApk()
    {
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
        {
            // Show dialog to hint using mobile data now
            new AlertDialog.Builder(getActivity()).setTitle(R.string.esp_upgrade_apk_mobile_data_title)
                .setMessage(R.string.esp_upgrade_apk_mobile_data_msg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        executeUpgradeApkTask();
                    }
                })
                .show();
        }
        else
        {
            executeUpgradeApkTask();
        }
    }
    
    private void executeUpgradeApkTask()
    {
        mUpgradeApkTask = new UpgradeApkTask();
        mUpgradeApkTask.execute();
    }
    
    private class UpgradeApkTask extends AsyncTask<Void, Integer, EspUpgradeApkResult>
    {
        @Override
        protected void onPreExecute()
        {
            mVersionUpgradePre.setEnabled(false);
        }
        
        @Override
        protected EspUpgradeApkResult doInBackground(Void... arg0)
        {
            return EspBaseApiUtil.upgradeApk(mUpdateListener);
        }
        
        @Override
        protected void onPostExecute(EspUpgradeApkResult result)
        {
            mVersionUpgradePre.setEnabled(true);
            
            switch (result)
            {
                case UPGRADE_COMPLETE:
                    Toast.makeText(getActivity(), R.string.esp_upgrade_apk_status_complete_toast, Toast.LENGTH_LONG)
                        .show();
                    mVersionUpgradePre.setSummary(R.string.esp_upgrade_apk_status_complete);
                    break;
                case DOWNLOAD_FAILED:
                    mVersionUpgradePre.setSummary(R.string.esp_upgrade_apk_status_download_failed);
                    break;
                case LOWER_VERSION:
                    mVersionUpgradePre.setSummary(R.string.esp_upgrade_apk_status_lower_version);
                    break;
                case NOT_FOUND:
                    mVersionUpgradePre.setSummary(R.string.esp_upgrade_apk_status_not_found);
                    break;
            }
        }
        
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            int percent = values[0];
            mVersionUpgradePre.setSummary(getString(R.string.esp_upgrade_apk_downloading, percent));
        }
        
        /**
         * Update download progress
         */
        private ProgressUpdateListener mUpdateListener = new ProgressUpdateListener()
        {
            
            @Override
            public void onProgress(long downloadSize, double percent)
            {
                int per = (int)(percent * 100);
                publishProgress(per);
            }
            
        };
    }
    
    /**
     * Show update log
     */
    private void showLogDialog()
    {
        /*
         * check for the full language + country resource, if not there, check for the only language resource, if not
         * there again, use default(en_us) language log
         */
        boolean isLogFileExist;
        Locale locale = Locale.getDefault();
        String languageCode = locale.getLanguage().toLowerCase(Locale.US);
        String countryCode = locale.getCountry().toLowerCase(Locale.US);
        
        String folderName = languageCode + "_" + countryCode;
        String path = VERSION_LOG_PATH.replace("%locale", folderName);
        // check full language + country resource
        isLogFileExist = isAssetFileExist(path);
        
        if (!isLogFileExist)
        {
            folderName = languageCode;
            path = VERSION_LOG_PATH.replace("%locale", folderName);
            
            // check the only language resource
            isLogFileExist = isAssetFileExist(path);
        }
        
        String url;
        if (isLogFileExist)
        {
            url = VERSION_LOG_URL.replaceAll("%locale", folderName);
        }
        else
        {
            url = DEFAULT_VERSION_LOG_URL;
        }
        
        WebView webview = new WebView(getActivity());
        webview.loadUrl(url);
        
        new AlertDialog.Builder(getActivity()).setView(webview).show();
    }
    
    /**
     * Check whether the log file exist
     * @param path
     * @return
     */
    private boolean isAssetFileExist(String path)
    {
        boolean result = true;
        
        final AssetManager am = getActivity().getAssets();
        InputStream is = null;
        try
        {
            is = am.open(path);
        }
        catch (Exception ignored)
        {
            result = false;
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }
        
        return result;
    }
    
    private void readLog()
    {
        final ReadLogTask task = new ReadLogTask()
        {
            @Override
            protected void onPreExecute()
            {
                mLogList.clear();
                mLogAdapter.notifyDataSetChanged();
                mLogDialog.show();
            }
            
            @Override
            protected void onProgressUpdate(String... values)
            {
                mLogList.add("\n" + values[0] + "\n");
                mLogAdapter.notifyDataSetChanged();
                mLogDialog.getListView().setSelection(mLogList.size() - 1);
            }
            
            @Override
            protected void onPostExecute(Boolean result)
            {
                if (mLogList.isEmpty())
                {
                    mLogList.add(getActivity().getString(R.string.esp_settings_debug_read_log_no_log));
                    mLogAdapter.notifyDataSetChanged();
                }
            }
        };
        mLogDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                task.cancel(true);
            }
        });
        task.execute();
    }
    
    private void onStoreLogChanged(Boolean store)
    {
        mShared.edit().putBoolean(EspStrings.Key.SETTINGS_KEY_STORE_LOG, store).commit();
        Logger root = Logger.getRootLogger();
        if (store)
        {
            root.addAppender(LogConfigurator.createFileAppender());
            log.debug("Open log file store");
        }
        else
        {
            root.removeAppender(LogConfigurator.APPENDER_NAME);
        }
    }
    
    private void clearLog()
    {
        File file = new File(LogConfigurator.DefaultLogFileDirPath);
        if (file.isDirectory())
        {
            File[] logFiles = file.listFiles();
            for (int i = 0; i < logFiles.length; i++)
            {
                logFiles[i].delete();
            }
        }
        else
        {
            log.warn("Delete path is not directry");
        }
    }
    
    private void showLoginDialog()
    {
        View view = getActivity().getLayoutInflater().inflate(R.layout.login_dialog, null);
        final EditText emailEdT = (EditText)view.findViewById(R.id.login_edt_account);
        final EditText pwdEdt = (EditText)view.findViewById(R.id.login_edt_password);
        
        new AlertDialog.Builder(getActivity()).setTitle(R.string.esp_login_login)
            .setView(view)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    String email = emailEdT.getText().toString();
                    String password = pwdEdt.getText().toString();
                    if (!TextUtils.isEmpty(email))
                    {
                        login(email, password);
                    }
                }
            })
            .show();
    }
    
    private void login(final String email, final String password)
    {
        new LoginTask(getActivity(), email, password, mAutoLoginPre.isChecked())
        {
            public void loginResult(EspLoginResult result)
            {
                if (result == EspLoginResult.SUC)
                {
                    mAccountPre.setTitle(email);
                    mAccountPre.setSummary("");
                    
                    Preference registerPre = findPreference(KEY_ACCOUNT_REGISTER);
                    if (registerPre != null)
                    {
                        getPreferenceScreen().removePreference(registerPre);
                    }
                    
                    mBroadcastManager.sendBroadcast(new Intent(EspStrings.Action.LOGIN_NEW_ACCOUNT));
                }
            }
        }.execute();
    }
}
