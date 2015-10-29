package com.espressif.iot.command.device.light;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.util.MeshUtil;

public class EspCommandLightPostStatusLocal implements IEspCommandLightPostStatusLocal
{
    private final static Logger log = Logger.getLogger(EspCommandLightPostStatusLocal.class);
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=light";
    }
    
    private JSONObject getRequestJSONObject(IEspStatusLight statusLight, boolean isResponseRequired)
    {
        JSONObject request = new JSONObject();
        JSONObject rgb = new JSONObject();
        try
        {
            rgb.put(Red, statusLight.getRed());
            rgb.put(Green, statusLight.getGreen());
            rgb.put(Blue, statusLight.getBlue());
            rgb.put(CWhite, statusLight.getCWhite());
            rgb.put(WWhite, statusLight.getWWhite());
            request.put(Period, statusLight.getPeriod());
            request.put(Rgb, rgb);
            if (isResponseRequired)
            {
                request.put(Response, 1);
            }
            else
            {
                request.put(Response, 0);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return request;
    }
    
    private JSONObject getRequestJSONObject(IEspStatusLight statusLight, List<String> macList,
        boolean isResponseRequired)
    {
        JSONObject result = getRequestJSONObject(statusLight, isResponseRequired);
        MeshUtil.addMulticastJSONValue(result, macList);
        
        return result;
    }
    
    private boolean postLightStatus2(InetAddress inetAddress, JSONObject postJSON, String deviceBssid,
        boolean isMeshDevice, boolean isResponseRequired, Runnable disconnectedCallback)
    {
        String uriString = getLocalUrl(inetAddress);
        JSONObject result = null;
        if (isResponseRequired)
        {
            if (deviceBssid == null || !isMeshDevice)
            {
                result = EspBaseApiUtil.Post(uriString, postJSON);
            }
            else
            {
                result = EspBaseApiUtil.PostForJson(uriString, deviceBssid, postJSON);
            }
            return (result != null);
        }
        else
        {
            // no response is available, so we treat it suc, when fail socket will be disconnected
            if (deviceBssid == null || !isMeshDevice)
            {
                // normal device
                EspBaseApiUtil.PostInstantly(uriString, postJSON, disconnectedCallback);
            }
            else
            {
                // mesh device
                EspBaseApiUtil.PostForJsonInstantly(uriString, deviceBssid, postJSON, disconnectedCallback);
            }
            return true;
        }
    }
    
    @Override
    public boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight)
    {
        boolean responseRequired = true;
        JSONObject postJSON = getRequestJSONObject(statusLight, responseRequired);
        boolean result = postLightStatus2(inetAddress, postJSON, null, false, responseRequired, null);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusLight=[" + statusLight + "]): " + result);
        return result;
    }
    
    @Override
    public boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight,
        String deviceBssid, boolean isMeshDevice)
    {
        boolean responseRequired = true;
        JSONObject postJSON = getRequestJSONObject(statusLight, responseRequired);
        boolean result = postLightStatus2(inetAddress, postJSON, deviceBssid, isMeshDevice, responseRequired, null);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusLight=[" + statusLight + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=[" + isMeshDevice
            + "]): " + result);
        return result;
    }
    
    @Override
    public void doCommandLightPostStatusLocalInstantly(InetAddress inetAddress, IEspStatusLight statusLight,
        String deviceBssid, boolean isMeshDevice, Runnable disconnectedCallback)
    {
        boolean responseRequired = false;
        JSONObject postJSON = getRequestJSONObject(statusLight, responseRequired);
        postLightStatus2(inetAddress, postJSON, deviceBssid, isMeshDevice, responseRequired, disconnectedCallback);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocalInstantly(inetAddress=["
            + inetAddress + "],statusLight=[" + statusLight + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=["
            + isMeshDevice + "])");
    }
    
    @Override
    public boolean doCommandMulticastPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight,
        List<String> bssids)
    {
        if (bssids.size() == 1)
        {
            boolean responseRequired = true;
            JSONObject postJSON = getRequestJSONObject(statusLight, responseRequired);
            return postLightStatus2(inetAddress, postJSON, bssids.get(0), true, responseRequired, null);
        }
        else
        {
            boolean result = true;
            List<String> macList = new ArrayList<String>();
            for (String bssid : bssids)
            {
                macList.add(MeshUtil.getMacAddressForMesh(bssid));
                if (macList.size() == MULTICAST_GROUP_LENGTH_LIMIT)
                {
                    if (!postMulticastCommand(inetAddress, statusLight, macList))
                    {
                        result = false;
                    }
                    macList.clear();
                }
            }
            if (!macList.isEmpty())
            {
                if (!postMulticastCommand(inetAddress, statusLight, macList))
                {
                    result = false;
                }
            }
            return result;
        }
    }
    
    private boolean postMulticastCommand(InetAddress inetAddress, IEspStatusLight statusLight, List<String> macList)
    {
        boolean responseRequired = true;
        JSONObject postJSON = getRequestJSONObject(statusLight, macList, responseRequired);
        boolean result = postLightStatus2(inetAddress, postJSON, MULTICAST_MAC, true, responseRequired, null);
        log.info("postMulticastCommand result = " + result);
        return result;
    }
}
