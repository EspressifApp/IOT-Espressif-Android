package com.espressif.iot.base.application;

import cn.sharesdk.framework.ShareSDK;

import com.espressif.iot.base.net.wifi.WifiAdmin;
import com.espressif.iot.base.threadpool.CachedThreadPool;
import com.espressif.iot.base.time.EspTimeManager;
import com.espressif.iot.db.EspGroupDBManager;
import com.espressif.iot.db.IOTApDBManager;
import com.espressif.iot.db.IOTDeviceDBManager;
import com.espressif.iot.db.IOTDownloadIdValueDBManager;
import com.espressif.iot.db.IOTGenericDataDBManager;
import com.espressif.iot.db.IOTGenericDataDirectoryDBManager;
import com.espressif.iot.db.IOTUserDBManager;
import com.espressif.iot.db.greenrobot.daos.DaoMaster;
import com.espressif.iot.db.greenrobot.daos.DaoMaster.DevOpenHelper;
import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.log.InitLogger;
import com.espressif.iot.util.EspStrings;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;

public class EspApplication extends Application
{
    private static EspApplication instance;
    
    public static EspApplication sharedInstance()
    {
        if (instance == null)
        {
            throw new NullPointerException(
                "EspApplication instance is null, please register in AndroidManifest.xml first");
        }
        return instance;
    }
    
    public static boolean SUPPORT_APK_UPGRADE = true;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        initAsyn();
        initSyn();
    }
    
    public Context getContext()
    {
        return getBaseContext();
    }
    
    public Resources getResources()
    {
        return getBaseContext().getResources();
    }
    
    public String getVersionName()
    {
        String version = "";
        try
        {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
            version = "Not found version";
        }
        return version;
    }
    
    public int getVersionCode()
    {
        int code = 0;
        try
        {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            code = pi.versionCode;
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return code;
    }
    
    public String getEspRootSDPath()
    {
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            path = Environment.getExternalStorageDirectory().toString() + "/Espressif/";
        }
        return path;
    }
    
    public String getContextFilesDirPath()
    {
        return getFilesDir().toString();
    }
    
    private String __formatString(int value)
    {
        String strValue = "";
        byte[] ary = __intToByteArray(value);
        for (int i = ary.length - 1; i >= 0; i--)
        {
            strValue += (ary[i] & 0xFF);
            if (i > 0)
            {
                strValue += ".";
            }
        }
        return strValue;
    }
    
    private byte[] __intToByteArray(int value)
    {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte)((value >>> offset) & 0xFF);
        }
        return b;
    }
    
    public String getGateway()
    {
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wm.getDhcpInfo();
        return __formatString(d.gateway);
    }
    
    private void initSyn()
    {
        // init db
        DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, EspStrings.DB.DB_NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        IOTUserDBManager.init(daoSession);
        IOTDeviceDBManager.init(daoSession);
        EspGroupDBManager.init(daoSession);
        IOTApDBManager.init(daoSession);
        IOTDownloadIdValueDBManager.init(daoSession);
        // data and data directory using seperate session for they maybe take long time
        daoSession = daoMaster.newSession();
        IOTGenericDataDBManager.init(daoSession);
        IOTGenericDataDirectoryDBManager.init(daoSession);
        
        // ShareSDK: third-party login
        ShareSDK.initSDK(this);
    }
    
    private void initAsyn()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                InitLogger.init();
                CachedThreadPool.getInstance();
                WifiAdmin.getInstance();
                EspTimeManager.getInstance().getUTCTimeLong();
            }
        }.start();
    }
}
