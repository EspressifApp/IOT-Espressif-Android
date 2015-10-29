package com.espressif.iot.command.device.espbutton;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.espbutton.BEspButton;
import com.espressif.iot.espbutton.IEspButtonGroup;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.MeshUtil;

public class EspCommandEspButtonGroupList implements IEspCommandEspButtonGroupList
{

    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/device/button/devices/?list_by_group";
    }

    @Override
    public List<IEspButtonGroup> doCommandEspButtonListGroup(IEspDevice inetDevice, String buttonMac)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + buttonMac);
        String url = getLocalUrl(inetDevice.getInetAddress());
        JSONObject resultJSON = EspBaseApiUtil.GetForJson(url, inetDevice.getBssid(), header);
        if (resultJSON != null)
        {
            try
            {
                int status = resultJSON.getInt(Status);
                if (status == HttpStatus.SC_OK)
                {
                    List<IEspButtonGroup> result = new ArrayList<IEspButtonGroup>();
                    JSONArray groupsArray = resultJSON.getJSONArray(KEY_GROUPS);
                    for (int i = 0; i < groupsArray.length(); i++)
                    {
                        JSONObject groupJSON = groupsArray.getJSONObject(i);
                        long id = groupJSON.getLong(KEY_GROUP_ID);
                        int macLen = groupJSON.getInt(KEY_MAC_LEN);
                        String macs = groupJSON.getString(KEY_MAC);
                        
                        List<String> bssids = MeshUtil.getRawBssidListByMacs(macs, macLen);
                        
                        IEspButtonGroup buttonGroup = BEspButton.getInstance().allocEspButtonGroup();
                        buttonGroup.setId(id);
                        buttonGroup.addDevicesBssid(bssids);
                        
                        result.add(buttonGroup);
                    }
                    
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
