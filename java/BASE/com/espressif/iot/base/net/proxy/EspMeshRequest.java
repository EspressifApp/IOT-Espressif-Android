package com.espressif.iot.base.net.proxy;

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
    
    private EspMeshRequest(int proto, String targetBssid, byte[] originRequestBytes)
    {
        mProto = proto;
        mTargetBssid = targetBssid;
        mOriginRequestBytes = originRequestBytes;
    }
    
    static EspMeshRequest createInstance(int proto, String targetBssid, byte[] originRequestBytes)
    {
        return new EspMeshRequest(proto, targetBssid, originRequestBytes);
    }
    
    public byte[] getRequestBytes()
    {
        return EspMeshPackageUtil.addMeshRequestPackageHeader(mProto, mTargetBssid, mOriginRequestBytes);
    }
}
