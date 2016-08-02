package com.espressif.iot.action.softap_sta_support;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;

import com.espressif.iot.action.softap_sta_support.ISSSActionDeviceUpgradeLocal;
import com.espressif.iot.action.softap_sta_support.SSSActionDeviceUpgradeLocalResult;

public class SSSActionDeviceUpgradeLocal implements ISSSActionDeviceUpgradeLocal
{
    private final static Logger log = Logger.getLogger(SSSActionDeviceUpgradeLocal.class);
    
    private String __formatString(int value)
    {
        String strValue = "";
        byte[] ary = __intToByteArray(value);
        for (int i = ary.length - 1; i >= 0; i--)
        {
            strValue += (ary[i] & 0xFF);
            if (i > 0)
            {
                strValue += ".";
            }
        }
        return strValue;
    }
    
    private byte[] __intToByteArray(int value)
    {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte)((value >>> offset) & 0xFF);
        }
        return b;
    }
    
    @Override
    public String getGatewayAddr(Context context)
    {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wm.getDhcpInfo();
        return __formatString(d.gateway);
    }
    
    @Override
    public SSSActionDeviceUpgradeLocalResult doActionSSSDeviceUpgradeLocal(String ipStr, Context context)
    {
        log.info("##doActionSSSDeviceUpgradeLocal(): ipStr = " + ipStr);
        // get which user is running in the device(user1 or user2)
        Boolean isUser1 = __getIsUser1(ipStr);
        // if user1 is running, pushing the user2.bin to device
        // vice versa
        if (isUser1 == null)
        {
            log.info("SSSActionDeviceUpgradeLocalResult.DEVICE_NOT_SUPPORT");
            return SSSActionDeviceUpgradeLocalResult.DEVICE_NOT_SUPPORT;
        }
        String filename;
        if (isUser1)
        {
            filename = USER2;
            log.info("user1.bin is running, filename = " + filename);
        }
        else
        {
            filename = USER1;
            log.info("user2.bin is running, filename = " + filename);
        }
        boolean success = false;
        // tell the device upgrading will be executed
        success = __postStart(ipStr);
        if (!success)
        {
            log.info("SSSActionDeviceUpgradeLocalResult.POST_START_FAIL");
            return SSSActionDeviceUpgradeLocalResult.POST_START_FAIL;
        }
        
        // get byte stream from assets
        byte[] data = __getByteArray(context, filename);
        if (data == null)
        {
            log.info("SSSActionDeviceUpgradeLocalResult.FILE_NOT_FOUND");
            return SSSActionDeviceUpgradeLocalResult.FILE_NOT_FOUND;
        }
        // push user.bin to device
        success = __postPushBin(ipStr, data, isUser1);
        if (!success)
        {
            log.info("SSSActionDeviceUpgradeLocalResult.PUSH_BIN_FAIL");
            return SSSActionDeviceUpgradeLocalResult.PUSH_BIN_FAIL;
        }
        
        log.info("SSSActionDeviceUpgradeLocalResult.SUC");
        return SSSActionDeviceUpgradeLocalResult.SUC;
    }
    
    @Override
    public boolean doActionSSSDevicePostReset(String ipStr)
    {
        return __postReset(ipStr);
    }
    
    @Override
    public Boolean __getIsUser1(String ipStr)
    {
        HttpClient httpClient = new DefaultHttpClient();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try
        {
            HttpGet httpGet = new HttpGet(URI_UPGRADE_GET_USER.replace("192.168.4.1", ipStr));
            HttpResponse response;
            HttpEntity httpEntity;
            // BufferedReader bufferedReader;
            StringBuilder builder = new StringBuilder();
            response = httpClient.execute(httpGet);
            // process the response
            httpEntity = response.getEntity();
            
            if (httpEntity != null)
            {
                builder = new StringBuilder();
                inputStream = httpEntity.getContent();
                
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                for (String line = null; (line = bufferedReader.readLine()) != null;)
                {
                    builder.append(line).append("\n");
                }
                // get the Json object
                JSONObject jsonObjectResult = null;
                if (builder.length() > 0)
                    jsonObjectResult = new JSONObject(builder.toString());
                else
                    jsonObjectResult = new JSONObject();
                // get getUser result
                String userResult = jsonObjectResult.getString(USER_BIN);
                if (userResult.equals(USER1))
                {
                    return true;
                }
                else if (userResult.equals(USER2))
                {
                    return false;
                }
            }
        }
        catch (ClientProtocolException e)
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
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null)
            {
                try
                {
                    bufferedReader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            httpClient.getConnectionManager().shutdown();
        }
        return null;
    }
    
    @Override
    public boolean __postStart(String ipStr)
    {
        String url = URI_UPGRADE_START.replace("192.168.4.1", ipStr);
        log.info("__postStart url = " + url);
        HttpPost httpPost = new HttpPost(url);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try
        {
            int result = httpClient.execute(httpPost).getStatusLine().getStatusCode();
            if (result == HttpStatus.SC_OK)
            {
                return true;
            }
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            httpClient.getConnectionManager().shutdown();
        }
        return true;
    }
    
    @Override
    public byte[] __getByteArray(Context context, String fileName)
    {
        log.debug("__getByteArray start");
        byte[] data = null;
        try
        {
            fileName = Environment.getExternalStorageDirectory().toString() + "/Espressif/local_bin/" + fileName;
            // InputStream input = context.getAssets().open(fileName);
            InputStream input = new FileInputStream(fileName);
            DataInputStream dis = new DataInputStream(input);
            data = new byte[dis.available()];
            log.error("data.size=" + data.length);
            dis.read(data);
            input.close();
            dis.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        log.debug("__getByteArray end");
        return data;
    }
    
    @Override
    public boolean __postPushBin(String ipStr, byte[] byteArray, boolean isUser1)
    {
        log.debug("__postPushBin");
        ByteArrayEntity arrayEntity = new ByteArrayEntity(byteArray);
        arrayEntity.setContentType("application/octet-stream");
        String uri = null;
        if (!isUser1)
        {
            uri = URI_UPGRADE_PUSH_USER1;
        }
        else
        {
            uri = URI_UPGRADE_PUSH_USER2;
        }
        uri = uri.replace("192.168.4.1", ipStr);
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(arrayEntity);
        HttpClient httpClient = new DefaultHttpClient();
        try
        {
            int result = httpClient.execute(httpPost).getStatusLine().getStatusCode();
            if (result == HttpStatus.SC_OK)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            httpClient.getConnectionManager().shutdown();
        }
        return false;
    }
    
    @Override
    public boolean __postReset(String ipStr)
    {
        HttpPost httpPost = new HttpPost(URI_UPGRADE_RESET.replace("192.168.4.1", ipStr));
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try
        {
            httpClient.execute(httpPost);
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            httpClient.getConnectionManager().shutdown();
        }
        /**
         * for some reason, after receiving reset command, the device will reboot and phone won't get response from
         * device. in almost all of situations, the reset command will suc, so return true forever
         */
        return true;
    }
    
}
