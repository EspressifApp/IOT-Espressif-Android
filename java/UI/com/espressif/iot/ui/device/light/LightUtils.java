package com.espressif.iot.ui.device.light;

import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class LightUtils {
    private LightUtils() {
    }

    public static IEspStatusLight generateStatus(int status, int... colors) {
        IEspStatusLight result = new EspStatusLight();
        result.setStatus(status);
        switch (status) {
            case IEspStatusLight.STATUS_OFF:
            case IEspStatusLight.STATUS_ON:
                break;

            case IEspStatusLight.STATUS_COLOR:
                int red = colors[0];
                int green = colors[1];
                int blue = colors[2];
                if (red == green && red == blue) {
                    if (red == 0) {
                        result.setStatus(IEspStatusLight.STATUS_OFF);
                    } else {
                        result.setStatus(IEspStatusLight.STATUS_BRIGHT);
                        result.setWhite(red);
                    }
                } else {
                    result.setRed(red);
                    result.setGreen(green);
                    result.setBlue(blue);
                }
                break;

            case IEspStatusLight.STATUS_BRIGHT:
                result.setWhite(colors[0]);
                break;
        }
        return result;
    }
}
