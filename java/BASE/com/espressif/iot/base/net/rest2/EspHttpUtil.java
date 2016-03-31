package com.espressif.iot.base.net.rest2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.EspStrings;

public class EspHttpUtil
{
    private final static Logger log = Logger.getLogger(EspHttpUtil.class);
    
    private final static int SOCKET_CONNECT_RETRY_TIME = 3;
    
    private final static String KEY_STATUS = "status";
    
    public static JSONObject Get(String url, HeaderPair... headers)
    {
        return Get(url, null, headers);
    }
    
    public static JSONObject Get(String url, JSONObject json, HeaderPair... headers)
    {
        String logTag =
            Thread.currentThread().toString() + "##" + EspHttpLogUtil.convertToCurl(true, null, url, headers);
        log.debug(logTag);
        // create httpClient
        EspHttpClient httpclient = EspHttpClient.getEspHttpClient();
        // create httpGet
        HttpUriRequest httpget = createHttpRequest(false, true, url, json, headers);
        // execute
        JSONObject result = executeHttpRequest(httpclient, httpget, null);
        log.debug(logTag + ":result=" + result);
        return result;
    }
    
    public static JSONObject Post(String url, JSONObject json, HeaderPair... headers)
    {
        String logTag =
            Thread.currentThread().toString() + "##" + EspHttpLogUtil.convertToCurl(false, json, url, headers);
        log.debug(logTag);
        // create httpClient
        EspHttpClient httpclient = EspHttpClient.getEspHttpClient();
        // create httpPost
        HttpUriRequest httppost = createHttpRequest(false, false, url, json, headers);
        // execute
        JSONObject result = executeHttpRequest(httpclient, httppost, null);
        log.debug(logTag + ":result=" + result);
        
        return result;
    }
    
    public static void PostInstantly(String url, JSONObject json, Runnable disconnectedCallback, HeaderPair... headers)
    {
        disconnectedCallback = disconnectedCallback != null ? disconnectedCallback : EspHttpRequest.ESP_DUMMY_RUNNABLE;
        String logTag =
            Thread.currentThread().toString() + "##" + EspHttpLogUtil.convertToCurl(false, json, url, headers);
        log.debug(logTag);
        // create httpClient
        EspHttpClient httpclient = EspHttpClient.getEspHttpClient();
        // create httpPost
        HttpUriRequest httppost = createHttpRequest(true, false, url, json, headers);
        // execute
        executeHttpRequest(httpclient, httppost, disconnectedCallback);
    }
    
    private static boolean __isHttpsSupported()
    {
        SharedPreferences sp =
            EspApplication.sharedInstance().getSharedPreferences(EspStrings.Key.SYSTEM_CONFIG, Context.MODE_PRIVATE);
        return sp.getBoolean(EspStrings.Key.HTTPS_SUPPORT, true);
    }
    
    private static void __setHttpsUnsupported()
    {
        log.error("__setHttpsUnsupported(): it shouldn't happen");
        SharedPreferences sp =
            EspApplication.sharedInstance().getSharedPreferences(EspStrings.Key.SYSTEM_CONFIG, Context.MODE_PRIVATE);
        sp.edit().putBoolean(EspStrings.Key.HTTPS_SUPPORT, false).commit();
    }
    
    private static HttpUriRequest createHttpRequest(boolean isInstantly, boolean isGet, String url, JSONObject json,
        HeaderPair... headers)
    {
        url = encodingUrl(url);
        if (!__isHttpsSupported())
        {
            url = url.replace("https", "http");
        }
        EspHttpRequest request = null;
        if (isGet)
        {
            request = new EspHttpRequest(url, EspHttpRequest.METHOD_GET);
        }
        else
        {
            request = new EspHttpRequest(url, EspHttpRequest.METHOD_POST);
        }
        if (isInstantly)
        {
            BasicHttpParams params = new BasicHttpParams();
            params.setParameter(EspHttpRequest.ESP_INSTANTLY, true);
            request.setParams(params);
        }
        // Add Headers
        for (int i = 0; i < headers.length; i++)
        {
            HeaderPair header = headers[i];
            request.addHeader(header.getName(), header.getValue());
        }
        // Set Entity
        if (json != null)
        {
            try
            {
                StringEntity se = new StringEntity(json.toString(), HTTP.UTF_8);
                request.setEntity(se);
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        return request;
    }
    
    private static String encodingUrl(String url)
    {
        final char[] specChars = {'+', ':'};
        
        String header;
        String body;
        if (url.startsWith("https://"))
        {
            header = "https://";
        }
        else if (url.startsWith("http://"))
        {
            header = "http://";
        }
        else
        {
            header = "";
        }
        
        body = url.substring(header.length());
        for (char specChar : specChars)
        {
            String encodedStr = encodingSpecUrlChar(specChar);
            body = body.replace("" + specChar, encodedStr);
        }
        
        return header + body;
    }
    
    private static String encodingSpecUrlChar(char c)
    {
        int asciiCode = (int)c;
        String hexStr = Integer.toHexString(asciiCode).toUpperCase(Locale.ENGLISH);
        if (hexStr.length() < 2)
        {
            hexStr = "0" + hexStr;
        }
        
        String result = "%" + hexStr;
        return result;
    }
    
    public static JSONObject executeHttpRequest(HttpClient httpclient, HttpUriRequest httpRequest,
        Runnable disconnectedCallback)
    {
        boolean isRetry = true;
        JSONObject result = null;
        for (int retry = 0; result == null && isRetry && retry < SOCKET_CONNECT_RETRY_TIME; ++retry)
        {
            isRetry = false;
            if (retry > 0)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    log.warn("executeHttpRequest InterruptedException");
                    break;
                }
            }
            HttpResponse response;
            try
            {
                response = httpclient.execute(httpRequest);
                
                int statusCode = response.getStatusLine().getStatusCode();
                
                HttpEntity entity = response.getEntity();
                if (entity == null && disconnectedCallback == null)
                {
                    log.warn("executeHttpRequest entity == null && disconnectedCallback == null");
                    break;
                }
                
                String resultStr = null;
                // when disconnectedCallbak!=null means no response command is executing
                if (disconnectedCallback == null)
                {
                    resultStr = EntityUtils.toString(entity);
                }
                
                if (!TextUtils.isEmpty(resultStr))
                {
                    log.info("executeHttpRequest result str = " + resultStr);
                    try
                    {
                        result = new JSONObject(resultStr);
                        if (!result.has(KEY_STATUS))
                        {
                            result.put(KEY_STATUS, statusCode);
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        result = null;
                    }
                }
                else
                {
                    log.info("executeHttpRequest result str = null");
                    result = new JSONObject();
                }
                if (result != null && disconnectedCallback == null)
                {
                    entity.consumeContent();
                }
            }
            catch (ClientProtocolException e)
            {
                e.printStackTrace();
                log.debug("Catch ClientProtocolException");
            }
            catch (SSLPeerUnverifiedException e)
            {
                __setHttpsUnsupported();
            }
            catch (IOException e)
            {
                if (e instanceof HttpHostConnectException)
                {
                    log.info("executeHttpRequest():: isRetry1 = true");
                    isRetry = true;
                }
                if (e instanceof ConnectTimeoutException)
                {
                    log.info("executeHttpRequest():: isRetry2 = true");
                    isRetry = true;
                }
                e.printStackTrace();
            }
        }
        httpRequest.abort();
        
        if (isRetry && disconnectedCallback != null)
        {
            disconnectedCallback.run();
        }
        
        return result;
    }
}
