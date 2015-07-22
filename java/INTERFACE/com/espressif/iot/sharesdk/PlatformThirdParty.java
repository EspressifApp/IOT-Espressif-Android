package com.espressif.iot.sharesdk;

public class PlatformThirdParty
{
    public final static String NAME_WEIBO = "SinaWeibo";
    public final static String NAME_WECHAT = "Wechat";
    public final static String NAME_QQ = "QQ";
    
    public final static String ESP_STATE_QQ = "qq";
    
    public static String convertShareSDKNameToEspState(String shardSDKName)
    {
        if (shardSDKName.equals(NAME_QQ))
        {
            return ESP_STATE_QQ;
        }
        
        return null;
    }
}
