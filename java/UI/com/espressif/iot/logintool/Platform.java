package com.espressif.iot.logintool;

public class Platform {
    private final Type mType;

    private final String mAppId;
    private final String mAppKey;
    private final String mAppSecret;

    private String mAccessToken;
    private String mOpenId;

    private PlatformListener mPlatformListener;

    private Platform(Type type, String appId, String appKey, String appSecret) {
        mType = type;
        mAppId = appId;
        mAppKey = appKey;
        mAppSecret = appSecret;
    }

    /**
     * @return the platform type
     */
    public Type getType() {
        return mType;
    }

    /**
     * @return the state for EspCloud
     */
    public String getState() {
        return mType.getState();
    }

    /**
     * @return APP id
     */
    public String getAppId() {
        return mAppId;
    }

    /**
     * @return APP key
     */
    public String getAppKey() {
        return mAppKey;
    }

    /**
     * @return APP secret value
     */
    public String getAppSecret() {
        return mAppSecret;
    }

    /**
     * Set access token
     * 
     * @param token
     */
    void setAccessToken(String token) {
        mAccessToken = token;
    }

    /**
     * @return access token
     */
    public String getAcessToken() {
        return mAccessToken;
    }

    /**
     * Set open id
     * 
     * @param openId
     */
    void setOpenId(String openId) {
        mOpenId = openId;
    }

    /**
     * @return open id
     */
    public String getOpenId() {
        return mOpenId;
    }

    /**
     * Set the platform listener
     * 
     * @param listener
     */
    public void setPlatformListener(PlatformListener listener) {
        mPlatformListener = listener;
    }

    /**
     * @return the #PlatformListener
     */
    PlatformListener getPlatformListener() {
        return mPlatformListener;
    }

    /**
     * Authorize the platform
     */
    public void authorize() {
        EspLoginSDK.getInstance().authorize(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("type = " + mType)
            .append(", ")
            .append("AppId = " + mAppId)
            .append(", ")
            .append("AppKey = " + mAppKey)
            .append(", ")
            .append("AppSecret = " + mAppSecret)
            .append(", ")
            .append("AccessToken = " + mAccessToken)
            .append(", ")
            .append("OpenId = " + mOpenId);
        return sb.toString();
    }

    /**
     * The platform type
     */
    public enum Type {
        QQ("qq");

        private final String mState;

        private Type(String state) {
            mState = state;
        }

        /**
         * @return the state for EspCloud
         */
        public String getState() {
            return mState;
        }
    }

    /**
     * Callback of authorize the platform
     */
    public interface PlatformListener {
        /**
         * Authorize successfully
         * 
         * @param platform
         */
        void onComplete(Platform platform);

        /**
         * Authorize error
         * 
         * @param platform
         * @param errorMsg
         */
        void onError(Platform platform, String errorMsg);

        /**
         * Cancel authorize
         * 
         * @param platform
         */
        void onCancel(Platform platform);
    }

    static class Builder {
        private Type type;
        private String appId;
        private String appKey;
        private String appSecret;

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder setAppSecret(String appSecret) {
            this.appSecret = appSecret;
            return this;
        }

        public Platform create() {
            Platform platform = new Platform(type, appId, appKey, appSecret);
            return platform;
        }
    }
}
