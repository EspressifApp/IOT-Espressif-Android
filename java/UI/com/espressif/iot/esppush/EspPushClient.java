package com.espressif.iot.esppush;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.AsyncTask;

import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.RandomUtil;

public class EspPushClient
{
    private final Logger log = Logger.getLogger(getClass());
    
    private Context mContext;
    private IEspUser mUser;
    
    private static final String SERVER_HOST = "iot.espressif.cn";
    private static final int SERVER_PORT = 8000;
    
    private Socket mSocket;
    private PrintStream mPrintStream;
    private BufferedReader mBufferedReader;
    
    private static final String KEY_PATH = "path";
    private static final String KEY_METHOD = "method";
    private static final String KEY_BODY = "body";
    private static final String KEY_ACTION = "action";
    private static final String KEY_TYPE = "type";
    private static final String KEY_PNS_TOKEN ="pns_token";
    private static final String KEY_PEER_TOKEN = "peer_token";
    private static final String KEY_META = "meta";
    private static final String KEY_AUTHORIZATION = "Authorization";
    private static final String KEY_STATUS = "status";
    private static final String KEY_DATA = "data";
    private static final String KEY_ALERT = "alert";
    
    public static final String ESPPUSH_ACTION_RECEIVE_MESSAGE = "esppush_action_receive_message";
    public static final String ESPPUSH_KEY_MESSAGE = "key_message";
    
    private ConnectTask mConnectTask;
    private static final String ESP_PNS_TOKEN = "mbox-fc36a67c11d41f70cf24db0e89e2ab7e113e6a89";
    
    private static final int PEER_TOKEN_MIN_LENGTH = 41;
    private static final String TOKEN_ILLEGAL_CHARS = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
    
    public EspPushClient(Context context)
    {
        mContext = context;
        mUser = BEspUser.getBuilder().getInstance();
    }
    
    /**
     * Execute a #AsyncTask to connect server if last task is finished.
     */
    public void connect()
    {
        log.debug("connect()");
        if (mConnectTask == null)
        {
            mConnectTask = new ConnectTask();
            mConnectTask.execute();
        }
    }
    
    /**
     * Disconnect the connection with server
     */
    public synchronized void disconnect()
    {
        log.debug("disconnect()");
        if (mSocket == null)
        {
            return;
        }
        
        // Close OutputStream
        mPrintStream.close();
        try
        {
            // Close InputStream
            mBufferedReader.close();
        }
        catch (IOException i)
        {
            log.error("disconnect() close mBufferedReader IOException");
            i.printStackTrace();
        }
        try
        {
            // Close Socket
            mSocket.close();
        }
        catch (IOException e)
        {
            log.error("disconnect() close mSocket IOException");
            e.printStackTrace();
        }
        
        mSocket = null;
        mPrintStream = null;
        mBufferedReader = null;
    }
    
    /**
     * Post message to server
     * 
     * @param message
     */
    public void post(String message)
    {
        if (mPrintStream != null)
        {
            log.info("Post message = " + message);
            mPrintStream.print(message + "\n");
        }
    }
    
    /**
     * Receiver message from server. If no message, it will be blocking
     * 
     * @return
     * @throws IOException
     */
    private String receive()
        throws IOException
    {
        try
        {
            String line = mBufferedReader.readLine();
            return line;
        }
        catch (IOException e)
        {
            log.error("receive() IOException");
            throw e;
        }
    }
    
    private boolean subscribe()
        throws JSONException, IOException
    {
        JSONObject postJSON = new JSONObject();
        
        JSONObject bodyJSON = new JSONObject();
        bodyJSON.put(KEY_ACTION, "subscribe");
        bodyJSON.put(KEY_TYPE, "pns_peer");
        bodyJSON.put(KEY_PNS_TOKEN, ESP_PNS_TOKEN);
        bodyJSON.put(KEY_PEER_TOKEN, getPeerToken());
        
        JSONObject metaJSON = new JSONObject();
        metaJSON.put(KEY_AUTHORIZATION, "token " + mUser.getUserKey());
        
        postJSON.put(KEY_PATH, "/v1/mbox/");
        postJSON.put(KEY_METHOD, "POST");
        postJSON.put(KEY_BODY, bodyJSON);
        postJSON.put(KEY_META, metaJSON);
        
        post(postJSON.toString());
        
        JSONObject reponseJSON = new JSONObject(receive());
        int httpStatus = reponseJSON.getInt(KEY_STATUS);
        
        return httpStatus == HttpStatus.SC_OK;
    }
    
    /**
     * Post a ping package to server
     */
    public void ping()
    {
        log.debug("ping()");
        try
        {
            JSONObject json = new JSONObject();
            json.put(KEY_PATH, "/v1/ping/");
            json.put(KEY_METHOD, "GET");
            post(json.toString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @return whether the client socket is connected
     */
    public boolean isConnteted()
    {
        return (mSocket != null && mSocket.isConnected());
    }
    
    private class ConnectTask extends AsyncTask<Void, Void, Boolean>
    {
        
        @Override
        protected Boolean doInBackground(Void... params)
        {
            if (mSocket != null)
            {
                disconnect();
            }
            log.debug("start connecting");
            try
            {
                InetAddress address = InetAddress.getByName(SERVER_HOST);
                mSocket = new Socket(address, SERVER_PORT);
                mPrintStream = new PrintStream(mSocket.getOutputStream());
                mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                
                return subscribe();
            }
            catch (IOException e)
            {
                log.error("ConnectTask catch IOException");
                e.printStackTrace();
                return false;
            }
            catch (JSONException e)
            {
                log.error("ConnectTask catch JSONException");
                e.printStackTrace();
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            log.debug("connect result = " + result);
            if (result)
            {
                new ReceivePushMessageThread().start();
            }
            else
            {
                disconnect();
            }
            
            mConnectTask = null;
        }
    }
    
    private class ReceivePushMessageThread extends Thread
    {
        @Override
        public void run()
        {
            log.debug("start ReceivePushMessageThread");
            while (true)
            {
                try
                {
                    String receiveMsg = receive();
                    log.info("receive msg = " + receiveMsg);
                    
                    JSONObject receiveJSON = new JSONObject(receiveMsg);
                    if (receiveJSON.has(KEY_BODY))
                    {
                        String notificationMsg =
                            receiveJSON.getJSONObject(KEY_BODY)
                                .getJSONObject(KEY_DATA)
                                .getString(KEY_ALERT);
                        Intent intent = new Intent(ESPPUSH_ACTION_RECEIVE_MESSAGE);
                        intent.putExtra(ESPPUSH_KEY_MESSAGE, notificationMsg);
                        mContext.sendBroadcast(intent);
                    }
                }
                catch (IOException e)
                {
                    log.error("ReceivePushMessageThread catch IOException");
                    e.printStackTrace();
                    disconnect();
                    log.debug("stop ReceivePushMessageThread");
                    return;
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private String getPeerToken()
    {
        StringBuilder tokenBuilder = new StringBuilder();
        String temp = getMac() + getSignatureMD5();
        for (int i = 0; i < temp.length(); i++)
        {
            char c = temp.charAt(i);
            if (TOKEN_ILLEGAL_CHARS.contains("" + c))
            {
                tokenBuilder.append(c);
            }
        }
        
        if (tokenBuilder.length() < PEER_TOKEN_MIN_LENGTH)
        {
            int lessLen = PEER_TOKEN_MIN_LENGTH - tokenBuilder.length();
            tokenBuilder.append(RandomUtil.randomString(lessLen));
        }
        
        return tokenBuilder.toString();
    }
    
    private String getMac()
    {
        String macSerial = "";
        try
        {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            
            String line;
            while ((line = input.readLine()) != null)
            {
                macSerial += line.trim();
            }
            
            input.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return macSerial;
    }
    
    public String getSignatureMD5()
    {
        String packageName = mContext.getApplicationInfo().packageName;
        try
        {
            PackageInfo pi = mContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature signature = (pi.signatures)[0];
            MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(signature.toByteArray());
            byte[] result = digest.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : result)
            {
                int number = b & 0xff;
                String str = Integer.toHexString(number);
                if (str.length() == 1)
                {
                    sb.append("0");
                }
                sb.append(str);
            }
            
            return sb.toString();
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        
        return "";
    }
}
