package com.espressif.iot.base.upgrade;

import com.espressif.iot.base.net.rest2.EspHttpDownloadUtil.ProgressUpdateListener;
import com.espressif.iot.base.upgrade.apk.UpgradeManagerApkOnline;
import com.espressif.iot.type.upgrade.EspUpgradeApkResult;

public class UpgradeAdministrator
{
    private static UpgradeAdministrator instance;
    
    public static UpgradeAdministrator getInstance()
    {
        if (instance == null)
        {
            instance = new UpgradeAdministrator();
        }
        return instance;
    }
    
    public synchronized EspUpgradeApkResult upgradeApk()
    {
        UpgradeManagerApkOnline apkUpgradeManager = new UpgradeManagerApkOnline();
        return apkUpgradeManager.upgrade();
    }
    
    public synchronized EspUpgradeApkResult upgradeApk(ProgressUpdateListener listener)
    {
        UpgradeManagerApkOnline apkUpgradeManager = new UpgradeManagerApkOnline();
        apkUpgradeManager.setOnProgressUpdateListener(listener);
        return apkUpgradeManager.upgrade();
    }
}
