package com.espressif.iot.logintool;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;

public class EspLoginSDK {
    public final String VERSION_CODE = "0.1.0";

    static final String KEY_PLATFORM_APP_ID = "platform_app_id";
    static final String KEY_PLATFORM_APP_KEY = "platform_app_key";

    private static Object mLock = new Object();
    private static EspLoginSDK instance;

    private Context mContext;

    private List<Platform> mPlatforms;

    private EspLoginSDK(Context context) {
        mContext = context;
        mPlatforms = new ArrayList<Platform>();
    }

    /**
     * Initialize the instance
     * 
     * @param context
     */
    public static void init(Context context) {
        synchronized (mLock) {
            if (instance == null) {
                instance = new EspLoginSDK(context);
            }
            instance.mContext = context;
            instance.parsePlatformXML();
        }
    }

    /**
     * Release the instance
     */
    public static void release() {
        synchronized (mLock) {
            if (instance != null) {
                instance.mContext = null;
                instance.mPlatforms.clear();
                instance = null;
            }
        }
    }

    /**
     * @return the instance of EspLoginSDK
     */
    public static EspLoginSDK getInstance() {
        if (instance == null) {
            throw new NullPointerException("The instance is null, call init(Context) first");
        }
        return instance;
    }

    private void parsePlatformXML() {
        mPlatforms.clear();
        LoginXMLParser parser = new LoginXMLParser(mContext);
        List<Platform> platforms = parser.parse();
        if (platforms != null) {
            for (Platform p : platforms) {
                System.out.println("platform = " + p.toString());
                if (p.getType() != null) {
                    mPlatforms.add(p);
                }
            }
        }
    }

    /**
     * @return the platforms in asset XML
     */
    public List<Platform> getPlatforms() {
        List<Platform> result = new ArrayList<Platform>();
        result.addAll(mPlatforms);

        return result;
    }

    /**
     * Authorize the platform
     * 
     * @param platform
     */
    public void authorize(Platform platform) {
        switch (platform.getType()) {
            case QQ:
                Intent intent = new Intent(mContext, EspLoginHelpActivity.class);
                intent.putExtra(KEY_PLATFORM_APP_ID, platform.getAppId());
                intent.putExtra(KEY_PLATFORM_APP_KEY, platform.getAppKey());
                mContext.startActivity(intent);
                break;
        }
    }

    void notifyAuthorizeComplete(Platform platform) {
        if (platform.getPlatformListener() != null) {
            platform.getPlatformListener().onComplete(platform);
        }
    }

    void notifyAuthorizeError(Platform platform, String errorMsg) {
        if (platform.getPlatformListener() != null) {
            platform.getPlatformListener().onError(platform, errorMsg);
        }
    }

    void notifyAuthrozeCancel(Platform platform) {
        if (platform.getPlatformListener() != null) {
            platform.getPlatformListener().onCancel(platform);
        }
    }
}
