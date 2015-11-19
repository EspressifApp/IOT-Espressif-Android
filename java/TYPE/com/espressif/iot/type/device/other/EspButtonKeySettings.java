package com.espressif.iot.type.device.other;

public class EspButtonKeySettings
{
    /**
     * Functions of EspButton keys
     */
    public static enum Func
    {
        NIL, SET_COLOR, TURN_ONOFF, SET_TIMER, SET_BRIGHTNESS
    }
    
    public static final int TURN_ON = 1;
    public static final int TURN_OFF = 0;
    public static final int TURN_ONOFF = -1;
    
    private Action mShortPressAction;
    private Action mLongPressAction;
    
    private int mId;
    
    public void setId(int id)
    {
        mId = id;
    }
    
    public int getId()
    {
        return mId;
    }
    
    public Action getShortPressAction()
    {
        return mShortPressAction;
    }
    
    public Action getLongPressAction()
    {
        return mLongPressAction;
    }
    
    /**
     * Init short press Action
     */
    public void initShortPressAction()
    {
        mShortPressAction = new Action();
    }
    
    /**
     * Init long press Action
     */
    public void initLongPressAction()
    {
        mLongPressAction = new Action();
    }
    
    /**
     * Init short press Action and long press Action
     */
    public void initActions()
    {
        initShortPressAction();
        initLongPressAction();
    }
    
    /**
     * Args of function
     */
    public static class Action
    {
        private Func mFunc = Func.NIL;
        
        private boolean mBroadcast = false;
        
        private int mColorRed;
        private int mColorGreen;
        private int mColorBlue;
        
        private int mOnOff;
        
        private static final long MAX_TIMER_TIME = 3600;
        private long mTimerTime;
        
        private int mBrightness;
        
        public void setFunc(Func func)
        {
            mFunc = func;
        }
        
        public Func getFunc()
        {
            return mFunc;
        }
        
        public void setBroadcast(boolean isBroadcast)
        {
            mBroadcast = isBroadcast;
        }
        
        public boolean isBroadcast()
        {
            return mBroadcast;
        }
        
        public void setRed(int red)
        {
            mColorRed = red;
        }
        
        public int getRed()
        {
            return mColorRed;
        }
        
        public void setGreen(int green)
        {
            mColorGreen = green;
        }
        
        public int getGreen()
        {
            return mColorGreen;
        }
        
        public void setBlue(int blue)
        {
            mColorBlue = blue;
        }
        
        public int getBlue()
        {
            return mColorBlue;
        }
        
        /**
         * 
         * @param onoff One of {@link #TURN_ON}, {@link #TURN_OFF}, or {@link #TURN_ONOFF}
         */
        public void setTurnOnOff(int onoff)
        {
            mOnOff = onoff;
        }
        
        /**
         * 
         * @return One of {@link #TURN_ON}, {@link #TURN_OFF}, or {@link #TURN_ONOFF}
         */
        public int getTurnOnOff()
        {
            return mOnOff;
        }
        
        /**
         * 
         * @param time the rang is 0 ~ 3600
         */
        public void setTimerTime(long time)
        {
            if (time > MAX_TIMER_TIME)
            {
                mTimerTime = MAX_TIMER_TIME;
            }
            else
            {
                mTimerTime = time;
            }
        }
        
        public long getTimerTime()
        {
            return mTimerTime;
        }
        
        public void setBrightness(int brightness)
        {
            mBrightness = brightness;
        }
        
        public int getBrightness()
        {
            return mBrightness;
        }
        
        @Override
        public String toString()
        {
            StringBuilder result = new StringBuilder();
            result.append('[');
            // Start append action
            result.append(mFunc.toString()).append(':');
            switch(mFunc)
            {
                case NIL:
                    result.append("nil");
                    break;
                case SET_BRIGHTNESS:
                    result.append(mBrightness);
                    break;
                case SET_COLOR:
                    result.append(mColorRed + "," + mColorGreen + "," + mColorBlue);
                    break;
                case SET_TIMER:
                    result.append(mTimerTime);
                    break;
                case TURN_ONOFF:
                    result.append(mOnOff);
                    break;
            }
            // End append action
            result.append(']');
            return result.toString();
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append('{');
        // Start append content
        result.append("id = " + mId).append(" | ");
        String shortPressAction = mShortPressAction == null ? "null" : mShortPressAction.toString();
        String longPressAction = mLongPressAction == null ? "null" : mLongPressAction.toString();
        result.append("short press action = " + shortPressAction).append(" | ");
        result.append("long press action = " + longPressAction);
        // End append content
        result.append('}');
        
        return result.toString();
    }
}
