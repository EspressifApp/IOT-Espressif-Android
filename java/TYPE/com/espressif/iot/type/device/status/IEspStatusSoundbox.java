package com.espressif.iot.type.device.status;

import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.other.EspAudio;

public interface IEspStatusSoundbox extends IEspDeviceStatus {
    public static final int ACTION_NULL = -1;
    public static final int ACTION_AUDIO = 0;
    public static final int ACTION_PLAY = 1;
    public static final int ACTION_VOLUME = 2;

    public static final int PLAY_STATUS_PAUSE = 0;
    public static final int PLAY_STATUS_PLAYING = 1;

    /**
     * Set a new EspAudio
     * 
     * @param audio
     */
    public void setAudio(EspAudio audio);

    /**
     * Get EspAudio
     * 
     * @return
     */
    public EspAudio getAudio();

    /**
     * Set play status
     * 
     * @param playStatus @see {@link #PLAY_STATUS_PAUSE} and {@link #PLAY_STATUS_PAUSE}
     */
    public void setPlayStatus(int playStatus);

    /**
     * Get play status
     * 
     * @return @see {@link #PLAY_STATUS_PAUSE} and {@link #PLAY_STATUS_PAUSE}
     */
    public int getPlayStatus();

    /**
     * Set volume percent, the range is 0 ~ 100
     * 
     * @param percent
     */
    public void setVolume(int percent);

    /**
     * Get volume percent
     * 
     * @return
     */
    public int getVolume();

    /**
     * Set the action need process
     * 
     * @param action
     */
    public void setAction(int action);

    /**
     * Get the action need process
     * 
     * @return
     */
    public int getAction();
}
