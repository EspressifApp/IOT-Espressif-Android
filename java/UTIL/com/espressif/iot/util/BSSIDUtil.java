package com.espressif.iot.util;

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
                result += subStr.toUpperCase();
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
        // 1a:fe:34:77:c0:00 change to 77C000
        String tail = "";
        tail += UpperCase(BSSID.substring(9, 11));
        tail += BSSID.substring(12, 14).toUpperCase();
        tail += BSSID.substring(15, 17).toUpperCase();
        return "ESP_" + tail;
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
}
