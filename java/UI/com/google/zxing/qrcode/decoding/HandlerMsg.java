package com.google.zxing.qrcode.decoding;

public final class HandlerMsg
{
    public static final int MSG_DECODE = 0x00000000;
    
    public static final int MSG_QUIT = 0x00000001;
    
    public static final int MSG_DECODE_SUCCEEDED = 0x00000002;
    
    public static final int MSG_DECODE_FAILED = 0x00000003;
    
    public static final int MSG_AUTO_FOCUS = 0x00000004;
    
    public static final int MSG_RESTART_PREVIEW = 0x00000005;
    
    public static final int MSG_RETURN_SCAN_RESULT = 0x00000006;
    
    public static final int MSG_LAUNCH_PRODUCT_QUERY = 0x00000007;
    
}
