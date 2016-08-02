package com.espressif.iot.type.device.status;

import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspStatusLight extends IEspDeviceStatus {
    public static final int STATUS_NULL = -1;
    public static final int STATUS_OFF = 0;
    public static final int STATUS_ON = 1;
    public static final int STATUS_COLOR = 2;
    public static final int STATUS_BRIGHT = 3;

    public static final int PERIOD_DEFAULT = 1000;

    /**
     * Get red value of the light
     * 
     * @return red value of the light
     */
    int getRed();

    /**
     * Set red value of the light
     * 
     * @param red the red value of the light
     */
    void setRed(int red);

    /**
     * Get green value of the light
     * 
     * @return green value of the light
     */
    int getGreen();

    /**
     * Set green value of the light
     * 
     * @param green the green value of the light
     */
    void setGreen(int green);

    /**
     * Get blue value of the light
     * 
     * @return blue value of the light
     */
    int getBlue();

    /**
     * Set blue value of the light
     * 
     * @param blue the blue value of the light
     */
    void setBlue(int blue);

    /**
     * Get period value of the light
     * 
     * @return the period value of the light
     */
    int getPeriod();

    /**
     * Set period value of the light
     * 
     * @param period the period value of the light
     */
    void setPeriod(int period);

    /**
     * Get the cold white value of the light
     * 
     * @return the cold white value of the light
     */
    @Deprecated
    int getCWhite();

    /**
     * Set the cold white value of the light
     * 
     * @param cWhite the cold while value
     */
    @Deprecated
    void setCWhite(int cWhite);

    /**
     * Get the warm white value of the light
     * 
     * @return the warm white value of the light
     */
    @Deprecated
    int getWWhite();

    /**
     * Set the warm white value of the light
     * 
     * @param wWhite the warm value of the light
     */
    @Deprecated
    void setWWhite(int wWhite);

    /**
     * Set the white value of the light
     * 
     * @param white the value of the light
     */
    void setWhite(int white);

    /**
     * Get the white value of the light
     * 
     * @return the white value of the light
     */
    int getWhite();

    /**
     * Set the status value of the light, One of {@link #STATUS_OFF}, {@link #STATUS_ON}, {@link #STATUS_COLOR} or
     * {@link #STATUS_BRIGHT}.
     * 
     * @param status
     */
    void setStatus(int status);

    /**
     * Get the status value of the light
     * 
     * @return One of {@link #STATUS_OFF}, {@link #STATUS_ON}, {@link #STATUS_COLOR} or {@link #STATUS_BRIGHT}.
     */
    int getStatus();

    /**
     * Get current color of the light
     * 
     * @return
     */
    int getCurrentColor();
}
