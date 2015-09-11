package com.espressif.iot.base.net.rest2;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;

class EspMeshNetUtil
{
    
    private static final Logger log = Logger.getLogger(EspMeshHttpUtil.class);
    
    private static class SubParentTopoResult
    {
        SubParentTopoResult()
        {
            iotAddressList = null;
            totalNum = -1;
        }
        
        List<IOTAddress> iotAddressList;
        
        int totalNum;
    }
    
    static SubParentTopoResult __GetSubParentTopoIOTAddressList2(InetAddress rootInetAddress, String deviceBssid,
        boolean isSubDevices)
    {
        log.debug("__GetSubParentTopoIOTAddressList2(): entrance");
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        // build request
        String uriStr = "http://" + rootInetAddress.getHostAddress() + "/config?command=mesh_info";
        JSONObject jsonResult = EspMeshHttpUtil.GetForJson(uriStr, deviceBssid);
        log.debug("__GetSubParentTopoIOTAddressList2(): jsonResult:" + jsonResult);
        if (jsonResult == null)
        {
            // return null to extinguish device hasn't child
            log.warn("__GetSubParentTopoIOTAddressList2(): jsonResult is null, return null");
            return null;
        }
        // parse response
        int totalNum = -1;
        try
        {
            if (isSubDevices)
            {
                log.debug("__GetSubParentTopoIOTAddressList2(): isSubDevices = true");
                JSONArray jsonArrayChildren = null;
                if (!jsonResult.isNull("children"))
                {
                    jsonArrayChildren = jsonResult.getJSONArray("children");
                    for (int i = 0; i < jsonArrayChildren.length(); ++i)
                    {
                        JSONObject jsonChild = jsonArrayChildren.getJSONObject(i);
                        if (!jsonChild.isNull("type") && !jsonChild.isNull("mac"))
                        {
                            String typeStr = jsonChild.getString("type");
                            EspDeviceType deviceTypeEnum = EspDeviceType.getEspTypeEnumByString(typeStr);
                            if (deviceTypeEnum == null)
                            {
                                // no more devices, so break
                                break;
                            }
                            String bssid = jsonChild.getString("mac");
                            IOTAddress iotAddress = new IOTAddress(bssid, rootInetAddress, true);
                            iotAddress.setParentBssid(deviceBssid);
                            iotAddress.setEspDeviceTypeEnum(deviceTypeEnum);
                            log.debug("__GetSubParentTopoIOTAddressList2(): iotAddress: " + iotAddress + " is added");
                            iotAddressList.add(iotAddress);
                        }
                    }
                }
                if (!jsonResult.isNull("num"))
                {
                    String numStr = jsonResult.getString("num");
                    totalNum = Integer.parseInt(numStr);
                    log.debug("__GetSubParentTopoIOTAddressList2(): totalNum: " + totalNum);
                }
            }
            else
            {
                log.debug("__GetSubParentTopoIOTAddressList2(): isSubDevices = false");
                JSONObject jsonParent = null;
                if (!jsonResult.isNull("parent") && !jsonResult.isNull("type"))
                {
                    jsonParent = jsonResult.getJSONObject("parent");
                    String typeStr = jsonResult.getString("type");
                    EspDeviceType deviceTypeEnum = EspDeviceType.getEspTypeEnumByString(typeStr);
                    if (deviceTypeEnum != null && !jsonParent.isNull("mac"))
                    {
                        String parentBssid = jsonParent.getString("mac");
                        IOTAddress iotAddress = new IOTAddress(deviceBssid, rootInetAddress, true);
                        iotAddress.setParentBssid(parentBssid);
                        iotAddress.setEspDeviceTypeEnum(deviceTypeEnum);
                        log.debug("__GetSubParentTopoIOTAddressList2(): iotAddress: " + iotAddress + " is added");
                        iotAddressList.add(iotAddress);
                    }
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        SubParentTopoResult result = new SubParentTopoResult();
        result.iotAddressList = iotAddressList;
        result.totalNum = totalNum;
        
        return result;
    }
    
    static List<IOTAddress> __GetTopoIOTAddressList2(InetAddress rootInetAddress, String deviceBssid,
        boolean isSubDevices)
    {
        log.debug("__GetTopoIOTAddressList2(): entrance");
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        
        int index = 0;
        String currentBssid = deviceBssid;
        IOTAddress next = null;
        int totalNum = -1;
        boolean isMoreDevice = false;
        do
        {
            next = null;
            // get sub topo list
            SubParentTopoResult subParentTopoResult =
                __GetSubParentTopoIOTAddressList2(rootInetAddress, currentBssid, isSubDevices);
            
            List<IOTAddress> subParentIOTAddressList = null;
            
            if (subParentTopoResult != null)
            {
                // update totalNum if necessary, totalNum should be init only once
                if (isSubDevices && iotAddressList.isEmpty() && subParentTopoResult.totalNum != -1)
                {
                    totalNum = subParentTopoResult.totalNum;
                }
                subParentIOTAddressList = subParentTopoResult.iotAddressList;
            }
            
            if (subParentIOTAddressList == null)
            {
                // when some error occurs, subParentIOTAddressList will null
                log.warn("__GetTopoIOTAddressList2(): subParentIOTAddressList is null");
                // although the client should be closed already, close it again to make it close surely
                return iotAddressList;
            }
            for (IOTAddress subIOTAddress : subParentIOTAddressList)
            {
                // only added the device which hasn't been added
                if (!iotAddressList.contains(subIOTAddress))
                {
                    iotAddressList.add(subIOTAddress);
                }
            }
            // check whether the device is the last one
            if (index < iotAddressList.size())
            {
                next = iotAddressList.get(index);
                currentBssid = next.getBSSID();
            }
            // move to next index
            ++index;
            // check whether there's more device, here 1 means root device
            isMoreDevice = iotAddressList.size() + 1 < totalNum;
        } while (next != null && isSubDevices && isMoreDevice);
        
        return iotAddressList;
    }
    
    static IOTAddress GetTopoIOTAddress2(InetAddress rootInetAddress, String deviceBssid)
    {
         List<IOTAddress> iotAddressList = __GetTopoIOTAddressList2(rootInetAddress, deviceBssid, false);
         if (iotAddressList == null || iotAddressList.isEmpty())
         {
         log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddress2(rootInetAddress=[" + rootInetAddress
         + "],deviceBssid=[" + deviceBssid + "]): empty, return null");
         return null;
         }
         IOTAddress iotAddress0 = iotAddressList.get(0);
         log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddress2(rootInetAddress=[" + rootInetAddress
         + "],deviceBssid=[" + deviceBssid + "]): " + iotAddress0);
         return iotAddress0;
    }
    
    static List<IOTAddress> GetTopoIOTAddressList2(InetAddress rootInetAddress, String rootBssid)
    {
         List<IOTAddress> iotAddressList = __GetTopoIOTAddressList2(rootInetAddress, rootBssid, true);
         log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddressList2(rootInetAddress=[" + rootInetAddress
         + "],rootBssid=[" + rootBssid + "]): " + iotAddressList);
         return iotAddressList;
    }
}
