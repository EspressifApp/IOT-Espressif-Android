package com.espressif.iot.base.net.rest2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.MeshUtil;

public class EspMeshHttpUtil
{
    
    private final static Logger log = Logger.getLogger(EspMeshHttpUtil.class);

    private final static String MDEV_MAC = "mdev_mac";
    
    private final static String SIP = "sip";
    
    private final static String FAKE_SIP = "FFFFFFFF";
    
    private final static String SPORT = "sport";
    
    private final static String FAKE_SPORT = "FFFF";
    
    private final static int SOCKET_CONNECT_RETRY_TIME = 3;
    
    private final static int IS_DEVICE_AVAILABLE_RETRY_TIME = 3;
    
    private final static long IS_DEVICE_AVAILABLE_INTERVAL = 200;
    
    // build command request
    private static EspHttpRequest createEspHttpCommandRequest(String uri, String command)
    {
        // Build Request
        EspHttpRequest request = new EspHttpRequest(uri, EspHttpRequest.METHOD_COMMAND);
        // Set Entity
        EspStringEntity se = null;
        try
        {
            se = new EspStringEntity(command);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        request.setEntity(se);
        
        // Return Request
        return request;
    }
    
    // build get,post request
    private static EspHttpRequest createEspHttpRequest(boolean isInstantly, String method, String uri,
        String deviceBssid, JSONObject json, HeaderPair... headers)
    {
        // Build Request
        EspHttpRequest request = null;
        if (method.equals(EspHttpRequest.METHOD_GET) || method.equals(EspHttpRequest.METHOD_POST))
        {
            request = new EspHttpRequest(uri, method);
        }
        else
        {
            throw new IllegalArgumentException("EspHttpRequest's method is invalid");
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
        
        // Add Necessary Elements
        if (json == null)
        {
            json = new JSONObject();
        }
        try
        {
            String mac = MeshUtil.getMacAddressForMesh(deviceBssid);
            json.put(SIP, FAKE_SIP);
            json.put(SPORT, FAKE_SPORT);
            json.put(MDEV_MAC, mac);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        // Set Entity
        EspStringEntity se = null;
        try
        {
            se = new EspStringEntity(json.toString() + "\r\n");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        request.setEntity(se);
        
        // Return Request
        return request;
    }
    
    private static JSONObject executeHttpRequest(HttpClient httpclient, HttpUriRequest httpRequest,
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
                e.printStackTrace();
            }
            catch (IOException e)
            {
                if (e instanceof HttpHostConnectException)
                {
                    log.info("executeHttpRequest():: isRetry = true");
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
    
    /**
     * check whether the device is available
     * 
     * @param rootInetAddrStr the root InetAddress String
     * @return
     */
    private static boolean __isDeviceAvailable(String rootInetAddrStr)
    {
        EspHttpClient client = EspHttpClient.getEspMeshHttpClient();
        String uri = "http://" + rootInetAddrStr;
        String command = MeshTypeUtil.createIsDeviceAvailableRequestContent();
        EspHttpRequest request = createEspHttpCommandRequest(uri, command);
        JSONObject respJson = executeHttpRequest(client, request, null);
        return respJson != null && MeshTypeUtil.checkIsDeviceAvailable(respJson);
    }
    
    public static boolean checkDeviceAvailable(JSONObject responseJSON)
    {
        return responseJSON != null && MeshTypeUtil.checkIsDeviceAvailable(responseJSON);
    }
    
    public static String createDeviceAvailableRequestContent()
    {
        return MeshTypeUtil.createIsDeviceAvailableRequestContent();
    }
    
    private static String getRootInetAddrStr(String uriStr)
    {
        try
        {
            URI uri = new URI(uriStr);
            return uri.getHost();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 
     * @param uriStr the uri String
     * @param deviceBssid the device's bssid
     * @param json the JSONObject
     * @param headers the headers of the request
     * @return the JSONObject result
     */
    private static JSONObject executeForJson(boolean isInstantly, boolean isGet, String uriStr, String deviceBssid,
        JSONObject json, Runnable disconnectedCallback, HeaderPair... headers)
    {
        EspHttpClient client = EspHttpClient.getEspMeshHttpClient();
        // check is device available
        boolean isDeviceAvailable = false;
        String rootInetAddrStr = getRootInetAddrStr(uriStr);
        for (int retry = 0; !isDeviceAvailable && retry < IS_DEVICE_AVAILABLE_RETRY_TIME; ++retry)
        {
            if (retry > 0)
            {
                log.warn("__executeForJson(): isDeviceAvailable = false, retry = " + retry + " time(s)");
                try
                {
                    Thread.sleep(IS_DEVICE_AVAILABLE_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    log.info("__executeForJson() InterruptedException");
                    return null;
                }
            }
            isDeviceAvailable = __isDeviceAvailable(rootInetAddrStr);
        }
        if (!isDeviceAvailable)
        {
            if (disconnectedCallback != null)
            {
                disconnectedCallback.run();
            }
            log.warn("__executeForJson(): device isn't avaialbe, return null");
            return null;
        }
        String method = null;
        if (isGet)
        {
            method = EspHttpRequest.METHOD_GET;
        }
        else
        {
            method = EspHttpRequest.METHOD_POST;
        }
        // build request
        EspHttpRequest request = createEspHttpRequest(isInstantly, method, uriStr, deviceBssid, json, headers);
        
        // execute
        JSONObject jsonResp = executeHttpRequest(client, request, disconnectedCallback);
        
        // response
        return jsonResp;
    }
    
    /**
     * 
     * @param uriStr the uri String
     * @param deviceBssid the device's bssid
     * @param json the JSONObject
     * @param headers the headers of the request
     * @return the JSONObject result
     */
    public static JSONObject PostForJson(String uriStr, String deviceBssid, JSONObject json, HeaderPair... headers)
    {
        JSONObject result = executeForJson(false, false, uriStr, deviceBssid, json, null, headers);
        log.debug(Thread.currentThread().toString() + "##PostForJson(uriStr=[" + uriStr + "],deviceBssid=[" + deviceBssid
            + "],json=[" + json + "],headers=[" + headers + "]): " + result);
        return result;
    }
    
    /**
     * 
     * @param uriStr the uri String
     * @param deviceBssid the device's bssid
     * @param headers the headers of the request
     * @return the JSONObject result
     */
    public static JSONObject GetForJson(String uriStr, String deviceBssid, HeaderPair... headers)
    {
        JSONObject result = executeForJson(false, true, uriStr, deviceBssid, null, null, headers);
        log.debug(Thread.currentThread().toString() + "##GetForJson(uriStr=[" + uriStr + "],deviceBssid=[" + deviceBssid
            + "],headers=[" + headers + "]): " + result);
        return result;
    }
    
    public static void PostForJsonInstantly(String uriStr, String deviceBssid, JSONObject json, Runnable disconnectedCallback,
        HeaderPair... headers)
    {
        disconnectedCallback = disconnectedCallback != null ? disconnectedCallback : EspHttpRequest.ESP_DUMMY_RUNNABLE;
        JSONObject result = executeForJson(true, false, uriStr, deviceBssid, json, disconnectedCallback, headers);
        log.debug(Thread.currentThread().toString() + "##PostForJsonInstantly(uriStr=[" + uriStr + "],deviceBssid=["
            + deviceBssid + "],json=[" + json + "],headers=[" + headers + "]): " + result);
    }
}
