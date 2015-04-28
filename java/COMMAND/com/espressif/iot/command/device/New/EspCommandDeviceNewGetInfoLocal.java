package com.espressif.iot.command.device.New;

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
    public DeviceInfo doCommandDeviceNewGetInfoLocal(IEspDeviceNew device)
    {
        try
        {
            String gateWay = EspApplication.sharedInstance().getGateway();
            JSONObject jo = EspBaseApiUtil.Get(GET_STATUS_URI_String.replace("192.168.4.1", gateWay));
            if (jo == null)
            {
                return null;
            }
            // get status
            JSONObject Status = jo.getJSONObject("Status");
            if (Status == null)
            {
                return null;
            }
            int status = Status.getInt("status");
            
            String type = DeviceInfo.TYPE_UNKONW;
            IOTAddress iotAddress = null;
            for (int i = 0; i < 5; i++) {
                iotAddress = EspBaseApiUtil.discoverDevice(BSSIDUtil.restoreSoftApBSSID(device.getBssid()));
                if (iotAddress  != null) {
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
