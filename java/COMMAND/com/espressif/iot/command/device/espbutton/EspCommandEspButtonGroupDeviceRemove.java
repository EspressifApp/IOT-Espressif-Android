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

public class EspCommandEspButtonGroupDeviceRemove implements IEspCommandEspButtonGroupDeviceRemove
{

    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/device/button/groups/devices/?action=delete";
    }

    @Override
    public boolean doCommandEspButtonGroupRemoveDevice(IEspDevice inetDevice, String buttonMac, long groupId,
        List<IEspDevice> removeDevices)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + buttonMac);
        String url = getLocalUrl(inetDevice.getInetAddress());
        try
        {
            JSONObject postJSON = new JSONObject();
            postJSON.put(KEY_GROUP_ID, groupId);
            postJSON.put(KEY_MAC_LEN, removeDevices.size());
            StringBuilder devicesMacs = new StringBuilder();
            for (IEspDevice device : removeDevices)
            {
                String deviceMac = MeshUtil.getMacAddressForMesh(device.getBssid());
                devicesMacs.append(deviceMac);
            }
            postJSON.put(KEY_MAC, devicesMacs.toString());
            
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
