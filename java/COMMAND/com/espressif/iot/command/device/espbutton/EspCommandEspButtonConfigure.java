package com.espressif.iot.command.device.espbutton;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.net.proxy.MeshCommunicationUtils;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.MeshUtil;

public class EspCommandEspButtonConfigure implements IEspCommandEspButtonConfigure
{
    private static final Logger log = Logger.getLogger(EspCommandEspButtonConfigure.class);
    
    /**
     * Store message from user
     */
    private Queue<String> mUserMsgQueue = new LinkedList<String>();
    
    private static final int COMMAND_CONTINUE = -1;
    private static final int COMMAND_OVER = 0;
    
    private boolean mPermitAll = false;
    private IEspDevice mInetDevice = null;
    private List<IEspDevice> mDeviceList;
    private String mDeviceMac;
    private boolean mBroadcast;
    private String mTargetUrl;
    private IEspButtonConfigureListener mListener = null;
    
    private static final long TIMEOUT = 60000L;
    private static final long PING_INTERVAL = 3000L;
    
    private Queue<JSONObject> mCacheResponseQueue = new LinkedList<JSONObject>();
    
    private int mRequestPairNum;
    
    private int mTaskSerial;
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress();
    }
    
    @Override
    public int doCommandEspButtonConfigure(List<IEspDevice> devices, String newTempKey, String newMacAddress,
        boolean isBroadcast, boolean permitAllRequest, IEspButtonConfigureListener listener, String... oldMacAddress)
    {
        mPermitAll = permitAllRequest;
        mDeviceList = devices;
        mBroadcast = isBroadcast;
        mInetDevice = mDeviceList.get(0);
        mListener = listener;
        mDeviceMac = mInetDevice.getBssid();
        mRequestPairNum = 0;
        mTargetUrl = getLocalUrl(mInetDevice.getInetAddress());
        mTaskSerial = MeshCommunicationUtils.generateLongSocketSerial();
        
        // check whether listener is null
        if (listener == null)
        {
            throw new IllegalArgumentException("IEspButtonConfigureListener is null");
        }
        
        // Send broadcast
        if (!broadcastButtonInfo(newTempKey, newMacAddress, oldMacAddress))
        {
            log.debug("EspButtonConfigure broadcast failed");
            return finishWithResult(RESULT_FAILED);
        }
        
        long startTime = System.currentTimeMillis();
        int result = COMMAND_CONTINUE;
        while (true)
        {
            if (listener.isInterrupted())
            {
                return RESULT_OVER;
            }
            
            try
            {
                Thread.sleep(PING_INTERVAL);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            
            // read message
            String userMessage = mUserMsgQueue.poll();
            if (userMessage == null)
            {
                // no user message
                JSONObject cacheResp = mCacheResponseQueue.poll();
                if (cacheResp != null)
                {
                    // get cache response
                    readReponse(cacheResp);
                }
                
                result = ping();
            }
            else if (userMessage.equals(IEspButtonConfigureListener.PAIR_PERMIT))
            {
                // get permit pair message from user
                result = postPairPermit(true);
            }
            else if (userMessage.equals(IEspButtonConfigureListener.PAIR_FORBID))
            {
                // get forbid pair message from user
                result = postPairPermit(false);
            }
            else if (userMessage.equals(IEspButtonConfigureListener.PAIR_CONTINUE))
            {
                if (++mRequestPairNum >= mDeviceList.size())
                {
                    return finishWithResult(RESULT_SUC);
                }
                
                // get pair next device message from user
                if (broadcastButtonInfo(newTempKey, newMacAddress, oldMacAddress))
                {
                    startTime = System.currentTimeMillis();
                    result = COMMAND_CONTINUE;
                }
                else
                {
                    return finishWithResult(RESULT_FAILED);
                }
            }
            else if (userMessage.equals(IEspButtonConfigureListener.PAIR_OVER))
            {
                // get complete pair message from user
                return finishWithResult(RESULT_OVER);
            }
            
            if (result == COMMAND_OVER)
            {
                return finishWithResult(RESULT_FAILED);
            }
            
            if (System.currentTimeMillis() - startTime > TIMEOUT)
            {
                // timeout
                if (isBroadcast)
                {
                    if (mRequestPairNum > 0)
                    {
                        return finishWithResult(RESULT_SUC);
                    }
                    else
                    {
                        return finishWithResult(RESULT_FAILED);
                    }
                    
                }
                else
                {
                    return finishWithResult(RESULT_FAILED);
                }
            }
        }
    }
    
    private int finishWithResult(int result)
    {
        return result;
    }
    
    private boolean broadcastButtonInfo(String newTempKey, String newMacAddress,
        String... oldMacAddress)
    {
        boolean result;
        
        JSONObject postJSON = new JSONObject();
        try
        {
            JSONObject buttonNewJSON = new JSONObject();
            buttonNewJSON.put(KEY_TEMP_KEY, newTempKey);
            buttonNewJSON.put(KEY_BUTTON_MAC, newMacAddress);
            postJSON.put(KEY_BUTTON_NEW, buttonNewJSON);
            postJSON.put(KEY_PATH, PATH_BROADCAST);
            
            if (oldMacAddress.length == 0)
            {
                postJSON.put(KEY_REPLACE, 0);
            }
            else
            {
                postJSON.put(KEY_REPLACE, 1);
                
                JSONObject buttonRemoveJSON = new JSONObject();
                StringBuilder oldMacs = new StringBuilder();
                for (int i = 0; i < oldMacAddress.length; i++)
                {
                    oldMacs.append(MeshUtil.getMacAddressForMesh(oldMacAddress[i]));
                }
                buttonRemoveJSON.put(KEY_MAC_LEN, oldMacAddress.length);
                buttonRemoveJSON.put(KEY_MAC, oldMacs.toString());
                postJSON.put(KEY_BUTTON_REMOVE, buttonRemoveJSON);
            }
            
            String bssid = mDeviceMac;
            HeaderPair multicastHeader = null;
            if (mInetDevice.getIsMeshDevice())
            {
                if (mDeviceList.size() > 1)
                {
                    if (mBroadcast)
                    {
                        bssid = MeshCommunicationUtils.BROADCAST_MAC;
                    }
                    else
                    {
                        bssid = MeshCommunicationUtils.MULTICAST_MAC;
                        StringBuilder macs = new StringBuilder();
                        for (IEspDevice device : mDeviceList)
                        {
                            macs.append(device.getBssid());
                        }
                        multicastHeader =
                            new HeaderPair(MeshCommunicationUtils.HEADER_MESH_MULTICAST_GROUP, macs.toString());
                    }
                }
                else
                {
                    bssid = mInetDevice.getBssid();
                }
            }

            JSONObject response;
            if (multicastHeader == null) {
                response = MeshCommunicationUtils.JsonPost(mTargetUrl, bssid, mTaskSerial, postJSON);
            } else {
                response = MeshCommunicationUtils.JsonPost(mTargetUrl, bssid, mTaskSerial, postJSON, multicastHeader);
            }

            if (response != null)
            {
                int status = response.getInt(Status);
                result = status == HttpStatus.SC_OK;
            }
            else
            {
                result = false;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            result = false;
        }
        
        if (mListener != null)
        {
            mListener.onBroadcastComplete(mInetDevice, result);
        }
        
        return result;
    }
    
    private int ping()
    {
        JSONObject postJSON = new JSONObject();
        try
        {
            postJSON.put(KEY_PATH, PATH_PING);
            
            return postPingAndReadResponse(postJSON);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return COMMAND_OVER;
    }
    
    private int postPingAndReadResponse(JSONObject postJSON) {
        JSONObject response = MeshCommunicationUtils.JsonPost(mTargetUrl, mDeviceMac, mTaskSerial, postJSON);
        if (response != null) {
            if (response.optString(KEY_PATH).equals(PATH_PING))
            {
                return COMMAND_CONTINUE;
            }
            else
            {
                mCacheResponseQueue.add(response);
                
                return readPingResponse();
            }
        }
        
        return COMMAND_OVER;
    }
    
    private int readPingResponse() {
        while (true) {
            JSONObject response = MeshCommunicationUtils.JsonReadOnly(mTargetUrl, mDeviceMac, mTaskSerial);
            if (response != null) {
                if (response.optString(KEY_PATH).equals(PATH_PING))
                {
                    return COMMAND_CONTINUE;
                }
                else
                {
                    mCacheResponseQueue.add(response);
                }
            }
            else {
                return COMMAND_OVER;
            }
        }
    }
    
    private int postPairPermit(boolean permit)
    {
        JSONObject postJSON = new JSONObject();
        int status = permit ? HttpStatus.SC_OK : HttpStatus.SC_FORBIDDEN;
        try
        {
            postJSON.put(Status, status);
            postJSON.put(KEY_PATH, PATH_PAIR_REQUEST);
            MeshCommunicationUtils.JsonNonResponsePost(mTargetUrl, mDeviceMac, mTaskSerial, postJSON);
            return COMMAND_CONTINUE;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return COMMAND_OVER;
    }
    
    private int readReponse(JSONObject response)
    {
        try
        {
            String path = response.getString(KEY_PATH);
            log.info("path = " + path);
            if (path.equals(PATH_PING))
            {
                return COMMAND_CONTINUE;
            }
            else if (path.equals(PATH_PAIR_REQUEST))
            {
                mDeviceMac = response.getString(KEY_DEVICE_MAC);
                mDeviceMac = MeshUtil.getRawMacAddress(mDeviceMac);
                if (mPermitAll)
                {
                    mUserMsgQueue.add(IEspButtonConfigureListener.PAIR_PERMIT);
                }
                else
                {
                    String buttonMac = response.getString(KEY_BUTTON_MAC);
                    if (mListener != null)
                    {
                        mListener.receivePairRequest(mDeviceMac, buttonMac, mUserMsgQueue);
                    }
                }
                
                return COMMAND_CONTINUE;
            }
            else if (path.equals(PATH_PAIR_RESULT))
            {
                if (mPermitAll)
                {
                    mUserMsgQueue.add(IEspButtonConfigureListener.PAIR_CONTINUE);
                }
                else
                {
                    String deviceMac = response.getString(KEY_DEVICE_MAC);
                    deviceMac = MeshUtil.getRawMacAddress(mDeviceMac);
                    boolean configResult = response.getInt(KEY_RESULT) == 1;
                    if (mListener != null)
                    {
                        mListener.receivePairResult(deviceMac, configResult, mUserMsgQueue);
                    }
                }
                
                return COMMAND_CONTINUE;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return COMMAND_CONTINUE;
    }
}
