package com.espressif.iot.command.device.espbutton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.net.rest2.EspMeshHttpUtil;
import com.espressif.iot.device.IEspDevice;
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
    private IEspButtonConfigureListener mListener = null;
    
    private static final long TIMEOUT = 60000L;
    private static final long PING_INTERVAL = 3000L;
    
    private static final int PORT = 8000;
    private static final int SO_TIMEOUT = 8000;
    private Socket mSocket;
    private PrintStream mPrintStream;
    private BufferedReader mBufferedReader;
    
    private Queue<JSONObject> mCacheResponseQueue = new LinkedList<JSONObject>();
    private static final int RESPONSE_TYPE_ASK = 0;
    private static final int RESPONSE_TYPE_PING = 1;
    
    private static final int ASK_DEVICE_COMMAND_LEN = 20;
    
    private int mRequestPairNum;
    
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
        
        String localUrl = getLocalUrl(mInetDevice.getInetAddress());
        if (!connect(localUrl))
        {
            log.debug("EspButtonConfigure connect failed");
            return finishWithResult(RESULT_FAILED);
        }
        
        // Send broadcast
        if (!broadcastButtonInfo(newTempKey, newMacAddress, oldMacAddress))
        {
            log.debug("EspButtonConfigure broadcast failed");
            return finishWithResult(RESULT_FAILED);
        }
        
        long waitTime = 0L;
        int result = COMMAND_CONTINUE;
        for (int i = 0; i < 1000; i++)
        {
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
                    waitTime = 0L;
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
            
            waitTime += PING_INTERVAL;
            if (waitTime > TIMEOUT)
            {
                // timeout
                return finishWithResult(RESULT_SUC);
            }
        }
        
        return finishWithResult(RESULT_FAILED);
    }
    
    private boolean connect(String url)
    {
        try
        {
            URL _URL = new URL(url);
            InetAddress address = InetAddress.getByName(_URL.getHost());
            mSocket = new Socket(address, PORT);
            mSocket.setKeepAlive(true);
            mSocket.setSoTimeout(SO_TIMEOUT);
            mPrintStream = new PrintStream(mSocket.getOutputStream());
            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            
            return true;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private void disconnect()
    {
        try
        {
            if (mPrintStream != null)
            {
                mPrintStream.close();
            }
            if (mBufferedReader != null)
            {
                mBufferedReader.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            if (mSocket != null)
            {
                mSocket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private int finishWithResult(int result)
    {
        disconnect();
        return result;
    }
    
    private void post(String content)
    {
        if (!content.endsWith("\r\n"))
        {
            content += "\r\n";
        }
        content = content.replace("\\/", "/");
        log.debug("EspButtonConfigure post " + content);
        mPrintStream.print(content);
    }
    
    private JSONObject receive()
    {
        try
        {
            String response = readLine();//  mBufferedReader.readLine();
            JSONObject result = new JSONObject(response);
            log.info("EspButtonConfigure receive() " + result.toString());
            
            return result;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private String readLine() throws IOException
    {
        StringBuilder result = new StringBuilder();
        int i;
        while ((i = mBufferedReader.read()) != -1)
        {
            char c = (char)i;
            result.append(c);
            if (c == '\n')
            {
                if (result.toString().contains("command"))
                {
                    int endLength = ASK_DEVICE_COMMAND_LEN - result.length();
                    if (endLength > 0)
                    {
                        char[] ends = new char[endLength];
                        int actuallyReadLen = mBufferedReader.read(ends);
                        if (actuallyReadLen != endLength)
                        {
                            log.error("readLine() command wrong length");
                            throw new IOException();
                        }
                        result.append(ends);
                    }
                    break;
                }
                else
                {
                    break;
                }
            }
        }
        
        return result.toString();
    }
    
    private JSONObject receive(int respType)
    {
        JSONObject response;
        while((response = receive()) != null)
        {
            switch(respType)
            {
                case RESPONSE_TYPE_ASK:
                    if (response.optString(KEY_PATH, null) == null)
                    {
                        return response;
                    }
                    else
                    {
                        mCacheResponseQueue.add(response);
                        break;
                    }
                case RESPONSE_TYPE_PING:
                    if (response.optString(KEY_PATH).equals(PATH_PING))
                    {
                        return response;
                    }
                    else
                    {
                        mCacheResponseQueue.add(response);
                        break;
                    }
            }
        }
        
        return null;
    }
    
    private void addMeshJSONKey(JSONObject json, String bssid)
        throws JSONException
    {
        String localPort = MeshUtil.getPortForMesh(mSocket.getLocalPort());
        json.put(KEY_SPORT, localPort);
        String localHostAddress = MeshUtil.getIpAddressForMesh(mSocket.getLocalAddress().getHostAddress());
        json.put(KEY_SIP, localHostAddress);
        
        String mac = MeshUtil.getMacAddressForMesh(bssid);
        json.put(KEY_MDEV_MAC, mac);
    }
    
    private boolean askDeviceAvailable()
    {
        if (!mInetDevice.getIsMeshDevice())
        {
            return true;
        }
        
        final int retryTime = 3;
        for (int i = 0; i < retryTime; i++)
        {
            String command = EspMeshHttpUtil.createDeviceAvailableRequestContent();
            post(command);
            JSONObject result = receive(RESPONSE_TYPE_ASK);
            if (result == null)
            {
                return false;
            }
            
            if (EspMeshHttpUtil.checkDeviceAvailable(result))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean broadcastButtonInfo(String newTempKey, String newMacAddress,
        String... oldMacAddress)
    {
        boolean result;
        if (!askDeviceAvailable())
        {
            result = false;
        }
        else
        {
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
                
                if (mInetDevice.getIsMeshDevice())
                {
                    String bssid;
                    if (mDeviceList.size() > 1)
                    {
                        if (mBroadcast)
                        {
                            bssid = BROADCAST_MAC;
                        }
                        else
                        {
                            bssid = MULTICAST_MAC;
                            List<String> macs = new ArrayList<String>();
                            for (IEspDevice device : mDeviceList)
                            {
                                macs.add(device.getBssid());
                            }
                            MeshUtil.addMulticastJSONValue(postJSON, macs);
                        }
                    }
                    else
                    {
                        bssid = mInetDevice.getBssid();
                    }
                    addMeshJSONKey(postJSON, bssid);
                }
                
                post(postJSON.toString());
                JSONObject response = receive();
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
        }
        
        if (mListener != null)
        {
            mListener.onBroadcastComplete(mInetDevice, result);
        }
        
        return result;
    }
    
    private int ping()
    {
        if (!askDeviceAvailable())
        {
            return COMMAND_OVER;
        }
        
        JSONObject postJSON = new JSONObject();
        try
        {
            postJSON.put(KEY_PATH, PATH_PING);
            if (mInetDevice.getIsMeshDevice())
            {
                addMeshJSONKey(postJSON, mDeviceMac);
            }
            post(postJSON.toString());
            JSONObject response = receive(RESPONSE_TYPE_PING);
            if (response != null)
            {
                return COMMAND_CONTINUE;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return COMMAND_OVER;
    }
    
    private int postPairPermit(boolean permit)
    {
        if (!askDeviceAvailable())
        {
            return COMMAND_OVER;
        }
        
        JSONObject postJSON = new JSONObject();
        int status = permit ? HttpStatus.SC_OK : HttpStatus.SC_FORBIDDEN;
        try
        {
            postJSON.put(Status, status);
            postJSON.put(KEY_PATH, PATH_PAIR_REQUEST);
            if (mInetDevice.getIsMeshDevice())
            {
                addMeshJSONKey(postJSON, mDeviceMac);
            }
            post(postJSON.toString());
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
