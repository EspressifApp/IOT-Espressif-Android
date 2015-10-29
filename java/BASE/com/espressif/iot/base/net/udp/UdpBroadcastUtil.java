package com.espressif.iot.base.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;

public class UdpBroadcastUtil
{
    
    private static final Logger log = Logger.getLogger(UdpBroadcastUtil.class);
    
    private static final String data = "Are You Espressif IOT Smart Device?";
    
    private static final int IOT_DEVICE_PORT = 1025;
    
    private static final int SO_TIMEOUT = 3000;
    
    private static final int RECEIVE_LEN = 64;
    
    /**
     * if the IOT_APP_PORT is occupied, other random port will be used
     */
    private static final int IOT_APP_PORT = 4025;
    
    private static InetAddress broadcastAddress;
    
    static
    {
        try
        {
            broadcastAddress = InetAddress.getByName("255.255.255.255");
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * alloc the port number
     * 
     * @param hostPort if -1<port<65536, it will allocate a random port, which could be used else it will allocate the
     *            specified port first, after that it will allocate a random one
     * @return DatagramSocket
     */
    private static DatagramSocket allocPort(int hostPort)
    {
        
        log.debug("allocPort() entrance");
        
        boolean success = false;
        DatagramSocket socket = null;
        
        // try to allocate the specified port
        if (-1 < hostPort && hostPort < 65536)
        {
            try
            {
                socket = new DatagramSocket(hostPort);
                success = true;
                log.debug(Thread.currentThread().toString() + "##allocPort(hostPort=[" + hostPort + "]) suc");
                return socket;
            }
            catch (SocketException e)
            {
                log.error(Thread.currentThread().toString() + "##allocPort(hostPort=[" + hostPort + "]) is used");
            }
        }
        // allocate a random port
        do
        {
            try
            {
                // [1024,65535] is the dynamic port range
                hostPort = 1024 + new Random().nextInt(65536 - 1024);
                socket = new DatagramSocket(hostPort);
                success = true;
            }
            catch (SocketException e)
            {
                e.printStackTrace();
            }
        } while (!success);
        return socket;
    }
    
    private static List<IOTAddress> __discoverDevices(String bssid)
    {
        List<IOTAddress> responseList = new ArrayList<IOTAddress>();
        DatagramSocket socket = null;
        byte buf_receive[] = new byte[RECEIVE_LEN];
        DatagramPacket pack = null;
        String receiveContent = null;
        String hostname = null;
        boolean isMesh = false;
        InetAddress responseAddr = null;
        String responseBSSID = null;
        String realData = null;
        if (bssid != null)
        {
            realData = data + " " + bssid;
        }
        else
        {
            realData = data;
        }
        try
        {
            // alloc port for the socket
            socket = allocPort(IOT_APP_PORT);
            // set receive timeout
            socket.setSoTimeout(SO_TIMEOUT);
            // broadcast content
            pack = new DatagramPacket(realData.getBytes(), realData.length(), broadcastAddress, IOT_DEVICE_PORT);
            log.debug(Thread.currentThread().toString() + "##__discoverDevices(bssid=[" + bssid + "]): socket send");
            socket.send(pack);
            pack.setData(buf_receive);
            long start = System.currentTimeMillis();
            log.debug(Thread.currentThread().toString() + "##__discoverDevices(bssid=[" + bssid
                + "]): socket receive...");
            do
            {
                socket.receive(pack);
                long consume = System.currentTimeMillis() - start;
                log.error("udp receivce cost: " + consume + " ms");
                log.debug(Thread.currentThread().toString() + "##__discoverDevices(bssid=[" + bssid
                    + "]): one socket received");
                receiveContent = new String(pack.getData(), pack.getOffset(), pack.getLength());
                
                if (UdpDataParser.isValid(receiveContent))
                {
                    String deviceTypeStr = UdpDataParser.filterType(receiveContent);
                    EspDeviceType deviceType = EspDeviceType.getEspTypeEnumByString(deviceTypeStr);
                    if (deviceType == null || !deviceType.isLocalSupport())
                    {
                        log.warn(Thread.currentThread().toString() + "##__discoverDevices(bssid=[" + bssid
                            + "]): type is null or the type of the device don't support local mode.");
                    }
                    else
                    {
                        hostname = UdpDataParser.filterIpAddress(receiveContent);
                        log.debug(Thread.currentThread().toString() + "##__discoverDevices(bssid=[" + bssid
                            + "]): hostname=" + hostname);
                        if (hostname.equals("0.0.0.0"))
                        {
                            log.warn(Thread.currentThread().toString() + "##__discoverDevices(bssid=[" + bssid
                                + "]): hostname is invalid");
                            continue;
                        }
                        responseAddr = InetAddress.getByName(hostname);
                        log.debug(receiveContent);
                        responseBSSID = UdpDataParser.filterBssid(receiveContent);
                        log.info(Thread.currentThread().toString() + "##__discoverDevices(bssid=[" + bssid
                            + "]): responseAddr = " + responseAddr + ",responseBSSID = " + responseBSSID);
                        isMesh = UdpDataParser.isMesh(receiveContent);
                        // add one response to the response list
                        IOTAddress iotAddress = new IOTAddress(responseBSSID, responseAddr, isMesh);
                        iotAddress.setEspDeviceTypeEnum(deviceType);

                        if (!responseList.contains(iotAddress))
                        {
                            responseList.add(iotAddress);
                        }
                    }
                }
            } while (bssid == null);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // ignore SocketTimeoutException
            if (e instanceof SocketTimeoutException)
            {
                
            }
            else
            {
                e.printStackTrace();
            }
        }
        finally
        {
            if (socket != null)
            {
                socket.disconnect();
                socket.close();
                log.info(Thread.currentThread().toString() + "##__discoverDevices(bssid=[" + bssid
                    + "]): socket is not null, closed in finally");
            }
            else
            {
                log.warn(Thread.currentThread().toString() + "##__discoverDevices(bssid=[" + bssid
                    + "]): sockect is null, closed in finally");
            }
        }
        return responseList;
    }
    
    /**
     * @see IOTAddress discover the specified IOT device in the same AP by UDP broadcast
     * 
     * @param bssid the IOT device's bssid
     * @return the specified device's IOTAddress (if found) or null(if not found)
     */
    public static IOTAddress discoverIOTDevice(String bssid)
    {
        List<IOTAddress> result = __discoverDevices(bssid);
        if (!result.isEmpty())
        {
            return result.get(0);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * @see IOTAddress discover IOT devices in the same AP by UDP broadcast
     * 
     * @return the List of IOTAddress
     */
    public static List<IOTAddress> discoverIOTDevices()
    {
        List<IOTAddress> result = __discoverDevices(null);
        if (result != null)
        {
            return result;
        }
        else
        {
            return Collections.emptyList();
        }
    }
    
}
