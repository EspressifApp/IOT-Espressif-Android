package com.espressif.iot.action.device.New;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.New.EspCommandDeviceNewActivateInternet;
import com.espressif.iot.command.device.New.IEspCommandDeviceNewActivateInternet;
import com.espressif.iot.db.IOTDeviceDBManager;
import com.espressif.iot.device.IEspDevice;

public class EspActionDeviceNewActivateInternet implements IEspActionDeviceNewActivateInternet
{
    private final static Logger log = Logger.getLogger(EspActionDeviceNewActivateInternet.class);
    
    private final static long TIMEOUT = 30 * 1000;
    
    static int count = 0;
    
    private static final String PREFIX_FILTER = "ESP_";
    
    // to support mesh in espressif, it won't bother others, so let it leave here
    private static final String PREFIX_FILTER2 = "espressif_";
    
    // "ESP_" + MAC address's 6 places
    private boolean isESPDevice(String SSID)
    {
        // "ESP_"'s length = 4, 4+6=10
        if (SSID.length() != 10)
            return false;
        for (int i = 0; i < PREFIX_FILTER.length(); i++)
        {
            if (i >= SSID.length() || SSID.charAt(i) != PREFIX_FILTER.charAt(i))
                return false;
        }
        return true;
    }
    
    // "espressif_" + MAC address's 6 places
    private boolean isESPDevice2(String SSID)
    {
        // "espressif_"'s length = 10, 10+6=16
        if (SSID.length() != 16)
            return false;
        for (int i = 0; i < PREFIX_FILTER2.length(); i++)
        {
            if (i >= SSID.length() || SSID.charAt(i) != PREFIX_FILTER2.charAt(i))
                return false;
        }
        return true;
    }
    
    /**
     * wait the wifi leave Esp Device. otherwise, the action will be blocked about 20 seconds or so by Android System.
     * for the wifi is connected but the wifi can't be accessible to Internet
     * 
     * @return
     * @throws InterruptedException
     */
    private boolean __waitEspDeviceDisconnected()
        throws InterruptedException
    {
        for (int retry = 0; retry < 20; retry++)
        {
            String ssid = EspBaseApiUtil.getWifiConnectedSsid();
            log.error("ssid: " + ssid);
            if (ssid != null && !(isESPDevice(ssid) && !isESPDevice2(ssid)))
            {
                return true;
            }
            else
            {
                Thread.sleep(1000);
            }
        }
        return false;
    }
    
    @Override
    public IEspDevice doActionDeviceNewActivateInternet(long userId, String userKey, String randomToken,
        long negativeDeviceId)
        throws InterruptedException
    {
        log.error("count=" + count++);
        long startTime = System.currentTimeMillis();
        boolean isSuc = __waitEspDeviceDisconnected();
        if (!isSuc)
        {
            log.warn(Thread.currentThread().toString() + "##__waitEspDeviceDisconnected() fail");
            if (!EspBaseApiUtil.isNetworkAvailable())
            {
                return null;
            }
        }
        // TODO
        // oldDeviceName
        // newDeviceName check
        // doRenameAction
        IEspCommandDeviceNewActivateInternet command = new EspCommandDeviceNewActivateInternet();
        IEspDevice device = null;
        do
        {
            device = command.doCommandNewActivateInternet(userId, userKey, randomToken);
            if (device != null)
            {
                // suc
                long timestamp = EspBaseApiUtil.getUTCTimeLong();
                if (timestamp == Long.MIN_VALUE)
                {
                    timestamp = System.currentTimeMillis();
                }
                device.setTimestamp(timestamp);
                break;
            }
            if (System.currentTimeMillis() - startTime > TIMEOUT)
            {
                // timeout
                log.warn(Thread.currentThread().toString() + "##doActionDeviceNewActivateInternet() timeout");
                break;
            }
            log.info(Thread.currentThread().toString() + "##doActionDeviceNewActivateInternet(userId=[" + userId
                + "],userKey=[" + userKey + "],randomToken=[" + randomToken + "],negativeDeviceId=[" + negativeDeviceId
                + "]): " + "sleep 1000ms try again");
            Thread.sleep(1000);
        } while (device == null);
        
        if (device != null)
        {
            IOTDeviceDBManager iotDeviceDBManager = IOTDeviceDBManager.getInstance();
            iotDeviceDBManager.deleteDevicesByDeviceId(negativeDeviceId);
        }
        log.debug(Thread.currentThread().toString() + "##doActionDeviceNewActivateInternet(userId=[" + userId
            + "],userKey=[" + userKey + "],randomToken=[" + randomToken + "],negativeDeviceId=[" + negativeDeviceId
            + "]): " + device);
        return device;
    }
    
}
