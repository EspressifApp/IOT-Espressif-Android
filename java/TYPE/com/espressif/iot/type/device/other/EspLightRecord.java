package com.espressif.iot.type.device.other;

/**
 *                linear
 * norm_value---------------------seekbar_value
 *                                     |
 *                                     |
 *                                     |
 *                                     | nonlinear
 *                                     |                                 
 *                                     |                                 
 *                linear               |
 * color_value---------------------status_value--------------------------post_value
 *                                              
 * 
 * max_rgb = period * 1000 / 45
 * 
 *                  range               function
 * norm_value:      [0,1.00]            normalization value
 * seekbar_value:   [0,max_rgb]         value of seekbar used by UI
 * status_value:    [0,max_rgb]         temp value
 * post_value:      [0,max_rgb]         value to send to device
 * color_value:     [0,255]             value of color
 * 
 * initXX() is used to init value by original one
 * setXX2YY is used to set YY by XX
 * circleAtX() is used to calculate by draw a circle at X axis
 * circleAtY() is used to calculate by draw a circle at Y axis
 * 
 * initByXX() is used to init instance by some parameters
 * updateByXX() is used to update current status and save last status
 * undoStatus() is used to restore last status
 * getXX() get method(NOTE: no set method, initByXX() and updateByXX() replace setXX())
 * 
 * @author afunx
 *
 */

public class EspLightRecord
{
    private static boolean DEBUG = false;
    
    private static int PERIOD_MIN = 1000;
    
    private static int PERIOD_MAX = 10000;
    
    private static int COLOR_RGB_MIN = 0;
    
    private static int COLOR_RGB_MAX = 254;
    
    private static double NORM_RGBW_MIN = 0.0;
    
    private static double NORM_RGBW_MAX = 1.0;
    
    // sb = significant bit
    private static int NORM_RGBW_SB = 2;
    
    private static int K = 21;
    
    private int period;
    
    private int seekbar_rgbw_min;
    
    private int seekbar_rgbw_max;
    
    private int status_rgbw_min;
    
    private int status_rgbw_max;
    
    private int post_rgbw_min;
    
    private int post_rgbw_max;
    
    private double norm_red;
    
    private double norm_green;
    
    private double norm_blue;
    
    private double norm_cw;
    
    private double norm_ww;
    
    private int seekbar_red;
    
    private int seekbar_green;
    
    private int seekbar_blue;
    
    private int seekbar_cw;
    
    private int seekbar_ww;
    
    private int color_red;
    
    private int color_blue;
    
    private int color_green;
    
    private int status_red;
    
    private int status_green;
    
    private int status_blue;
    
    private int status_cw;
    
    private int status_ww;
    
    private int post_red;
    
    private int post_green;
    
    private int post_blue;
    
    private int post_cw;
    
    private int post_ww;
    
    private EspLightRecord mLastLightStatus;
    
    private EspLightRecord()
    {
    }
    
    // get period
    public int getPeriod()
    {
        return period;
    }
    
    // get last period
    public int getLastPeriod()
    {
        return mLastLightStatus.period;
    }
    
    // get norm value str
    private static String __getNormStrByNorm(double norm, int sb, double norm_rgbw_min, double norm_rgbw_max)
    {
        double _norm = round(norm, sb, norm_rgbw_min, norm_rgbw_max);
        return form(_norm, NORM_RGBW_SB);
    }
    
    public static String getNormStrByNorm(double norm, double norm_rgbw_min, double norm_rgbw_max)
    {
        return __getNormStrByNorm(norm, NORM_RGBW_SB, norm_rgbw_min, norm_rgbw_max);
    }
    
    public String getNormRedStr()
    {
        return __getNormStrByNorm(norm_red, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
    }
    
    public String getNormGreenStr()
    {
        return __getNormStrByNorm(norm_green, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
    }
    
    public String getNormBlueStr()
    {
        return __getNormStrByNorm(norm_blue, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
    }
    
    public String getNormCwStr()
    {
        return __getNormStrByNorm(norm_cw, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
    }
    
    public String getNormWwStr()
    {
        return __getNormStrByNorm(norm_ww, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
    }
    
    // get seekbar value
    public int getSeekbarRed()
    {
        return seekbar_red;
    }
    
    public int getSeekbarGreen()
    {
        return seekbar_green;
    }
    
    public int getSeekbarBlue()
    {
        return seekbar_blue;
    }
    
    public int getSeekbarCw()
    {
        return seekbar_cw;
    }
    
    public int getSeekbarWw()
    {
        return seekbar_ww;
    }
    
    // get color value
    public int getColorRed()
    {
        return color_red;
    }
    
    public int getColorGreen()
    {
        return color_green;
    }
    
    public int getColorBlue()
    {
        return color_blue;
    }
    
    // get status value
    public int getStatusRed()
    {
        return status_red;
    }
    
    public int getStatusGreen()
    {
        return status_green;
    }
    
    public int getStatusBlue()
    {
        return status_blue;
    }
    
    public int getStatusCw()
    {
        return status_cw;
    }
    
    public int getStatusWw()
    {
        return status_ww;
    }
    
    // get post value
    public int getPostRed()
    {
        return post_red;
    }
    
    public int getPostGreen()
    {
        return post_green;
    }
    
    public int getPostBlue()
    {
        return post_blue;
    }
    
    public int getPostCw()
    {
        return post_cw;
    }
    
    public int getPostWw()
    {
        return post_ww;
    }
    
    // get last seekbar value
    public int getLastSeekbarRed()
    {
        return mLastLightStatus.seekbar_red;
    }
    
    public int getLastSeekbarGreen()
    {
        return mLastLightStatus.seekbar_green;
    }
    
    public int getLastSeekbarBlue()
    {
        return mLastLightStatus.seekbar_blue;
    }
    
    public int getLastSeekbarCw()
    {
        return mLastLightStatus.seekbar_cw;
    }
    
    public int getLastSeekbarWw()
    {
        return mLastLightStatus.seekbar_ww;
    }
    
    // get last color value
    public int getLastColorRed()
    {
        return mLastLightStatus.color_red;
    }
    
    public int getLastColorGreen()
    {
        return mLastLightStatus.color_green;
    }
    
    public int getLastColorBlue()
    {
        return mLastLightStatus.color_blue;
    }
    
    // get last status value
    public int getLastStatusRed()
    {
        return mLastLightStatus.status_red;
    }
    
    public int getLastStatusGreen()
    {
        return mLastLightStatus.status_green;
    }
    
    public int getLastStatusBlue()
    {
        return mLastLightStatus.status_blue;
    }
    
    public int getLastStatusCw()
    {
        return mLastLightStatus.status_cw;
    }
    
    public int getLastStatusWw()
    {
        return mLastLightStatus.status_ww;
    }
    
    // get last post value
    public int getLastPostRed()
    {
        return mLastLightStatus.post_red;
    }
    
    public int getLastPostGreen()
    {
        return mLastLightStatus.post_green;
    }
    
    public int getLastPostBlue()
    {
        return mLastLightStatus.post_blue;
    }
    
    public int getLastPostCw()
    {
        return mLastLightStatus.post_cw;
    }
    
    public int getLastPostWw()
    {
        return mLastLightStatus.post_ww;
    }
    
    public void undoStatus()
    {
        if (mLastLightStatus != null)
        {
            period = mLastLightStatus.period;
            seekbar_rgbw_min = mLastLightStatus.seekbar_rgbw_min;
            seekbar_rgbw_max = mLastLightStatus.seekbar_rgbw_max;
            status_rgbw_min = mLastLightStatus.status_rgbw_min;
            status_rgbw_max = mLastLightStatus.status_rgbw_max;
            post_rgbw_min = mLastLightStatus.post_rgbw_min;
            post_rgbw_max = mLastLightStatus.post_rgbw_max;
            norm_red = mLastLightStatus.norm_red;
            norm_green = mLastLightStatus.norm_green;
            norm_blue = mLastLightStatus.norm_blue;
            norm_cw = mLastLightStatus.norm_cw;
            norm_ww = mLastLightStatus.norm_ww;
            seekbar_red = mLastLightStatus.seekbar_red;
            seekbar_green = mLastLightStatus.seekbar_green;
            seekbar_blue = mLastLightStatus.seekbar_blue;
            seekbar_cw = mLastLightStatus.seekbar_cw;
            seekbar_ww = mLastLightStatus.seekbar_ww;
            color_red = mLastLightStatus.color_red;
            color_blue = mLastLightStatus.color_blue;
            color_green = mLastLightStatus.color_green;
            status_red = mLastLightStatus.status_red;
            status_green = mLastLightStatus.status_green;
            status_blue = mLastLightStatus.status_blue;
            status_cw = mLastLightStatus.status_cw;
            status_ww = mLastLightStatus.status_ww;
            post_red = mLastLightStatus.post_red;
            post_green = mLastLightStatus.post_green;
            post_blue = mLastLightStatus.post_blue;
            post_cw = mLastLightStatus.post_cw;
            post_ww = mLastLightStatus.post_ww;
            if (DEBUG)
            {
                System.out.println("execute undoStatus() mLastLightStatus.norm_red:" + mLastLightStatus.norm_red
                    + ",norm_red:" + norm_red);
            }
        }
    }
    
    public void saveLastStatus()
    {
        if (mLastLightStatus == null)
        {
            mLastLightStatus = new EspLightRecord();
        }
        mLastLightStatus.period = period;
        mLastLightStatus.seekbar_rgbw_min = seekbar_rgbw_min;
        mLastLightStatus.seekbar_rgbw_max = seekbar_rgbw_max;
        mLastLightStatus.status_rgbw_min = status_rgbw_min;
        mLastLightStatus.status_rgbw_max = status_rgbw_max;
        mLastLightStatus.post_rgbw_min = post_rgbw_min;
        mLastLightStatus.post_rgbw_max = post_rgbw_max;
        mLastLightStatus.norm_red = norm_red;
        mLastLightStatus.norm_green = norm_green;
        mLastLightStatus.norm_blue = norm_blue;
        mLastLightStatus.norm_cw = norm_cw;
        mLastLightStatus.norm_ww = norm_ww;
        mLastLightStatus.seekbar_red = seekbar_red;
        mLastLightStatus.seekbar_green = seekbar_green;
        mLastLightStatus.seekbar_blue = seekbar_blue;
        mLastLightStatus.seekbar_cw = seekbar_cw;
        mLastLightStatus.seekbar_ww = seekbar_ww;
        mLastLightStatus.color_red = color_red;
        mLastLightStatus.color_green = color_green;
        mLastLightStatus.color_blue = color_blue;
        mLastLightStatus.status_red = status_red;
        mLastLightStatus.status_green = status_green;
        mLastLightStatus.status_blue = status_blue;
        mLastLightStatus.status_cw = status_cw;
        mLastLightStatus.status_ww = status_ww;
        mLastLightStatus.post_red = post_red;
        mLastLightStatus.post_green = post_green;
        mLastLightStatus.post_blue = post_blue;
        mLastLightStatus.post_cw = post_cw;
        mLastLightStatus.post_ww = post_ww;
        if (DEBUG)
        {
            System.out.println("execute saveLastStatus() mLastLightStatus.norm_red:" + mLastLightStatus.norm_red
                + ",norm_red:" + norm_red);
            System.out.println("execute saveLastStatus() mLastLightStatus.seekbar_red:" + mLastLightStatus.seekbar_red
                + ",seekbar_red:" + seekbar_red);
        }
    }
    
    private static String form(double value, int sb)
    {
        String valueStr = Double.toString(value);
        int valueSb = valueStr.split("\\.")[1].length();
        if (valueSb > sb)
        {
            throw new IllegalArgumentException("value = " + value + ",sb = " + sb
                + ",valueSb > sb,please call round() method before calling form()");
        }
        else if (valueSb == sb)
        {
            return valueStr;
        }
        else
        {
            StringBuilder str = new StringBuilder(valueStr);
            for (int i = valueSb; i < sb; ++i)
            {
                str.append("0");
            }
            return str.toString();
        }
    }
    
    private static double round(double value, int sb, double min, double max)
    {
        value = restrict(value, min, max);
        if (value == min)
        {
            return min;
        }
        else if (value == max)
        {
            return max;
        }
        else
        {
            long multiple = (long)Math.pow(10, sb);
            long min_multi = (long)(min * multiple) + 1;
            long max_multi = (long)(max * multiple) - 1;
            long result_long = (long)(value * multiple * 10) + 5;
            result_long /= 10;
            result_long = restrict(result_long, min_multi, max_multi);
            double result = 1.0 * result_long / (multiple);
            return result;
        }
    }
    
    private static double restrict(double value, double min, double max)
    {
        return (value < min ? min : (value < max ? value : max));
    }
    
    private static int restrict(int value, int min, int max)
    {
        return (value < min ? min : (value < max ? value : max));
    }
    
    private static long restrict(long value, long min, long max)
    {
        return (value < min ? min : (value < max ? value : max));
    }
    
    private static int __getRgbwMax(int period)
    {
        return period * 1000 / 45;
    }
    
    public static int getRgbwMax(int period)
    {
        return __getRgbwMax(period);
    }
    
    private void initPeriod(int period)
    {
        this.period = restrict(period, PERIOD_MIN, PERIOD_MAX);
    }
    
    private void initMinMax(int period)
    {
        this.post_rgbw_min = this.status_rgbw_min = this.seekbar_rgbw_min = 0;
        this.post_rgbw_max = this.status_rgbw_max = this.seekbar_rgbw_max = __getRgbwMax(period);
    }
    
    private void initNorm(double norm_red, double norm_green, double norm_blue, double norm_cw, double norm_ww)
    {
        this.norm_red = restrict(norm_red, NORM_RGBW_MIN, NORM_RGBW_MAX);
        this.norm_green = restrict(norm_green, NORM_RGBW_MIN, NORM_RGBW_MAX);
        this.norm_blue = restrict(norm_blue, NORM_RGBW_MIN, NORM_RGBW_MAX);
        this.norm_cw = restrict(norm_cw, NORM_RGBW_MIN, NORM_RGBW_MAX);
        this.norm_ww = restrict(norm_ww, NORM_RGBW_MIN, NORM_RGBW_MAX);
        
        // EXACT
//        this.norm_red = round(this.norm_red, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
//        this.norm_green = round(this.norm_green, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
//        this.norm_blue = round(this.norm_blue, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
//        this.norm_cw = round(this.norm_cw, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
//        this.norm_ww = round(this.norm_ww, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
    }
    
    private void initSeekbar(int seekbar_red, int seekbar_green, int seekbar_blue, int seekbar_cw, int seekbar_ww)
    {
        this.seekbar_red = restrict(seekbar_red, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
        this.seekbar_green = restrict(seekbar_green, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
        this.seekbar_blue = restrict(seekbar_blue, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
        this.seekbar_cw = restrict(seekbar_cw, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
        this.seekbar_ww = restrict(seekbar_ww, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
    }
    
    private void initPost(int post_red, int post_green, int post_blue, int post_cw, int post_ww)
    {
        this.post_red = restrict(post_red, this.post_rgbw_min, post_rgbw_max);
        this.post_green = restrict(post_green, this.post_rgbw_min, post_rgbw_max);
        this.post_blue = restrict(post_blue, this.post_rgbw_min, post_rgbw_max);
        this.post_cw = restrict(post_cw, this.post_rgbw_min, post_rgbw_max);
        this.post_ww = restrict(post_ww, this.post_rgbw_min, post_rgbw_max);
    }
    
    private void initColor(int color_red, int color_green, int color_blue)
    {
        this.color_red = restrict(color_red, COLOR_RGB_MIN, COLOR_RGB_MAX);
        this.color_green = color_green;
        this.color_blue = color_blue;
    }
    
    
    // norm = 1.0 * x / max
    // y = max * k^(norm-1)
    private static int exponentFunc(int k, int max, int value)
    {
        if (value == 0)
        {
            return 0;
        }
        double norm = 1.0 * value / max;
        return (int)(max * Math.pow(k, norm) / k);
    }
    
    // x = (lg(y/max)/lg(k) + 1) * max
    private static int logFunc(int k, int max, int value)
    {
        if (value == 0)
        {
            return 0;
        }
        return (int)(((Math.log(1.0 * value / max) / Math.log(k)) + 1) * max);
    }
    
    // (x-max)^2 + y^2 = max^2
    // y = (max^2 - (x-max)^2) ^ 0.5
//    private static int circleAtX(int max, int value)
//    {
//        double max2 = Math.pow(max, 2);
//        double valueMinMax2 = Math.pow(value - max, 2);
//        return (int)Math.pow(max2 - valueMinMax2, 0.5);
//    }
    
    // x^2 + (y-max)^2 = max^2
    // y = max - (max^2 - x^2) ^ 0.5
//    private static int circleAtY(int max, int value)
//    {
//        double max2 = Math.pow(max, 2);
//        double value2 = Math.pow(value, 2);
//        return (int)(max - Math.pow(max2 - value2, 0.5));
//    }
    
    private void setColor2Status()
    {
        this.status_red = this.color_red * this.status_rgbw_max / COLOR_RGB_MAX;
        this.status_green = this.color_green * this.status_rgbw_max / COLOR_RGB_MAX;
        this.status_blue = this.color_blue * this.status_rgbw_max / COLOR_RGB_MAX;
        this.status_cw = 0;
        this.status_ww = 0;
    }
    
    private static int __getColorByStatus(int status_color,int status_rgbw_max)
    {
        return status_color * COLOR_RGB_MAX / status_rgbw_max;
    }
    
    private void setStatus2Color()
    {
        this.color_red = __getColorByStatus(this.status_red, this.status_rgbw_max);
        this.color_green = __getColorByStatus(this.status_green, this.status_rgbw_max);
        this.color_blue = __getColorByStatus(this.status_blue, this.status_rgbw_max);
    }
    
    // seekbar_value = circleAtY ( max_value , status_value );
    private static int __getSeekbarByStatus(int status_rgbw_max, int status)
    {
//        return circleAtY(status_rgbw_max, status);
        return logFunc(K, status_rgbw_max, status);
    }
    
    private void setStatus2Seekbar()
    {
        this.seekbar_red = __getSeekbarByStatus(this.status_rgbw_max, this.status_red);
        this.seekbar_green = __getSeekbarByStatus(this.status_rgbw_max, this.status_green);
        this.seekbar_blue = __getSeekbarByStatus(this.status_rgbw_max, this.status_blue);
        this.seekbar_cw = __getSeekbarByStatus(this.status_rgbw_max, this.status_cw);
        this.seekbar_ww = __getSeekbarByStatus(this.status_rgbw_max, this.status_ww);
    }
    
    // post_value = status_value
    private static int __getPostByStatus(int status)
    {
        return status;
    }
    
    public static int getPostBySeekbar(int period, int seekbar)
    {
        int seekbar_rgbw_max = __getRgbwMax(period);
        int status = __getStatusBySeekbar(seekbar_rgbw_max, seekbar);
        int post = __getPostByStatus(status);
        return post;
    }
    
    // post_value = status_value
    private void setStatus2Post()
    {
        this.post_red = __getPostByStatus(this.status_red);
        this.post_green = __getPostByStatus(this.status_green);
        this.post_blue = __getPostByStatus(this.status_blue);
        this.post_cw = __getPostByStatus(this.status_cw);
        this.post_ww = __getPostByStatus(this.status_ww);
    }
    
    private static int __getStatusByPost(int post)
    {
        return post;
    }
    
    // status_value = post_value
    private void setPost2Status()
    {
        this.status_red = __getStatusByPost(this.post_red);
        this.status_green = __getStatusByPost(this.post_green);
        this.status_blue = __getStatusByPost(this.post_blue);
        this.status_cw = __getStatusByPost(this.post_cw);
        this.status_ww = __getStatusByPost(this.post_ww);
    }

    private static int __getStatusBySeekbar(int seekbar_rgbw_max,int seekbar)
    {
//        int status = circleAtX(seekbar_rgbw_max, seekbar);
        int status = exponentFunc(K, seekbar_rgbw_max, seekbar);
        return status;
    }
    
    public static int getColorBySeekbarPeriod(int seekbar, int period)
    {
        int status_rgbw_max = EspLightRecord.getRgbwMax(period);
        int seekbar_rgbw_max = status_rgbw_max;
        int status_color = __getStatusBySeekbar(seekbar_rgbw_max, seekbar);
        int color = __getColorByStatus(status_color, status_rgbw_max);
        return color;
    }
    
    /**
     * get seekbar color by the current status
     * @param seekbar the seekbar value
     * @return the color value
     */
    public int getColorBySeekbar(int seekbar)
    {
        int seekbar_rgbw_max = this.seekbar_rgbw_max;
        int status_rgbw_max = this.status_rgbw_max;
        int status_color = __getStatusBySeekbar(seekbar_rgbw_max, seekbar);
        int color = __getColorByStatus(status_color, status_rgbw_max);
        return color;
    }
    
    // status_value = circleAtX ( max_value , seekbar_value );
    private void setSeekbar2Status()
    {
        this.status_red = __getStatusBySeekbar(this.seekbar_rgbw_max, this.seekbar_red);
        this.status_green = __getStatusBySeekbar(this.seekbar_rgbw_max, this.seekbar_green);
        this.status_blue = __getStatusBySeekbar(this.seekbar_rgbw_max, this.seekbar_blue);
        this.status_cw = __getStatusBySeekbar(this.seekbar_rgbw_max, this.seekbar_cw);
        this.status_ww = __getStatusBySeekbar(this.seekbar_rgbw_max, this.seekbar_ww);
    }
    
    private static int __getSeekbarByNorm(double norm, double norm_rgbw_min, double norm_rgbw_max, int seekbar_rgbw_max)
    {
        norm = restrict(norm, norm_rgbw_min, norm_rgbw_max);
        int seekbar = (int)(norm * seekbar_rgbw_max);
        return seekbar;
    }
    
    public static int getSeekbarByNorm(double norm, double norm_rgbw_min, double norm_rgbw_max, int seekbar_rgbw_max)
    {
        return __getSeekbarByNorm(norm, norm_rgbw_min, norm_rgbw_max, seekbar_rgbw_max);
    }
    
    // seekbar_value = norm_value * seekbar_rgbw_max
    private void setNorm2Seekbar()
    {
        this.seekbar_red = __getSeekbarByNorm(this.norm_red, NORM_RGBW_MIN, NORM_RGBW_MAX, this.seekbar_rgbw_max);
        this.seekbar_green = __getSeekbarByNorm(this.norm_green, NORM_RGBW_MIN, NORM_RGBW_MAX, this.seekbar_rgbw_max);
        this.seekbar_blue = __getSeekbarByNorm(this.norm_blue, NORM_RGBW_MIN, NORM_RGBW_MAX, this.seekbar_rgbw_max);
        this.seekbar_cw = __getSeekbarByNorm(this.norm_cw, NORM_RGBW_MIN, NORM_RGBW_MAX, this.seekbar_rgbw_max);
        this.seekbar_ww = __getSeekbarByNorm(this.norm_ww, NORM_RGBW_MIN, NORM_RGBW_MAX, this.seekbar_rgbw_max);
    }
    
    private static double __getNormBySeekbar(int seekbar, int seekbar_rgbw_min, int seekbar_rgbw_max)
    {
        seekbar = restrict(seekbar, seekbar_rgbw_min, seekbar_rgbw_max);
        double norm = 1.0 * seekbar / seekbar_rgbw_max;
//        norm = round(norm, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
        return norm;
    }
    
    public static double getNormBySeekbar(int seekbar, int seekbar_rgbw_min, int seekbar_rgbw_max)
    {
        double norm = __getNormBySeekbar(seekbar, seekbar_rgbw_min, seekbar_rgbw_max);
        return norm;
    }
    
    public static String getNormStrBySeekbar(int seekbar, int seekbar_rgbw_min, int seekbar_rgbw_max)
    {
        double norm = __getNormBySeekbar(seekbar, seekbar_rgbw_min, seekbar_rgbw_max);
        norm = round(norm, NORM_RGBW_SB, NORM_RGBW_MIN, NORM_RGBW_MAX);
        return form(norm, NORM_RGBW_SB);
    }
    
    // norm_value = 1.0 * seekbar_value / seekbar_rgbw_max
    private void setSeekbar2Norm()
    {
        this.norm_red = __getNormBySeekbar(this.seekbar_red, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
        this.norm_green = __getNormBySeekbar(this.seekbar_green, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
        this.norm_blue = __getNormBySeekbar(this.seekbar_blue, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
        this.norm_cw = __getNormBySeekbar(this.seekbar_cw, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
        this.norm_ww = __getNormBySeekbar(this.seekbar_ww, this.seekbar_rgbw_min, this.seekbar_rgbw_max);
    }
    
    private void initByNorm(int period, double norm_red, double norm_green, double norm_blue, double norm_cw,
        double norm_ww)
    {
        initPeriod(period);
        initMinMax(this.period);
        initNorm(norm_red, norm_green, norm_blue, norm_cw, norm_ww);
        setNorm2Seekbar();
        setSeekbar2Status();
        setStatus2Post();
        setStatus2Color();
    }
    
    public void updateByNorm(int period, double norm_red, double norm_green, double norm_blue, double norm_cw,
        double norm_ww)
    {
        initByNorm(period, norm_red, norm_green, norm_blue, norm_cw, norm_ww);
//        saveLastStatus();
    }
    
    static EspLightRecord createInstanceByNorm(int period, double norm_red, double norm_green, double norm_blue,
        double norm_cw, double norm_ww)
    {
        EspLightRecord instance = new EspLightRecord();
        instance.initByNorm(period, norm_red, norm_green, norm_blue, norm_cw, norm_ww);
        return instance;
    }
    
    private void initBySeekbar(int period, int seekbar_red, int seekbar_green, int seekbar_blue, int seekbar_cw,
        int seekbar_ww)
    {
        if (DEBUG)
            System.out.println("initBySeekbar() seekbar_red:" + seekbar_red);
        initPeriod(period);
        initMinMax(this.period);
        initSeekbar(seekbar_red, seekbar_green, seekbar_blue, seekbar_cw, seekbar_ww);
        if (DEBUG)
            System.out.println("initBySeekbar() seekbar_red1:" + this.seekbar_red);
        setSeekbar2Norm();
        if (DEBUG)
            System.out.println("initBySeekbar() norm_red:" + this.norm_red);
        setSeekbar2Status();
        if (DEBUG)
            System.out.println("initBySeekbar() status_red:" + this.status_red);
        setStatus2Post();
        if (DEBUG)
            System.out.println("initBySeekbar() post_red:" + this.post_red);
        setStatus2Color();
        if (DEBUG)
            System.out.println("initBySeekbar() color_red:" + this.color_red);
        if (DEBUG)
        {
            System.out.println("initBySeekbar() this:" + this);
        }
    }
    
    public void updateBySeekbar(int period, int seekbar_red, int seekbar_green, int seekbar_blue, int seekbar_cw,
        int seekbar_ww)
    {
        initBySeekbar(period, seekbar_red, seekbar_green, seekbar_blue, seekbar_cw, seekbar_ww);
//        saveLastStatus();
    }
    
    static EspLightRecord createInstanceBySeekbar(int period, int seekbar_red, int seekbar_green, int seekbar_blue,
        int seekbar_cw, int seekbar_ww)
    {
        EspLightRecord instance = new EspLightRecord();
        instance.initBySeekbar(period, seekbar_red, seekbar_green, seekbar_blue, seekbar_cw, seekbar_ww);
        return instance;
    }
    
    private void initByPost(int period, int post_red, int post_green, int post_blue, int post_cw, int post_ww)
    {
        initPeriod(period);
        initMinMax(this.period);
        initPost(post_red, post_green, post_blue, post_cw, post_ww);
        setPost2Status();
        setStatus2Color();
        setStatus2Seekbar();
        setSeekbar2Norm();
    }
    
    public void updateByPost(int period, int post_red, int post_green, int post_blue, int post_cw, int post_ww)
    {
        initByPost(period, post_red, post_green, post_blue, post_cw, post_ww);
//        saveLastStatus();
    }
    
    public static EspLightRecord createInstanceByPost(int period, int post_red, int post_green, int post_blue, int post_cw,
        int post_ww)
    {
        EspLightRecord instance = new EspLightRecord();
        instance.initByPost(period, post_red, post_green, post_blue, post_cw, post_ww);
        return instance;
    }
    
    private void initByColor(int period, int color_red, int color_green, int color_blue)
    {
        initPeriod(period);
        initMinMax(this.period);
        initColor(color_red, color_green, color_blue);
        setColor2Status();
        setStatus2Post();
        setStatus2Seekbar();
        setSeekbar2Norm();
    }
    
    public void updateByColor(int period, int color_red, int color_green, int color_blue)
    {
        initByColor(period, color_red, color_green, color_blue);
//        saveLastStatus();
    }
    
    static EspLightRecord createInstanceByColor(int period, int color_red, int color_green, int color_blue)
    {
        EspLightRecord instance = new EspLightRecord();
        instance.initByColor(period, color_red, color_green, color_blue);
        return instance;
    }
    
    public static EspLightRecord createFakeInstance()
    {
        EspLightRecord instance = new EspLightRecord();
        int period = 0;
        int seekbar_red = 0;
        int seekbar_green = 0;
        int seekbar_blue = 0;
        int seekbar_cw = 0;
        int seekbar_ww = 0;
        instance.initBySeekbar(period, seekbar_red, seekbar_green, seekbar_blue, seekbar_cw, seekbar_ww);
        return instance;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("period:" + period + "\n");
        sb.append("seekbar_rgbw_min:" + seekbar_rgbw_min + "\n");
        sb.append("seekbar_rgbw_max:" + seekbar_rgbw_max + "\n");
        sb.append("status_rgbw_min:" + status_rgbw_min + "\n");
        sb.append("status_rgbw_max:" + status_rgbw_max + "\n");
        sb.append("norm_red:" + getNormRedStr() + "\n");
        sb.append("norm_green:" + getNormGreenStr() + "\n");
        sb.append("norm_blue:" + getNormBlueStr() + "\n");
        sb.append("norm_cw:" + getNormCwStr() + "\n");
        sb.append("norm_ww:" + getNormWwStr() + "\n");
        sb.append("seekbar_red:" + seekbar_red + "\n");
        sb.append("seekbar_green:" + seekbar_green + "\n");
        sb.append("seekbar_blue:" + seekbar_blue + "\n");
        sb.append("seekbar_cw:" + seekbar_cw + "\n");
        sb.append("seekbar_ww:" + seekbar_ww + "\n");
        sb.append("color_red:" + color_red + "\n");
        sb.append("color_green:" + color_green + "\n");
        sb.append("color_blue:" + color_blue + "\n");
        sb.append("status_red:" + status_red + "\n");
        sb.append("status_green:" + status_green + "\n");
        sb.append("status_blue:" + status_blue + "\n");
        sb.append("status_cw:" + status_cw + "\n");
        sb.append("status_ww:" + status_ww + "\n");
        sb.append("post_red:" + post_red + "\n");
        sb.append("post_green:" + post_green + "\n");
        sb.append("post_blue:" + post_blue + "\n");
        sb.append("post_cw:" + post_cw + "\n");
        sb.append("post_ww:" + post_ww + "\n");
        return sb.toString();
    }
    
    public static void main(String args[])
    {
        System.out.println();
        System.out.println("lightStatusNorm:\n");
        int period = 1000;
        double norm_red = 1.0;
        double norm_green = 0.8;
        double norm_blue = 0.5;
        double norm_cw = 0.3;
        double norm_ww = 0.01;
        EspLightRecord lightStatusNorm =
            EspLightRecord.createInstanceByNorm(period, norm_red, norm_green, norm_blue, norm_cw, norm_ww);
        System.out.println(lightStatusNorm);
        System.out.println();
        
        System.out.println("lightStatusSeekbar:\n");
        int seekbar_red = 22222;
        int seekbar_green = (int)(22222 * 0.8);
        int seekbar_blue = (int)(22222 * 0.5);
        int seekbar_cw = (int)(22222 * 0.3);
        int seekbar_ww = (int)(22222 * 0.01);
        EspLightRecord lightStatusSeekbar =
            EspLightRecord.createInstanceBySeekbar(period,
                seekbar_red,
                seekbar_green,
                seekbar_blue,
                seekbar_cw,
                seekbar_ww);
        System.out.println(lightStatusSeekbar);
        System.out.println();
        
        System.out.println("lightStatusPost:\n");
        int post_red = 22222;
        int post_green = 21772;
        int post_blue = 19244;
        int post_cw = 15869;
        int post_ww = 3133;
        EspLightRecord lightStatusStatus =
            EspLightRecord.createInstanceByPost(period, post_red, post_green, post_blue, post_cw, post_ww);
        System.out.println(lightStatusStatus);
        System.out.println();
        
        System.out.println("lightStatusColor:\n");
        int color_red = 254;
        int color_green = 248;
        int color_blue = 219;
        EspLightRecord lightStatusColor = EspLightRecord.createInstanceByColor(period, color_red, color_green, color_blue);
        System.out.println(lightStatusColor);
        System.out.println();
        
        System.out.println("update lightStatusColor:\n");
        int update_color_red = 120;
        int update_color_green = 121;
        int update_color_blue = 122;
        lightStatusColor.updateByColor(period, update_color_red, update_color_green, update_color_blue);
        System.out.println(lightStatusColor);
        System.out.println();
        
        System.out.println("undo lightStatusColor:\n");
        lightStatusColor.undoStatus();
        System.out.println(lightStatusColor);
        System.out.println();
        
        System.out.println("round test:\n");
        for (int i = 0; i <= 1000; i++)
        {
            double value = 1.0 * i / 1000;
            int sb = 2;
            double min = 0;
            double max = 1;
            System.out.println(value + ":" + round(value, sb, min, max));
        }
    }
}
