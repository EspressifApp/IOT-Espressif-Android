package com.espressif.iot.action.device.longsocket;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspLightResultParser implements IEspLightResultParser
{
    /*
     * Singleton lazy initialization start
     */
    private EspLightResultParser()
    {
    }
    
    private static class InstanceHolder
    {
        static EspLightResultParser instance = new EspLightResultParser();
    }
    
    public static EspLightResultParser getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public IEspStatusLight parseGetStatusLocalResult(JSONObject result)
    {
        if (result == null)
        {
            return null;
        }
        try
        {
            int period = result.getInt(Period);
            JSONObject rgb = result.getJSONObject(Rgb);
            int red = rgb.getInt(Red);
            int green = rgb.getInt(Green);
            int blue = rgb.getInt(Blue);
            IEspStatusLight status = new EspStatusLight();
            status.setPeriod(period);
            status.setRed(red);
            status.setGreen(green);
            status.setBlue(blue);
            return status;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean parsePostStatusLocalResult(JSONObject result)
    {
        return result != null;
    }

    @Override
    public IEspStatusLight parseGetStatusInternetResult(JSONObject result)
    {
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
                int period = data.getInt(X);
                int red = data.getInt(Y);
                int green = data.getInt(Z);
                int blue = data.getInt(K);
                IEspStatusLight statusLight = new EspStatusLight();
                statusLight.setPeriod(period);
                statusLight.setRed(red);
                statusLight.setGreen(green);
                statusLight.setBlue(blue);
                return statusLight;
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
    public boolean parsePostStatusInternetResult(JSONObject result)
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
            return true;
        }
        else
        {
            return false;
        }
    }
    
}
