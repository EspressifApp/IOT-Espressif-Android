package com.espressif.iot.action.softap_sta_support;

import com.espressif.iot.action.device.IEspActionUnactivated;

import android.content.Context;

public interface ISSSActionDeviceUpgradeLocal extends IEspActionUnactivated
{
    static final String URI_UPGRADE_START = "http://192.168.4.1/upgrade?command=start";
    
    static final String URI_UPGRADE_GET_USER = "http://192.168.4.1/upgrade?command=getuser";
    
    static final String URI_UPGRADE_PUSH_USER1 = "http://192.168.4.1/device/bin/upgrade/?bin=user1.bin";
    
    static final String URI_UPGRADE_PUSH_USER2 = "http://192.168.4.1/device/bin/upgrade/?bin=user2.bin";
    
    static final String URI_UPGRADE_RESET = "http://192.168.4.1/upgrade?command=reset";
    
    static final String USER_BIN = "user_bin";
    
    static final String USER1 = "user1.bin";
    
    static final String USER2 = "user2.bin";
    
    /**
     * @param context the Android Context
     * @return the gateway addr like "255.255.255.2"
     */
    String getGatewayAddr(Context context);
    
    /**
     * 
     * @param ipStr the ip address like "192.168.4.1"
     * @param context the Android Context
     * @return @see SSSActionDeviceUpgradeLocalResult
     */
    SSSActionDeviceUpgradeLocalResult doActionSSSDeviceUpgradeLocal(String ipStr, Context context);
    
    /**
     * reset the device, make it switch to new user1.bin(or user2.bin)
     * 
     * @param ipStr the ip address like "192.168.4.1"
     * @return whether the device is reset
     */
    boolean doActionSSSDevicePostReset(String ipStr);
    
    /**
     * get which bin is running
     * 
     * @param ipStr the ip address like "192.168.4.1"
     * @return True means user1.bin is running, False means user2.bin is running, null means fail
     */
    Boolean __getIsUser1(String ipStr);
    
    /**
     * post start command to tell device upgrading will come
     * 
     * @param ipStr the ip address like "192.168.4.1"
     * @return whether the post is suc
     */
    boolean __postStart(String ipStr);
    
    /**
     * get user1.bin(or user2.bin)'s bytes
     * 
     * @param context the Android Context
     * @param fileName "user1.bin" or "user2.bin"
     * @return the byte[] of user1.bin(or user2.bin)
     */
    byte[] __getByteArray(Context context, String fileName);
    
    /**
     * 
     * @param ipStr the ip address like "192.168.4.1"
     * @param byteArray the byte[] of user1.bin(or user2.bin)
     * @param isUser1 whether it is user1.bin or user2.bin
     * @return whether the post is suc
     */
    boolean __postPushBin(String ipStr, byte[] byteArray, boolean isUser1);
    
    /**
     * reset the device, make it switch to new user1.bin(or user2.bin)
     * 
     * @param ipStr the ip address like "192.168.4.1"
     * @return whether the post is suc
     */
    boolean __postReset(String ipStr);
}
