package com.espressif.iot.user.builder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.BSSIDUtil;

public class EspSSSUser
{
    private static EspSSSUser instance;
    
    public static EspSSSUser getInstance() {
        if (instance == null) {
            instance = new EspSSSUser();
        }
        
        return instance;
    }
    
    private List<IEspDeviceSSS> mDeviceList;
    
    private EspSSSUser(){
        mDeviceList = new Vector<IEspDeviceSSS>();
    }
    
    public List<IEspDeviceSSS> getDeviceList()
    {
        return mDeviceList;
    }
    
    public IEspDeviceSSS getDeviceByBssid(String bssid)
    {
        for (IEspDeviceSSS device : mDeviceList)
        {
            if (device.getBssid().equals(bssid))
            {
                return device;
            }
        }
        
        return null;
    }
    
    public void scanDevices()
    {
        List<IEspDeviceSSS> result = new ArrayList<IEspDeviceSSS>();
        List<IOTAddress> iotList = EspBaseApiUtil.discoverDevices();
        boolean hasMeshDevice = false;
        for (IOTAddress iot : iotList)
        {
            if (iot.isMeshDevice())
            {
                hasMeshDevice = true;
            }
            iot.setSSID(BSSIDUtil.genDeviceNameByBSSID(iot.getBSSID()));
            result.add(BEspDevice.createSSSDevice(iot));
        }
        
        if (hasMeshDevice)
        {
            String connectedSsid = EspBaseApiUtil.getWifiConnectedSsid();
            if (!TextUtils.isEmpty(connectedSsid))
            {
                String gateway = EspApplication.sharedInstance().getGateway();
                try
                {
                    WifiManager wifiManager =
                        (WifiManager)EspApplication.sharedInstance().getSystemService(Context.WIFI_SERVICE);
                    InetAddress inetAddress = InetAddress.getByName(gateway);
                    IOTAddress iotAddress =
                        new IOTAddress(wifiManager.getConnectionInfo().getBSSID(), inetAddress, true);
                    iotAddress.setEspDeviceTypeEnum(EspDeviceType.ROOT);
                    iotAddress.setSSID(connectedSsid);
                    
                    result.add(0, BEspDevice.createSSSDevice(iotAddress));
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        mDeviceList.clear();
        mDeviceList.addAll(result);
    }
}
