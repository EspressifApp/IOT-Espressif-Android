package com.espressif.iot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//98:fe:34:77:ce:00
public class BSSIDUtil
{
    
    /**
     * for the reason, we use some bit to differ whether it is softap or sta. so, we may get the polluted bssid.
     * 
     * softap bssid: 1a:fe:34:77:c0:00 sta bssid: 18:fe:34:77:c0:00
     * 
     * @param BSSID
     * @return the real BSSID(SoftAp's BSSID)
     */
    public static String restoreSoftApBSSID(String BSSID)
    {
        String pollutedBitStr = BSSID.substring(1, 2);
        Integer pollutedBitInt = 0;
        // get pollutedBitInt according to 0x..
        pollutedBitInt = Integer.parseInt(pollutedBitStr, 16);
        Integer cleanBitInt = pollutedBitInt | 0x02;
        String cleanBitStr = Integer.toHexString(cleanBitInt);
        return BSSID.substring(0, 1) + cleanBitStr + BSSID.substring(2);
    }
    
    /**
     * for the reason, we use some bit to differ whether it is softap or sta. so, we may get the polluted bssid.
     * 
     * softap bssid: 1a:fe:34:77:c0:00 sta bssid: 18:fe:34:77:c0:00
     * 
     * @param BSSID
     * @return the sta BSSID(Sta's BSSID)
     */
    public static String restoreStaBSSID(String BSSID)
    {
        String pollutedBitStr = BSSID.substring(1, 2);
        Integer pollutedBitInt = 0;
        // get pollutedBitInt according to 0x..
        pollutedBitInt = Integer.parseInt(pollutedBitStr, 16);
        Integer cleanBitInt = pollutedBitInt & (~0x02);
        String cleanBitStr = Integer.toHexString(cleanBitInt);
        return BSSID.substring(0, 1) + cleanBitStr + BSSID.substring(2);
    }
    
    // upper case if 'a' to 'z'
    private static String UpperCase(String str)
    {
        int len = str.length();
        String result = "";
        for (int i = 0; i < len; i++)
        {
            String subStr = str.substring(i, i + 1);
            char subChar = str.charAt(i);
            if (subChar >= 'a' && subChar <= 'z')
                result += subStr.toUpperCase(Locale.US);
            else
                result += subStr;
        }
        return result;
    }
    
    /**
     * generate Device Name By BSSID
     * 
     * @param BSSID
     * @return "ESP_XXXXXX", "XXXXXX" is the last 6 of BSSID
     */
    public static String genDeviceNameByBSSID(String BSSID)
    {
        return genDeviceNameByBSSID("ESP_", BSSID);
    }
    
    /**
     * generate Device Name By BSSID
     * 
     * @param prefix the prefix of the name
     * @param BSSID
     * @return prefix + "XXXXXX", "XXXXXX" is the last 6 of BSSID
     */
    public static String genDeviceNameByBSSID(String prefix, String BSSID)
    {
        // 1a:fe:34:77:c0:00 change to 77C000
        System.out.println("genDeviceNameByBSSID BSSID =  " + BSSID);
        if (BSSID.length() == 12)
        {
            BSSID = MeshUtil.getRawMacAddress(BSSID);
        }
        String tail = "";
        tail += UpperCase(BSSID.substring(9, 11));
        tail += BSSID.substring(12, 14).toUpperCase(Locale.US);
        tail += BSSID.substring(15, 17).toUpperCase(Locale.US);
        return prefix + tail;
    }
    
    /**
     * 1a:fe:34:77:c0:00 and 18:fe:34:77:c0:00 are the same BSSID whether the two BSSID is the same ignore the first
     * 
     * @param BSSID1
     * @param BSSID2
     * @return
     */
    public static boolean isEqualIgnore2chars(String BSSID1, String BSSID2)
    {
        return (BSSID1.substring(3).equals(BSSID2.substring(3)));
    }
    
    /**
     * @param bssid the device's bssid
     * @return whether the device's bssid is belong to sta state
     */
    public static boolean isStaDevice(String bssid)
    {
        return bssid.subSequence(0, 2).equals("18");
    }
    
    /**
     * @param bssid the device's bssid
     * @return whether the device's bssid is belong to softap state
     */
    public static boolean isSoftapDevice(String bssid)
    {
        return bssid.subSequence(0, 2).equals("1a");
    }
    
    /**
     * restore the bssid from esptouch result
     * 
     * @param BSSID like 18fe34abcdef or 18FE34ABCDEF
     * @return like 18:fe:34:ab:cd:ef
     */
    public static String restoreBSSID(String BSSID)
    {
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < BSSID.length(); index += 2)
        {
            sb.append(BSSID.substring(index, index + 2));
            if (index != BSSID.length() - 2)
            {
                sb.append(":");
            }
        }
        return sb.toString().toLowerCase(Locale.US);
    }
    
    /**
     * check whether the bssid is belong to ESP device
     * 
     * @param BSSID the bssid to be checked
     * @return whether the bssid is belong to ESP device
     */
    public static boolean isEspDevice(String BSSID)
    {
        // ESP wifi's sta bssid is started with "18:fe:34"
        return BSSID != null && (BSSID.startsWith("18:fe:34"));
    }
    
    /**
     * get bssid list from bssids String
     * 
     * @param bssids bssids String
     * @return bssid list
     */
    public static List<String> getBssidList(String bssids)
    {
        // 18:fe:34:a2:c6:db length is 17
        if (bssids.length() % 17 != 0)
        {
            return null;
        }
        List<String> bssidList = new ArrayList<String>();
        for (int i = 0; i < bssids.length(); i += 17)
        {
            String bssid = bssids.substring(i, i + 17);
            bssidList.add(bssid);
        }
        return bssidList;
    }
}
