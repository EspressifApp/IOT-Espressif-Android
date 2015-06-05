package com.espressif.iot.command.device.New;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.type.device.DeviceInfo;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.BSSIDUtil;

public class EspCommandDeviceNewGetInfoLocal implements IEspCommandDeviceNewGetInfoLocal
{
    private final Logger log = Logger.getLogger(EspCommandDeviceNewGetInfoLocal.class);
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "client?command=status";
    }
    
    @Override
    public DeviceInfo doCommandDeviceNewGetInfoLocal(IEspDeviceNew device)
    {
        try
        {
            String gateWay = EspApplication.sharedInstance().getGateway();
            InetAddress inetAddress = null;
            try
            {
                inetAddress = InetAddress.getByName(gateWay);
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            String urlString = getLocalUrl(inetAddress);
            JSONObject jo = EspBaseApiUtil.Get(urlString);
            if (jo == null)
            {
                return null;
            }
            // get status
            
            if (jo.isNull("Status"))
            {
                return null;
            }
            JSONObject Status = jo.getJSONObject("Status");
            int status = Status.getInt("status");
            
            String type = DeviceInfo.TYPE_UNKONW;
            IOTAddress iotAddress = null;
            for (int i = 0; i < 5; i++)
            {
                iotAddress = EspBaseApiUtil.discoverDevice(BSSIDUtil.restoreSoftApBSSID(device.getBssid()));
                if (iotAddress != null)
                {
                    type = iotAddress.getDeviceTypeEnum().toString();
                    break;
                }
            }
            
            String version = "unknow";
            
            DeviceInfo info = new DeviceInfo(type, version, status);
            info.setIOTAddress(iotAddress);
            
            log.info("DeviceInfo = " + info.toString());
            return info;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
