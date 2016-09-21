package com.espressif.iot.command.device.light;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.net.proxy.MeshCommunicationUtils;
import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.DeviceResponseStatus;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandLightNewProtocol implements IEspCommandLight, IEspCommandLocal, IEspCommandInternet {
    private Logger log = Logger.getLogger(EspCommandLightNewProtocol.class);

    /*
     * Local command start
     */
    @Override
    public String getLocalUrl(InetAddress inetAddress) {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=light";
    }

    private JSONObject getRequestJSONLocal(IEspStatusLight statusLight, boolean isResponseRequired) {
        JSONObject result = new JSONObject();
        try {
            int status = statusLight.getStatus();
            result.put(KEY_STATUS, status);
            result.put(KEY_PERIOD, IEspStatusLight.PERIOD_DEFAULT);
            switch (status) {
                case IEspStatusLight.STATUS_OFF:
                case IEspStatusLight.STATUS_ON:
                    break;
                case IEspStatusLight.STATUS_COLOR:
                    result.put(KEY_COLOR,
                        new JSONObject().put(KEY_RED, statusLight.getRed())
                            .put(KEY_GREEN, statusLight.getGreen())
                            .put(KEY_BLUE, statusLight.getBlue()));
                    break;
                case IEspStatusLight.STATUS_BRIGHT:
                    result.put(KEY_COLOR, new JSONObject().put(KEY_WHITE, statusLight.getWhite()));
                    break;
            }

            if (isResponseRequired) {
                result.put(DeviceResponseStatus.KEY_RESPONSE_STATUS, DeviceResponseStatus.RESPONSE_REQUIRED);
            } else {
                result.put(DeviceResponseStatus.KEY_RESPONSE_STATUS, DeviceResponseStatus.RESPONSE_NO_NEED);
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean localPost(InetAddress inetAddress, IEspStatusLight statusLight, String deviceBssid,
        boolean isMeshDevice, boolean isResponseRequired, Runnable disconnectedCallback) {
        String url = getLocalUrl(inetAddress);
        JSONObject postJSON = getRequestJSONLocal(statusLight, isResponseRequired);

        if (isResponseRequired) {
            JSONObject respJSON;
            if (!isMeshDevice || deviceBssid == null) {
                respJSON = EspBaseApiUtil.Post(url, postJSON);
            } else {
                respJSON = EspBaseApiUtil.PostForJson(url, deviceBssid, postJSON);
            }
            log.info("localPost " + respJSON);
            return (respJSON != null);
        } else {
            // no response is available, so we treat it suc, when fail socket will be disconnected
            if (!isMeshDevice || deviceBssid == null) {
                // normal device
                EspBaseApiUtil.PostInstantly(url, postJSON, disconnectedCallback);
            } else {
                // mesh device
                EspBaseApiUtil.PostForJsonInstantly(url, deviceBssid, postJSON, disconnectedCallback);
            }
            return true;
        }
    }

    public boolean postLocal(IEspDevice device, IEspStatusLight statusLight) {
        boolean responseRequired = true;
        InetAddress inetAddress = device.getInetAddress();
        boolean isMeshDevice = device.getIsMeshDevice();
        String deviceBssid = device.getBssid();
        boolean result = localPost(inetAddress, statusLight, deviceBssid, isMeshDevice, responseRequired, null);

        log.debug("##postLocal(inetAddress=[" + inetAddress + "],statusLight=[" + statusLight + "],deviceBssid=["
            + deviceBssid + "],isMeshDevice=[" + isMeshDevice + "]): " + result);

        return result;
    }

    public void postLocalInstantly(IEspDevice device, IEspStatusLight statusLight, Runnable disconnectedCallback) {
        boolean responseRequired = false;
        InetAddress inetAddress = device.getInetAddress();
        boolean isMeshDevice = device.getIsMeshDevice();
        String deviceBssid = device.getBssid();
        localPost(inetAddress, statusLight, deviceBssid, isMeshDevice, responseRequired, disconnectedCallback);

        log.debug("##postLocalInstantly(inetAddress=[" + inetAddress + "],statusLight=[" + statusLight
            + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=[" + isMeshDevice + "])");
    }

    public boolean postLocalMulticast(InetAddress inetAddress, IEspStatusLight statusLight, List<String> bssids) {
        if (bssids.size() == 1) {
            boolean responseRequired = true;
            return localPost(inetAddress, statusLight, bssids.get(0), true, responseRequired, null);
        } else {
            boolean result = true;
            List<String> macList = new ArrayList<String>();
            for (String bssid : bssids) {
                macList.add(bssid);
                if (macList.size() == MULTICAST_GROUP_LENGTH_LIMIT) {
                    if (!localPostMulticast(inetAddress, statusLight, macList)) {
                        result = false;
                    }
                    macList.clear();
                }
            }
            if (!macList.isEmpty()) {
                if (!localPostMulticast(inetAddress, statusLight, macList)) {
                    result = false;
                }
            }
            return result;
        }
    }

    private boolean localPostMulticast(InetAddress inetAddress, IEspStatusLight statusLight, List<String> macList) {
        StringBuilder macs = new StringBuilder();
        for (String mac : macList) {
            macs.append(mac);
        }
        HeaderPair multicastHeader =
            new HeaderPair(MeshCommunicationUtils.HEADER_MESH_MULTICAST_GROUP, macs.toString());

        boolean responseRequired = true;
        JSONObject postJSON = getRequestJSONLocal(statusLight, responseRequired);

        JSONObject respJSON = EspBaseApiUtil.PostForJson(getLocalUrl(inetAddress),
            MeshCommunicationUtils.MULTICAST_MAC,
            postJSON,
            multicastHeader);
        boolean result = respJSON != null;
        log.info("localPostMulticast result = " + result);
        return result;
    }

    public IEspStatusLight getLocal(IEspDevice device) {
        InetAddress inetAddress = device.getInetAddress();
        boolean isMeshDevice = device.getIsMeshDevice();
        String url = getLocalUrl(inetAddress);
        JSONObject json = null;
        if (!isMeshDevice) {
            json = EspBaseApiUtil.Get(url);
        } else {
            json = EspBaseApiUtil.GetForJson(url, device.getBssid());
        }

        if (json == null) {
            return null;
        }

        try {
            int status = json.getInt(KEY_STATUS);
            switch (status) {
                case IEspStatusLight.STATUS_BRIGHT:
                case IEspStatusLight.STATUS_COLOR:
                case IEspStatusLight.STATUS_OFF:
                case IEspStatusLight.STATUS_ON:
                    break;
                default:
                    return null;
            }
//            int period = json.optInt(KEY_PERIOD);
            int period = IEspStatusLight.PERIOD_DEFAULT;
            JSONObject colorJSON = json.getJSONObject(KEY_COLOR);
            int red = colorJSON.getInt(KEY_RED);
            int green = colorJSON.getInt(KEY_GREEN);
            int blue = colorJSON.getInt(KEY_BLUE);
            int white = colorJSON.getInt(KEY_WHITE);

            IEspStatusLight result = new EspStatusLight();
            result.setStatus(status);
            result.setPeriod(period);
            result.setRed(red);
            result.setGreen(green);
            result.setBlue(blue);
            result.setWhite(white);

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
     * Local command end
     */

    /*
     * Internet command start
     */
    private JSONObject getRequestJSONInternet(IEspStatusLight statusLight) {
        JSONObject result = new JSONObject();
        try {
            JSONObject dataJSON = new JSONObject();
            int status = statusLight.getStatus();
            dataJSON.put(X, status);
            dataJSON.put(Y, IEspStatusLight.PERIOD_DEFAULT);
            JSONObject colorJSON = new JSONObject();
            switch (status) {
                case IEspStatusLight.STATUS_ON:
                case IEspStatusLight.STATUS_OFF:
                    break;
                case IEspStatusLight.STATUS_COLOR:
                    colorJSON.put(KEY_RED, statusLight.getRed());
                    colorJSON.put(KEY_GREEN, statusLight.getGreen());
                    colorJSON.put(KEY_BLUE, statusLight.getBlue());
                    dataJSON.put(Z, colorJSON);
                    break;
                case IEspStatusLight.STATUS_BRIGHT:
                    colorJSON.put(KEY_WHITE, statusLight.getWhite());
                    dataJSON.put(Z, colorJSON);
                    break;
                default:
                    return null;
            }

            result.put(Datapoint, dataJSON);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean internetPost(String deviceKey, IEspStatusLight statusLight) {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        String url = IEspCommandLightPostStatusInternet.URL;
        JSONObject postJSON = getRequestJSONInternet(statusLight);
        JSONObject respJSON = EspBaseApiUtil.Post(url, postJSON, header);
        if (respJSON == null) {
            return false;
        }

        try {
            int respCode = respJSON.getInt(Status);
            return respCode == HttpURLConnection.HTTP_OK;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean postInternet(IEspDevice device, IEspStatusLight statusLight) {
        String deviceKey = device.getKey();
        boolean result = internetPost(deviceKey, statusLight);
        log.debug("##postInternet(deviceKey=[" + deviceKey + "],statusLight=[" + statusLight + "]): " + result);
        return result;
    }

    public boolean postInternetMulticast(String deviceKey, IEspStatusLight statusLight, List<String> bssids) {
        boolean result = true;
        List<String> macList = new ArrayList<String>();
        for (String bssid : bssids) {
            macList.add(bssid);
            if (macList.size() == MULTICAST_GROUP_LENGTH_LIMIT) {
                if (!internetPostMulticast(deviceKey, statusLight, macList)) {
                    result = false;
                }
                macList.clear();
            }
        }
        if (!macList.isEmpty()) {
            if (!internetPostMulticast(deviceKey, statusLight, macList)) {
                result = false;
            }
        }
        return result;
    }

    private boolean internetPostMulticast(String deviceKey, IEspStatusLight statusLight, List<String> macList) {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);

        StringBuilder urlBuilder = new StringBuilder(URL_MULTICAST);
        for (int i = 0; i < macList.size(); i++) {
            String mac = macList.get(i);
            urlBuilder.append(mac);
            if (i < macList.size() - 1) {
                urlBuilder.append(",");
            }
        }

        try {
            JSONObject postJSON = getRequestJSONInternet(statusLight);
            JSONObject result = EspBaseApiUtil.Post(urlBuilder.toString(), postJSON, header);
            if (result != null) {
                int httpStatus = result.getInt(Status);
                return httpStatus == HttpStatus.SC_OK;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public IEspStatusLight getInternet(IEspDevice device) {
        String headerKey = Authorization;
        String headerValue = Token + " " + device.getKey();
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        JSONObject respJSON = EspBaseApiUtil.Get(IEspCommandLightPostStatusInternet.URL, header);
        if (respJSON == null) {
            return null;
        }

        try {
            int httpStatus = respJSON.getInt(Status);
            if (httpStatus != HttpURLConnection.HTTP_OK) {
                return null;
            }

            JSONObject dataJSON = respJSON.getJSONObject(Datapoint);
            int status = dataJSON.getInt(X);
            switch (status) {
                case IEspStatusLight.STATUS_BRIGHT:
                case IEspStatusLight.STATUS_COLOR:
                case IEspStatusLight.STATUS_OFF:
                case IEspStatusLight.STATUS_ON:
                    break;
                default:
                    return null;
            }
//            int period = dataJSON.getInt(Y);
            int period = IEspStatusLight.PERIOD_DEFAULT;
            JSONObject colorJSON = dataJSON.getJSONObject(Z);
            int red = colorJSON.getInt(KEY_RED);
            int green = colorJSON.getInt(KEY_GREEN);
            int blue = colorJSON.getInt(KEY_BLUE);
            int white = colorJSON.getInt(KEY_WHITE);

            IEspStatusLight result = new EspStatusLight();
            result.setStatus(status);
            result.setPeriod(period);
            result.setRed(red);
            result.setGreen(green);
            result.setBlue(blue);
            result.setWhite(white);

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    /*
     * Internet command end
     */
}
