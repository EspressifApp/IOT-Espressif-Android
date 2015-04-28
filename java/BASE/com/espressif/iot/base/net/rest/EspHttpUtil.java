package com.espressif.iot.base.net.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.EspStrings;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class EspHttpUtil
{
    private final static Logger log = Logger.getLogger(EspHttpUtil.class);
    
    private final static int CONNECTION_TIMEOUT = 2 * 1000;
    
    private final static int SO_TIMEOUT = 4 * 1000;
    
    private final static int MAX_RETRY_TIMES = 3;
    
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
        DefaultHttpClient httpclient = createHttpClient();
        // create httpGet
        HttpUriRequest httpget = createHttpRequest(true, url, json, headers);
        // Set Retry Handler
        MyHttpRequestRetryHandler retryHandler = new MyHttpRequestRetryHandler();
        httpclient.setHttpRequestRetryHandler(retryHandler);
        // execute
        JSONObject result = executeHttpRequest(httpclient, httpget);
        log.debug(logTag + ":result=" + result);
        return result;
    }
    
    public static JSONObject Post(String url, JSONObject json, HeaderPair... headers)
    {
        String logTag =
            Thread.currentThread().toString() + "##" + EspHttpLogUtil.convertToCurl(false, json, url, headers);
        log.debug(logTag);
        // create httpClient
        DefaultHttpClient httpclient = createHttpClient();
        // create httpPost
        HttpUriRequest httppost = createHttpRequest(false, url, json, headers);
        // Set Retry Handler
        MyHttpRequestRetryHandler retryHandler = new MyHttpRequestRetryHandler();
        httpclient.setHttpRequestRetryHandler(retryHandler);
        // execute
        JSONObject result = executeHttpRequest(httpclient, httppost);
        log.debug(logTag + ":result=" + result);
        
        return result;
    }
    
    private static class MyHttpRequestRetryHandler implements HttpRequestRetryHandler
    {
        
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context)
        {
            if (executionCount >= MAX_RETRY_TIMES)
            {
                // Do not retry if over max retry count
                return false;
            }
            
            if (exception instanceof NoHttpResponseException)
            {
                // Retry if the server dropped connection on us
                return true;
            }
            
            if (exception instanceof SSLHandshakeException)
            {
                // Do not retry on SSL handshake exception
                return false;
            }
            
            HttpRequest request = (HttpRequest)context.getAttribute(ExecutionContext.HTTP_REQUEST);
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent)
            {
                // Retry if the request is considered idempotentreturn true;
                return true;
            }
            
            return false;
        }
        
    }
    
    private static DefaultHttpClient createHttpClient()
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        // Set Timeout
        BasicHttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
        httpclient.setParams(params);
        return httpclient;
    }
    
    private static boolean __isHttpsSupported()
    {
        SharedPreferences sp =
            EspApplication.sharedInstance().getSharedPreferences(EspStrings.Key.SYSTEM_CONFIG, Context.MODE_PRIVATE);
        return sp.getBoolean(EspStrings.Key.HTTPS_SUPPORT, true);
    }
    
    private static void __setHttpsUnsupported()
    {
        SharedPreferences sp =
            EspApplication.sharedInstance().getSharedPreferences(EspStrings.Key.SYSTEM_CONFIG, Context.MODE_PRIVATE);
        sp.edit().putBoolean(EspStrings.Key.HTTPS_SUPPORT, false).commit();
    }
    
    private static HttpUriRequest createHttpRequest(boolean isGet, String url, JSONObject json, HeaderPair... headers)
    {
        // Char '+' must convert
        url = url.replace("+", "%2B");
        if(!__isHttpsSupported())
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
        // Add Headers
        for (int i = 0; i < headers.length; i++)
        {
            HeaderPair header = headers[i];
            request.addHeader(header.getName(), header.getValue());
        }
        // SetEntity
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
    
    private static JSONObject executeHttpRequest(HttpClient httpclient, HttpUriRequest httpRequest)
    {
        HttpResponse response;
        try
        {
            response = httpclient.execute(httpRequest);
            
            HttpEntity entity = response.getEntity();
            if (entity == null)
            {
                return null;
            }
            
            JSONObject result;
            String resultStr = EntityUtils.toString(entity);
            if (!TextUtils.isEmpty(resultStr))
            {
                try
                {
                    result = new JSONObject(resultStr);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    result = null;
                }
            }
            else
            {
                result = new JSONObject();
            }
            entity.consumeContent();
            return result;
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
             e.printStackTrace();
        }
        finally
        {
            // Do not need the rest
            httpRequest.abort();
            httpclient.getConnectionManager().shutdown();
        }
        
        return null;
    }
}
