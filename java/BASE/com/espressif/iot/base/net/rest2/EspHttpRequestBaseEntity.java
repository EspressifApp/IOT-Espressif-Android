package com.espressif.iot.base.net.rest2;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class EspHttpRequestBaseEntity implements IEspHttpRequest
{
    private static final String PATH = "path";
    
    private static final String METHOD = "method";
    
    private static final String META = "meta";
    
    private static final String GET = "get";
    
    private static final String POST = "post";
    
    private static final String URL_QUERY_DELIMITER = "&";
    
    private static final String EQUAL = "=";
    
    private static final String ESCAPE = "\r\n";
    
    private final Map<String, String> mHeaderParams;
    
    private final Map<String, String> mQueryParams;
    
    private final String mMethod;
    
    private final String mPath;
    
    private final String mHost;
    
    private final String mScheme;
    
    private final String mContent;
    
    private final String mRelativeUrl;
    
    public EspHttpRequestBaseEntity(String method, String uriStr, String content)
    {
        this.mMethod = method;
        this.mContent = content;
        // parse URI
        URI uri = URI.create(uriStr);
        this.mScheme = uri.getScheme();
        this.mRelativeUrl = (uri.getQuery() != null) ? (uri.getPath() + "?" + uri.getQuery()) : (uri.getPath());
        this.mPath = uri.getPath();
        this.mHost = uri.getHost();
        // hash map to store header params
        this.mHeaderParams = new HashMap<String, String>();
        this.mQueryParams = new HashMap<String, String>();
        __parseQuery(uri.getQuery());
    }
    
    /**
     * put the header param into map
     * 
     * @param key the key of the param
     * @param value the value of the param
     */
    public void putHeaderParams(String key, String value)
    {
        this.mHeaderParams.put(key, value);
    }
    
    /**
     * put the query param into map
     * 
     * @param key the key of the param
     * @param value the value of the param
     */
    public void putQueryParams(String key, String value)
    {
        this.mQueryParams.put(key, value);
    }
    
    /**
     * Get the host of the uri
     * 
     * @return the host of the uri
     */
    public String getHost()
    {
        return this.mHost;
    }
    
    /**
     * Get the scheme of the uri
     * 
     * @return the scheme of the uri
     */
    public String getScheme()
    {
        return this.mScheme;
    }
    
    // like the format: a=1&b=2
    private void __parseQuery(String query)
    {
        if (query != null)
        {
            String[] query1Array = query.split(URL_QUERY_DELIMITER);
            for (String query1 : query1Array)
            {
                String[] query2Array = query1.split(EQUAL);
                if (query2Array.length != 2)
                {
                    throw new IllegalArgumentException("bad url argument");
                }
                mQueryParams.put(query2Array[0], query2Array[1]);
            }
        }
    }
    
    public EspHttpRequestBaseEntity(String method, String uriStr)
    {
        this(method, uriStr, null);
    }
    
    /**
     * { "nonce": 10086, "path": "/v1/device/", "method": "GET", "meta": { "Authorization": "token 000..." }, "get":
     * {"a"=1,"b"=2}, "post": {"commandKey":"commandValue"}, }\r\n
     */
    @Override
    public String toString()
    {
        JSONObject jsonResult = new JSONObject();
        try
        {
            jsonResult.put(PATH, "__path");
            jsonResult.put(METHOD, this.mMethod);
            if (!mHeaderParams.isEmpty())
            {
                JSONObject metaJson = new JSONObject();
                for (String headerKey : mHeaderParams.keySet())
                {
                    metaJson.put(headerKey, mHeaderParams.get(headerKey));
                }
                jsonResult.put(META, metaJson);
            }
            if (!mQueryParams.isEmpty())
            {
                JSONObject queryJson = new JSONObject();
                for (String headerKey : mQueryParams.keySet())
                {
                    queryJson.put(headerKey, mQueryParams.get(headerKey));
                }
                jsonResult.put(GET, queryJson);
            }
            if (!TextUtils.isEmpty(mContent))
            {
                JSONObject postJson = new JSONObject(mContent);
                jsonResult.put(POST, postJson);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonResult.toString().replace("__path", this.mPath) + ESCAPE;
    }
    
    @Override
    public String getRelativeUrl()
    {
        return this.mRelativeUrl;
    }
    
    @Override
    public String getContent()
    {
        return this.mContent;
    }
    
    public static void main(String args[])
    {
        String method = "GET";
        String uriStr = "http://iot.espressif.cn/v1/device";
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("header1", "value1");
            jsonObject.put("header2", "value2");
            String content = jsonObject.toString();
            EspHttpRequestBaseEntity baseEntity = new EspHttpRequestBaseEntity(method, uriStr, content);
            System.out.println("#########baseEntity: " + baseEntity);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
    }
}
