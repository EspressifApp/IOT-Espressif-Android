package com.espressif.iot.base.net.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.net.rest2.EspHttpUtil;
import com.espressif.iot.type.device.other.DeviceResponseStatus;
import com.espressif.iot.type.net.HeaderPair;

public final class MeshCommunicationUtils
{
    private static final boolean DEBUG = true;
    private static final boolean USE_LOG4J = true;
    private static final Class<?> CLASS = MeshCommunicationUtils.class;
    
    private static final String KEY_STATUS = "status";
    
    private static final int CONN_TIMEOUT = 2000;
    private static final int READ_TIMEOUT = 4000;
    
    // when Integer.MAX_VALUE urlConnection will throw IllegalArgumentException: timeout < 0,
    // and 30 * 1000 is enough to replace Integer.MAX_VALUE
    private static final int READ_TIMEOUT_INFINITE = 30 * 1000;
    
    public static final String BROADCAST_MAC = "00:00:00:00:00:00";
    public static final String MULTICAST_MAC = "01:00:5e:00:00:00";
    
    public static final String HEADER_MESH_HOST = "Mesh-Host";
    public static final String HEADER_MESH_BSSID = "Mesh-Bssid";
    public static final String HEADER_MESH_MULTICAST_GROUP = "Mesh-Group";
    public static final String HEADER_PROXY_TIMEOUT = "Proxy-Timeout";
    
    public static final String HEADER_PROTO_TYPE = "M-Proto-Type";
    public static final String HEADER_NON_RESPONSE = "Non-Response";
    public static final String HEADER_READ_ONLY = "Read-Only";
    public static final String HEADER_TASK_SERIAL = "Task-Serial";
    public static final String HEADER_TASK_TIMEOUT = "Task-Timeout";
    
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    
    public static final int SERIAL_NORMAL_TASK = 0;
    private static volatile int SERIAL_LONG_TASK = 1;
    private static final Object SERIAL_LOCK = new Object();
    private static final JSONObject JSON_EMPTY = new JSONObject();
    
    /**
     * Get a new long socket task serial
     * 
     * @return
     */
    public static int generateLongSocketSerial() {
        synchronized (SERIAL_LOCK)
        {
            return SERIAL_LONG_TASK++;
        }
    }
    
    private static JSONObject executeHttpRequest(String url, int localServerPort, String method, String bssid,
        JSONObject postJSON, boolean nonResponse, HeaderPair... headers)
    {
        HttpURLConnection urlConn = null;
        try
        {
            URL targetURL = new URL(url);
            
            String localUrl = url.replace(targetURL.getHost(), "localhost:" + localServerPort);
            MeshLog.i(DEBUG, USE_LOG4J, CLASS, "Local url = " + localUrl);
            URL localURL = new URL(localUrl);
            urlConn = (HttpURLConnection)localURL.openConnection();
            urlConn.setConnectTimeout(CONN_TIMEOUT);
            urlConn.setReadTimeout(READ_TIMEOUT_INFINITE);
            
            // Set method
            urlConn.setRequestMethod(method);
            
            // Set headers
            urlConn.addRequestProperty(HEADER_PROXY_TIMEOUT, "" + READ_TIMEOUT);
            urlConn.addRequestProperty(HEADER_MESH_HOST, targetURL.getHost());
            urlConn.addRequestProperty(HEADER_MESH_BSSID, bssid);
            if (nonResponse)
            {
                urlConn.addRequestProperty(HEADER_NON_RESPONSE, "1");
            }
            for (HeaderPair header : headers)
            {
                urlConn.addRequestProperty(header.getName(), header.getValue());
            }
            
            // Connect or post
            if (postJSON != null)
            {
                // Add necessary json
                if (!nonResponse && (bssid.equals(MULTICAST_MAC) || bssid.equals(BROADCAST_MAC))) {
                    postJSON.put(DeviceResponseStatus.KEY_RESPONSE_STATUS, DeviceResponseStatus.RESPONSE_ROOT_ONLY);
                }
                MeshLog.i(DEBUG, USE_LOG4J, CLASS, "Post json = " + postJSON.toString());
                byte[] bytes = EspHttpUtil.decoceJSON(postJSON).getBytes();
                urlConn.setDoOutput(true);
                // IllegalArgumentException: timeout < 0 when READ_TIMEOUT_INFINITE is Integer.MAX_VALUE
                OutputStream os = urlConn.getOutputStream();
                os.write(bytes);
                os.flush();
            }
            else
            {
                urlConn.connect();
            }
            
            // Read response
            InputStream is = urlConn.getInputStream();
            
            if (nonResponse)
            {
                return JSON_EMPTY;
            }
            
            StringBuilder stringBuilder = new StringBuilder();
            int i;
            while ((i = is.read()) != -1)
            {
                stringBuilder.append((char)i);
            }
            
            String jsonStr = stringBuilder.toString();
            MeshLog.i(DEBUG, USE_LOG4J, CLASS, "Response = " + jsonStr);
            JSONObject result = new JSONObject(jsonStr);
            if (!result.has(KEY_STATUS))
            {
                int httpStatus = urlConn.getResponseCode();
                result.put(KEY_STATUS, httpStatus);
            }
            return result;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // don't forget to disconnect() url connection
            if (urlConn != null)
            {
                urlConn.disconnect();
            }
        }
        return null;
    }
    
    private static HeaderPair[] newDstHeaders(HeaderPair[] srcHeaders, HeaderPair... newHeaders) {
        int srcHeadersLength = srcHeaders == null ? 0 : srcHeaders.length;
        int newHeadersLength = newHeaders == null ? 0 : newHeaders.length;
        HeaderPair[] dstHeaders = new HeaderPair[srcHeadersLength + newHeadersLength];
        for (int i = 0; i < dstHeaders.length; i++) {
            if (i < srcHeadersLength)
            {
                dstHeaders[i] = srcHeaders[i];
            }
            else
            {
                dstHeaders[i] = newHeaders[i - srcHeadersLength];
            }
        }
        
        return dstHeaders;
    }
    
    /**
     * Post Http get request to target url
     * 
     * @param url
     * @param bssid
     * @param headers
     * @return null is failed
     */
    public static JSONObject HttpGet(String url, String bssid, HeaderPair... headers)
    {
        int port = EspProxyServerImpl.getInstance().getEspProxyServerPort();
        return executeHttpRequest(url, port, METHOD_GET, bssid, null, false, headers);
    }
    
    /**
     * Post Http post request to target url
     * 
     * @param url
     * @param bssid
     * @param postJSON
     * @param headers
     * @return null is failed
     */
    public static JSONObject HttpPost(String url, String bssid, JSONObject postJSON, HeaderPair... headers)
    {
        int port = EspProxyServerImpl.getInstance().getEspProxyServerPort();
        return executeHttpRequest(url, port, METHOD_POST, bssid, postJSON, false, headers);
    }
    
    /**
     * Post Http post request to target url and don't wait response
     * 
     * @param url
     * @param bssid
     * @param postJSON
     * @param headers
     * @return null is failed, empty JSONObject is successful
     */
    public static JSONObject HttpNonResponsePost(String url, String bssid, JSONObject postJSON, HeaderPair... headers)
    {
        int port = EspProxyServerImpl.getInstance().getEspProxyServerPort();
        return executeHttpRequest(url, port, METHOD_POST, bssid, postJSON, true, headers);
    }
    
    /**
     * Just post Json to target url, no Http headers
     * 
     * @param url
     * @param bssid
     * @param postJSON
     * @param headers
     * @return null is failed
     */
    public static JSONObject JsonPost(String url, String bssid, JSONObject postJSON, HeaderPair... headers) {
        return JsonPost(url, bssid, SERIAL_NORMAL_TASK, postJSON, headers);
    }
    
    /**
     * Just post Json to target url, no Http headers
     * 
     * @param url
     * @param bssid
     * @param serial long socket task serial
     * @param postJSON
     * @param headers
     * @return null is failed
     */
    public static JSONObject JsonPost(String url, String bssid, int serial, JSONObject postJSON, HeaderPair... headers) {
        int port  = EspProxyServerImpl.getInstance().getEspProxyServerPort();
        HeaderPair jsonHeader = new HeaderPair(HEADER_PROTO_TYPE, "" + EspProxyTask.M_PROTO_JSON);
        HeaderPair serialHeader = new HeaderPair(HEADER_TASK_SERIAL, "" + serial);
        HeaderPair[] hps = newDstHeaders(headers, jsonHeader, serialHeader);
        return executeHttpRequest(url, port, METHOD_POST, bssid, postJSON, false, hps);
    }
    
    /**
     * Post read response request to target url, don't send request
     * 
     * @param url
     * @param bssid
     * @param headers
     * @return null is failed
     */
    public static JSONObject JsonReadOnly(String url, String bssid, HeaderPair... headers) {
        return JsonReadOnly(url, bssid, SERIAL_NORMAL_TASK, headers);
    }
    
    /**
     * Post read response request to target url, don't send request
     * 
     * @param url
     * @param bssid
     * @param serial
     * @param taskTimeout   task timeout
     * @param headers
     * @return
     */
    public static JSONObject JsonReadOnly(String url, String bssid, int serial, int taskTimeout, HeaderPair... headers)
    {
        int port = EspProxyServerImpl.getInstance().getEspProxyServerPort();
        HeaderPair readHeader = new HeaderPair(HEADER_READ_ONLY, "1");
        HeaderPair jsonHeader = new HeaderPair(HEADER_PROTO_TYPE, "" + EspProxyTask.M_PROTO_JSON);
        HeaderPair serialHeader = new HeaderPair(HEADER_TASK_SERIAL, "" + serial);
        HeaderPair timeoutHeader = new HeaderPair(HEADER_TASK_TIMEOUT, "" + taskTimeout);
        HeaderPair[] hps = newDstHeaders(headers, readHeader, jsonHeader, serialHeader, timeoutHeader);
        return executeHttpRequest(url, port, METHOD_POST, bssid, null, false, hps);
    }
    
    /**
     * Post read response request to target url, don't send request
     * 
     * @param url
     * @param bssid
     * @param serial long socket task serial
     * @param headers
     * @return null is failed
     */
    public static JSONObject JsonReadOnly(String url, String bssid, int serial, HeaderPair... headers)
    {
        return JsonReadOnly(url, bssid, serial, 0, headers);
    }
    
    /**
     * Just post Json to target url, no Http headers. And don't wait response
     * 
     * @param url
     * @param bssid
     * @param postJSON
     * @return null is failed, empty JSONObject is successful
     */
    public static JSONObject JsonNonResponsePost(String url, String bssid, JSONObject postJSON) {
        return JsonNonResponsePost(url, bssid, SERIAL_NORMAL_TASK, postJSON);
    }
    
    /**
     * Just post Json to target url, no Http headers. And don't wait response
     * 
     * @param url
     * @param bssid
     * @param serial long socket task serial
     * @param postJSON
     * @return null is failed, empty JSONObject is successful
     */
    public static JSONObject JsonNonResponsePost(String url, String bssid, int serial, JSONObject postJSON) {
        int port  = EspProxyServerImpl.getInstance().getEspProxyServerPort();
        HeaderPair jsonHeader = new HeaderPair(HEADER_PROTO_TYPE, "" + EspProxyTask.M_PROTO_JSON);
        HeaderPair serialHeader = new HeaderPair(HEADER_TASK_SERIAL, "" + serial);
        return executeHttpRequest(url, port, METHOD_POST, bssid, postJSON, true, jsonHeader, serialHeader);
    }
}
