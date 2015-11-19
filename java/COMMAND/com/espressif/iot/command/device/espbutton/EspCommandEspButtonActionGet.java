package com.espressif.iot.command.device.espbutton;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.EspButtonKeySettings;

public class EspCommandEspButtonActionGet extends EspCommandEspButtonActionAbs implements IEspCommandEspButtonActionGet
{
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/device/espnow/action_set";
    }
    
    @Override
    public List<EspButtonKeySettings> doCommandEspButtonActionGet(IEspDevice device)
    {
        String url = getLocalUrl(device.getInetAddress());
        
        JSONObject responseJSON;
        if (device.getIsMeshDevice())
        {
            responseJSON = EspBaseApiUtil.GetForJson(url, device.getBssid());
        }
        else
        {
            responseJSON = EspBaseApiUtil.Get(url);
        }
        if (responseJSON != null)
        {
            try
            {
                int httpStatus = responseJSON.getInt(Status);
                if (httpStatus == HttpStatus.SC_OK)
                {
                    List<EspButtonKeySettings> result = new ArrayList<EspButtonKeySettings>();
                    for (int i = 1; i <= 8; i++)
                    {
                        String keyValue = getKeyValue(i);
                        String keyShortPress = KEY_SHORT_PRESS.replace(BUTTON_KEY_REPLACE, keyValue);
                        String keyShortPressArg = KEY_SHROT_PRESS_ARG.replace(BUTTON_KEY_REPLACE, keyValue);
                        String keyLongPress = KEY_LONG_PRESS.replace(BUTTON_KEY_REPLACE, keyValue);
                        String keyLongPressArg = KEY_LONG_PRESS_ARG.replace(BUTTON_KEY_REPLACE, keyValue);
                        
                        String shortFuncValue = responseJSON.optString(keyShortPress);
                        if (!TextUtils.isEmpty(shortFuncValue))
                        {
                            EspButtonKeySettings shortSettings = new EspButtonKeySettings();
                            shortSettings.initShortPressAction();
                            shortSettings.setId(i);
                            EspButtonKeySettings.Action shortAction = shortSettings.getShortPressAction();
                            shortAction.setFunc(getFunc(shortFuncValue));
                            String shortArg = responseJSON.getString(keyShortPressArg);
                            setActionArgs(shortAction, shortArg);
                            
                            result.add(shortSettings);
                        }
                        
                        String longFuncValue = responseJSON.optString(keyLongPress);
                        if (!TextUtils.isEmpty(longFuncValue))
                        {
                            EspButtonKeySettings longSettings = new EspButtonKeySettings();
                            longSettings.initLongPressAction();
                            longSettings.setId(i);
                            EspButtonKeySettings.Action longAction = longSettings.getLongPressAction();
                            longAction.setFunc(getFunc(longFuncValue));
                            String longArg = responseJSON.getString(keyLongPressArg);
                            setActionArgs(longAction, longArg);
                            
                            result.add(longSettings);
                        }
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
    
    /**
     * Parse settings content
     * 
     * @param action
     * @param argStr
     */
    protected void setActionArgs(EspButtonKeySettings.Action action, String argStr)
    {
        String[] args = TextUtils.split(argStr, ",");
        if (args.length > 0)
        {
            int broadcast = Integer.parseInt(args[0]);
            action.setBroadcast(broadcast == 1);
        }
        
        switch (action.getFunc())
        {
            case NIL:
                break;
            case SET_COLOR:
                int red = Integer.parseInt(args[1]);
                int green = Integer.parseInt(args[2]);
                int blue = Integer.parseInt(args[3]);
                action.setRed(red);
                action.setGreen(green);
                action.setBlue(blue);
                break;
            case SET_TIMER:
                long timerTime = Long.parseLong(args[1]);
                action.setTimerTime(timerTime);
                break;
            case TURN_ONOFF:
                int onoff = Integer.parseInt(args[1]);
                action.setTurnOnOff(onoff);
                break;
            case SET_BRIGHTNESS:
                int brightness = Integer.parseInt(args[1]);
                action.setBrightness(brightness);
                break;
        
        }
    }
}
