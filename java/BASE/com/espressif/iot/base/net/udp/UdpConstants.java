package com.espressif.iot.base.net.udp;

public class UdpConstants {
    public static final byte[] ESP_HEADER = new byte[] {0x45, 0x53, 0x50};

    public static final int UDP_SERVER_PORT_DEFAULT = 32655;

    public static final byte MSG_TYPE_ADDRESS = 0x02;
    public static final byte MSG_TYPE_TWINKLE = 0x03;
}
