package com.espressif.iot.base.net.mdns;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;

public class MdnsDiscoverUtil
{
    
    private static final Logger log = Logger.getLogger(MdnsDiscoverUtil.class);

    private static final long timeout = 2000;
    private static final String IOT_SERVICE_TYPE = "_iot._tcp.local";
    private static final String KEY_BSSID = "bssid";
    private static final String KEY_TYPE = "type";
    
    private static IOTAddress __parseSeviceInfo2IOTAddress(ServiceInfo serviceInfo)
    {
        byte[] textBytes = serviceInfo.getTextBytes();
        log.debug("__parseSeviceInfo2IOTAddress(): textBytes toString: " + new String(textBytes));
        // check whether the serviceInfo is valid
        int index = 0;
        while (index < textBytes.length)
        {
            index += (1 + textBytes[index]);
        }
        if (index != textBytes.length)
        {
            log.warn("__parseSeviceInfo2IOTAddress(): bad serviceInfo format, return null");
            return null;
        }
        // get InetAddress
        InetAddress inetAddress = null;
        inetAddress = serviceInfo.getInet4Addresses()[0];
        // get bssid, device type, device version
        Map<String, String> keyValue = new HashMap<String, String>();
        index = 0;
        String part = null;
        String[] subParts = null;
        while (index < textBytes.length)
        {
            part = new String(textBytes, index + 1, textBytes[index]);
            subParts = part.split("=");
            // if the content is invalid, just ignore it
            if (subParts.length == 2)
            {
                keyValue.put(subParts[0], subParts[1]);
            }
            index += (1 + textBytes[index]);
        }
        String bssid = keyValue.get(KEY_BSSID);
        String type = keyValue.get(KEY_TYPE);
        if (bssid == null || type == null)
        {
            log.warn("__parseSeviceInfo2IOTAddress(): bssid = null or type = null, return null");
            return null;
        }
        EspDeviceType deviceType = EspDeviceType.getEspTypeEnumByString(type);
        if (deviceType == null)
        {
            log.warn("__parseSeviceInfo2IOTAddress(): deviceType = null, return null");
            return null;
        }
        IOTAddress iotAddress = new IOTAddress(bssid, inetAddress);
        iotAddress.setEspDeviceTypeEnum(deviceType);
        return iotAddress;
    }
    
    private static List<IOTAddress> __parseSeviceInfoArray2IOTAddressList(ServiceInfo[] serviceInfoArray)
    {
        if (serviceInfoArray == null)
        {
            return Collections.emptyList();
        }
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        IOTAddress iotAddress = null;
        for (ServiceInfo serviceInfo : serviceInfoArray)
        {
            iotAddress = __parseSeviceInfo2IOTAddress(serviceInfo);
            if (iotAddress != null)
            {
                iotAddressList.add(iotAddress);
            }
        }
        if (iotAddressList.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            return iotAddressList;
        }
    }

    private static List<IOTAddress> __discoverIOTDevices()
    {
        log.debug("__discoverIOTDevices() entrance");
        try
        {
            final JmDNS mdnsService = JmDNS.create();
            log.debug("__discoverIOTDevices() JmDNS.create() finished");
            ServiceInfo[] serviceInfoArray = mdnsService.list(IOT_SERVICE_TYPE, timeout);
            List<IOTAddress> iotAddressList = __parseSeviceInfoArray2IOTAddressList(serviceInfoArray);
            // close the JmDNS asyn, for close() will take too much time, so we close it asyn
            EspBaseApiUtil.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        mdnsService.close();
                    }
                    catch (IOException igrnoeException)
                    {
                    }
                }
            });
            return iotAddressList;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        log.debug("__discoverIOTDevices() exit with emptyList");
        return Collections.emptyList();
    }
    
    public static List<IOTAddress> discoverIOTDevices()
    {
        return __discoverIOTDevices();
    }
}
