package com.espressif.iot.base.net.proxy;

import java.util.List;

/**
 * class for Mesh request
 * 
 * @author afunx
 * 
 */
public class EspMeshRequest
{
    private final int mProto;
    
    private final String mTargetBssid;
    
    private final byte[] mOriginRequestBytes;
    
    private final List<String> mTargetBssidList;
    
    private EspMeshRequest(int proto, String targetBssid, byte[] originRequestBytes)
    {
        mProto = proto;
        mTargetBssid = targetBssid;
        mOriginRequestBytes = originRequestBytes;
        mTargetBssidList = null;
    }
    
    private EspMeshRequest(int proto, List<String> targetBssidList, byte[] originRequestBytes)
    {
        mProto = proto;
        mTargetBssid = null;
        mOriginRequestBytes = originRequestBytes;
        mTargetBssidList = targetBssidList;
    }
    
    static EspMeshRequest createInstance(int proto, String targetBssid, byte[] originRequestBytes)
    {
        return new EspMeshRequest(proto, targetBssid, originRequestBytes);
    }
    
    static EspMeshRequest createInstance(int proto, List<String> targetBssidList, byte[] originRequestBytes)
    {
        return new EspMeshRequest(proto, targetBssidList, originRequestBytes);
    }
    
    public byte[] getRequestBytes()
    {
        if (mTargetBssidList == null)
        {
            return EspMeshPackageUtil.addMeshRequestPackageHeader(mProto, mTargetBssid, mOriginRequestBytes);
        }
        else
        {
            return EspMeshPackageUtil.addMeshGroupRequestPackageHeader(mProto, mTargetBssidList, mOriginRequestBytes);
        }
    }
    
}
