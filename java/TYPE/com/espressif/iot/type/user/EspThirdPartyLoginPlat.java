package com.espressif.iot.type.user;

public class EspThirdPartyLoginPlat
{
    private String mAccessToken;
    
    private String mState;
    
    private String mOpenId;
    
    /**
     * Set the access token
     * 
     * @param accessToken
     */
    public void setAccessToken(String accessToken)
    {
        mAccessToken = accessToken;
    }
    
    /**
     * 
     * @return the access token
     */
    public String getAccessToken()
    {
        return mAccessToken;
    }
    
    /**
     * Set the state of the platform
     * 
     * @param state
     */
    public void setState(String state)
    {
        mState = state;
    }
    
    /**
     * 
     * @return the state of the platform
     */
    public String getState()
    {
        return mState;
    }
    
    /**
     * Set the open id
     * 
     * @param openId
     */
    public void setOpenId(String openId)
    {
        mOpenId = openId;
    }
    
    /**
     * 
     * @return the open id
     */
    public String getOpenId()
    {
        return mOpenId;
    }
    
    public final static String SHARESDK_NAME_WEIBO = "SinaWeibo";
    public final static String SHARESDK_NAME_WECHAT = "Wechat";
    public final static String SHARESDK_NAME_QQ = "QQ";
    public final static String SHARESDK_NAME_FACEBOOK = "Facebook";
    public final static String SHARESDK_NAME_TWITTER = "Twitter";
    
    public final static String ESP_STATE_WEIBO = "weibo";
    public final static String ESP_STATE_WECHAT = "weixin_t";
    public final static String ESP_STATE_QQ = "qq";
    
    public static String convertShareSDKNameToEspState(final String shareSDKName)
    {
        if (shareSDKName.equals(SHARESDK_NAME_QQ))
        {
            return ESP_STATE_QQ;
        }
        else if (shareSDKName.equals(SHARESDK_NAME_WECHAT))
        {
            return ESP_STATE_WECHAT;
        }
        else if (shareSDKName.equals(SHARESDK_NAME_WEIBO))
        {
            return ESP_STATE_WEIBO;
        }
        else if (shareSDKName.equals(SHARESDK_NAME_FACEBOOK))
        {
            // TODO
        }
        else if (shareSDKName.equals(SHARESDK_NAME_TWITTER))
        {
            // TODO
        }
        
        return null;
    }
}
