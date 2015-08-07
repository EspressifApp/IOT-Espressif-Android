package com.espressif.iot.base.net.rest.mesh;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class EspSocketRequestBaseEntity implements IEspSocketRequest
{
    private static final char SPACE = ' ';
    private static final String HTTP_1_1 = "HTTP/1.1";
    private static final String HOST = "Host";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String DELIMITER = ": ";
    private static final String ESCAPE = "\r\n";
    
    private final Map<String, String> mHeaerParams;
    private final String mMethod;
    private final String mOriginUrl;
    private final String mRelativeUrl;
    private final String mHost;
    private final String mContent;
    private final String mScheme;
    private final int mContentLength;
    
    /**
     * Constructor of EspSocketRequestBaseEntity
     * @param method the method
     * @param uriStr the String of URI
     * @param content the content
     */
    public EspSocketRequestBaseEntity(String method, String uriStr, String content)
    {
        this.mMethod = method;
        this.mContent = content;
        this.mOriginUrl = uriStr;
        if (content != null)
        {
            this.mContentLength = content.length() + ESCAPE.length();
        }
        else
        {
            this.mContentLength = 0;
        }
        // parse URI
        URI uri = URI.create(uriStr);
        this.mScheme = uri.getScheme();
        this.mRelativeUrl = (uri.getQuery() != null) ? (uri.getPath() + "?" + uri.getQuery()) : (uri.getPath());
        System.out.println("EspSocketRequestBaseEntity mRelativeUrl:" + mRelativeUrl);
        this.mHost = uri.getHost();
        // hash map to store header params
        this.mHeaerParams = new HashMap<String, String>();
    }
    
    /**
     * Constructor of EspSocketRequestBaseEntity
     * @param method the method
     * @param uriStr the String of URI
     */
    public EspSocketRequestBaseEntity(String method, String uriStr)
    {
        this(method, uriStr, null);
    }
    
    /**
     * put the header param into map
     * @param key the key of the param
     * @param value the value of the param
     */
    public void putHeaderParams(String key,String value)
    {
        this.mHeaerParams.put(key, value);
    }

    /**
     * Get the relative Url
     * @return the relative Url
     */
    public String getRelativeUrl()
    {
    	return this.mRelativeUrl;
    }
    /**
     * Get the scheme of the uri
     * @return the scheme of the uri
     */
    public String getScheme(){
        return this.mScheme;
    }
    
    /**
     * Get the host of the uri
     * @return the host of the uri
     */
    public String getHost(){
        return this.mHost;
    }
    
    @Override
    public String getContent()
    {
        return this.mContent;
    }
    
    @Override
    public String getOriginUri()
    {
        return this.mOriginUrl;
    }
    /*
     * POST /config?command=light HTTP/1.1
     * Host: 192.168.11.102
     * Content-Length: 113
     * 
     * {"freq":500,"rgb":{"red":255,"green":255,"blue":255},
     * "sip": "C0A80B7B", "sport": "0FA1"}
     */
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(mMethod);sb.append(SPACE);sb.append(mRelativeUrl);sb.append(SPACE);sb.append(HTTP_1_1);sb.append(ESCAPE);
        sb.append(HOST);sb.append(DELIMITER);sb.append(mHost);sb.append(ESCAPE);
        sb.append(CONTENT_LENGTH);sb.append(DELIMITER);sb.append(mContentLength);sb.append(ESCAPE);
        for(String headerKey : mHeaerParams.keySet())
        {
            sb.append(headerKey);sb.append(DELIMITER);sb.append(mHeaerParams.get(headerKey));sb.append(ESCAPE);
        }
        sb.append(ESCAPE);
        if(mContent!=null)
        {
            sb.append(mContent);
            sb.append(ESCAPE);
        }
        return sb.toString();
    }
}
