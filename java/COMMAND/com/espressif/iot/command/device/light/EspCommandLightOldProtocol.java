package com.espressif.iot.command.device.light;

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
import com.espressif.iot.type.device.other.EspLightRecord;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandLightOldProtocol implements IEspCommandLight, IEspCommandLocal, IEspCommandInternet {
    private final static Logger log = Logger.getLogger(EspCommandLightOldProtocol.class);

    private static final String KEY_RESPONSE = "response";

    private int convertRGBWToStatus(int period, int rgbw) {
        int statusMax = EspLightRecord.getRgbwMax(period);
        int rgbwMax = 255;

        return rgbw * statusMax / rgbwMax;
    }

    private int convertStatusToRGBW(int period, int status) {
        int statusMax = EspLightRecord.getRgbwMax(period);
        int rgbwMax = 255;

        return status * rgbwMax / statusMax;
    }

    private void setGetResultStatus(IEspStatusLight lightStatus) {
        int red = lightStatus.getRed();
        int green = lightStatus.getGreen();
        int blue = lightStatus.getBlue();
        int white = Math.max(lightStatus.getCWhite(), lightStatus.getWWhite());
        if (red == green && red == blue) {
            lightStatus.setStatus(IEspStatusLight.STATUS_BRIGHT);
            lightStatus.setWhite(Math.max(white, red));
        } else {
            lightStatus.setStatus(IEspStatusLight.STATUS_COLOR);
        }
    }

    /*
     * Local command start
     */
    @Override
    public String getLocalUrl(InetAddress inetAddress) {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=light";
    }

    private JSONObject getRequestJSONLocal(IEspStatusLight statusLight, boolean isResponseRequired) {
        JSONObject request = new JSONObject();
        JSONObject rgb = new JSONObject();
        int period = IEspStatusLight.PERIOD_DEFAULT;
        int red = convertRGBWToStatus(period, statusLight.getRed());
        int green = convertRGBWToStatus(period, statusLight.getGreen());
        int blue = convertRGBWToStatus(period, statusLight.getBlue());
        int cw = convertRGBWToStatus(period, statusLight.getCWhite());
        int ww = convertRGBWToStatus(period, statusLight.getWWhite());
        try {
            rgb.put(Red, red);
            rgb.put(Green, green);
            rgb.put(Blue, blue);
            rgb.put(CWhite, cw);
            rgb.put(WWhite, ww);
            request.put(Period, period);
            request.put(Rgb, rgb);
            if (isResponseRequired) {
                request.put(KEY_RESPONSE, 1);
            } else {
                request.put(KEY_RESPONSE, 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return request;
    }

    private boolean localPost(InetAddress inetAddress, JSONObject postJSON, String deviceBssid, boolean isMeshDevice,
        boolean isResponseRequired, Runnable disconnectedCallback) {
        String uriString = getLocalUrl(inetAddress);
        JSONObject result = null;
        if (isResponseRequired) {
            if (!isMeshDevice || deviceBssid == null) {
                result = EspBaseApiUtil.Post(uriString, postJSON);
            } else {
                result = EspBaseApiUtil.PostForJson(uriString, deviceBssid, postJSON);
            }
            return (result != null);
        } else {
            // no response is available, so we treat it suc, when fail socket will be disconnected
            if (!isMeshDevice || deviceBssid == null) {
                // normal device
                EspBaseApiUtil.PostInstantly(uriString, postJSON, disconnectedCallback);
            } else {
                // mesh device
                EspBaseApiUtil.PostForJsonInstantly(uriString, deviceBssid, postJSON, disconnectedCallback);
            }
            return true;
        }
    }

    public boolean postLocal(IEspDevice device, IEspStatusLight statusLight) {
        boolean responseRequired = true;
        JSONObject postJSON = getRequestJSONLocal(statusLight, responseRequired);
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean result = localPost(inetAddress, postJSON, deviceBssid, isMeshDevice, responseRequired, null);

        log.debug("##postLocal(inetAddress=[" + inetAddress + "],statusLight=[" + statusLight + "],deviceBssid=["
            + deviceBssid + "],isMeshDevice=[" + isMeshDevice + "]): " + result);

        return result;
    }

    public void postLocalInstantly(IEspDevice device, IEspStatusLight statusLight, Runnable disconnectedCallback) {
        boolean responseRequired = false;
        JSONObject postJSON = getRequestJSONLocal(statusLight, responseRequired);
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        localPost(inetAddress, postJSON, deviceBssid, isMeshDevice, responseRequired, disconnectedCallback);

        log.debug("##postLocalInstantly(inetAddress=[" + inetAddress + "],statusLight=[" + statusLight
            + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=[" + isMeshDevice + "])");
    }

    public boolean postLocalMulticast(InetAddress inetAddress, IEspStatusLight statusLight, List<String> bssids) {
        if (bssids.size() == 1) {
            boolean responseRequired = true;
            JSONObject postJSON = getRequestJSONLocal(statusLight, responseRequired);
            return localPost(inetAddress, postJSON, bssids.get(0), true, responseRequired, null);
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
        log.info("postMulticastCommand result = " + result);
        return result;
    }

    public IEspStatusLight getLocal(IEspDevice device) {
        String uriString = getLocalUrl(device.getInetAddress());
        JSONObject jo = null;
        if (!device.getIsMeshDevice()) {
            jo = EspBaseApiUtil.Get(uriString);
        } else {
            jo = EspBaseApiUtil.GetForJson(uriString, device.getBssid());
        }
        if (jo == null) {
            return null;
        }
        try {
//            int period = jo.getInt(Period);
            int period = IEspStatusLight.PERIOD_DEFAULT;
            JSONObject rgb = jo.getJSONObject(Rgb);
            int red = rgb.getInt(Red);
            int green = rgb.getInt(Green);
            int blue = rgb.getInt(Blue);
            int cwhite = rgb.getInt(CWhite);
            int wwhite = rgb.getInt(WWhite);
            IEspStatusLight status = new EspStatusLight();
            status.setPeriod(period);
            status.setRed(convertStatusToRGBW(period, red));
            status.setGreen(convertStatusToRGBW(period, green));
            status.setBlue(convertStatusToRGBW(period, blue));
            status.setCWhite(convertStatusToRGBW(period, cwhite));
            status.setWWhite(convertStatusToRGBW(period, wwhite));
            setGetResultStatus(status);
            return status;
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
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectX = new JSONObject();
        int period = IEspStatusLight.PERIOD_DEFAULT;
        int red = convertRGBWToStatus(period, statusLight.getRed());
        int green = convertRGBWToStatus(period, statusLight.getGreen());
        int blue = convertRGBWToStatus(period, statusLight.getBlue());
        int white = convertRGBWToStatus(period, statusLight.getCWhite());
        try {
            jsonObjectX.put(X, period);
            jsonObjectX.put(Y, red);
            jsonObjectX.put(Z, green);
            jsonObjectX.put(K, blue);
            jsonObjectX.put(L, white);
            jsonObject.put(Datapoint, jsonObjectX);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private boolean internetPost(String deviceKey, IEspStatusLight statusLight) {

        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        String url = IEspCommandLightPostStatusInternet.URL;
        JSONObject jsonObject = getRequestJSONInternet(statusLight);
        JSONObject result = EspBaseApiUtil.Post(url, jsonObject, header);
        if (result == null) {
            return false;
        }

        int status = -1;
        try {
            status = result.getInt(Status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status == HttpStatus.SC_OK;
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
        String deviceKey = device.getKey();
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        JSONObject result = null;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        String url = IEspCommandLightPostStatusInternet.URL;
        result = EspBaseApiUtil.Get(url, header);
        if (result == null) {
            return null;
        }
        try {
            int status = -1;
            try {
                status = result.getInt(Status);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (status == HttpStatus.SC_OK) {
                JSONObject data = result.getJSONObject(Datapoint);
//                int period = data.getInt(X);
                int period = IEspStatusLight.PERIOD_DEFAULT;
                int red = data.getInt(Y);
                int green = data.getInt(Z);
                int blue = data.getInt(K);
                int white = data.getInt(L);
                IEspStatusLight statusLight = new EspStatusLight();
                statusLight.setPeriod(period);
                statusLight.setRed(convertStatusToRGBW(period, red));
                statusLight.setGreen(convertStatusToRGBW(period, green));
                statusLight.setBlue(convertStatusToRGBW(period, blue));
                statusLight.setWhite(convertStatusToRGBW(period, white));
                setGetResultStatus(statusLight);
                return statusLight;
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
     * Internet command end
     */
}
