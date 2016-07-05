package com.espressif.iot.util;

import android.graphics.Color;

public class ColorUtil {
    public static int getGradientColor(int sColor, int dColor, float percent) {
        int a = ave(Color.alpha(sColor), Color.alpha(dColor), percent);
        int r = ave(Color.red(sColor), Color.red(dColor), percent);
        int g = ave(Color.green(sColor), Color.green(dColor), percent);
        int b = ave(Color.blue(sColor), Color.blue(dColor), percent);
        int color = Color.argb(a, r, g, b);

        return color;
    }

    private static int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }
}
