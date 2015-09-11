package com.espressif.iot.base.upgrade.apk;

import java.io.File;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.base.net.rest2.EspHttpDownloadUtil.ProgressUpdateListener;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.type.upgrade.EspUpgradeApkResult;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

public class UpgradeManagerApkOnline
{
    
    private final Logger log = Logger.getLogger(UpgradeManagerApkOnline.class);
    
    // master key:
    // "32dd6ab79e6050f1c0477f2f1c29e2169cf6e9b5"
    // owner key:
    // "ecf9b29755f701afc315fd0d709b2fa983d20094"
    private final static String KEY_MASTER = "32dd6ab79e6050f1c0477f2f1c29e2169cf6e9b5";
    
    private final static String Authorization = "Authorization";
    
    private final static String TOKEN = "token";
    
    private final static String URL_NEWEST_APK_INFO = "https://iot.espressif.cn/v1/device/rom/";
    
    private final static String KEY_STATUS = "status";
    
    private final static String KEY_ROMS = "productRoms";
    
    private final static String KEY_NEWEST_VERSION = "recommended_rom_version";
    
    private final static String KEY_APK_VERSION = "version";
    
    private final static String KEY_APK_FILES = "files";
    
    private final static String KEY_APK_NAME = "name";
    
    private final static String APK_FILE_NAME = "Esp.apk";
    private final static String APK_FILE_TEMP_NAME = "Esp.apk.temp";
    
    private EspApplication mApplication;
    
    private ProgressUpdateListener mProgressListener;
    
    public UpgradeManagerApkOnline()
    {
        mApplication = EspApplication.sharedInstance();
    }
    
    /**
     * Check and upgrade newest apk from server
     * 
     * @return upgrade status result
     */
    public EspUpgradeApkResult upgrade()
    {
        JSONObject apkInfo = getNewestApkInfo();
        if (apkInfo == null)
        {
            log.debug("getNewestApkInfo() is null");
            return EspUpgradeApkResult.NOT_FOUND;
        }
        
        String version;
        String apkName;
        try
        {
            version = apkInfo.getString(KEY_APK_VERSION);
            JSONObject apkJSON = apkInfo.getJSONArray(KEY_APK_FILES).getJSONObject(0);
            apkName = apkJSON.getString(KEY_APK_NAME);
        }
        catch (JSONException je)
        {
            je.printStackTrace();
            return EspUpgradeApkResult.NOT_FOUND;
        }
        catch (IndexOutOfBoundsException iobe)
        {
            iobe.printStackTrace();
            return EspUpgradeApkResult.NOT_FOUND;
        }
        
        if (!checkApkVersion(version))
        {
            return EspUpgradeApkResult.LOWER_VERSION;
        }
        
        if (downloadNewestApk(version, apkName))
        {
            return EspUpgradeApkResult.UPGRADE_COMPLETE;
        }
        else
        {
            return EspUpgradeApkResult.DOWNLOAD_FAILED;
        }
    }
    
    private String getHeaderValue(String tokenValue)
    {
        return TOKEN + " " + tokenValue;
    }
    
    /**
     * Get the newest apk info from server
     * 
     * @return newest apk json
     */
    private JSONObject getNewestApkInfo()
    {
        String headerKey = Authorization;
        String headerValue = getHeaderValue(KEY_MASTER);
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        
        JSONObject romJSON = EspBaseApiUtil.Get(URL_NEWEST_APK_INFO, header);
        
        if (romJSON == null)
        {
            log.debug("rest get apk info null");
            return null;
        }
        
        try
        {
            int status = romJSON.getInt(KEY_STATUS);
            if (status != HttpStatus.SC_OK)
            {
                return null;
            }
            
            String newestVersion = romJSON.getString(KEY_NEWEST_VERSION);
            
            JSONArray romsArray = romJSON.getJSONArray(KEY_ROMS);
            for (int i = 0; i < romsArray.length(); i++)
            {
                JSONObject apkJSON = romsArray.getJSONObject(i);
                String version = apkJSON.getString(KEY_APK_VERSION);
                if (version.equals(newestVersion))
                {
                    log.debug("get newest apk info success");
                    return apkJSON;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        
        return null;
    }
    
    /**
     * Check version value of apk local and apk on the server
     * 
     * @param networkVersion
     * @return true: local version is lower than server version. false: local version is higher than server version.
     */
    private boolean checkApkVersion(String networkVersion)
    {
        UpgradeInfoApk networkApkInfo = new UpgradeInfoApk(networkVersion);
        if (!networkApkInfo.isLegal())
        {
            return false;
        }
        long networkApkVersionValue = networkApkInfo.getVersionValue();
        
        String localVersion = mApplication.getVersionName();
        UpgradeInfoApk localApkInfo = new UpgradeInfoApk(localVersion);
        long localApkVersionValue;
        if (localApkInfo.isLegal())
        {
            localApkVersionValue = localApkInfo.getVersionValue();
        }
        else
        {
            localApkVersionValue = 0;
        }
        log.info("network apk version value = " + networkApkVersionValue);
        log.info("local apk version value = " + localApkVersionValue);
        
        return (localApkVersionValue < networkApkVersionValue);
    }
    
    /**
     * Download apk from server
     * 
     * @param version
     * @param apkName
     * @return download apk from server success or not
     */
    private boolean downloadNewestApk(String version, String apkName)
    {
        String url = getDownloadUrl(version, apkName);
        log.info("apk url = " + url);
        HeaderPair header = new HeaderPair(Authorization, getHeaderValue(KEY_MASTER));
        String folderPath = getApkDirPath();
        
        boolean downloadSuc = false;
        
        // Check APK in download folder
        PackageManager pm = mApplication.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(folderPath + APK_FILE_NAME, PackageManager.GET_ACTIVITIES);
        if (info != null)
        {
            String existApkVersion = info.versionName;
            if (existApkVersion.equals(version))
            {
                downloadSuc = true;
            }
        }
        
        if (!downloadSuc)
        {
            // Download from server
            downloadSuc = EspBaseApiUtil.download(mProgressListener, url, folderPath, APK_FILE_TEMP_NAME, header);
            if (downloadSuc)
            {
                File tempApkFile = new File(folderPath + APK_FILE_TEMP_NAME);
                tempApkFile.renameTo(new File(folderPath + APK_FILE_NAME));
            }
        }
        
        if (downloadSuc)
        {
            installApk(folderPath + APK_FILE_NAME);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private void installApk(String apkPath)
    {
        File apk = new File(apkPath);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(apk), type);
        mApplication.startActivity(intent);
    }
    
    private String getDownloadUrl(String version, String fileName)
    {
        return URL_NEWEST_APK_INFO + "?action=download_rom&version=" + version + "&filename=" + fileName;
    }
    
    public void setOnProgressUpdateListener(ProgressUpdateListener listener)
    {
        if (mProgressListener != listener)
        {
            mProgressListener = listener;
        }
    }
    
    private String getApkDirPath()
    {
        return mApplication.getEspRootSDPath() + "apk/";
    }
}