package com.espressif.iot.command.device.espbutton;

import com.espressif.iot.type.device.other.EspButtonKeySettings;

public abstract class EspCommandEspButtonActionAbs
{
    private static final String FUNC_VALUE_NIL = "nil";
    private static final String FUNC_VALUE_SET_COLOR = "func1";
    private static final String FUNC_VALUE_TURN_ONOFF = "func2";
    private static final String FUNC_VALUE_SET_TIMER = "func3";
    private static final String FUNC_VALUE_SET_BRIGHTNESS = "func4";
    
    protected String getKeyValue(int keyId)
    {
        switch (keyId)
        {
            case 1:
                return "1";
            case 2:
                return "2";
            case 3:
                return "3";
            case 4:
                return "4";
            case 5:
                return "U";
            case 6:
                return "D";
            case 7:
                return "L";
            case 8:
                return "R";
            default:
                return "null";
        }
    }
    
    protected String getFuncValue(EspButtonKeySettings.Func func)
    {
        switch (func)
        {
            case NIL:
                return FUNC_VALUE_NIL;
            case SET_COLOR:
                return FUNC_VALUE_SET_COLOR;
            case TURN_ONOFF:
                return FUNC_VALUE_TURN_ONOFF;
            case SET_TIMER:
                return FUNC_VALUE_SET_TIMER;
            case SET_BRIGHTNESS:
                return FUNC_VALUE_SET_BRIGHTNESS;
        }
        
        return FUNC_VALUE_NIL;
    }
    
    protected EspButtonKeySettings.Func getFunc(String funcValue)
    {
        if (funcValue.equals(FUNC_VALUE_SET_COLOR))
        {
            return EspButtonKeySettings.Func.SET_COLOR;
        }
        else if (funcValue.equals(FUNC_VALUE_TURN_ONOFF))
        {
            return EspButtonKeySettings.Func.TURN_ONOFF;
        }
        else if (funcValue.equals(FUNC_VALUE_SET_TIMER))
        {
            return EspButtonKeySettings.Func.SET_TIMER;
        }
        else if (funcValue.equals(FUNC_VALUE_SET_BRIGHTNESS))
        {
            return EspButtonKeySettings.Func.SET_BRIGHTNESS;
        }
        else
        {
            return EspButtonKeySettings.Func.NIL;
        }
    }
    
    protected String getArgValue(EspButtonKeySettings.Action pressAction)
    {
        String result = FUNC_VALUE_NIL;
        switch (pressAction.getFunc())
        {
            case NIL:
                result = FUNC_VALUE_NIL;
                break;
            case SET_COLOR:
                int red = pressAction.getRed();
                int green = pressAction.getGreen();
                int blue = pressAction.getBlue();
                result = red + "," + green + "," + blue;
                break;
            case TURN_ONOFF:
                result = "" + pressAction.getTurnOnOff();
                break;
            case SET_TIMER:
                result = "" + pressAction.getTimerTime();
                break;
            case SET_BRIGHTNESS:
                result = "" + pressAction.getBrightness();
                break;
        }
        
        int broadcast = pressAction.isBroadcast() ? 1 : 0;
        result = broadcast + "," + result;
        
        return result;
    }
}
