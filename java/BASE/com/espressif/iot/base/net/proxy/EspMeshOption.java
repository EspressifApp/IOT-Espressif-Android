package com.espressif.iot.base.net.proxy;

enum MESH_OPTION_TYPE
{
    M_O_CONGEST_REQ,    // congest request option
    M_O_CONGEST_RESP,   // congest response option
    M_O_ROUTER_SPREAD,  // router information spread option
    M_O_ROUTE_ADD,      // route table update (node joins mesh) option
    M_O_ROUTE_DEL,      // route table update (node leaves mesh) option
    M_O_TOPO_REQ,       // topology request option
    M_O_TOPO_RESP,      // topology response option
    M_O_MCAST_GRP,      // group list of mcast
    M_O_MESH_FRAG,      // mesh management fragment option
    M_O_USR_FRAG,       // user data fragment
    M_O_USR_OPTION,     // user option
}

/**
 * EspMeshOption is used for parse MESH OPTION in MESH HEADER
 * 
 * @author afunx
 * 
 */
public class EspMeshOption
{
    private static int M_OPTION_TYPE_SIZE = 1;
    
    private static int M_OPTION_LENGTH_SIZE = 1;
    
    private static int M_OPTION_TYPE_LENGTH_SIZE = M_OPTION_TYPE_SIZE + M_OPTION_LENGTH_SIZE;
    
    private int mDeviceAvailableCount = 0;
    
    static EspMeshOption createInstance(byte[] responseBytes, int packageLength, int optionLength)
    {
        try
        {
            return new EspMeshOption(responseBytes, packageLength, optionLength);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    private EspMeshOption(byte[] responseBytes, int packageLength, int optionLength)
    {
        if (optionLength <= 0)
        {
            throw new IllegalArgumentException("option length <= 0, optionLength = " + optionLength);
        }
        if (packageLength <= optionLength)
        {
            throw new IllegalArgumentException("packageLength <= optionLength, packageLength = " + packageLength
                + ", optionLength = " + optionLength);
        }
        parse(responseBytes, packageLength, optionLength);
    }
    
    private int parseTLV(byte[] responseBytes, int offset, int count)
    {
        int value = 0;
        for (int index = count - 1; index >= 0; --index)
        {
            value <<= 0x08;
            value += 0xff & responseBytes[offset + index];
        }
        return value;
    }
    
    private MESH_OPTION_TYPE parseType(byte[] responseBytes, int typeOffset, int typeCount)
    {
        int typeValue = parseTLV(responseBytes, typeOffset, typeCount);
        MESH_OPTION_TYPE type = MESH_OPTION_TYPE.values()[typeValue];
        return type;
    }
    
    private int parseLength(byte[] responseBytes, int lengthOffset, int lengthCount)
    {
        return parseTLV(responseBytes, lengthOffset, lengthCount);
    }
    
    private int parseValue(byte[] responseBytes, int valueOffset, int valueCount)
    {
        return parseTLV(responseBytes, valueOffset, valueCount);
    }
    
    private void parse(byte[] responseBytes, int packageLength, int optionLength)
    {
        int bodyLength = packageLength - EspMeshPackageUtil.M_HEADER_LEN - optionLength;
        int offset = EspMeshPackageUtil.M_HEADER_LEN + bodyLength + EspMeshPackageUtil.M_OPTION_LEN;
        // parse TLV
        while (offset < packageLength)
        {
            MESH_OPTION_TYPE type = parseType(responseBytes, offset, M_OPTION_TYPE_SIZE);
            offset += M_OPTION_TYPE_SIZE;
            // reduce 2 bytes for type and length
            int length = parseLength(responseBytes, offset, M_OPTION_LENGTH_SIZE) - M_OPTION_TYPE_LENGTH_SIZE;
            offset += M_OPTION_LENGTH_SIZE;
            switch (type)
            {
                case M_O_CONGEST_REQ:
                    throw new IllegalArgumentException("M_O_CONGEST_REQ shouldn't be sent to mobile");
                case M_O_CONGEST_RESP:
                    if (length != 0x02)
                    {
                        throw new IllegalArgumentException("M_O_CONGEST_RESP length != 2, length = " + length);
                    }
                    mDeviceAvailableCount = parseValue(responseBytes, offset, length);
                    offset += length;
                    break;
                case M_O_MCAST_GRP:
                    throw new IllegalArgumentException("M_O_MCAST_GRP shouldn't be sent to mobile");
                case M_O_MESH_FRAG:
                    throw new IllegalArgumentException("M_O_MESH_FRAG shouldn't be sent to mobile");
                case M_O_ROUTER_SPREAD:
                    throw new IllegalArgumentException("M_O_ROUTER_SPREAD shouldn't be sent to mobile");
                case M_O_ROUTE_ADD:
                    throw new IllegalArgumentException("M_O_ROUTE_ADD shouldn't be sent to mobile");
                case M_O_ROUTE_DEL:
                    throw new IllegalArgumentException("M_O_ROUTE_DEL shouldn't be sent to mobile");
                case M_O_TOPO_REQ:
                    throw new IllegalArgumentException("M_O_TOPO_REQ shouldn't be sent to mobile");
                case M_O_TOPO_RESP:
                    throw new IllegalArgumentException("M_O_TOPO_RESP shouldn't be sent to mobile");
                case M_O_USR_FRAG:
                    throw new IllegalArgumentException("M_O_USR_FRAG shouldn't be sent to mobile");
                case M_O_USR_OPTION:
                    throw new IllegalArgumentException("M_O_USR_OPTION shouldn't be sent to mobile");
            }
        }
    }
    
    public int getDeviceAvailableCount()
    {
        return mDeviceAvailableCount;
    }
}
