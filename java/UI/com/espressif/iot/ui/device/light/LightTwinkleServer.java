package com.espressif.iot.ui.device.light;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.net.rest2.EspHttpUtil;
import com.espressif.iot.model.device.http.HttpMessage;
import com.espressif.iot.model.device.http.HttpRequest;
import com.espressif.iot.model.device.http.HttpResponse;
import com.espressif.iot.model.thread.FinishThread;

import android.os.Handler;
import android.os.Looper;

public enum LightTwinkleServer {
    INSTANCE;

    private static final Logger log = Logger.getLogger(LightTwinkleServer.class);

    private static final String PATH_TWINKLE = "/twinkle";

    private static final String KEY_STATUS = "status";
    private static final String KEY_BSSID = "bssid";

    private ServerSocket mServerSocket;
    private AcceptThread mAcceptThread;

    private Set<OnLightTwinkleListener> mTwinkleListenerList = new HashSet<OnLightTwinkleListener>();

    public synchronized void openServer() {
        if (mServerSocket != null) {
            throw new IllegalStateException("The server is running");
        }

        Random r = new Random();
        while (true) {
            int port = r.nextInt(20000);
            port += 20000;
            try {
                mServerSocket = new ServerSocket(port);
                log.debug("LightTwinkleServer has opend, port = " + port);

                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void closeServer() {
        log.debug("LightTwinkleServer close");
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.setRun(false);
            mAcceptThread.finish();
        }
        mAcceptThread = null;
    }

    public int getPort() {
        return mServerSocket == null ? -1 : mServerSocket.getLocalPort();
    }

    private class AcceptThread extends FinishThread {
        private volatile boolean mRun = true;

        @Override
        public void execute() {
            while (mRun) {
                try {
                    final Socket socket = mServerSocket.accept();
                    log.debug("LightTwinkleServer accept a socket");
                    socket.setSoTimeout(5000);
                    new CommunicateThread(socket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        public void setRun(boolean run) {
            mRun = run;
        }
    }

    public interface OnLightTwinkleListener {
        public void onTwinkle(String bssid);
    }

    public void registerOnLightTwinkleListener(OnLightTwinkleListener listener) {
        if (listener != null) {
            mTwinkleListenerList.add(listener);
        }
    }

    public void unRegisterOnLightTwinkleListener(OnLightTwinkleListener listener) {
        mTwinkleListenerList.remove(listener);
    }

    private class CommunicateThread extends Thread {
        private final Socket mSocket;

        private HttpRequest mRequest;
        private HttpResponse mResponse;

        private Handler mHandler;

        public CommunicateThread(Socket socket) {
            mSocket = socket;

            mRequest = new HttpRequest();
            mResponse = new HttpResponse();
        }

        @Override
        public void run() {
            mHandler = new Handler(Looper.getMainLooper());
            try {
                EspHttpUtil.readHttpRequest(mSocket, mRequest);
                log.info("LightTwinkleServer read request: " + mRequest.toString());

                parseRequest(mRequest);

                String response = mResponse.toString();
                log.info("LightTwinkleServer send response: " + response);
                mSocket.getOutputStream().write(response.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (HttpException e) {
                e.printStackTrace();
            } finally {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mHandler = null;
        }

        private void parseRequest(HttpRequest request) {
            String path = request.getPath();
            if (path.equals(PATH_TWINKLE)) {
                parseRequestTwinkle(request);
            } else {
                setResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Unknow url", null);
            }
        }

        private void setResponse(int httpStatus, String httpMsg, String content) {
            mResponse.setStatus(httpStatus);
            mResponse.setMessage(httpMsg);

            if (content != null) {
                mResponse.setContent(content);
                mResponse.addHeader(HttpMessage.CONTENT_LENGTH, "" + content.length());
            }
        }

        private void parseRequestTwinkle(HttpRequest request) {
            JSONObject respJSON = new JSONObject();

            // Get request content
            String content = request.getContent();
            if (content == null) {
                try {
                    respJSON.put(KEY_STATUS, HttpURLConnection.HTTP_BAD_REQUEST);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResponse(HttpURLConnection.HTTP_BAD_REQUEST, "No content", respJSON.toString());
                return;
            }

            // Get twinkle BSSID
            String twinkleBssid = null;
            try {
                JSONObject requJSON = new JSONObject(content);
                twinkleBssid = requJSON.getString(KEY_BSSID);
            } catch (JSONException e1) {
                e1.printStackTrace();
                try {
                    respJSON.put(KEY_STATUS, HttpURLConnection.HTTP_BAD_REQUEST);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResponse(HttpURLConnection.HTTP_BAD_REQUEST, "JSON error", respJSON.toString());
                return;
            }

            if (twinkleBssid != null) {
                try {
                    respJSON.put(KEY_STATUS, HttpURLConnection.HTTP_OK);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResponse(HttpURLConnection.HTTP_OK, "OK", respJSON.toString());

                // Notify twinkle, run on UI thread
                mHandler.post(new TwinkleRunnable(twinkleBssid));
            }
        }

        private class TwinkleRunnable implements Runnable {
            final String mBssid;

            public TwinkleRunnable(String bssid) {
                mBssid = bssid;
            }

            @Override
            public void run() {
                for (OnLightTwinkleListener listener : mTwinkleListenerList) {
                    listener.onTwinkle(mBssid);
                }
            }
        }
    } // end CommunicateThread
}
