package com.espressif.iot.base.net.udp;


/**
 * help UdpBroadcastUtil to parse the data sent by device
 * @author afunx
 *
 */
public class UdpDataParser
{
    // "I'm Plug.18:fe:34:9a:b1:4c 192.168.100.126"
    // "I'm Plug with mesh.18:fe:34:9a:b1:4c 192.168.100.126"
    
    // '^' beginning sign
    // '\\w' word symbol containing '_'
    // '\\.' .
    // '\\d' 0-9
    // '$' ending sign
    
    private static final String DEVICE_PATTERN_TYPE = "^I'm ((\\w)+( )*)+\\.";
    private static final String DEVICE_PATTERN_BSSID = "([0-9a-fA-F]{2}:){5}([0-9a-fA-F]{2} )";
    private static final String DEVICE_PATTERN_IP = "(\\d+\\.){3}(\\d+)";
    
    private static final String DEVICE_PATTERN = DEVICE_PATTERN_TYPE + DEVICE_PATTERN_BSSID + DEVICE_PATTERN_IP;
    
    /**
     * check whether the data is valid
     * @param data the content String get from UDP Broadcast
     * @return whether the data is valid
     */
    public static boolean isValid(String data)
    {
        return (data.matches(DEVICE_PATTERN));
    }
    
    /**
     * check whether the device is mesh
     * @param data the content String get from UDP Broadcast
     * @return whether the device is mesh
     */
    public static boolean isMesh(String data)
    {
        return data.contains("with mesh");
    }
    
    /**
     * filter the device's type String from data
     * @param data the content String get from UDP Broadcast
     * @return the device's type String
     */
    public static String filterType(String data)
    {
        String[] dataSplitArray= data.split("\\.");
        return dataSplitArray[0].split(" ")[1];
    }
    
    /**
     * filter the device's bssid from data
     * @param data the content String get from UDP Broadcast
     * @return the device's bssid String
     */
    public static String filterBssid(String data)
    {
        String[] dataSplitArray= data.split("\\.");
        return dataSplitArray[1].split(" ")[0];
    }
    
    /**
     * filter the device's ip address from data
     * @param data the content String get from UDP Broadcast
     * @return the device's ip address String
     */
    public static String filterIpAddress(String data)
    {
        String[] dataSplitArray= data.split(" ");
        return dataSplitArray[dataSplitArray.length - 1];
    }

    private static void testIsValid()
    {
        System.out.println("testIsValid()");
        
        String deviceData = "I'm Plug.18:fe:34:9a:b1:4c 192.168.100.126";
        String deviceMeshData = "I'm Plug with mesh.18:fe:34:9a:b1:4c 192.168.100.126";
        
        if (isValid(deviceData) == true && isValid(deviceMeshData) == true)
        {
            System.out.println("isValid pass");
        }
        else
        {
            System.out.println("isValid fail");
        }
        
        if (isMesh(deviceData) == false && isMesh(deviceMeshData) == true)
        {
            System.out.println("isMesh pass");
        }
        else
        {
            System.out.println("isMesh fail");
        }
        
        if (filterIpAddress(deviceData).equals("192.168.100.126") && filterIpAddress(deviceMeshData).equals("192.168.100.126"))
        {
            System.out.println("filterIpAddress pass");
        }
        else
        {
            System.out.println("filterIpAddress fail");
        }
        
        if (filterBssid(deviceData).equals("18:fe:34:9a:b1:4c") && filterBssid(deviceMeshData).equals("18:fe:34:9a:b1:4c"))
        {
            System.out.println("filterBssid pass");
        }
        else
        {
            System.out.println("filterBssid fail");
        }
        
        if (filterType(deviceData).equals("Plug") && filterType(deviceMeshData).equals("Plug"))
        {
            System.out.println("filterType pass");
        }
        else
        {
            System.out.println("filterType fail");
        }
    }
    
    public static void main(String args[])
    {
        testIsValid();
    }
}
