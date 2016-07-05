package com.espressif.iot.command.device;

public interface IEspCommandSoundbox extends IEspCommandDevice {
    public static final String KEY_TYPE = "type";
    public static final String KEY_URL = "url";
    public static final String KEY_NAME = "name";
    public static final String KEY_LIVE = "live";
    public static final String KEY_VOLUME = "volume";
    public static final String KEY_PLAY_STATUS = "play_status";
}
