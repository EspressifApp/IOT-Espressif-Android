package com.espressif.iot.base.net.proxy;

import java.util.ArrayList;
import java.util.List;

/**
 * 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f
 * ver   o  flags          proto                   len                                            
 * dst_addr
 *                                                 src_addr           
 * ot_len                                          option_list
 * 
 * 
 * ver: 2 bits, version of mesh;
 * o: 1 bit, exist flag of options in mesh header.
 * 
 * 
 * flags: 5 bits,
 * 00  01  02  03  04
 * CP  CR  resv

 * CP: piggyback congest permit in packet
 * CR: piggyback congest request in packet
 * resv: reserve for future.
 * 
 * 
 * proto: 8 bits,
 * 00  01  02  03  04  05  06  07
 * D   P2P protocol
 * 
 * D: direction of packet(0:downwards, 1:upwards)
 * P2P: Node to Node packet
 * protocol: protocol used by user data
 * current protocol type is as follows:
 * M_PROTO_NONE = 0,
 * M_PROTO_HTTP = 1,
 * M_PROTO_JSON = 2,
 * M_PROTO_MQTT = 3
 * 
 * len: 2 bytes, length of mesh packet in bytes(include mesh header)
 * dst_addr: 6 bytes, destiny address
 * proto.D = 0 (downwards) or P2P = 1 (Node-to-Node packet)
 * dst_addr represents the mac address of destiny device
 * Broadcast or multiplecast packet
 * dst_addr represents the broadcast or multiplecast mac address
 * (
 * broadcast:    0x00 0x00 0x00 0x00 0x00 0x00
 * multiplecast: 0x01 0x00 0x5E 0x00 0x00 0x00
 * )
 * 
 * 
 * src_addr: 6 bytes, source address(for Mobile or Server src_addr could be 
 * 0x00 0x00 0x00 0x00 0x00 0x00 and the mesh device will fill in automatically)
 * 
 * proto.P2P = 1
 * src_addr represents the mac address of source device
 * Broadcast or multiplecast packet
 * src_addr represents the mac address of source device
 * proto.D = 1(upwards)
 * src_addr represents the mac address of source device
 * proto.D = 0(downwards) and forward packet into mesh
 * src_addr represents the IP and port of Mobile or Server
 * 
 * 
 * options:
 * ot_len: represent the total length of options (include ot_len field)
 * option_list: the element list of the options
 * otype: 1 Byte, option type
 * olen: 1 Byte, the length of current option
 * ovalue: the value of current option
 * 
 * mesh_option_type:
 * M_O_CONGEST_REQ   = 0,
 * M_O_CONGEST_RESP  = 1,
 * M_O_ROUTER_SPREAD = 2,
 * M_O_ROUTER_ADD    = 3,
 * M_O_ROUTER_DEL    = 4,
 * M_O_TOPO_REQ      = 5,
 * M_O_TOPO_RESP     = 6,
 * M_O_MCAST_GRP     = 7,
 * M_O_MESH_FRAG     = 8,
 * M_O_USR_FRAG      = 9,
 * M_O_USER_OPTION   = 10
 * 
 * @author afunx
 *
 */
public class EspMeshPackageUtil
{

    private static final boolean DEBUG = true;
    private static final boolean USE_LOG4J = true;
    private static final Class<?> CLASS = EspMeshPackageUtil.class;
    
    private static final int M_OPTION_HEADER_LEN = 2;
    
    private static final int M_O_MCAST_GRP = 7;
    
    private static final int VER = 0;
    
    private static String MULTIPLE_CAST_BSSID = "01:00:5e:00:00:00";
    
    private static int MAC_ADDR_LEN = 6;
    
    static int M_OPTION_LEN = 2;
    
    static int M_HEADER_LEN = 16;
    
    public static int M_PROTO_NONE = 0;
    
    public static int M_PROTO_HTTP = 1;
    
    public static int M_PROTO_JSON = 2;
    
    public static int M_PROTO_MQTT = 3;
    
    
    // get first byte of Mesh Package Header
    private static byte get1Byte(int ver, boolean optionExist)
    {
        // version has 2 bits
        int result = ver & 0x03;
        if (optionExist)
        {
            // optionExist is 3 bit
            result = result | 0x04;
        }
        // flags are fixed at present
        final int flags = 0x02 << 0x03;
        result |= flags;
        return (byte)result;
    }
    
    private static byte get2Byte(int protocol)
    {
        return (byte)(protocol << 0x02);
    }
    
    private static byte[] getLengthBytes(int packageLength)
    {
        if (packageLength > 65535)
        {
            throw new IllegalArgumentException("packageLength is too large");
        }
        byte lByte = (byte)(packageLength & 0x00ff);
        byte hByte = (byte)((packageLength & 0xff00) >> 0x08);
        return new byte[] {lByte, hByte};
    }
    
    private static byte[] getDestAddrBytes(String targetBssid)
    {
        byte[] results = new byte[MAC_ADDR_LEN];
        String[] bssidSplits = targetBssid.split(":");
        if (bssidSplits.length != MAC_ADDR_LEN)
        {
            throw new IllegalArgumentException("invalid targetBssid: " + targetBssid);
        }
        for (int i = 0; i < bssidSplits.length; ++i)
        {
            int hex = Integer.parseInt(bssidSplits[i], 16);
            results[i] = (byte)hex;
        }
        return results;
    }
    
    private static byte[] getSrcAddrBytes()
    {
        return new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    }
    
    private static byte[] getOptionLengthBytes(int optionLength)
    {
        return getLengthBytes(optionLength);
    }
    
    private static byte[] getOptionBytes(List<String> targetBssidList)
    {
        int targetBssidListSize = targetBssidList.size();
        final int optionSizeEach = MAC_ADDR_LEN + M_OPTION_HEADER_LEN;
        byte[] optionBytes = new byte[targetBssidListSize * optionSizeEach];
        int optionBytesOffset = 0;
        
        for (int bssidIndex = 0; bssidIndex < targetBssidListSize; ++bssidIndex)
        {
            optionBytes[optionBytesOffset++] = M_O_MCAST_GRP;
            optionBytes[optionBytesOffset++] = (byte)optionSizeEach;
            String targetBssid = targetBssidList.get(bssidIndex);
            byte[] bssidBytes = getDestAddrBytes(targetBssid);
            for (int offset = 0; offset < MAC_ADDR_LEN; ++offset)
            {
                optionBytes[offset + optionBytesOffset] = bssidBytes[offset];
            }
            optionBytesOffset += MAC_ADDR_LEN;
        }
        
        return optionBytes;
    }
    
    private static int getMeshRequestOptionLength(List<String> groupBssidList)
    {
        return (M_OPTION_LEN + (M_OPTION_HEADER_LEN + MAC_ADDR_LEN) * groupBssidList.size());
    }
    
    private static byte[] __addMeshRequestPackageHeader(int proto, String targetBssid, byte[] requestBytes,
        List<String> groupBssidList)
    {
        int optionLength = 0;
        // update optionLength if necessary
        if (groupBssidList != null)
        {
            // check whether groupBssidList.size() is zero
            if (groupBssidList.size() == 0)
            {
                throw new IllegalArgumentException("groupBssidList.size() should equal 0");
            }
            // don't forget to add M_OPTION_LEN
            optionLength = getMeshRequestOptionLength(groupBssidList);
        }
        // packageLength = M_HEADER_LEN + requestBytes.length+optionLength
        int packageLength = M_HEADER_LEN + requestBytes.length + optionLength;
        // build new request bytes
        byte[] newRequestBytes = new byte[packageLength];
        // build package before optionList
        int ver = VER;
        boolean optionExist = optionLength != 0;
        int headerOffset = 0;
        // build first two bytes
        newRequestBytes[headerOffset++] = get1Byte(ver, optionExist);
        newRequestBytes[headerOffset++] = get2Byte(proto);
        // build length bytes
        byte[] lengthBytes = getLengthBytes(packageLength);
        for (int offset = 0; offset < lengthBytes.length; ++offset)
        {
            newRequestBytes[offset + headerOffset] = lengthBytes[offset];
        }
        headerOffset += lengthBytes.length;
        // build dest addr bytes
        byte[] destAddrBytes = getDestAddrBytes(targetBssid);
        for (int offset = 0; offset < destAddrBytes.length; ++offset)
        {
            newRequestBytes[offset + headerOffset] = destAddrBytes[offset];
        }
        headerOffset += destAddrBytes.length;
        // build src addr bytes
        byte[] srcAddrBytes = getSrcAddrBytes();
        for (int offset = 0; offset < srcAddrBytes.length; ++offset)
        {
            newRequestBytes[offset + headerOffset] = srcAddrBytes[offset];
        }
        headerOffset += srcAddrBytes.length;
        // build package of optionList if necessary
        if (optionLength != 0)
        {
            byte[] optionLengthBytes = getOptionLengthBytes(optionLength);
            for (int offset = 0; offset < optionLengthBytes.length; ++offset)
            {
                newRequestBytes[offset + headerOffset] = optionLengthBytes[offset];
            }
            headerOffset += optionLengthBytes.length;
            byte[] optionBytes = getOptionBytes(groupBssidList);
            for (int offset = 0; offset < optionBytes.length; ++offset)
            {
                newRequestBytes[offset + headerOffset] = optionBytes[offset];
            }
            headerOffset += optionBytes.length;
        }
        // build package content
        for (int offset = 0; offset < requestBytes.length; ++offset)
        {
            newRequestBytes[offset + headerOffset] = requestBytes[offset];
        }
        headerOffset += requestBytes.length;
        return newRequestBytes;
    }
    
    // tested
    public static byte[] addMeshRequestPackageHeader(int proto, String targetBssid, byte[] requestBytes)
    {
        byte[] newRequestBytes = __addMeshRequestPackageHeader(proto, targetBssid, requestBytes, null);
//        String message =
//            " addMeshRequestPackageHeader() proto:" + proto + ", targetBssid:" + targetBssid + ", newRequestBytes:"
//                + EspSocketUtil.toHexString(newRequestBytes);
//        MeshLog.d(DEBUG, USE_LOG4J, CLASS, message);
        return newRequestBytes;
    }
    
    public static byte[] addMeshGroupRequestPackageHeader(int proto, List<String> groupBssidList, byte[] requestBytes)
    {
        byte[] newRequestBytes =
            __addMeshRequestPackageHeader(proto, MULTIPLE_CAST_BSSID, requestBytes, groupBssidList);
        String message =
            " addMeshGroupRequestPackageHeader() proto:" + proto + ", groupBssidList:" + groupBssidList
                + ", newRequestBytes:" + EspSocketUtil.toHexString(newRequestBytes);
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, message);
        return newRequestBytes;
    }
    
    // tested
    public static int getResponseProto(byte[] responseBytes)
    {
        return (responseBytes[0x01] & 0xff) >> 0x02;
    }
    
    // tested
    public static int getResponsePackageLength(byte[] responseBytes)
    {
        return (responseBytes[0x02] & 0xff) | ((responseBytes[0x03] & 0xff) << 0x08);
    }
    
    // tested
    public static int getResponseOptionLength(byte[] responseBytes)
    {
        if ((responseBytes[0] & 0x04) == 0)
        {
            return 0;
        }
        else
        {
            return ((responseBytes[0x10] & 0xff) | (responseBytes[0x11] & 0xff) << 0x08);
        }
    }
    
    // tested
    public static byte[] getPureResponseBytes(byte[] responseBytes)
    {
        int packageLength = getResponsePackageLength(responseBytes);
        int optionLength = getResponseOptionLength(responseBytes);
        int pureResponseOffset = M_HEADER_LEN + optionLength;
        int pureResponseCount = packageLength - optionLength - M_HEADER_LEN;
        byte[] pureResponseBytes = new byte[pureResponseCount];
        for (int i = 0; i < pureResponseCount; ++i)
        {
            pureResponseBytes[i] = responseBytes[pureResponseOffset + i];
        }
        return pureResponseBytes;
    }
    
    public static boolean isDeviceAvailable(byte[] responseBytes)
    {
        return (responseBytes[0] & 0x08) != 0;
    }
    
    public static String getDeviceBssid(byte[] responseBytes)
    {
        int deviceBssidOffset = 0x0a;
        int deviceBssidCount = MAC_ADDR_LEN;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deviceBssidCount; ++i)
        {
            if (i != 0)
            {
                sb.append(":");
            }
            int hexValue = 0xff & responseBytes[deviceBssidOffset + i];
            if (hexValue < 0x0f)
            {
                sb.append("0");
            }
            sb.append(Integer.toHexString(hexValue));
        }
        return sb.toString();
    }
    
    // tested
    public static boolean isBodyEmpty(int packageLength, int optionLength)
    {
        return packageLength - optionLength == M_HEADER_LEN;
    }
    
    public static void main(String args[])
    {
        int ver = 0x01;
        boolean optionExist = true;
        byte b1 = get1Byte(ver, optionExist);
        if (b1 == 21)
        {
            System.out.println("get1Byte() pass");
        }
        int proto = M_PROTO_HTTP;
        byte b2 = get2Byte(proto);
        if (b2 == 4)
        {
            System.out.println("get2Byte() pass");
        }
        int packageLength = 0x10;
        byte[] packageLengthBytes = getLengthBytes(packageLength);
        if (packageLengthBytes[0] == 0x10 && packageLengthBytes[1] == 0x00)
        {
            System.out.println("getLengthBytes() pass");
        }
        else
        {
            System.out.println("getLengthBytes() fail");
        }
        String targetBssid = "18:fe:34:ab:cd:ef";
        byte[] destAddrBytes = getDestAddrBytes(targetBssid);
        if ((destAddrBytes[0] & 0xff) == 0x18 && (destAddrBytes[1] & 0xff) == 0xfe && (destAddrBytes[2] & 0xff) == 0x34
            && (destAddrBytes[3] & 0xff) == 0xab && (destAddrBytes[4] & 0xff) == 0xcd
            && (destAddrBytes[5] & 0xff) == 0xef)
        {
            System.out.println("getDestAddrBytes() pass");
        }
        else
        {
            System.out.println("getDestAddrBytes() fail");
        }
        
        List<String> targetBssidList = new ArrayList<String>();
        targetBssidList.add("18:fe:34:ab:cd:ef");
        targetBssidList.add("81:ef:43:ba:dc:fe");
        byte[] targetBssidListBytes = getOptionBytes(targetBssidList);
        if ((targetBssidListBytes[0] & 0xff) == 0x18 && (targetBssidListBytes[1] & 0xff) == 0xfe
            && (targetBssidListBytes[2] & 0xff) == 0x34 && (targetBssidListBytes[3] & 0xff) == 0xab
            && (targetBssidListBytes[4] & 0xff) == 0xcd && (targetBssidListBytes[5] & 0xff) == 0xef
            && (targetBssidListBytes[6] & 0xff) == 0x81 && (targetBssidListBytes[7] & 0xff) == 0xef
            && (targetBssidListBytes[8] & 0xff) == 0x43 && (targetBssidListBytes[9] & 0xff) == 0xba
            && (targetBssidListBytes[10] & 0xff) == 0xdc && (targetBssidListBytes[11] & 0xff) == 0xfe)
        {
            System.out.println("getOptionBytes() pass");
        }
        else
        {
            System.out.println("getOptionBytes() fail");
        }
        
        byte[] responsePackageLength = new byte[] {0x00, 0x00, 0x10, 0x20};
        packageLength = getResponsePackageLength(responsePackageLength);
        if (packageLength == (0x20 << 8) + 0x10)
        {
            System.out.println("getResponsePackageLength() pass");
        }
        else
        {
            System.out.println("getResponsePackageLength() fail");
        }
        
        byte[] responseOptionLength =
            new byte[] {0x04, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
                0x10, 0x11};
        int optionLength = getResponseOptionLength(responseOptionLength);
        if (optionLength == (0x11 << 8) + 0x10)
        {
            System.out.println("getResponseOptionLength() pass");
        }
        else
        {
            System.out.println("getResponseOptionLength() fail");
        }
        
        byte[] responseDeviceAvailable = new byte[] {0x08};
        boolean isDeviceAvailable = isDeviceAvailable(responseDeviceAvailable);
        if (isDeviceAvailable)
        {
            System.out.println("isDeviceAvailable() pass");
        }
        else
        {
            System.out.println("isDeviceAvailable() fail");
        }
        
        byte[] responseDeviceBssid =
            new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x18, (byte)0xfe, 0x34, (byte)0xab,
                (byte)0xcd, (byte)0xef};
        String deviceBssid = getDeviceBssid(responseDeviceBssid);
        if (deviceBssid.equals("18:fe:34:ab:cd:ef"))
        {
            System.out.println("getDeviceBssid() pass");
        }
        else
        {
            System.out.println("getDeviceBssid() fail");
        }
    }
}
