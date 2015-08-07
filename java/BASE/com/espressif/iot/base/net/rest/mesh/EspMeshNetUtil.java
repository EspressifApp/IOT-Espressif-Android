package com.espressif.iot.base.net.rest.mesh;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.InputStreamUtils;
import com.espressif.iot.util.MeshUtil;

class EspMeshCommand
{
   /**
    *   struct mesh_command {
    *       struct { // command section
    *           u8 f_req:1; // flow digestion request flag;
    *           u8 f_resp:1; // flow digestion response flag;
    *           u8 resv:6; // reserve for future
    *       } comm;
    *       struct { // comamnd value
    *           u8 resv:4;
    *           u8 f_cap:4; // current capacity
    *       } val;
    *   };
    */
    private char mComm;
    
    private char mVal;
    
    public EspMeshCommand(String command)
    {
        this.mComm = command.charAt(0);
        this.mVal = command.charAt(1);
    }
    
    public boolean isRequest()
    {
        return (mComm & 0x01) != 0;
    }
    
    public boolean isResponse()
    {
        return (mComm & 0x02) != 0;
    }
    
    public boolean isFree()
    {
        return getCapability() > 0;
    }
    
    public int getCapability()
    {
        return mVal >> 4;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("f_req:" + isRequest() + "\n");
        sb.append("f_resp:" + isResponse() + "\n");
        sb.append("f_cap:" + getCapability() + "\n");
        return sb.toString();
    }
}

public class EspMeshNetUtil
{
    private static final Logger log = Logger.getLogger(EspMeshNetUtil.class);
    
    private final static String COMMAND = "command";
    
    private final static String MAC = "mdev_mac";
    
    private final static String SIP = "sip";
    
    private final static String PORT = "sport";
    
    private static final int CONNECTION_TIMEOUT = 5000;
    
    private static final int SO_TIMEOUT = 5000;
    
    private static final int MESH_PORT = 8000;
    
    private static final String METHOD_GET = "GET";
    
    private static final String METHOD_POST = "POST";
    
    private static final int CONNECT_RETRY = 3;
    
    private static final int IS_DEVICE_AVAILABLE_RETRY = 3;
    
    private static void __closeClient(EspSocketClient client)
    {
        log.info("__closeClient " + client.getTargetAddress());
        try
        {
            client.close();
        }
        catch (IOException ignore)
        {
            ignore.printStackTrace();
        }
    }
    
    private static String __createIsDeviceAvailableRequestContent(String deviceBssid, String localInetAddress, int port)
    {
        log.debug("__createIsDeviceAvailableRequestContent()");
        JSONObject json = new JSONObject();
        
        String _content = null;
        try
        {
            /**
             * struct mesh_command { 
             *      struct { // command section
             *          u8 f_req:1;// flow digestion request flag;
             *          u8 f_resp:1; // flow digestion response flag;
             *          u8 resv:6; // reserve for future
             *      } comm;
             *      struct { // comamnd value
             *          u8 resv:4;
             *          u8 f_cap:4; // current capacity
             *      } val;
             * };
             */
            // String commandValueStr = MeshUtil.ascii2String("1");
            // commandValueStr += MeshUtil.ascii2String("0");
            String commandValueStr = InputStreamUtils.byte2String(new byte[] {1, 0});
            _content = commandValueStr;
            json.put("command", "XXX");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        String content = json.toString();
        content = content.replace("XXX", _content);
        return content;
    }
    
    private static boolean __checkIsDeviceAvailable(String responseCommand)
    {
        log.debug("__checkIsDeviceAvailable()");
        if (responseCommand == null)
        {
            log.warn("__checkIsDeviceAvailable(): responseCommand is null, return false");
        }
        EspMeshCommand response = new EspMeshCommand(responseCommand);
        return response.isRequest() && response.isResponse() && response.isFree();
    }
    
    private static boolean __isDeviceAvailable(EspSocketClient client, String uriStr, String deviceBssid,
        String localInetAddress, int port)
    {
        log.debug("__isDeviceAvailable()");
        // create request
        String content = __createIsDeviceAvailableRequestContent(deviceBssid, localInetAddress, port);
        IEspSocketRequest requestEntity = new EspSocketRequestBaseEntity(METHOD_POST, uriStr, content);
        boolean isAvailable = false;
        for (int retry = 0; !isAvailable && retry < IS_DEVICE_AVAILABLE_RETRY; retry++)
        {
            // sleep 500ms when it isn't first time retry
            if (retry != 0)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    // when the Thread is interrupt, we think that there's no necessary to
                    // request __isDeviceAvailable(), just return false.
                    return false;
                }
            }
            
            // send request
            if (!__sendRequest(client, requestEntity))
            {
                log.warn("__isDeviceAvailable(): send request fail");
                continue;
            }
            
            // receive response
            IEspSocketResponse responseEntity = __receiveOneResponse(client);
            log.info("__isDeviceAvailable() response:\n" + responseEntity);
            String command = null;
            if (responseEntity != null)
            {
                try
                {
                    JSONObject resultJson = new JSONObject(responseEntity.getContentBodyStr());
                    command = resultJson.getString(COMMAND);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            
            // analyze the response to decide whether the device is available
            isAvailable = command != null ? __checkIsDeviceAvailable(command) : false;
        }
        return isAvailable;
    }
    
    private static boolean __connect(EspSocketClient client, String host, int port, int connectionTimeout)
    {
        log.debug("__connect() host:" + host + ",port:" + port + ",connectionTimeout:" + connectionTimeout);
        if (client.connect(host, port, connectionTimeout))
        {
            log.info("__connect() suc, host:" + host + ",port:" + port + ",connectionTimeout:" + connectionTimeout);
            return true;
        }
        else
        {
            log.warn("__connect() fail, host:" + host + ",port:" + port + ",connectionTimeout:" + connectionTimeout);
            return false;
        }
    }
    
    private static boolean __sendRequest(EspSocketClient client, IEspSocketRequest requestEntity)
    {
        return __sendRequest(client, requestEntity.toString());
    }
    
    private static boolean __sendRequest(EspSocketClient client, String requestStr)
    {
        log.debug("__sendRequest()");
        try
        {
            client.writeRequest(requestStr);
            log.debug("__sendRequest():\n" + requestStr);
        }
        catch (IOException e)
        {
            log.warn("__sendRequest() fail");
            return false;
        }
        log.info("__sendRequest() suc");
        return true;
    }
    
    private static IEspSocketResponse __receiveOneResponse(EspSocketClient client)
    {
        log.debug("__receiveOneResponse()");
        return client.readResponseEntity();
    }
    
    private static String __receiveOneCommandResponse(EspSocketClient client)
    {
        log.debug("__receiveOneCommandResponse()");
        return client.readCommandResponse();
    }
    
    private static JSONObject __addNecessaryElements(JSONObject json, String deviceBssid, String localInetAddress,
        int port)
    {
        log.debug("__addNecessaryElements()");
        if (json == null)
        {
            json = new JSONObject();
        }
        if (deviceBssid == null)
        {
            throw new IllegalArgumentException("__addNecessaryElements(): deviceBssid is null");
        }
        try
        {
            json.put(SIP, MeshUtil.getIpAddressForMesh(localInetAddress));
            json.put(PORT, MeshUtil.getPortForMesh(port));
            json.put(MAC, MeshUtil.getMacAddressForMesh(deviceBssid));
        }
        catch (JSONException e)
        {
            throw new IllegalArgumentException();
        }
        return json;
    }
    
    private static JSONObject __executeForJson(EspSocketClient client, String method, String uriStr,
        String deviceBssid, JSONObject json, boolean checkIsDeviceAvailable, boolean closeClientImmediately,
        int targetPort, int connectTimeout, int connectRetry, boolean isResultRead, int soTimeout,
        HeaderPair... headers)
    {
        log.debug("__executeForJson()");
        boolean isConnectRequired = false;
        // it is used just to get host by uri
        EspSocketRequestBaseEntity request1 = new EspSocketRequestBaseEntity(method, uriStr);
        String host = request1.getHost();
        if (client == null)
        {
            client = new EspSocketClient();
            isConnectRequired = true;
        }
        client.setSoTimeout(soTimeout);
        // connect
        boolean isConnectSuc = false;
        if (isConnectRequired)
        {
            for (int i = 0; i < connectRetry; i++)
            {
                if (__connect(client, host, targetPort, connectTimeout))
                {
                    isConnectSuc = true;
                    break;
                }
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            isConnectSuc = true;
        }
        if (!isConnectSuc)
        {
            __closeClient(client);
            return null;
        }
        String localInetAddress = client.getLocalAddressStr();
        int localPort = client.getLocalPort();
        
        // add necessary elements in json
        json = __addNecessaryElements(json, deviceBssid, localInetAddress, localPort);
        EspSocketRequestBaseEntity request = new EspSocketRequestBaseEntity(method, uriStr, json.toString());
        if (headers != null)
        {
            // add headers
            for (HeaderPair header : headers)
            {
                request.putHeaderParams(header.getName(), header.getValue());
            }
        }
        
        // check whether device is available
        if (checkIsDeviceAvailable && !__isDeviceAvailable(client, uriStr, deviceBssid, localInetAddress, localPort))
        {
            __closeClient(client);
            return null;
        }
        
        // send request
        if (!__sendRequest(client, request))
        {
            log.warn("__executeForJson(): send request fail");
            __closeClient(client);
            return null;
        }
        
        JSONObject jsonResult = null;
        if (isResultRead)
        {
            // receive response
            IEspSocketResponse responseEntity = __receiveOneResponse(client);
            if (responseEntity == null)
            {
                log.warn("responseEntity is null,return null");
                __closeClient(client);
                return null;
            }
            
            try
            {
                jsonResult = new JSONObject(responseEntity.getContentBodyStr());
            }
            catch (JSONException e)
            {
                log.warn("build json result fail, return null");
                __closeClient(client);
                return null;
            }
        }
        if (closeClientImmediately)
        {
            __closeClient(client);
        }
        // if (responseEntity.getStatus() != HttpStatus.SC_OK) {
        // log.warn("status 400 Bad Request");
        // return null;
        // }
        return jsonResult;
    }
    
    private static JSONArray __executeForJsonArray(EspSocketClient client, String method, String uriStr,
        String deviceBssid, JSONObject json, boolean checkIsDeviceAvailable, boolean closeClientImmdeiately,
        int targetPort, int connectTimeout, int connectRetry, boolean isResultRead, int soTimeout,
        HeaderPair... headers)
    {
        log.debug("__executeForJsonArray()");
        boolean isConnectRequired = false;
        JSONArray jsonArrayResult = new JSONArray();
        
        // it is used just to get host by uri
        IEspSocketRequest request1 = new EspSocketRequestBaseEntity(method, uriStr);
        String host = request1.getHost();
        if (client == null)
        {
            client = new EspSocketClient();
            isConnectRequired = true;
        }
        client.setSoTimeout(soTimeout);
        
        // connect
        boolean isConnectSuc = false;
        if (isConnectRequired)
        {
            for (int i = 0; i < connectRetry; i++)
            {
                if (__connect(client, host, targetPort, connectTimeout))
                {
                    isConnectSuc = true;
                    break;
                }
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            isConnectSuc = true;
        }
        if (!isConnectSuc)
        {
            __closeClient(client);
            return null;
        }
        String localInetAddress = client.getLocalAddressStr();
        int localPort = client.getLocalPort();
        
        // add necessary elements in json
        json = __addNecessaryElements(json, deviceBssid, localInetAddress, localPort);
        log.debug("json = " + json);
        EspSocketRequestBaseEntity request = new EspSocketRequestBaseEntity(method, uriStr, json.toString());
        
        if (headers != null)
        {
            // add headers
            for (HeaderPair header : headers)
            {
                request.putHeaderParams(header.getName(), header.getValue());
            }
        }
        
        // check whether device is available
        if (checkIsDeviceAvailable && !__isDeviceAvailable(client, uriStr, deviceBssid, localInetAddress, localPort))
        {
            __closeClient(client);
            return null;
        }
        
        // send request
        if (!__sendRequest(client, request))
        {
            log.warn("__executeForJsonArray(): send request fail");
            __closeClient(client);
            return null;
        }
        
        log.error("jsonArrayResult (start) ip: " + host);
        if (isResultRead)
        {
            // receive response
            IEspSocketResponse responseEntity = __receiveOneResponse(client);
            JSONObject jsonResult = null;
            while (responseEntity != null)
            {
                try
                {
                    jsonResult = new JSONObject(responseEntity.getContentBodyStr());
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                if (jsonResult != null)
                {
                    jsonArrayResult.put(jsonResult);
                    log.debug("one response is received");
                    responseEntity = __receiveOneResponse(client);
                }
                else
                {
                    break;
                }
            }
            log.debug("receive responses end");
        }
        if (closeClientImmdeiately)
        {
            __closeClient(client);
        }
        log.error("jsonArrayResult (end) ip: " + host);
        log.error("jsonArrayResult.length(): " + jsonArrayResult.length());
        log.error("jsonArrayResult: " + jsonArrayResult);
        return (jsonArrayResult.length() == 0) ? (null) : (jsonArrayResult);
    }
    
    // temp method, just for mesh
    static JSONObject executeForJson(EspSocketClient client, String method, String uriStr, String deviceBssid,
        JSONObject json, HeaderPair... headers)
    {
        return __executeForJson(client,
            method,
            uriStr,
            deviceBssid,
            json,
            true,
            true,
            MESH_PORT,
            CONNECTION_TIMEOUT,
            CONNECT_RETRY,
            true,
            SO_TIMEOUT,
            headers);
    }
    
    /**
     * execute GET to get JSONObject by Mesh Net
     * 
     * @param uriStr the uri String
     * @param deviceBssid the bssid of the device
     * @param headers the headers of the request
     * @return the JSONObject result
     */
    public static JSONObject GetForJson(String uriStr, String deviceBssid, HeaderPair... headers)
    {
        return __executeForJson(null,
            METHOD_GET,
            uriStr,
            deviceBssid,
            null,
            true,
            true,
            MESH_PORT,
            CONNECTION_TIMEOUT,
            CONNECT_RETRY,
            true,
            SO_TIMEOUT,
            headers);
    }
    
    /**
     * execute GET to get JSONArray by Mesh Net
     * 
     * @param uriStr the uri String
     * @param deviceBssid the bssid of the device
     * @param headers the headers of the request
     * @return the JSONArray result
     */
    public static JSONArray GetForJsonArray(String uriStr, String deviceBssid, HeaderPair... headers)
    {
        return __executeForJsonArray(null,
            METHOD_GET,
            uriStr,
            deviceBssid,
            null,
            true,
            true,
            MESH_PORT,
            CONNECTION_TIMEOUT,
            CONNECT_RETRY,
            true,
            SO_TIMEOUT,
            headers);
    }
    
    /**
     * execute POST to get JSONObject by Mesh Net
     * 
     * @param uriStr the uri String
     * @param deviceBssid the bssid of the device
     * @param headers the headers of the request
     * @return the JSONObject result
     */
    public static JSONObject PostForJson(String uriStr, String deviceBssid, JSONObject json, HeaderPair... headers)
    {
        return __executeForJson(null,
            METHOD_POST,
            uriStr,
            deviceBssid,
            json,
            true,
            true,
            MESH_PORT,
            CONNECTION_TIMEOUT,
            CONNECT_RETRY,
            true,
            SO_TIMEOUT,
            headers);
    }
    
    /**
     * execute POST to get JSONArray by Mesh Net
     * 
     * @param uriStr the uri String
     * @param deviceBssid the bssid of the device
     * @param headers the headers of the request
     * @return the JSONArray result
     */
    public static JSONArray PostForJsonArray(String uriStr, String deviceBssid, JSONObject json, HeaderPair... headers)
    {
        return __executeForJsonArray(null,
            METHOD_POST,
            uriStr,
            deviceBssid,
            json,
            true,
            true,
            MESH_PORT,
            CONNECTION_TIMEOUT,
            CONNECT_RETRY,
            true,
            SO_TIMEOUT,
            headers);
    }
    
    /**
     * execute GET to get JSONObject by Mesh Net
     * 
     * @param client the EspSocketClient or null(if null new client will be created)
     * @param uriStr the uri String
     * @param deviceBssid the bssid of the device
     * @param checkIsDeviceAvailable whether check is device available before sending request
     * @param closeClientImmdeiately whether close the client immediate after sending request
     * @param targetPort the port of the target
     * @param connectTimeout connection timeout in milliseconds
     * @param connectRetry connect retry time
     * @param isResultRead whether the result will be read
     * @param soTimeout socket read timeout
     * @param headers the headers of the request
     * @return the JSONObject result
     */
    public static JSONObject GetForJson(EspSocketClient client, String uriStr, String deviceBssid,
        boolean checkIsDeviceAvailable, boolean closeClientImmdeiately, int targetPort, int connectTimeout,
        int connectRetry, boolean isResultRead, int soTimeout, HeaderPair... headers)
    {
        return __executeForJson(client,
            METHOD_GET,
            uriStr,
            deviceBssid,
            null,
            checkIsDeviceAvailable,
            closeClientImmdeiately,
            targetPort,
            connectTimeout,
            connectRetry,
            isResultRead,
            soTimeout,
            headers);
    }
    
    /**
     * execute POST to get JSONObject by Mesh Net
     * 
     * @param client the EspSocketClient or null(if null new client will be created)
     * @param uriStr the uri String
     * @param deviceBssid the bssid of the device
     * @param json the json to be posted
     * @param checkIsDeviceAvailable whether check is device available before sending request
     * @param closeClientImmdeiately whether close the client immediate after sending request
     * @param targetPort the port of the target
     * @param connectTimeout connection timeout in milliseconds
     * @param connectRetry connect retry time
     * @param isResultRead whether the result will be read
     * @param soTimeout socket read timeout
     * @param headers the headers of the request
     * @return the JSONObject result
     */
    public static JSONObject PostForJson(EspSocketClient client, String uriStr, String deviceBssid, JSONObject json,
        boolean checkIsDeviceAvailable, boolean closeClientImmdeiately, int targetPort, int connectTimeout,
        int connectRetry, boolean isResultRead, int soTimeout, HeaderPair... headers)
    {
        return __executeForJson(client,
            METHOD_POST,
            uriStr,
            deviceBssid,
            json,
            checkIsDeviceAvailable,
            closeClientImmdeiately,
            targetPort,
            connectTimeout,
            connectRetry,
            isResultRead,
            soTimeout,
            headers);
    }
    
    static List<IOTAddress> __GetTopoIOTAddressList(InetAddress rootInetAddress, String rootDeviceBssid,
        String deviceBssid)
    {
        log.debug("__GetTopoIOTAddressList(): entrance");
        // build base parameters
        String uriStr = "http:/" + rootInetAddress;
        // it is used just to get host by uri
        IEspSocketRequest request1 = new EspSocketRequestBaseEntity(METHOD_GET, uriStr);
        String host = request1.getHost();
        EspSocketClient client = new EspSocketClient();
        client.setSoTimeout(SO_TIMEOUT);
        // connect
        log.debug("__GetTopoIOTAddressList(): connect");
        boolean isConnectSuc = false;
        for (int i = 0; i < CONNECT_RETRY; i++)
        {
            if (__connect(client, host, MESH_PORT, CONNECTION_TIMEOUT))
            {
                isConnectSuc = true;
                break;
            }
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        if (!isConnectSuc)
        {
            __closeClient(client);
            return null;
        }
        String localInetAddress = client.getLocalAddressStr();
        int localPort = client.getLocalPort();
        // build request
        String getTopoRequest = null;
        if (deviceBssid == null)
        {
            getTopoRequest = MeshTypeUtil.createGetTopologyRequestContent();
        }
        else
        {
            getTopoRequest = MeshTypeUtil.createIsDeviceLocalRequestContent(deviceBssid);
        }
        // check whether device is available
        log.debug("__GetTopoIOTAddressList(): check whether device is available");
        if (!__isDeviceAvailable(client, uriStr, deviceBssid, localInetAddress, localPort))
        {
            __closeClient(client);
            return null;
        }
        // send request
        log.debug("__GetTopoIOTAddressList(): send request");
        if (!__sendRequest(client, getTopoRequest))
        {
            log.warn("__executeForJsonArray(): send request fail");
            __closeClient(client);
            return null;
        }
        log.debug("__GetTopoIOTAddressList(): host: " + host);
        // receive and parse response
        log.debug("__GetTopoIOTAddressList(): receive and parse response");
        JSONObject jsonResult = null;
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        boolean hasNext = true;
        while (hasNext)
        {
            String responseStr = __receiveOneCommandResponse(client);
            log.debug("__GetTopoIOTAddressList(): receive one response: " + responseStr);
            if (responseStr == null)
            {
                hasNext = false;
            }
            else
            {
                try
                {
                    jsonResult = new JSONObject(responseStr);
                    if (deviceBssid != null)
                    {
                        hasNext = false;
                        IOTAddress iotAddress =
                            MeshTypeUtil.extractDeviceIOTAddress(jsonResult,
                                rootInetAddress,
                                deviceBssid,
                                rootDeviceBssid);
                        if (iotAddress != null)
                        {
                            log.debug("__GetTopoIOTAddressList(): find iotAddress:" + iotAddress);
                            iotAddressList.add(iotAddress);
                        }
                    }
                    else
                    {
                        hasNext =
                            MeshTypeUtil.extractDeviceIOTAddresses(jsonResult,
                                rootInetAddress,
                                iotAddressList,
                                rootDeviceBssid);
                        log.debug("__GetTopoIOTAddressList(): current iotAddressList:" + iotAddressList + ", hasNext:"
                            + hasNext);
                    }
                }
                catch (JSONException e)
                {
                    hasNext = false;
                    e.printStackTrace();
                }
            }
        }
        return iotAddressList;
    }
    
    static IOTAddress GetTopoIOTAddress(InetAddress rootInetAddress, String rootBssid, String deviceBssid)
    {
        List<IOTAddress> iotAddressList = __GetTopoIOTAddressList(rootInetAddress, rootBssid, deviceBssid);
        if (iotAddressList == null || iotAddressList.isEmpty())
        {
            log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddress(rootInetAddress=[" + rootInetAddress
                + "],rootBssid=[" + rootBssid + "],deviceBssid=[" + deviceBssid + "]): empty, return null");
            return null;
        }
        IOTAddress iotAddress0 = iotAddressList.get(0);
        log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddress(rootInetAddress=[" + rootInetAddress
            + "],rootBssid=[" + rootBssid + "],deviceBssid=[" + deviceBssid + "]): " + iotAddress0);
        return iotAddress0;
    }
    
    static List<IOTAddress> GetTopoIOTAddressList(InetAddress rootInetAddress, String rootBssid)
    {
        List<IOTAddress> iotAddressList = __GetTopoIOTAddressList(rootInetAddress, rootBssid, null);
        log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddressList(rootInetAddress=[" + rootInetAddress
            + "],rootBssid=[" + rootBssid + "]): " + iotAddressList);
        return iotAddressList;
    }
    
    static List<IOTAddress> __GetSubParentTopoIOTAddressList2(EspSocketClient client, InetAddress rootInetAddress,
        String deviceBssid, boolean isSubDevices)
    {
        log.debug("__GetSubParentTopoIOTAddressList2(): entrance");
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        // build request
        String uriStr = "http://" + rootInetAddress.getHostAddress() + "/config?command=mesh_info";
        String method = METHOD_GET;
        boolean checkIsDeviceAvailable = true;
        boolean closeClientImmediately = false;
        int targetPort = MESH_PORT;
        // the parameter isn't used for client isn't null
        int connectTimeout = CONNECTION_TIMEOUT;
        int connectRetry = CONNECT_RETRY;
        boolean isResultRead = true;
        int soTimeout = SO_TIMEOUT;
        // send request and receive request
        JSONObject jsonResult =
            __executeForJson(client,
                method,
                uriStr,
                deviceBssid,
                null,
                checkIsDeviceAvailable,
                closeClientImmediately,
                targetPort,
                connectTimeout,
                connectRetry,
                isResultRead,
                soTimeout);
        log.debug("__GetSubParentTopoIOTAddressList2(): jsonResult:" + jsonResult);
        if (jsonResult == null)
        {
            // return null to extinguish device hasn't child
            log.warn("__GetSubParentTopoIOTAddressList2(): jsonResult is null, return null");
            return null;
        }
        // parse response
        try
        {
            if (isSubDevices)
            {
                log.debug("__GetSubParentTopoIOTAddressList2(): isSubDevices = true");
                JSONArray jsonArrayChildren = null;
                if (!jsonResult.isNull("children"))
                {
                    jsonArrayChildren = jsonResult.getJSONArray("children");
                    for (int i = 0; i < jsonArrayChildren.length(); ++i)
                    {
                        JSONObject jsonChild = jsonArrayChildren.getJSONObject(i);
                        if (!jsonChild.isNull("type") && !jsonChild.isNull("mac"))
                        {
                            String typeStr = jsonChild.getString("type");
                            EspDeviceType deviceTypeEnum = EspDeviceType.getEspTypeEnumByString(typeStr);
                            if (deviceTypeEnum == null)
                            {
                                // no more devices, so break
                                break;
                            }
                            String bssid = jsonChild.getString("mac");
                            IOTAddress iotAddress = new IOTAddress(bssid, rootInetAddress, true);
                            iotAddress.setParentBssid(deviceBssid);
                            iotAddress.setEspDeviceTypeEnum(deviceTypeEnum);
                            log.debug("__GetSubParentTopoIOTAddressList2(): iotAddress: " + iotAddress + " is added");
                            iotAddressList.add(iotAddress);
                        }
                    }
                }
            }
            else
            {
                log.debug("__GetSubParentTopoIOTAddressList2(): isSubDevices = false");
                JSONObject jsonParent = null;
                if (!jsonResult.isNull("parent") && !jsonResult.isNull("type"))
                {
                    jsonParent = jsonResult.getJSONObject("parent");
                    String typeStr = jsonResult.getString("type");
                    EspDeviceType deviceTypeEnum = EspDeviceType.getEspTypeEnumByString(typeStr);
                    if (deviceTypeEnum != null && !jsonParent.isNull("mac"))
                    {
                        String parentBssid = jsonParent.getString("mac");
                        IOTAddress iotAddress = new IOTAddress(deviceBssid, rootInetAddress, true);
                        iotAddress.setParentBssid(parentBssid);
                        iotAddress.setEspDeviceTypeEnum(deviceTypeEnum);
                        log.debug("__GetSubParentTopoIOTAddressList2(): iotAddress: " + iotAddress + " is added");
                        iotAddressList.add(iotAddress);
                    }
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return iotAddressList;
    }
    
    static List<IOTAddress> __GetTopoIOTAddressList2(InetAddress rootInetAddress, String deviceBssid,
        boolean isSubDevices)
    {
        log.debug("__GetTopoIOTAddressList2(): entrance");
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        // connect to root device
        // build base parameters
        String uriStr = "http://" + rootInetAddress.getHostAddress();
        // it is used just to get host by uri
        IEspSocketRequest request1 = new EspSocketRequestBaseEntity(METHOD_GET, uriStr);
        String host = request1.getHost();
        EspSocketClient client = new EspSocketClient();
        client.setSoTimeout(SO_TIMEOUT);
        // connect
        log.debug("__GetTopoIOTAddressList2(): connect");
        boolean isConnectSuc = false;
        for (int i = 0; i < CONNECT_RETRY; i++)
        {
            if (__connect(client, host, MESH_PORT, CONNECTION_TIMEOUT))
            {
                isConnectSuc = true;
                break;
            }
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        if (!isConnectSuc)
        {
            __closeClient(client);
            return Collections.emptyList();
        }
        
        int index = 0;
        String currentBssid = deviceBssid;
        IOTAddress next = null;
        do
        {
            next = null;
            // get sub topo list
            List<IOTAddress> subParentIOTAddressList =
                __GetSubParentTopoIOTAddressList2(client, rootInetAddress, currentBssid, isSubDevices);
            if (subParentIOTAddressList == null)
            {
                // when some error occurs, subParentIOTAddressList will null
                log.warn("__GetTopoIOTAddressList2(): subParentIOTAddressList is null");
                // although the client should be closed already, close it again to make it close surely
                __closeClient(client);
                return iotAddressList;
            }
            for (IOTAddress subIOTAddress : subParentIOTAddressList)
            {
                // only added the device which hasn't been added
                if (!iotAddressList.contains(subIOTAddress))
                {
                    iotAddressList.add(subIOTAddress);
                }
            }
            // check whether the device is the last one
            if (index < iotAddressList.size())
            {
                next = iotAddressList.get(index);
                currentBssid = next.getBSSID();
            }
            // move to next index
            ++index;
        } while (next != null && isSubDevices);
        
        __closeClient(client);
        return iotAddressList;
    }
    
    static IOTAddress GetTopoIOTAddress2(InetAddress rootInetAddress, String deviceBssid)
    {
        List<IOTAddress> iotAddressList = __GetTopoIOTAddressList2(rootInetAddress, deviceBssid, false);
        if (iotAddressList == null || iotAddressList.isEmpty())
        {
            log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddress2(rootInetAddress=[" + rootInetAddress
                + "],deviceBssid=[" + deviceBssid + "]): empty, return null");
            return null;
        }
        IOTAddress iotAddress0 = iotAddressList.get(0);
        log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddress2(rootInetAddress=[" + rootInetAddress
            + "],deviceBssid=[" + deviceBssid + "]): " + iotAddress0);
        return iotAddress0;
    }
    
    static List<IOTAddress> GetTopoIOTAddressList2(InetAddress rootInetAddress, String rootBssid)
    {
        List<IOTAddress> iotAddressList = __GetTopoIOTAddressList2(rootInetAddress, rootBssid, true);
        log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddressList2(rootInetAddress=[" + rootInetAddress
            + "],rootBssid=[" + rootBssid + "]): " + iotAddressList);
        return iotAddressList;
    }
    
}
