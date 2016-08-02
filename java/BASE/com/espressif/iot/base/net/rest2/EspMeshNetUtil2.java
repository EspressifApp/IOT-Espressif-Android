package com.espressif.iot.base.net.rest2;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;

public class EspMeshNetUtil2
{
    private static final Logger log = Logger.getLogger(EspMeshNetUtil2.class);
    
    private static class MeshDevice
    {
        private static final int FAIL_TIME_TORELANCE = 1;
        
        // mesh device's IOTAddress
        private final IOTAddress mIOTAddress;
        
        // how many children belong to the mesh device(excluding itself)
        private final int mChildrenCount;
        
        // whether the mesh device is processing
        private volatile boolean mIsProcessing = false;
        
        // whether the mesh device is processed
        private volatile boolean mIsProcessed = false;
        
        // whether the mesh device is processed suc
        private volatile boolean mIsSuc = false;

        private volatile int mFailTime = 0;
        
        private final List<MeshDevice> mChildrenList;
        
        private MeshDevice(IOTAddress iotAddress, int childrenCount)
        {
            this.mIOTAddress = iotAddress;
            this.mChildrenCount = childrenCount;
            this.mChildrenList = new ArrayList<EspMeshNetUtil2.MeshDevice>();
        }
        
        List<MeshDevice> getChildrenList()
        {
            return this.mChildrenList;
        }
        
        boolean addChild(MeshDevice child)
        {
            if (this.mChildrenList.contains(child))
            {
                log.warn("MeshDevice bssid: " + mIOTAddress.getBSSID() + " has gotten the child bssid: "
                    + child.mIOTAddress.getBSSID() + " already");
                return false;
            }
            else
            {
                return this.mChildrenList.add(child);
            }
        }
        
        IOTAddress getIOTAddress()
        {
            return mIOTAddress;
        }
        
        int getChildrenCount()
        {
            return mChildrenCount;
        }
        
        void setIsProcessing(boolean isProcessing)
        {
            mIsProcessing = isProcessing;
        }
        
        boolean isProcessing()
        {
            return mIsProcessing;
        }
        
        void setIsProcessed(boolean isSuc)
        {
            if (isSuc)
            {
                mIsProcessed = true;
                mIsSuc = true;
                if (mFailTime > 0)
                {
                    log.info("MeshDevice " + mIOTAddress.getBSSID() + " retry " + mFailTime + " time suc");
                }
            }
            else
            {
                ++mFailTime;
                if (mFailTime < FAIL_TIME_TORELANCE)
                {
                    mIsProcessing = false;
                    mIsProcessed = false;
                    log.debug("MeshDevice " + mIOTAddress.getBSSID() + " retry " + mFailTime + " time...");
                }
                else
                {
                    mIsProcessed = true;
                    mIsSuc = false;
                    log.warn("MeshDevice " + mIOTAddress.getBSSID() + " retry " + mFailTime + " time fail");
                }
            }
        }
        
        boolean isSuc()
        {
            return mIsSuc;
        }
        
        boolean isProcessed()
        {
            return mIsProcessed;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (!(o instanceof MeshDevice))
                return false;
            
            final MeshDevice other = (MeshDevice)o;
            // IOTAddress determine the equality of MeshDevice
            if (this.mIOTAddress.equals(other.mIOTAddress))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("[MeshDevice bssid: " + mIOTAddress.getBSSID() + ", children bssids: ");
            for (MeshDevice child : mChildrenList)
            {
                sb.append(child.mIOTAddress.getBSSID() + ", ");
            }
            sb.append("]");
            return sb.toString();
        }
        
        static class CREATOR
        {
            static MeshDevice createInstance(String bssid, InetAddress rootInetAddress, String parentBssid,
                EspDeviceType deviceTypeEnum, int childrenCount, String romVersion, int rssi, String info)
            {
                IOTAddress iotAddress = new IOTAddress(bssid, rootInetAddress, true);
                iotAddress.setParentBssid(parentBssid);
                iotAddress.setEspDeviceTypeEnum(deviceTypeEnum);
                iotAddress.setRomVersion(romVersion);
                iotAddress.setRssi(rssi);
                iotAddress.setInfo(info);
                MeshDevice meshDevice = new MeshDevice(iotAddress, childrenCount);
                return meshDevice;
            }
        }
    }
    
    
    /*
     * {
     *    "parent": {
     *        "mac": "1a:fe:34:a1:06:8f",
     *        "ver": "v1.1.4t45772(o)"
     *     },
     *     "type": "Light",
     *     "num": 24,
     *     "children": [
     *     {
     *       "type": "Light",
     *       "mac": "18:fe:34:a2:c7:62",
     *       "ver": "v1.1.4t45772(o)",
     *       "num": 13
     *     },
     *     {
     *       "type": "Light",
     *       "mac": "18:fe:34:a1:06:d7",
     *       "ver": "v1.1.4t45772(o)",
     *       "num": 8
     *     }
     *     ],
     *     "mdev_mac": "18FE34A1090C",
     *     "ver": "v1.1.4t45772(o)"
     *  }
     */
    
    private static MeshDevice queryMeshDevice(InetAddress inetAddr, String deviceBssid)
    {
        // build request
        log.debug("queryMeshDevice() inetAddr: " + inetAddr + ", deviceBssid: " + deviceBssid);
        String uriStr = "http://" + inetAddr.getHostAddress() + "/config?command=mesh_info";
        // send request and receive response
        JSONObject jsonResult = EspBaseApiUtil.GetForJson(uriStr, deviceBssid);
        log.debug("queryMeshDevice() jsonResult: " + jsonResult);
        // check whether response is null
        if (jsonResult == null)
        {
            log.warn("queryMeshDevice() jsonResult is null, return null");
            return null;
        }
        // parse response
        MeshDevice currentDevice = null;
        try
        {
            // parse current device
            String currentParentBssid = jsonResult.getJSONObject("parent").getString("mac");
            String deviceTypeStr = jsonResult.getString("type");
            String currentBssid = BSSIDUtil.restoreBSSID(jsonResult.getString("mdev_mac"));
            if (!currentBssid.equals(deviceBssid))
            {
                log.warn("queryMeshDevice() currentBssid: " + currentBssid + ", deviceBssid: " + deviceBssid
                    + " aren't equal, return null");
                return null;
            }
            EspDeviceType currentDeviceType = EspDeviceType.getEspTypeEnumByString(deviceTypeStr);
            // json response "num" including device itself, thus -1
            int currentCount = jsonResult.getInt("num") - 1;
            // parse rom version
            String romVersion = jsonResult.isNull("ver") ? null : jsonResult.getString("ver");
            int rssi = jsonResult.optInt("rssi", IEspDevice.RSSI_NULL);
            String info = jsonResult.optString("info", null);
            // build current device
            currentDevice =
                MeshDevice.CREATOR.createInstance(deviceBssid,
                    inetAddr,
                    currentParentBssid,
                    currentDeviceType,
                    currentCount,
                    romVersion,
                    rssi,
                    info);
            // parse children device
            JSONArray jsonArrayChildren = jsonResult.getJSONArray("children");
            for (int i = 0; i < jsonArrayChildren.length(); ++i)
            {
                JSONObject jsonChild = jsonArrayChildren.getJSONObject(i);
                String childDeviceTypeStr = jsonChild.getString("type");
                EspDeviceType childDeviceType = EspDeviceType.getEspTypeEnumByString(childDeviceTypeStr);
                if (childDeviceType == null)
                {
                    // no more devices, so break
                    break;
                }
                String childBssid = jsonChild.getString("mac");
                // json response "num" excluding device itself
                int childCount = jsonChild.getInt("num");
                // parse rom version
                String childRomVersion = jsonChild.isNull("ver") ? null : jsonChild.getString("ver");
                int childRssi = jsonChild.optInt("rssi", IEspDevice.RSSI_NULL);
                String childInfo = jsonChild.optString("info", null);
                // build child device
                MeshDevice childDevice =
                    MeshDevice.CREATOR.createInstance(childBssid,
                        inetAddr,
                        currentBssid,
                        childDeviceType,
                        childCount,
                        childRomVersion,
                        childRssi,
                        childInfo);
                // add child device into current device's child list
                currentDevice.addChild(childDevice);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        log.debug("queryMeshDevice() currentDevice: " + currentDevice);
        return currentDevice;
    }
    
    private static void updateTempStaDeviceList4(String rootBssid, List<MeshDevice> meshDeviceList)
    {
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        IEspUser user = BEspUser.getBuilder().getInstance();
        for (MeshDevice meshDevice : meshDeviceList)
        {
            IOTAddress iotAddress = meshDevice.getIOTAddress();
            iotAddress.setRootBssid(rootBssid);
            iotAddressList.add(iotAddress);
        }
        user.__addTempStaDeviceList(iotAddressList);
    }
    
    private static synchronized boolean isMoreDevices(MeshDevice rootDevice, List<MeshDevice> deviceList)
    {
        int totalNum = rootDevice.getChildrenCount() + 1;
        int totalProcessing = 0;
        int totalSuc = 0;
        int totalFail = 0;
        for (MeshDevice device : deviceList)
        {
            if (device.isProcessed())
            {
                if (device.isSuc())
                {
                    ++totalSuc;
                }
                else
                {
                    
                    totalFail += (device.getChildrenCount() + 1);
                }
            }
            else if (device.isProcessing())
            {
                ++totalProcessing;
            }
        }
//        System.out.println("bh getTopoIOTAddressList5() totalNum: " + totalNum
//            +", totalProcessing: " + totalProcessing + ", totalSuc: " + totalSuc
//            +", totalFail:" + totalFail);
        return totalNum > totalProcessing + totalSuc + totalFail;
    }
    
    private static void addNewDevices(MeshDevice newDevice, String rootBssid, List<MeshDevice> deviceList)
    {
        List<MeshDevice> newDeviceList = new ArrayList<MeshDevice>();
        if (!deviceList.contains(newDevice))
        {
            newDeviceList.add(newDevice);
        }
        newDeviceList.addAll(newDevice.getChildrenList());
        
        for(MeshDevice newDeviceInList : newDeviceList)
        {
            if (deviceList.contains(newDeviceInList))
            {
                log.warn("addNewDevices() newDeviceInList: " + newDeviceInList + " is in deviceList already");
            }
            else
            {
                deviceList.add(newDeviceInList);
            }
        }
        
        updateTempStaDeviceList4(rootBssid, newDeviceList);
        
    }
    
    private static synchronized MeshDevice getFreshDevice2(MeshDevice rootDevice, List<MeshDevice> deviceList)
    {
        for (MeshDevice device : deviceList)
        {
            if (!device.isProcessed() && !device.isProcessing())
            {
                device.setIsProcessing(true);
                return device;
            }
        }
        return null;
    }
    
    private static synchronized int getExecutedTaskCount(List<MeshDevice> deviceList)
    {
        int count = 0;
        for (MeshDevice device : deviceList)
        {
            if (device.isProcessing())
            {
                ++count;
            }
        }
        return count;
    }
    
    private static void buildResultList(List<IOTAddress> iotAddressList, List<MeshDevice> meshDeviceList)
    {
        for (MeshDevice meshDevice : meshDeviceList)
        {
            iotAddressList.add(meshDevice.getIOTAddress());
        }
    }
    
    private static class GetTopoTask implements Runnable
    {
        private final String mRootBssid;
        
        private final MeshDevice mFreshDevice;
        
        private final List<MeshDevice> mMeshDeviceList;
        
        GetTopoTask(InetAddress rootInetAddr, String rootBssid, MeshDevice rootDevice, MeshDevice freshDevice,
            List<MeshDevice> meshDeviceList)
        {
            this.mRootBssid = rootBssid;
            this.mFreshDevice = freshDevice;
            this.mMeshDeviceList = meshDeviceList;
        }
        
        @Override
        public void run()
        {
            // query mesh device
            InetAddress inetAddr = mFreshDevice.getIOTAddress().getInetAddress();
            String deviceBssid = mFreshDevice.getIOTAddress().getBSSID();
            MeshDevice queryDevice = queryMeshDevice(inetAddr, deviceBssid);
            // process query device
            if (queryDevice == null)
            {
                mFreshDevice.setIsProcessed(false);
            }
            else
            {
                // add devices
                addNewDevices(queryDevice, mRootBssid, mMeshDeviceList);
                
                mFreshDevice.setIsProcessed(true);
                
                for (MeshDevice childMeshDevice : queryDevice.getChildrenList())
                {
                    if (childMeshDevice.getChildrenCount() == 0)
                    {
                        childMeshDevice.setIsProcessed(true);
                    }
                }
            }
            
            mFreshDevice.setIsProcessing(false);
        }
    }
    
    private static List<IOTAddress> getTopoIOTAddressList5(InetAddress rootInetAddr, String rootBssid)
    {
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        // query root mesh device
        MeshDevice rootDevice = queryMeshDevice(rootInetAddr, rootBssid);
        
        rootDevice.getIOTAddress().setParentBssid(null);
        
        List<MeshDevice> meshDeviceList = new Vector<MeshDevice>();
        
        final int MAX_PROCESSING_TASK = 20;

        if (rootDevice != null)
        {
            // root device clear processing
            rootDevice.setIsProcessing(false);
            // root device set processed
            rootDevice.setIsProcessed(true);
            // add devices
            addNewDevices(rootDevice, rootBssid, meshDeviceList);
            
            boolean isTaskListEmptyLast = false;
            boolean isBreakTaskListEmpty = false;
            boolean isSleepy = false;
            // check whether there're more devices
            boolean isMoreDevices = isMoreDevices(rootDevice, meshDeviceList);
            while (isMoreDevices)
            {
                int executedTaskCount = getExecutedTaskCount(meshDeviceList);
                
                // check whether the main task is sleepy
                isSleepy =  executedTaskCount >= MAX_PROCESSING_TASK;
                
                // distribute tasks
                for (int taskIndex = 0; taskIndex < MAX_PROCESSING_TASK - executedTaskCount; ++taskIndex)
                {
                    // get fresh device
                    MeshDevice freshDevice = getFreshDevice2(rootDevice, meshDeviceList);
                    if (freshDevice == null)
                    {
                        log.debug("getTopoIOTAddressList5() no fresh devices, so sleepy and break");
                        isSleepy = true;
                        if (executedTaskCount == 0)
                        {
                            if (isTaskListEmptyLast)
                            {
                                log.info("getTopoIOTAddressList5() isBreakTaskListEmpty = true");
                                isBreakTaskListEmpty = true;
                            }
                            else
                            {
                                log.info("getTopoIOTAddressList5() isTaskListEmptyLast = true");
                                isTaskListEmptyLast = true;
                            }
                        }
                        else
                        {
                            isTaskListEmptyLast = false;
                        }
                        break;
                    }
                    else
                    {
                        isTaskListEmptyLast = false;
                        GetTopoTask task =
                            new GetTopoTask(rootInetAddr, rootBssid, rootDevice, freshDevice, meshDeviceList);
                        EspBaseApiUtil.submit(task);
                    }
                }
                
                if (isBreakTaskListEmpty)
                {
                    log.warn("getTopoIOTAddressList5() device number exist err, no more devices exist, so break"
                        + ", meshDeviceList.size() is " + meshDeviceList.size());
                    break;
                }
                
                // check whether there're more devices
                isMoreDevices = isMoreDevices(rootDevice, meshDeviceList);
                
                // sleep some time if necessary
                if (isMoreDevices && isSleepy)
                {
                    log.debug("getTopoIOTAddressList5() sleep some time");
                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                        log.warn("getTopoIOTAddressList5() InterrucptedException occur");
                        buildResultList(iotAddressList, meshDeviceList);
                        return iotAddressList;
                    }
                }
                
            }
        }
        
        int checkTime = 0;
        while (hasDeviceProcessing(meshDeviceList))
        {
            checkTime++;
            if (checkTime % 10 == 0) {
                log.warn("Check hasDeviceProcessing " + checkTime + " times");
            }
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        log.debug("Check hasDeviceProcessing complete");
        
        buildResultList(iotAddressList, meshDeviceList);
        return iotAddressList;
    }
    
    private static boolean hasDeviceProcessing(List<MeshDevice> meshDeviceList)
    {
        for (MeshDevice device : meshDeviceList)
        {
            if (device.isProcessing())
            {
                return true;
            }
        }
        
        return false;
    }
    
    private static IOTAddress getTopoIOTAddress5(InetAddress rootInetAddress, String deviceBssid)
    {
        MeshDevice meshDevice = queryMeshDevice(rootInetAddress, deviceBssid);
        return meshDevice != null ? meshDevice.getIOTAddress() : null;
    }
    
    static IOTAddress GetTopoIOTAddress5(InetAddress rootInetAddress, String deviceBssid)
    {
        IOTAddress iotAddress = getTopoIOTAddress5(rootInetAddress, deviceBssid);
        log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddressList5(rootInetAddress=[" + rootInetAddress
            + "],deviceBssid=[" + deviceBssid + "]): " + iotAddress);
        return iotAddress;
    }
    
    static List<IOTAddress> GetTopoIOTAddressList5(InetAddress rootInetAddress, String rootBssid)
    {
        List<IOTAddress> iotAddressList = getTopoIOTAddressList5(rootInetAddress, rootBssid);
        log.debug(Thread.currentThread().toString() + "##GetTopoIOTAddressList5(rootInetAddress=[" + rootInetAddress
            + "],rootBssid=[" + rootBssid + "]): " + iotAddressList);
        return iotAddressList;
    }
}