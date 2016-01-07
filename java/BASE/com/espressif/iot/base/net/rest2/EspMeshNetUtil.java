package com.espressif.iot.base.net.rest2;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

class EspMeshNetUtil
{
    
    private static final Logger log = Logger.getLogger(EspMeshHttpUtil.class);
    
    private static class MeshTopoResult
    {
        MeshTopoResult()
        {
            mIOTAddress = null;
            mSubMeshTopoResultList = new ArrayList<MeshTopoResult>();
            mTotalNum = -1;
        }
        
        private int mTotalNum;
        
        private IOTAddress mIOTAddress;
        
        private List<MeshTopoResult> mSubMeshTopoResultList;
        
        void setTotalNum(int totalNum)
        {
            mTotalNum = totalNum;
        }
        
        int getTotalNum()
        {
            return mTotalNum;
        }
        
        void setIOTAddress(IOTAddress iotAddress)
        {
            mIOTAddress = iotAddress;
        }
        
        IOTAddress getIOTAddress()
        {
            return mIOTAddress;
        }
        
        void addTopoResultChild(MeshTopoResult topoResultChild)
        {
            mSubMeshTopoResultList.add(topoResultChild);
        }
        
        List<MeshTopoResult> getTopoResultChildList()
        {
            return mSubMeshTopoResultList;
        }
        
        @Override
        public boolean equals(Object o)
        {
            return super.equals(o);
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("[ iotAddress = ");
            sb.append(mIOTAddress);
            sb.append(" | totalNum = ");
            sb.append(mTotalNum);
            sb.append(" | subMeshTopoResultList = ");
            for (MeshTopoResult child : mSubMeshTopoResultList)
            {
                sb.append(child);
            }
            sb.append(" ]");
            return sb.toString();
        }
    }
    
    private static MeshTopoResult getMeshTopoResult(InetAddress rootInetAddress, String targetBssid)
    {
        log.debug("getMeshTopoResult() entrance");
        MeshTopoResult topoResult = new MeshTopoResult();
        // build request
        String uriStr = "http://" + rootInetAddress.getHostAddress() + "/config?command=mesh_info";
        // send request and receive response
        JSONObject jsonResult = EspMeshHttpUtil.GetForJson(uriStr, targetBssid);
        // check whether the jsonResult is null
        if (jsonResult == null)
        {
            // return null
            log.warn("getMeshTopoResult() jsonResult is null, return null");
            return null;
        }
        // parse response
        JSONArray jsonArrayChildren = null;
        try
        {
            // parse
            if (!jsonResult.isNull("children"))
            {
                jsonArrayChildren = jsonResult.getJSONArray("children");
                for (int i = 0; i < jsonArrayChildren.length(); ++i)
                {
                    MeshTopoResult topoResultChild = new MeshTopoResult();
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
                        iotAddress.setParentBssid(targetBssid);
                        iotAddress.setEspDeviceTypeEnum(deviceTypeEnum);
                        topoResultChild.setIOTAddress(iotAddress);
                        
                    }
                    if (!jsonChild.isNull("num"))
                    {
                        String numStr = jsonChild.getString("num");
                        int num = Integer.parseInt(numStr);
                        topoResultChild.setTotalNum(num);
                    }
                    topoResult.addTopoResultChild(topoResultChild);
                    log.debug("getMeshTopoResult() topoResultChild: " + topoResultChild + " is added");
                }
            }
            // parse num
            if (!jsonResult.isNull("num"))
            {
                String numStr = jsonResult.getString("num");
                int num = Integer.parseInt(numStr);
                topoResult.setTotalNum(num);
                log.debug("getMeshTopoResult() totalNum: " + num);
            }
            // parse iotAddress and parent
            JSONObject jsonParent = null;
            if (!jsonResult.isNull("parent") && !jsonResult.isNull("type"))
            {
                jsonParent = jsonResult.getJSONObject("parent");
                String typeStr = jsonResult.getString("type");
                EspDeviceType deviceTypeEnum = EspDeviceType.getEspTypeEnumByString(typeStr);
                if (deviceTypeEnum != null && !jsonParent.isNull("mac"))
                {
                    String parentBssid = jsonParent.getString("mac");
                    IOTAddress iotAddress = new IOTAddress(targetBssid, rootInetAddress, true);
                    iotAddress.setParentBssid(parentBssid);
                    iotAddress.setEspDeviceTypeEnum(deviceTypeEnum);
                    topoResult.setIOTAddress(iotAddress);
                    log.debug("getMeshTopoResult() iotAddress: " + iotAddress);
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        log.info("getMeshTopoResult() topoResult: " + topoResult);
        return topoResult;
    }
    
    private static List<MeshTopoResult> getMeshTopoResultsRecursion(final InetAddress rootInetAddress,final String rootBssid,
        final String targetBssid)
    {
        List<MeshTopoResult> topoResultList = new ArrayList<MeshTopoResult>();
        // get topo result
        MeshTopoResult topoResult = getMeshTopoResult(rootInetAddress, targetBssid);
        if (topoResult == null)
        {
            log.warn("getMeshTopoResultsRecursion() topoResult is null, return Collections.emptyList()");
            return Collections.emptyList();
        }
        // get topo child list result and process them
        List<MeshTopoResult> resultChildList = topoResult.getTopoResultChildList();
        topoResultList.addAll(resultChildList);
        updateTempStaDeviceList(rootBssid, resultChildList);
        // get mesh topo results recursively if necessary
        if (topoResult.getTotalNum() == 1 || resultChildList.size() == 0)
        {
            log.debug("getMeshTopoResultsRecursion() topoResult.getTotalNum()=" + topoResult.getTotalNum()
                + ", resultChildList.size()=" + resultChildList.size() + ", return Collections.emptyList()");
            return Collections.emptyList();
        }
        List<Future<List<MeshTopoResult>>> futureList = new ArrayList<Future<List<MeshTopoResult>>>();
        for (MeshTopoResult topoResultChild : resultChildList)
        {
            if (topoResultChild.getTotalNum() == 0)
            {
                log.debug("getMeshTopoResultsRecursion() topoResultChild: " + topoResultChild
                    + " is leaf node, continue");
                continue;
            }
            else
            {
                // new sub-task and execute
                final String subTargetBssid = topoResultChild.getIOTAddress().getBSSID();
                Future<List<MeshTopoResult>> future = EspBaseApiUtil.submit(new Callable<List<MeshTopoResult>>()
                {
                    @Override
                    public List<MeshTopoResult> call()
                        throws Exception
                    {
                        try
                        {
                            return getMeshTopoResultsRecursion(rootInetAddress, rootBssid, subTargetBssid);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            return Collections.emptyList();
                        }
                    }
                });
                futureList.add(future);
            }
        }
        // wait all sub-task finished
        for (Future<List<MeshTopoResult>> future : futureList)
        {
            try
            {
                List<MeshTopoResult> meshTopoResults = future.get();
                topoResultList.addAll(meshTopoResults);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
        }
        return topoResultList;
    }
    
    private static void updateTempStaDeviceList(String rootBssid, List<MeshTopoResult> topoResultChildList)
    {
        IEspUser user = BEspUser.getBuilder().getInstance();
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        for (MeshTopoResult topoResultChild : topoResultChildList)
        {
            IOTAddress iotAddress = topoResultChild.getIOTAddress();
            if (!iotAddressList.contains(iotAddress))
            {
                iotAddressList.add(iotAddress);
            }
            else
            {
                log.warn("updateTempStaDeviceList() iotAddress is duplicate");
            }
        }
        user.__addTempStaDeviceList(iotAddressList);
    }
    
    static IOTAddress GetTopoIOTAddress3(InetAddress rootInetAddress, String deviceBssid)
    {
        MeshTopoResult topoResult = getMeshTopoResult(rootInetAddress, deviceBssid);
        if (topoResult == null)
        {
            log.warn(Thread.currentThread().toString() + "##GetTopoIOTAddress3(rootInetAddress=[" + rootInetAddress
                + "],deviceBssid=[" + deviceBssid + "]): empty, return null");
            return null;
        }
        else
        {
            IOTAddress result = topoResult.getIOTAddress();
            log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddress3(rootInetAddress=[" + rootInetAddress
                + "],deviceBssid=[" + deviceBssid + "]): " + result);
            return result;
        }
    }
    
    static List<IOTAddress> GetTopoIOTAddressList3(InetAddress rootInetAddress, String rootBssid)
    {
        List<MeshTopoResult> meshTopoResultList = getMeshTopoResultsRecursion(rootInetAddress, rootBssid, rootBssid);
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        for (MeshTopoResult meshTopoResult : meshTopoResultList)
        {
            IOTAddress iotAddress = meshTopoResult.getIOTAddress();
            if (!iotAddressList.contains(iotAddress))
            {
                iotAddressList.add(iotAddress);
            }
            else
            {
                log.warn("GetTopoIOTAddressList3() iotAddress:" + iotAddress + " is in iotAddressList already");
            }
        }
        return iotAddressList;
    }
    
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
    
    /**
     * update user's temporary sta device list 
     * @param rootBssid
     * @param iotAddressList
     */
    static void __updateTempStaDeviceList(String rootBssid, List<IOTAddress> iotAddressList)
    {
        IEspUser user = BEspUser.getBuilder().getInstance();
        for (IOTAddress iotAddress : iotAddressList)
        {
            iotAddress.setRootBssid(rootBssid);
        }
        user.__addTempStaDeviceList(iotAddressList);
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
                if (isSubDevices)
                {
                    __updateTempStaDeviceList(deviceBssid, subParentIOTAddressList);
                }
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
            log.error(Thread.currentThread().toString() + "__GetTopoIOTAddressList2(): iotAddressList.size() + 1:"
                + (iotAddressList.size() + 1) + ", totalNum:" + totalNum + ", next:" + next + ", isSubDevices:"
                + isSubDevices + ", isMoreDevice:" + isMoreDevice);
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
