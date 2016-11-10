package com.espressif.iot.logintool;

public class Platform {
    private final Type mType;

    private String mAppId;
    private String mAppKey;

    private String mAccessToken;
    private String mOpenId;

    private PlatformListener mPlatformListener;

    public Platform(Type type) {
        mType = type;
    }

    public Type getType() {
        return mType;
    }

    public String getState() {
        return mType.getState();
    }

    public void setAppId(String appId) {
        mAppId = appId;
    }

    public String getAppId() {
        return mAppId;
    }

    public void setAppKey(String appKey) {
        mAppKey = appKey;
    }

    public String getAppKey() {
        return mAppKey;
    }

    public void setAccessToken(String token) {
        mAccessToken = token;
    }

    public String getAppcessToken() {
        return mAccessToken;
    }

    public void setOpenId(String openId) {
        mOpenId = openId;
    }

    public String getOpenId() {
        return mOpenId;
    }

    public void setPlatformListener(PlatformListener listener) {
        mPlatformListener = listener;
    }

    public enum Type {
        QQ("qq");

        private final String mState;

        private Type(String state) {
            mState = state;
        }

        public String getState() {
            return mState;
        }
    }

    public interface PlatformListener {
        void onComplete(Platform platform);

        void onError(Platform platform, String errorMsg);

        void onCancel(Platform platform);
    }
}
