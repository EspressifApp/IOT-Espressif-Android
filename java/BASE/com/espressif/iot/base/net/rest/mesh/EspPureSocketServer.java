package com.espressif.iot.base.net.rest.mesh;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.util.Base64Util;

public class EspPureSocketServer
{
    private static final Logger log = Logger.getLogger(EspPureSocketServer.class);
    private static final String ESCAPE = "\r\n";
    private static final String ACTION = "action";
    private static final String DOWNLOAD_ROM_BASE64 = "download_rom_base64";
    private static final String DEVICE_UPGRADE_SUC = "device_upgrade_success";
    private static final String DEVICE_UPGRADE_FAIL = "device_upgrade_failed";
    private static final String OFFSET = "offset";
    private static final String TOTAL = "total";
    private static final String SIZE = "size";
    private static final String SIZE_BASE64 = "size_base64";
    private static final String VERSION = "version";
    private static final String STATUS = "status";
    private static final String FILE_NAME = "filename";
    private static final String USER1_BIN = "user1.bin";
    private static final String USER2_BIN = "user2.bin";
    private static final String ROUTER = "router";
    private static final String DEVICE_ROM = "device_rom";
    private static final String ROM_BASE64 = "rom_base64";
    private static final String GET = "get";
    private static final String SPORT = "sport";
    private static final String SIP = "sip";
    private final byte[] mUser1Bin;
    private final byte[] mUser2Bin;
    private boolean mIsFinished = false;
    private final String mIpAddr;
    private final String mRouter;
    private final String mDeviceBssid;
    
    enum RequestType
    {
        INVALID, MESH_DEVICE_UPGRADE_LOCAL, MESH_DEVICE_UPGRADE_LOCAL_SUC, MESH_DEVICE_UPGRADE_LOCAL_FAIL
    }
    
    private final EspSocketClient mClient;
    
    EspPureSocketServer(EspSocketClient client,byte[] user1bin,byte[] user2bin,String targetIpAddr
        ,String router,String deviceBssid)
    {
        this.mClient = client;
        this.mUser1Bin = user1bin;
        this.mUser2Bin = user2bin;
        this.mIpAddr = targetIpAddr;
        this.mRouter = router;
        this.mDeviceBssid = deviceBssid;
    }
    
    public boolean handle()
    {
        try
        {
            String oneline = mClient.readLine();
            if (oneline == null)
            {
                return false;
            }
            try
            {
                JSONObject json = new JSONObject(oneline);
                RequestType type = select(json);
                switch (type)
                {
                    case INVALID:
                        log.warn("invalid request,return false");
                        return false;
                    case MESH_DEVICE_UPGRADE_LOCAL:
                        log.debug("MESH_DEVICE_UPGRADE_LOCAL");
                        return __executeMeshDeviceUpgradeLocal(json);
                    case MESH_DEVICE_UPGRADE_LOCAL_SUC:
                        log.debug("MESH_DEVICE_UPGRADE_LOCAL_SUC");
                        return __executeMeshDeviceUpgradeLocalSuc();
                    case MESH_DEVICE_UPGRADE_LOCAL_FAIL:
                        log.debug("MESH_DEVICE_UPGRADE_LOCAL_FAIL");
                        return __executeMeshDeviceUpgradeLocalFail();
                    default:
                        break;
                }
            }
            catch (JSONException e)
            {
                log.warn("bad json format");
                e.printStackTrace();
                return false;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    // close the EspSocketClient
    private void __close()
    {
        try
        {
            this.mClient.close();
        }
        catch (IOException ignore)
        {
        }
    }
    
    public boolean isClosed()
    {
        return this.mClient.isClosed();
    }
    
    public boolean isFinished()
    {
        return this.mIsFinished;
    }
    
    public void close()
    {
        __close();
    }

    // select which method to execute
    // hard coding here now, for only use it once at the moment
    private RequestType select(JSONObject jsonRequest)
    {
        try
        {
            JSONObject jsonGet = jsonRequest.getJSONObject(GET);
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

//    Request is like this:
//    {"get": {"action": "download_rom_base64", "version": "v1.2", "router" : "1234FFFF","sip":"1F2F3F",
//    "sport":"8000", "filename":"user1.bin", "offset": 0, "size": 100}, "meta": {"Authorization": "token
//     746065cb68348eb376fc3ff36a4572c6fd22258e"}, "path": "/v1/device/rom/","method": "GET"}

//    Response is like this:
//    {"status": 200,  "router" : "1234FFFF","sip":"1F2F3F","sport":"8000", 
//      "device_rom": {"rom_base64":
//      "6QMAAAQAEEAAABBAQGYAAAQOAEASwfAJAw==",
//      "filename": "user1.bin", "version": "v1.2", "offset": 0, "action":
//      "download_rom_base64", "router": "", "total": 234812, "size": 100}}
    private boolean __executeMeshDeviceUpgradeLocal(JSONObject jsonRequest)
    {
        try
        {   
            log.debug("__executeMeshDeviceUpgradeLocal() entrance");
            // parse request
            String router = jsonRequest.getString(ROUTER);
            log.debug("__executeMeshDeviceUpgradeLocal(): router = " + router);
            String sip = jsonRequest.getString(SIP);
            log.debug("__executeMeshDeviceUpgradeLocal(): sip = " + sip);
            String sport = jsonRequest.getString(SPORT);
            log.debug("__executeMeshDeviceUpgradeLocal(): sport = " + sport);
            // get
            JSONObject jsonGet = jsonRequest.getJSONObject(GET);
            String action = jsonGet.getString(ACTION);
            log.debug("__executeMeshDeviceUpgradeLocal(): action = " + action);
            String filename = jsonGet.getString(FILE_NAME);
            log.debug("__executeMeshDeviceUpgradeLocal(): filename = " + filename);
            String version = jsonGet.getString(VERSION);
            log.debug("__executeMeshDeviceUpgradeLocal(): version = " + version);
            byte[] bin = null;
            if(filename.equals(USER1_BIN))
            {
                bin = mUser1Bin;
            }
            else if(filename.equals(USER2_BIN))
            {
                bin = mUser2Bin;
            }
            else
            {
                log.warn("filename is invalid, it isn't 'user1.bin' or 'user2.bin'.");
                return false;
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
//          Response is like this:
//          {"status": 200, "device_rom": {"rom_base64":
//            "6QMAAAQAEEAAABBAQGYAAAQOAEASwfAJAw==",
//            "filename": "user1.bin", "version": "v1.2", "offset": 0, "action":
//            "download_rom_base64", "router": "", "total": 234812, "size": 100, "size_base64": 134}}
            JSONObject jsonResponse = new JSONObject();
            JSONObject jsonDeviceRom = new JSONObject();
//            jsonDeviceRom.put(ROM_BASE64, new String(encoded));
            jsonDeviceRom.put(FILE_NAME, filename);
            jsonDeviceRom.put(VERSION, version);
            jsonDeviceRom.put(OFFSET, offset);
            jsonDeviceRom.put(TOTAL, total);
            jsonDeviceRom.put(SIZE, size);
            jsonDeviceRom.put(SIZE_BASE64, size_base64);
            jsonDeviceRom.put(ACTION, action);
            jsonDeviceRom.put(ROUTER, router);
            jsonDeviceRom.put(SIP, sip);
            jsonDeviceRom.put(SPORT, sport);
            jsonDeviceRom.put(ROM_BASE64, "__rombase64");
            jsonResponse.put(DEVICE_ROM, jsonDeviceRom);
//            jsonResponse.put(POST, jsonPost);
            jsonResponse.put(STATUS, 200);
            if (!mClient.isClosed())
            {
                log.debug("before writeRequest");
                mClient.writeRequest(jsonResponse.toString().replace("__rombase64", new String(encoded)) + ESCAPE);
                log.debug("after writeRequest");
                return true;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (IOException writeException)
        {
            try
            {
                mClient.close();
            }
            catch (IOException ignore)
            {
            }
            writeException.printStackTrace();
        }
        return false;
    }
    
    private void __executeReset()
    {
        // send reset command
        String uriStr = "http://" + mIpAddr + "/upgrade?command=reset";
        log.debug("__executeReset(): uriStr = " + uriStr);
        EspMeshNetUtil.executeForJson(mClient, "POST", uriStr, mRouter, mDeviceBssid, null);
    }
    
    private boolean __executeMeshDeviceUpgradeLocalSuc()
    {
        __executeReset();
        __close();
        this.mIsFinished = true;
        return true;
    }
    
    private boolean __executeMeshDeviceUpgradeLocalFail()
    {
        __close();
        return true;
    }
}
