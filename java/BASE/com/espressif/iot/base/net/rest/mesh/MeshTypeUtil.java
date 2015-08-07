package com.espressif.iot.base.net.rest.mesh;

import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.InputStreamUtils;
import com.espressif.iot.util.MeshUtil;

public class MeshTypeUtil
{
    private static final Logger log = Logger.getLogger(MeshTypeUtil.class);
    
    private static final String COMMAND = "command";
    
    private static final String ESCAPE = "\r\n";
    
    private static final int TYPE_LEN = 2;
    
    private static final int CMD_LEN = 2;
    
    private static final int MAC_LEN = 6;
    
    private static final int F_REQ = 0x01;
    
    private static final int F_RESP = 0x02;
    
    private static final int S_ROUTER = 0x04;
    
    private static final int M_ADD = 0x08;
    
    private static final int M_DEL = 0x10;
    
    private static final int M_TOPO = 0x20;
    
    private static class MeshCommand
    {
        private char command;
        
        private char union;
        
        MeshCommand(String cmd)
        {
            command = cmd.charAt(0);
            union = cmd.charAt(1);
        }
        
        public boolean isRequest()
        {
            return (command & F_REQ) != 0;
        }
        
        public boolean isResponse()
        {
            return (command & F_RESP) != 0;
        }
        
        public boolean isGetTopology()
        {
            return (command & M_TOPO) != 0;
        }
        
        // the valid response which apk received should be both isRequest() and isResponse()
        public boolean isValid()
        {
            return isRequest() && isResponse();
        }
        
        public boolean isFree()
        {
            return getCapability() > 0;
        }
        
        public int getCapability()
        {
            return union >> 4;
        }
        
        public int getLength()
        {
            return union & 0xff;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("f_req:" + isRequest() + "\n");
            sb.append("f_resp:" + isResponse() + "\n");
            sb.append("m_topo:" + isGetTopology() + "\n");
            sb.append("f_cap:" + getCapability() + "\n");
            sb.append("f_len:" + getLength() + "\n");
            return sb.toString();
        }
    }
    
    //  struct command_value {
    //      struct {
    //          uint8_t f_req:1; // flow request
    //          uint8_t f_resp:1; // flow response
    //          uint8_t s_router:1; // spread router information
    //          uint8_t m_add:1; // MAC route table add
    //          uint8_t m_del:1; // MAC route table delete
    //          uint8_t m_topo:1; // used to get topology
    //          uint8_t resv:2; // reserve for fulture
    //      } comm;
    //      union {
    //          struct {
    //              uint8_t resv:4;
    //              uint8_t f_cap:4; // current flow capacity
    //          } flow;
    //          struct {
    //              uint8_t len;
    //          } router_info;
    //          struct {
    //              uint8_t len;
    //          } dev_mac_info;
    //          struct {
    //              uint8_t len;
    //          } topology_info;
    //      } val;
    //  }
    
    // create mesh command by bytes
    private static String createRequestContent(byte[] bytes)
    {
        JSONObject json = new JSONObject();
        String _content = null;
        try
        {
            String commandValueStr = InputStreamUtils.byte2String(bytes);
            _content = commandValueStr;
            json.put(COMMAND, "XXX");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        String content = json.toString() + ESCAPE;
        content = content.replace("XXX", _content);
        return content;
    }
    
    private static String getCmdJsonStr(JSONObject respJson)
    {
        try
        {
            String respStr = respJson.getString(COMMAND);
            return respStr;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * create "is device available" request content
     * 
     * @return "is device available" request content
     */
    static String createIsDeviceAvailableRequestContent()
    {
        // command.f_req = 0x01, command.union = 0
        return createRequestContent(new byte[] {F_REQ, 0});
    }
    
    /**
     * create "get topology" request content
     * 
     * @return "get topology" request content
     */
    static String createGetTopologyRequestContent()
    {
        // command.f_req = 0x20, command.union = 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00(the last six 0x00 means mac,
        // 0x00,0x00,0x00,0x00,0x00,0x00 means broadcast mac)
        return createRequestContent(new byte[] {M_TOPO, MAC_LEN, 0, 0, 0, 0, 0, 0});
    }
    
    /**
     * create "is device local" request content
     * 
     * @param deviceBssid the device's bssid
     * @return "is device local" request content
     */
    static String createIsDeviceLocalRequestContent(String deviceBssid)
    {
        // refer to createGetTopologyRequestContent() method
        byte[] bssidBytes = MeshUtil.getMacAddressBytes(deviceBssid);
        byte[] reqBytes = new byte[CMD_LEN + bssidBytes.length];
        reqBytes[0] = M_TOPO;
        reqBytes[1] = MAC_LEN;
        for (int i = 0; i < bssidBytes.length; ++i)
        {
            reqBytes[i + CMD_LEN] = bssidBytes[i];
        }
        return createRequestContent(reqBytes);
    }
    
    /**
     * check whether the device is available by response String
     * 
     * @param respStr the response String
     * @return whether the device is available
     */
    static boolean checkIsDeviceAvailable(String respStr)
    {
        if (respStr.length() != CMD_LEN)
        {
            log.warn("checkIsDeviceAvailable(): respStr is not valid:" + respStr + ",return false");
            return false;
        }
        MeshCommand cmd = new MeshCommand(respStr);
        if (!cmd.isValid())
        {
            log.warn("checkIsDeviceAvailable(): cmd is not valid, cmd:" + cmd + ",return false");
            return false;
        }
        if (!cmd.isFree())
        {
            log.warn("checkIsDeviceAvailable(): cap is not enought, return false");
            return false;
        }
        return true;
    }
    
    /**
     * check whether the device is available by response JSONObject
     * 
     * @param respJson the response JSONObject
     * @return whether the device is available
     */
    static boolean checkIsDeviceAvailable(JSONObject respJson)
    {
        String respStr = getCmdJsonStr(respJson);
        if (respStr != null)
        {
            return checkIsDeviceAvailable(respStr);
        }
        else
        {
            return false;
        }
    }
    
    /**
     * extract device IOTAddresses
     * 
     * @param respJson the response JSONObject
     * @param iotAddressList the IOTAddress list where the new IOTAddress belong to device is added
     * @param rootDeviceBssid the bssid of root device
     * @return whether there's more device bssids to be received
     */
    static boolean extractDeviceIOTAddresses(JSONObject respJson,InetAddress rootInetAddress, List<IOTAddress> iotAddressList, String rootDeviceBssid)
    {
        String respStr = getCmdJsonStr(respJson);
        if (respStr != null)
        {
            return extractDeviceIOTAddresses(respStr, rootInetAddress, iotAddressList, rootDeviceBssid);
        }
        else
        {
            return false;
        }
    }
    
    /**
     * extract device IOTAddresses
     * 
     * @param respStr the response String
     * @param iotAddressList the IOTAddress list where the new IOTAddress belong to device is added
     * @param parentDeviceBssid the bssid of parent device
     * @return whether there's more device bssids to be received
     */
    static boolean extractDeviceIOTAddresses(String respStr, InetAddress rootInetAddress, List<IOTAddress> iotAddressList,
        String parentDeviceBssid)
    {
        int respLen = respStr.length();
        if (respLen < CMD_LEN + MAC_LEN)
        {
            log.warn("extractDeviceIOTAddresses(): respLen is too little:" + respLen + ",return false");
            return false;
        }
        MeshCommand respCmd = new MeshCommand(respStr.substring(0, 2));
        if (!respCmd.isGetTopology())
        {
            log.warn("extractDeviceIOTAddresses(): respCmd is not GetTopology:" + respCmd + ",return false");
            return false;
        }
        if ((respLen - CMD_LEN) % MAC_LEN != 0)
        {
            log.warn("extractDeviceIOTAddresses(): respLen is not valid:" + respLen + ",return false");
            return false;
        }
        for (int index = CMD_LEN; index < respStr.length(); index += MAC_LEN)
        {
            // extract the bssid
            byte[] bssidBytes = InputStreamUtils.String2Bytes(respStr.substring(index, index + MAC_LEN));
            String bssid = MeshUtil.getMacAddressStr(bssidBytes);
            String typeStr = bssid.substring(0, TYPE_LEN);
            String staBssid = "18" + bssid.substring(TYPE_LEN);
            IOTAddress iotAddress = new IOTAddress(staBssid, rootInetAddress, true);
            // the default device type is light
            iotAddress.setEspDeviceTypeEnum(EspDeviceType.LIGHT);
            // don't forget to set parent bssid
            iotAddress.setParentBssid(parentDeviceBssid);
            // add the bssid if the device list doesn't contain it
            if (!iotAddressList.contains(staBssid))
            {
                int typeValue = Integer.parseInt(typeStr, 16);
                log.error("extractDeviceIOTAddresses(): typeValue:" + typeValue);
                iotAddressList.add(iotAddress);
            }
            else
            {
                log.warn("extractDeviceIOTAddresses(): iotAddress:" + iotAddress + " is in the deviceBssidList already");
            }
        }
        boolean isMoreBssidExist = true;
        for (IOTAddress iotAddress : iotAddressList)
        {
            if (iotAddress.getBSSID().equals(parentDeviceBssid))
            {
                isMoreBssidExist = false;
                break;
            }
        }
        // check whether there's more bssid
        return isMoreBssidExist;
    }
    
    /**
     * extract device IOTAddress by response String
     * 
     * @param respStr the response String
     * @param iotAddressList the IOTAddress list where the new IOTAddress belong to device is added
     * @param deviceBssid the device's bssid
     * @param rootDeviceBssid the bssid of root device
     * @return the device IOTAddress or null
     */
    static IOTAddress extractDeviceIOTAddress(String respStr, InetAddress rootInetAddress, String deviceBssid,
        String rootDeviceBssid)
    {
        int respLen = respStr.length();
        if (respLen != CMD_LEN + MAC_LEN)
        {
            log.warn("extractDeviceIOTAddress(): respLen is not valid:" + respLen + ",return false");
            return null;
        }
        // extract the bssid
        byte[] bssidBytes = InputStreamUtils.String2Bytes(respStr.substring(CMD_LEN, CMD_LEN + MAC_LEN));
        String bssid = MeshUtil.getMacAddressStr(bssidBytes);
        String typeStr = bssid.substring(0, TYPE_LEN);
        String staBssid = "18" + bssid.substring(TYPE_LEN);
        // check whether the IOTAddress's staBssid is the specified one
        if (!deviceBssid.equals(staBssid))
        {
            return null;
        }
        else
        {
            int typeValue = Integer.parseInt(typeStr, 16);
            log.error("extractDeviceIOTAddress(): typeValue:" + typeValue);
            IOTAddress iotAddress = new IOTAddress(staBssid, rootInetAddress, true);
            // don't forget to set root bssid
            iotAddress.setParentBssid(rootDeviceBssid);
            // the default device type is light
            iotAddress.setEspDeviceTypeEnum(EspDeviceType.LIGHT);
            return iotAddress;
        }
    }
    
    /**
     * extract device IOTAddress by response JSONObject
     * 
     * @param respJson the response JSONObject
     * @param iotAddressList the IOTAddress list where the new IOTAddress belong to device is added
     * @param deviceBssid the device's bssid
     * @param rootDeviceBssid the bssid of root device
     * @return the device IOTAddress or null
     */
    static IOTAddress extractDeviceIOTAddress(JSONObject respJson, InetAddress rootInetAddress, String deviceBssid,
        String rootDeviceBssid)
    {
        String respStr = getCmdJsonStr(respJson);
        if (respStr != null)
        {
            return extractDeviceIOTAddress(respStr, rootInetAddress, deviceBssid, rootDeviceBssid);
        }
        else
        {
            return null;
        }
    }
}
