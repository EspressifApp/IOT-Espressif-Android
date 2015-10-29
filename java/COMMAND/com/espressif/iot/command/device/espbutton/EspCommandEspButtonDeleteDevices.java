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

public class EspCommandEspButtonDeleteDevices implements IEspCommandEspButtonDeleteDevices
{
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/device/button/devices/?action=delete";
    }
    
    @Override
    public boolean doCommandEspButtonDeleteDevices(IEspDevice inetDevice, String buttonMac, List<IEspDevice> delDevices)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + buttonMac);
        String url = getLocalUrl(inetDevice.getInetAddress());
        
        try
        {
            JSONObject postJSON = new JSONObject();
            postJSON.put(KEY_MAC_LEN, delDevices.size());
            StringBuilder macs = new StringBuilder();
            for (IEspDevice device : delDevices)
            {
                String mac = MeshUtil.getMacAddressForMesh(device.getBssid());
                macs.append(mac);
            }
            postJSON.put(KEY_MAC, macs.toString());
            
            JSONObject resultJSON = EspBaseApiUtil.PostForJson(url, inetDevice.getBssid(), postJSON, header);
            if (resultJSON != null)
            {
                int status = resultJSON.getInt(Status);
                return status == HttpStatus.SC_OK;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
}
