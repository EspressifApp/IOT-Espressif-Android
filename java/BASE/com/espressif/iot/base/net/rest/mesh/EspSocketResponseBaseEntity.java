package com.espressif.iot.base.net.rest.mesh;

import java.util.HashMap;
import java.util.Map;

public class EspSocketResponseBaseEntity implements IEspSocketResponse
{
    public static final int UNKNOWN_STATUS = Integer.MIN_VALUE;
    
    private final String mContentHeaderStr;
    
    private final int mContentBodyLength;
    
    private final String mContentBodyStr;

    private final int mStatus;
    
    private final Map<String, String> mHeaerProperties;
    
    // HTTP/1.0 200 OK\r\n
    // HTTP/1.1 200 OK\r\n
    // HTTP/1.0 400 BAD REQUEST\r\n
    // HTTP/1.1 400 BAD REQUEST\r\n
    private static final String STATUS_HEAD_REGEX = "^HTTP/1\\.[0|1] \\d{3} \\S*\r\n[\\s\\S]*";
    
    /**
     * Constructor of EspSocketResponseEntity
     * @param header the header
     */
    public EspSocketResponseBaseEntity(String header)
    {
        this.mContentHeaderStr = header;
        this.mContentBodyLength = 0;
        this.mContentBodyStr = null;
        this.mStatus = __parseStatus(header);
        this.mHeaerProperties = new HashMap<String, String>();
        __putHeaderPropertiedIntoMap(header, this.mHeaerProperties);
    }
    
    /**
     * Constructor of EspSocketResponseEntity
     * @param header the header
     * @param body the body
     */
    public EspSocketResponseBaseEntity(String header, String body)
    {
        this.mContentHeaderStr = header;
        this.mContentBodyLength = body.length();
        this.mContentBodyStr = body;
        this.mStatus = __parseStatus(header);
        this.mHeaerProperties = new HashMap<String, String>();
        __putHeaderPropertiedIntoMap(header, this.mHeaerProperties);
    }
    
    /**
     * Get the content header String
     * @return the content header String
     */
    public String getContentHeaderStr()
    {
        return mContentHeaderStr;
    }
    
    /**
     * Get the content body length
     * @return the content body length
     */
    public int getContentBodyLength()
    {
        return mContentBodyLength;
    }
    
    /**
     * Get the content body by String
     * @return the content body by String
     */
    public String getContentBodyStr()
    {
        return mContentBodyStr;
    }
    
    /**
     * Get the status
     * @return the status of int
     */
    public int getStatus()
    {
        return mStatus;
    }
    
    /**
     * Get the header property by its key
     * @param key the key of header property
     * @return the value mapped to the key
     */
    public String getHeaderProperty(String key)
    {
        return mHeaerProperties.get(key);
    }
   
    /*
     * "HTTP/1.0 200 OK\r\n"
     */
    private int __parseStatus(String header)
    {
        if (header.matches(STATUS_HEAD_REGEX))
        {
            int beginIndex = header.indexOf(' ') + 1;
            int endIndex = header.indexOf(' ', beginIndex);
            return Integer.parseInt(header.substring(beginIndex, endIndex));
        }
        else
        {
            return UNKNOWN_STATUS;
        }
    }
    
    /*
     * e.g.
     * 
     * HTTP/1.0 200 OK \r\n
     * Content-Length: 58\r\n
     * Server: lwIP/1.4.0\r\n
     * Content-type: application/json\r\n
     * Espires: Fri, 10 Apr 2008 14:00:00 GMT\r\n
     * Pragma: no-cache\r\n
     */
    
    private void __putHeaderPropertiedIntoMap(String header, Map<String, String> map)
    {
        String[] headerSplits = header.split("\r\n");
        // ignore the first line
        for (int i = 1; i < headerSplits.length; i++)
        {
            String[] keyValue = headerSplits[i].split(": ");
            // ignore the bad format about properties
            if (keyValue.length == 2)
            {
                map.put(keyValue[0], keyValue[1]);
            }
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        if (mContentHeaderStr != null)
        {
            sb.append("mContentHeaderStr:\n" + mContentHeaderStr + "\n");
        }
        else
        {
            sb.append("mContentHeaderStr: null\n");
        }
        sb.append("mContentBodyLength:" + mContentBodyLength + "\n");
        sb.append("mContentBodyStr:\n" + mContentBodyStr + "\n");
        sb.append("mStatus:" + mStatus + "\n");
        sb.append("mHeaerProperties:\n" + mHeaerProperties + "\n");
        return sb.toString();
    }
}
