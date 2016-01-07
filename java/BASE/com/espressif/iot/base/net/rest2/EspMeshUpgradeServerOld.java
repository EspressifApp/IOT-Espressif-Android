package com.espressif.iot.base.net.rest2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.util.Base64Util;
import com.espressif.iot.util.MeshUtil;

public class EspMeshUpgradeServerOld
{
    private final Logger log = Logger.getLogger(EspMeshUpgradeServerOld.class);
    
    private final String ESCAPE = "\r\n";
    
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
    
    private final String MAC = "mdev_mac";
    
    private final String DEVICE_ROM = "device_rom";
    
    private final String ROM_BASE64 = "rom_base64";
    
    private final String GET = "get";
    
    private final String SPORT = "sport";
    
    private final String SIP = "sip";
    
    private final byte[] mUser1Bin;
    
    private final byte[] mUser2Bin;
    
    private boolean mIsFinished;
    
    private final InetAddress mInetAddr;
    
    private final String mDeviceBssid;
    
    private final Socket mSocket;
    
    private final int COMMAND_LEN = 20;
    
    private final int MESH_PORT_INT = 8000;
    
    private boolean mIsSuc;
    
    enum RequestType
    {
        INVALID, MESH_DEVICE_UPGRADE_LOCAL, MESH_DEVICE_UPGRADE_LOCAL_SUC, MESH_DEVICE_UPGRADE_LOCAL_FAIL
    }
    
    private EspMeshUpgradeServerOld(byte[] user1bin, byte[] user2bin, InetAddress inetAddr, String deviceBssid)
    {
        this.mUser1Bin = user1bin;
        this.mUser2Bin = user2bin;
        this.mInetAddr = inetAddr;
        this.mDeviceBssid = deviceBssid;
        this.mSocket = new Socket();
    }
    
    public static EspMeshUpgradeServerOld createInstance(byte[] user1bin, byte[] user2bin, InetAddress inetAddr,
        String deviceBssid)
    {
        EspMeshUpgradeServerOld instance = new EspMeshUpgradeServerOld(user1bin, user2bin, inetAddr, deviceBssid);
        return instance;
    }
    
    /**
     * read one line from the socket's inputstream
     * 
     * @param socket the socket to be read from
     * @return one line or null
     */
    private String readLine(Socket socket)
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return reader.readLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * read command of is device available from device
     * @param socket the socket to be read from
     * @return is device available result
     */
    private String readCommand(Socket socket)
    {
        DataInputStream inputStream = null;
        StringBuilder sb = new StringBuilder();
        int receiveCount = 0;
        int receiveValue = 0;
        try
        {
            inputStream = new DataInputStream(socket.getInputStream());
            while ((receiveValue = inputStream.read()) != -1)
            {
                char c = (char)receiveValue;
                sb.append(c);
                if (++receiveCount >= COMMAND_LEN)
                {
                    break;
                }
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    private boolean isDeviceAvailable(Socket socket)
    {
        // build is device available request
        String request = MeshTypeUtil.createIsDeviceAvailableRequestContent();
        // send is device available
        __write(socket, request);
        // receive is device available
        String response = readCommand(socket);
        JSONObject responseJson = null;
        try
        {
            if (response != null)
            {
                responseJson = new JSONObject(response);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        // parse is device available
        boolean isDeviceAvailable = responseJson == null ? false : MeshTypeUtil.checkIsDeviceAvailable(responseJson);
        log.debug("isDeviceAvailable(): " + isDeviceAvailable + ", responseJson:" + responseJson);
        return isDeviceAvailable;
    }
    
    /**
     * write message to socket's outputstream directly
     * 
     * @param socket the socket to be written
     * @param msg the message to be written
     * @return wheter the message is written suc
     */
    private boolean __write(Socket socket, String msg)
    {
        // add writer
        DataOutputStream writer;
        // write
        try
        {
            writer = new DataOutputStream(socket.getOutputStream());
            writer.writeBytes(msg);
            writer.flush();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * write message to socket's outputstream if device is available
     * 
     * @param socket the socket to be written
     * @param msg the message to be written
     * @return wheter the message is written suc
     */
    private boolean write(Socket socket, String msg)
    {
        final int retryTotal = 3;
        boolean isDeviceAvailable = false;
        for (int retry = 0; !isDeviceAvailable && retry < retryTotal; ++retry)
        {
            isDeviceAvailable = isDeviceAvailable(socket);
            if (isDeviceAvailable)
            {
                break;
            }
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        if (!isDeviceAvailable)
        {
            log.warn("bh write() device isn't available, return false");
            return false;
        }
        else
        {
            log.error("bh __write() entrance");
            boolean isSuc = __write(socket, msg);
            log.error("bh __write() isSuc: " + isSuc);
            return isSuc;
        }
    }
    
    /**
     * close the socket
     * 
     * @param socket the socket to be closed
     */
    private void close(Socket socket)
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * check whether the socket is null or closed
     * 
     * @param socket the socket to be checked
     * @return whether the socket is closed
     */
    private boolean isClosed(Socket socket)
    {
        return socket != null ? socket.isClosed() : true;
    }
    
    /**
     * connect the socket to the device
     * 
     * @return whether it is connected suc
     */
    public boolean connect(int timeout)
    {
        SocketAddress sockaddr = new InetSocketAddress(mInetAddr, MESH_PORT_INT);
        try
        {
            mSocket.connect(sockaddr, timeout);
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * close the socket
     */
    public void close()
    {
        if (mSocket != null)
        {
            close(mSocket);
        }
    }
    
    /**
     * build mesh device upgrade request which is sent to mesh device
     * @return the request which is sent to mesh device
     */
    private String buildMeshDeviceUpgradeRequest1(String version)
    {
        String method = GET;
        String url = "http://" + mInetAddr.getHostAddress() + "/v1/device/rpc/";
        JSONObject jsonPost = new JSONObject();
        String localInetAddress = mSocket.getLocalAddress().getHostAddress();
        int localPort = mSocket.getLocalPort();
        try
        {
            jsonPost.put(SIP, MeshUtil.getIpAddressForMesh(localInetAddress));
            jsonPost.put(SPORT, MeshUtil.getPortForMesh(localPort));
            jsonPost.put(MAC, MeshUtil.getMacAddressForMesh(mDeviceBssid));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        EspHttpRequestBaseEntity requestEntity = new EspHttpRequestBaseEntity(method, url, jsonPost.toString());
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
    
    /**
     * request mesh device upgrading
     * 
     * @param version the version of bin to be upgraded
     * @return whether the mesh device is ready to upgrade
     */
    public boolean requestUpgrade(String version)
    {
        // build request
        String request = buildMeshDeviceUpgradeRequest1(version);
        // send request to mesh device
        boolean isWriteSuc = write(mSocket, request);
        if (!isWriteSuc)
        {
            log.warn("requestUpgrade() fail for write fail, return false");
            return false;
        }
        // receive response from the mesh device
        String response = readLine(mSocket);
        if (response == null)
        {
            log.warn("requestUpgrade() fail for readLine fail, return false");
        }
        // analyze the response
        boolean isResponseSuc = analyzeUpgradeResponse1(response);
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
     * @param requestJson the request from mesh device
     * @return the response to be sent to mesh device
     */
    private String executeMeshDeviceUpgradeLocal(JSONObject requestJson)
    {
        try
        {
            String bssid = requestJson.getString(MAC);
            String sip = requestJson.getString(SIP);
            String sport = requestJson.getString(SPORT);
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
            jsonDeviceRom.put(MAC, bssid);
            jsonDeviceRom.put(SIP, sip);
            jsonDeviceRom.put(SPORT, sport);
            jsonDeviceRom.put(ROM_BASE64, "__rombase64");
            jsonResponse.put(DEVICE_ROM, jsonDeviceRom);
            jsonResponse.put(STATUS, 200);
            return jsonResponse.toString().replace("__rombase64", new String(encoded)) + ESCAPE;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * execute mesh device upgrade local suc
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
        requestEntity.putQueryParams(MAC, MeshUtil.getMacAddressForMesh(mDeviceBssid));
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
        // receive request from the mesh device
        String request = readLine(mSocket);
        if (request == null)
        {
            return false;
        }
        log.debug("handle(): receive request from mesh device:" + request);
        JSONObject requestJson = null;
        try
        {
            requestJson = new JSONObject(request);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
        // analyze the request and build the response
        RequestType requestType = analyzeUpgradeRequest1(requestJson);
        String response = null;
        switch(requestType)
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
            boolean isWriteSuc = write(mSocket, response);
            log.debug("handle(): send response to mesh device isSuc:" + isWriteSuc);
            return isWriteSuc;
        }
        return false;
    }
    
    public boolean listen(long timeout)
    {
        if (isClosed(mSocket))
        {
            throw new IllegalStateException("listen(): mSocket has been closed already");
        }
        
        // clear mIsSuc and mIsFinished
        mIsSuc = false;
        mIsFinished = false;
        
        long start = System.currentTimeMillis();
        while (!mIsFinished && System.currentTimeMillis() - start < timeout)
        {
            if (!handle())
            {
                // when the request is invalid, sleep 100ms to let other threads run
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        if (!mIsFinished && !mIsSuc)
        {
            log.warn("listen fail for timeout:" + timeout + " ms");
        }
        
        close(mSocket);
        return mIsSuc;
    }
    
}
