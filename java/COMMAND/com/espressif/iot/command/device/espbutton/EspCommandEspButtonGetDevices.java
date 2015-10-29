package com.espressif.iot.command.device.espbutton;

import java.net.InetAddress;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.MeshUtil;

public class EspCommandEspButtonGetDevices implements IEspCommandEspButtonGetDevices
{
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/device/button/devices";
    }
    
    @Override
    public List<String> doCommandEspButtonGetDevices(IEspDevice inetDevice, String macAddress)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + macAddress);
        String url = getLocalUrl(inetDevice.getInetAddress());
        JSONObject resultJSON = EspBaseApiUtil.GetForJson(url, inetDevice.getBssid(), header);
        if (resultJSON != null)
        {
            try
            {
                int status = resultJSON.getInt(Status);
                if (status == HttpStatus.SC_OK)
                {
                    int macLen = resultJSON.getInt(KEY_MAC_LEN);
                    String mac = resultJSON.getString(KEY_MAC);
                    List<String> result = MeshUtil.getRawBssidListByMacs(mac, macLen);
                    
                    return result;
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
}
