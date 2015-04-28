package com.espressif.iot.command.device.remote;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.EspStatusRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandRemoteGetStatusInternet implements IEspCommandRemoteGetStatusInternet
{
    private final static Logger log = Logger.getLogger(EspCommandRemoteGetStatusInternet.class);
    
    private IEspStatusRemote getCurrentRemoteStatus(String deviceKey)
    {
        
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        
        JSONObject result = null;
        
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        result = EspBaseApiUtil.Get(URL, header);
        if (result == null)
        {
            return null;
        }
        try
        {
            int status = -1;
            try
            {
                if (result != null)
                {
                    status = Integer.parseInt(result.getString(Status));
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            if (status == HttpStatus.SC_OK)
            {
                JSONObject data = result.getJSONObject(Datapoint);
                int address = data.getInt(X);
                int cmd = data.getInt(Y);
                int rep = data.getInt(Z);
                IEspStatusRemote statusRemote = new EspStatusRemote();
                statusRemote.setAddress(address);
                statusRemote.setCommand(cmd);
                statusRemote.setRepeat(rep);
                return statusRemote;
            }
            else
            {
                return null;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public IEspStatusRemote doCommandRemoteGetStatusInternet(String deviceKey)
    {
        IEspStatusRemote result = getCurrentRemoteStatus(deviceKey);
        log.debug(Thread.currentThread().toString() + "##doCommandRemoteGetStatusInternet(deviceKey=[" + deviceKey
            + "]): " + result);
        return result;
    }
    
}
