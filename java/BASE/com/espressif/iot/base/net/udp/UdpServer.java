package com.espressif.iot.base.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.espressif.iot.model.thread.FinishThread;
import com.espressif.iot.util.BSSIDUtil;

import android.os.Handler;
import android.os.Looper;

public enum UdpServer {
    INSTANCE;

    private static Logger sLog = Logger.getLogger(UdpServer.class);

    private DatagramSocket mSocket;
    private ReceiveThread mReceiveThread;

    private static final byte[] sEspHeader = UdpConstants.ESP_HEADER;

    private static final byte MSG_TYPE_TWINKLE = UdpConstants.MSG_TYPE_TWINKLE;

    private Set<OnLightTwinkleListener> mTwinkleListenerList = new HashSet<OnLightTwinkleListener>();

    public interface OnLightTwinkleListener {
        public void onTwinkle(String[] bssids);
    }

    private volatile boolean mCloseMark = false;

    public synchronized void open() {
        if (mSocket != null) {
            throw new IllegalStateException("The UPD server is running");
        }

        mCloseMark = false;

        try {
            mSocket = new DatagramSocket(UdpConstants.UDP_SERVER_PORT_DEFAULT);
        } catch (SocketException e) {
            e.printStackTrace();
            Random r = new Random();
            while (true) {
                int port = r.nextInt(10000);
                port += 10000;
                try {
                    mSocket = new DatagramSocket(port);
                    break;
                } catch (SocketException ee) {
                    ee.printStackTrace();
                }
            }
        }
        sLog.debug("UDP server open, port = " + mSocket.getLocalPort());

        if (mReceiveThread == null) {
            mReceiveThread = new ReceiveThread();
            mReceiveThread.start();
        }
    }

    public synchronized void close() {
        sLog.debug("UDP server close");
        mCloseMark = true;

        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }

        if (mReceiveThread != null) {
            mReceiveThread.setRun(false);
            mReceiveThread.finish();
            mReceiveThread = null;
        }
    }

    private class ReceiveThread extends FinishThread {
        private Handler mHandler;

        private volatile boolean mRun = true;

        public void setRun(boolean run) {
            mRun = run;
        }

        @Override
        public void execute() {
            mHandler = new Handler(Looper.getMainLooper());
            byte[] data = new byte[300];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            while (mRun) {
                try {
                    sLog.debug("UDP receive blocking");
                    mSocket.receive(packet);
                    sLog.debug("UDP receive data");

                    if (!checkEspHeader(data)) {
                        continue;
                    }

                    byte type = data[3];
                    int len = data[4] & 0xff;
                    byte[] value = new byte[len];
                    final int valueStartIndex = 5;
                    for (int i = 0; i < len; i++) {
                        value[i] = data[i + valueStartIndex];
                    }
                    sLog.info("UDP receive type = " + type);
                    sLog.info("UDP receive length = " + len);
                    switch (type) {
                        case MSG_TYPE_TWINKLE:
                            parseTypeTwinkle(value);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            mHandler = null;

            if (!mCloseMark) {
                new ReceiveExceptionProcessThread().start();
            }
        }

        private boolean checkEspHeader(byte[] data) {
            for (int i = 0; i < sEspHeader.length; i++) {
                int headerByte = sEspHeader[i];
                int dataByte = data[i];

                if (headerByte != dataByte) {
                    return false;
                }
            }

            return true;
        }

        private void parseTypeTwinkle(byte[] value) {
            StringBuilder bssidSb = new StringBuilder();
            for (int i = 0; i < value.length; i++) {
                byte b = value[i];
                String hex = Integer.toHexString(b & 0xff);
                bssidSb.append(hex);
                if ((i + 1) % 6 == 0) {
                    bssidSb.append(',');
                } else {
                    bssidSb.append(':');
                }
            }
            bssidSb.deleteCharAt(bssidSb.length() - 1);
            String[] bssids = bssidSb.toString().split(",");
            for (int i = 0; i < bssids.length; i++) {
                sLog.info("UDP receive bssid = " + bssids[i]);
                bssids[i] = BSSIDUtil.restoreStaBSSID(bssids[i]);
            }
            mHandler.post(new TwinkleRunnable(bssids));
        }

        private class TwinkleRunnable implements Runnable {
            private final String[] bssids;

            public TwinkleRunnable(String[] bssids) {
                this.bssids = bssids;
            }

            @Override
            public void run() {
                for (OnLightTwinkleListener listener : mTwinkleListenerList) {
                    sLog.debug("UDP TWINKLE");
                    listener.onTwinkle(bssids);
                }
            }

        }
    }

    private class ReceiveExceptionProcessThread extends Thread {
        @Override
        public void run() {
            close();
            open();
        }
    }

    public int getPort() {
        if (mSocket == null) {
            return -1;
        } else {
            return mSocket.getLocalPort();
        }
    }

    public void registerOnLightTwinkleListener(OnLightTwinkleListener listener) {
        if (listener != null) {
            mTwinkleListenerList.add(listener);
        }
    }

    public void unRegisterOnLightTwinkleListener(OnLightTwinkleListener listener) {
        mTwinkleListenerList.remove(listener);
    }
}
