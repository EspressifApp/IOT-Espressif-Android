package com.espressif.iot.base.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class LightUdpClient {
    private static Logger sLog = Logger.getLogger(LightUdpClient.class);

    private static final int IOT_DEVICE_PORT = 1025;
    private static final byte MSG_TYPE_ADDRESS = UdpConstants.MSG_TYPE_ADDRESS;
    private static final byte[] sEspHeader = UdpConstants.ESP_HEADER;

    private Context mContext;

    private DatagramSocket mSocket;

    public LightUdpClient(Context context) {
        mContext = context;
        try {
            mSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public synchronized void close() {
        mSocket.close();
        mSocket = null;
    }

    public boolean notifyAddress() {
        byte[] ip = getIpBytes();
        if (ip == null) {
            sLog.warn("notifyAddress() ip is null");
            return false;
        }
        byte[] port = getPortBytes();
        if (port == null) {
            sLog.warn("notifyAddress() port is null");
            return false;
        }

        List<Byte> dataList = new ArrayList<Byte>();
        // Add header
        for (byte b : sEspHeader) {
            dataList.add(b);
        }
        // Add type
        dataList.add(MSG_TYPE_ADDRESS);
        // Add length
        byte ipVer = (byte)ip.length;
        dataList.add((byte)(1 + ip.length + port.length));
        // Add value
        // Add ip version ipv4 = 0x04, ipv6 = 0x06
        dataList.add(ipVer);
        // Add ip address
        for (byte b : ip) {
            dataList.add(b);
        }
        // Add ip port
        for (byte b : port) {
            dataList.add(b);
        }
        byte[] data = new byte[dataList.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = dataList.get(i);
        }

        InetAddress broadcastAddress;
        try {
            broadcastAddress = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, IOT_DEVICE_PORT);
        try {
            mSocket.send(packet);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private byte[] getIpBytes() {
        WifiManager wm = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wm.getConnectionInfo();
        if (info == null) {
            return null;
        }

        byte[] ip = new byte[4];
        int ipValue = info.getIpAddress();
        for (int i = 0; i <= 3; i++) {
            ip[i] = (byte)((ipValue >> (i * 8)) & 0xff);
        }
        return ip;
    }

    private byte[] getPortBytes() {
        int udpPort = UdpServer.INSTANCE.getPort();
        if (udpPort < 0) {
            return null;
        }

        byte[] port = new byte[2];
        port[0] = (byte)(udpPort >> 8);
        port[1] = (byte)(udpPort & 0xff);
        return port;
    }
}
