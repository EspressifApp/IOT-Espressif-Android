package com.espressif.iot.base.net.rest2;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

class MeshTypeUtil
{
    private static final Logger log = Logger.getLogger(MeshTypeUtil.class);
    
    private static final String COMMAND = "command";
    
    private static final String ESCAPE = "\r\n";
    
    private static final int TYPE_LEN = 2;
    
    // private static final int CMD_LEN = 2;
    private static final int CMD_LEN = 4;
    
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
            command = (char)((cmd.charAt(0)) | cmd.charAt(1) << 8);
            union = (char)((cmd.charAt(2)) | cmd.charAt(3) << 8);
        }
        
        boolean isRequest()
        {
            return (command & F_REQ) != 0;
        }
        
        boolean isResponse()
        {
            return (command & F_RESP) != 0;
        }
        
        boolean isGetTopology()
        {
            return (command & M_TOPO) != 0;
        }
        
        // the valid response which apk received should be both isRequest() and isResponse()
        boolean isValid()
        {
            return isRequest() && isResponse();
        }
        
        boolean isFree()
        {
            return getCapability() > 0;
        }
        
        int getCapability()
        {
            return union >> 4;
        }
        
        int getLength()
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
    
    // struct command_value {
    // struct {
    // uint16_t f_req:1; // flow request
    // uint16_t f_resp:1; // flow response
    // uint16_t s_router:1; // spread router information
    // uint16_t m_add:1; // MAC route table add
    // uint16_t m_del:1; // MAC route table delete
    // uint16_t m_topo:1; // used to get topology
    // uint16_t resv:10; // reserve for fulture
    // } comm;
    // union {
    // struct {
    // uint16_t resv:12;
    // uint16_t f_cap:4; // current flow capacity
    // } flow;
    // struct {
    // uint16_t len;
    // } router_info;
    // struct {
    // uint16_t len;
    // } dev_mac_info;
    // struct {
    // uint16_t len;
    // } topology_info;
    // } val;
    // };
    
    // create mesh command by bytes
    private static String createRequestContent(byte[] bytes)
    {
        JSONObject json = new JSONObject();
        String _content = null;
        try
        {
            String commandValueStr = null;
            try
            {
                commandValueStr = new String(bytes, "ISO-8859-1");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
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
        // command.f_req = 0x01 0x00, command.union = 0x00 0x00
        return createRequestContent(new byte[] {F_REQ, 0, 0, 0});
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
    
}
