package com.espressif.iot.base.net.rest2;

import java.net.InetAddress;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.net.proxy.MeshCommunicationUtils;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.Base64Util;

public class EspMeshUpgradeServer
{
    private final Logger log = Logger.getLogger(EspMeshUpgradeServer.class);
    
    private final String ACTION = "action";
    
    private final String DOWNLOAD_ROM_BASE64 = "download_rom_base64";
    
    private final String DEVICE_UPGRADE_SUC = "device_upgrade_success";
    
    private final String DEVICE_UPGRADE_FAIL = "device_upgrade_failed";
    
    private final String OFFSET = "offset";
    
    private final String TOTAL = "total";
    
    private final String SIZE = "size";
    
    private final String SIZE_BASE64 = "size_base64";
    
    private final String VERSION = "version";
    
    private final String SYS_UPGRADE = "sys_upgrade";
    
    private final String DELIVER_TO_DEVICE = "deliver_to_deivce";
    
    private final String TRUE = "true";
    
    private final String STATUS = "status";
    
    private final String FILE_NAME = "filename";
    
    private final String USER1_BIN = "user1.bin";
    
    private final String USER2_BIN = "user2.bin";
    
    private final String DEVICE_ROM = "device_rom";
    
    private final String ROM_BASE64 = "rom_base64";
    
    private final String GET = "get";
    
    private final byte[] mUser1Bin;
    
    private final byte[] mUser2Bin;
    
    private boolean mIsFirstPackage;
    
    private boolean mIsFinished;
    
    private final InetAddress mInetAddr;
    
    private final String mDeviceBssid;
    
    private boolean mIsSuc;
    
    // for mesh device upgrade local require the socket keep connection all the time,
    // mSerial is the tag to differ different sockets
    private volatile int mSerial;
    
    enum RequestType
    {
        INVALID, MESH_DEVICE_UPGRADE_LOCAL, MESH_DEVICE_UPGRADE_LOCAL_SUC, MESH_DEVICE_UPGRADE_LOCAL_FAIL
    }
    
    private EspMeshUpgradeServer(byte[] user1bin, byte[] user2bin, InetAddress inetAddr, String deviceBssid)
    {
        this.mUser1Bin = user1bin;
        this.mUser2Bin = user2bin;
        this.mInetAddr = inetAddr;
        this.mDeviceBssid = deviceBssid;
    }
    
    public static EspMeshUpgradeServer createInstance(byte[] user1bin, byte[] user2bin, InetAddress inetAddr,
        String deviceBssid)
    {
        EspMeshUpgradeServer instance = new EspMeshUpgradeServer(user1bin, user2bin, inetAddr, deviceBssid);
        return instance;
    }
    
    /**
     * build mesh device upgrade request which is sent to mesh device
     * 
     * @param url the url of the request
     * @param version the version of the upgrade bin
     * @return the request which is sent to mesh device
     */
    private String buildMeshDeviceUpgradeRequest1(String url, String version)
    {
        String method = GET;
        EspHttpRequestBaseEntity requestEntity = new EspHttpRequestBaseEntity(method, url);
        requestEntity.putQueryParams(ACTION, SYS_UPGRADE);
        requestEntity.putQueryParams(VERSION, version);
        requestEntity.putQueryParams(DELIVER_TO_DEVICE, TRUE);
        return requestEntity.toString();
    }
    
    /**
     * analyze mesh device upgrading response
     * 
     * @param response the response sent by mesh device
     * @return whether the mesh device is ready to upgrade
     */
    private boolean analyzeUpgradeResponse1(String response)
    {
        EspHttpResponseBaseEntity responseEntity = new EspHttpResponseBaseEntity(response);
        return responseEntity.isValid() && responseEntity.getStatus() == HttpStatus.SC_OK;
    }
    
    // generate long socket serial for long socket tag
    private void generateLongSocketSerial()
    {
        this.mSerial = MeshCommunicationUtils.generateLongSocketSerial();
    }
    
    /**
     * request mesh device upgrading
     * 
     * @param version the version of bin to be upgraded
     * @return whether the mesh device is ready to upgrade
     */
    public boolean requestUpgrade(String version)
    {
        generateLongSocketSerial();
        String url = "http://" + mInetAddr.getHostAddress() + "/v1/device/rpc/";
        // build request
        String request = buildMeshDeviceUpgradeRequest1(url, version);
        JSONObject postJSON = null;
        try
        {
            postJSON = new JSONObject(request);
        }
        catch (JSONException e)
        {
            throw new IllegalArgumentException("requestUpgrade() request isn't json :" + postJSON);
        }
        // send request to mesh device and receive the response
        String bssid = mDeviceBssid;
        int serial = mSerial;
        HeaderPair[] headers = null;
        JSONObject responseJson = MeshCommunicationUtils.JsonPost(url, bssid, serial, postJSON, headers);
        if (responseJson == null)
        {
            log.warn("requestUpgrade() fail, return false");
            return false;
        }
        String responseStr = responseJson.toString();
        // analyze the response
        boolean isResponseSuc = analyzeUpgradeResponse1(responseStr);
        log.debug("requestUpgrade(): " + isResponseSuc);
        return isResponseSuc;
    }
    
    /**
     * analyze mesh device upgrading request
     * 
     * @param requestJson the request sent by mesh device
     * @return the request type
     */
    private RequestType analyzeUpgradeRequest1(JSONObject requestJson)
    {
        try
        {
            JSONObject jsonGet = requestJson.getJSONObject(GET);
            String action = jsonGet.getString(ACTION);
            if (action.equals(DOWNLOAD_ROM_BASE64))
            {
                return RequestType.MESH_DEVICE_UPGRADE_LOCAL;
            }
            else if (action.equals(DEVICE_UPGRADE_SUC))
            {
                return RequestType.MESH_DEVICE_UPGRADE_LOCAL_SUC;
            }
            else if (action.equals(DEVICE_UPGRADE_FAIL))
            {
                return RequestType.MESH_DEVICE_UPGRADE_LOCAL_FAIL;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return RequestType.INVALID;
    }
    
    /**
     * execute mesh device upgrade local
     * 
     * @param requestJson the request from mesh device
     * @return the response to be sent to mesh device
     */
    private String executeMeshDeviceUpgradeLocal(JSONObject requestJson)
    {
        try
        {
            JSONObject jsonGet = requestJson.getJSONObject(GET);
            String action = jsonGet.getString(ACTION);
            String filename = jsonGet.getString(FILE_NAME);
            String version = jsonGet.getString(VERSION);
            byte[] bin = null;
            if (filename.equals(USER1_BIN))
            {
                bin = mUser1Bin;
            }
            else if (filename.equals(USER2_BIN))
            {
                bin = mUser2Bin;
            }
            else
            {
                log.warn("filename is invalid, it isn't 'user1.bin' or 'user2.bin'.");
                return null;
            }
            int total = bin.length;
            int offset = jsonGet.getInt(OFFSET);
            log.debug("__executeMeshDeviceUpgradeLocal(): offset = " + offset);
            int size = jsonGet.getInt(SIZE);
            log.debug("__executeMeshDeviceUpgradeLocal(): size = " + size);
            if (offset + size > total)
            {
                size = total - offset;
            }
            byte[] encoded = Base64Util.encode(bin, offset, size);
            int size_base64 = encoded.length;
            // Response is like this:
            // {"status": 200, "device_rom": {"rom_base64":
            // "6QMAAAQAEEAAABBAQGYAAAQOAEASwfAJAw==",
            // "filename": "user1.bin", "version": "v1.2", "offset": 0, "action":
            JSONObject jsonResponse = new JSONObject();
            JSONObject jsonDeviceRom = new JSONObject();
            jsonDeviceRom.put(FILE_NAME, filename);
            jsonDeviceRom.put(VERSION, version);
            jsonDeviceRom.put(OFFSET, offset);
            jsonDeviceRom.put(TOTAL, total);
            jsonDeviceRom.put(SIZE, size);
            jsonDeviceRom.put(SIZE_BASE64, size_base64);
            jsonDeviceRom.put(ACTION, action);
            jsonDeviceRom.put(ROM_BASE64, "__rombase64");
            jsonResponse.put(DEVICE_ROM, jsonDeviceRom);
            jsonResponse.put(STATUS, 200);
            return jsonResponse.toString().replace("__rombase64", new String(encoded));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * execute mesh device upgrade local suc
     * 
     * @return the response to be sent to mesh device
     */
    private String executeMeshDeviceUpgradeLocalSuc()
    {
        // set mIsFinished and mIsSuc
        mIsFinished = true;
        mIsSuc = true;
        // build reset request as the mesh device's response
        String uriStr = "http://" + mInetAddr.getHostAddress() + "/upgrade?action=sys_reboot";
        String method = "POST";
        EspHttpRequestBaseEntity requestEntity = new EspHttpRequestBaseEntity(method, uriStr);
        return requestEntity.toString();
    }
    
    /**
     * execute mesh device upgrade local fail
     */
    private void executeMeshDeviceUpgradeLocalFail()
    {
        // set mIsFinished and mIsSuc
        mIsFinished = true;
        mIsSuc = false;
    }
    
    /**
     * handle one request
     * 
     * @return whether handle suc
     */
    private boolean handle()
    {
        String url = "http://" + mInetAddr.getHostAddress() + "/v1/device/rpc/";
        String bssid = mDeviceBssid;
        int serial = mSerial;
        HeaderPair[] headers = null;
        // for device requirement, taskTimeout has to be set 15 seconds, but it won't take so much time except first 2
        // packages
        int taskTimeout = 15000;
        if (mIsFirstPackage)
        {
            taskTimeout = 15000;
            mIsFirstPackage = false;
        }
        // receive request from the mesh device
        JSONObject requestJson = MeshCommunicationUtils.JsonReadOnly(url, bssid, serial, taskTimeout, headers);
        if (requestJson == null)
        {
            log.warn("hancle(): requestJson is null, return false");
            return false;
        }
        log.debug("handle(): receive request from mesh device:" + requestJson);
        // analyze the request and build the response
        RequestType requestType = analyzeUpgradeRequest1(requestJson);
        String response = null;
        switch (requestType)
        {
            case INVALID:
                log.warn("handle(): requestType is INVALID");
                return false;
            case MESH_DEVICE_UPGRADE_LOCAL:
                log.debug("handle(): requestType is LOCAL");
                response = executeMeshDeviceUpgradeLocal(requestJson);
                break;
            case MESH_DEVICE_UPGRADE_LOCAL_FAIL:
                log.debug("handle(): requestType is LOCAL FAIL");
                executeMeshDeviceUpgradeLocalFail();
                break;
            case MESH_DEVICE_UPGRADE_LOCAL_SUC:
                log.debug("handle(): requestType is LOCAL SUC");
                response = executeMeshDeviceUpgradeLocalSuc();
                break;
        }
        // send the response to the mesh device
        if (response != null)
        {
            log.debug("handle(): send response to mesh device:" + response);
            JSONObject postJSON = null;
            try
            {
                postJSON = new JSONObject(response);
            }
            catch (JSONException e)
            {
                throw new IllegalArgumentException("response isn't json: " + postJSON);
            }
            boolean isWriteSuc = MeshCommunicationUtils.JsonNonResponsePost(url, bssid, serial, postJSON) != null;
            log.debug("handle(): send response to mesh device isSuc:" + isWriteSuc);
            return isWriteSuc;
        }
        return false;
    }
    
    public boolean listen(long timeout)
    {
        // clear mIsSuc and mIsFinished
        mIsSuc = false;
        mIsFinished = false;
        mIsFirstPackage = true;
        
        long start = System.currentTimeMillis();
        while (!mIsFinished && System.currentTimeMillis() - start < timeout)
        {
            if (!handle())
            {
                log.warn("listen() handle() fail");
                executeMeshDeviceUpgradeLocalFail();
                break;
            }
        }
        
        if (!mIsFinished && !mIsSuc)
        {
            log.warn("listen fail for timeout:" + timeout + " ms");
        }
        
        return mIsSuc;
    }
}
