package com.espressif.iot.command.device.espbutton;

import java.net.InetAddress;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.EspButtonKeySettings;

public class EspCommandEspButtonActionSet extends EspCommandEspButtonActionAbs implements IEspCommandEspButtonActionSet
{
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/device/espnow/action_set";
    }
    
    @Override
    public boolean doCommandEspButtonActionSet(IEspDevice device, EspButtonKeySettings settings)
    {
        EspButtonKeySettings.Action pressAction;
        String keyPress;
        String keyPressArg;
        if (settings.getShortPressAction() != null)
        {
            pressAction = settings.getShortPressAction();
            keyPress = KEY_SHORT_PRESS;
            keyPressArg = KEY_SHROT_PRESS_ARG;
        }
        else
        {
            pressAction = settings.getLongPressAction();
            keyPress = KEY_LONG_PRESS;
            keyPressArg = KEY_LONG_PRESS_ARG;
        }
        
        String keyValue = getKeyValue(settings.getId());
        keyPress = keyPress.replace(BUTTON_KEY_REPLACE, keyValue);
        keyPressArg = keyPressArg.replace(BUTTON_KEY_REPLACE, keyValue);
        
        String func = getFuncValue(pressAction.getFunc());
        String funcArg = getArgValue(pressAction);
        
        try
        {
            JSONObject postJSON = new JSONObject();
            postJSON.put(keyPress, func);
            postJSON.put(keyPressArg, funcArg);
            
            String url = getLocalUrl(device.getInetAddress());
            JSONObject responseJSON;
            if (device.getIsMeshDevice())
            {
                responseJSON = EspBaseApiUtil.PostForJson(url, device.getBssid(), postJSON);
            }
            else
            {
                responseJSON = EspBaseApiUtil.Post(url, postJSON);
            }
            if (responseJSON != null)
            {
                int httpStatus = responseJSON.getInt(Status);
                return httpStatus == HttpStatus.SC_OK;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
}
