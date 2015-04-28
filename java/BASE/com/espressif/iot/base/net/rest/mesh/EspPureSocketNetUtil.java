package com.espressif.iot.base.net.rest.mesh;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.util.MeshUtil;

/**
 * for it is used by mesh device upgrade local only, so we don't make it abstract.
 * if it is used in more situation, it should be more abstract.
 * @author afunx
 *
 */
public class EspPureSocketNetUtil
{
    private final static String ROUTER = "router";
    private final static String SIP = "sip";
    private final static String SPORT = "sport";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int SO_TIMEOUT = 10000;
    private static final int MESH_PORT = 8000;
    
    private static String __getLoalUpgradeUri(InetAddress targetAddress)
    {
        return "http://" + targetAddress.getHostAddress() + "/v1/device/rpc/";
    }
    
    /**
     * build the connect to the uri
     * @param targetInetAddress the mesh device's InetAddress
     * @return the EspSocketClient(if connect suc) or null(if connect fail)
     */
    public static EspSocketClient connect(InetAddress targetAddress)
    {
        EspSocketClient client = new EspSocketClient();
        String host = targetAddress.getHostAddress();
        client.setSoTimeout(SO_TIMEOUT);
        if(client.connect(host, MESH_PORT,CONNECTION_TIMEOUT))
        {
            return client;
        }
        else
        {
            try
            {
                client.close();
            }
            catch (IOException ignore)
            {
            }
            return null;
        }
    }
    
    /**
     * send mesh upgrade local request to mesh device
     * @param client the EspSocketClient
     * @param router the mesh device's router
     * @param targetInetAddress the mesh device's InetAddress
     * @param version the version to be upgraded
     * @return whether the mesh device is ready to upgrade local
     */
    public static boolean executeMeshUpgradeLocalRequest(EspSocketClient client,String router,InetAddress targetInetAddress, String version)
    {
        String method = "GET";
        String uriStr = __getLoalUpgradeUri(targetInetAddress);
        JSONObject jsonPost = new JSONObject();
        String localInetAddress = client.getLocalAddressStr();
//        int localPort = client.getLocalPort();
        try
        {
            jsonPost.put(SIP, MeshUtil.getIpAddressForMesh(localInetAddress));
//            jsonPost.put(SPORT, MeshUtil.getPortForMesh(localPort));
            jsonPost.put(SPORT, "8000");
            jsonPost.put(ROUTER, router);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
        EspPureSocketRequestBaseEntity request = new EspPureSocketRequestBaseEntity(method, uriStr, jsonPost.toString());
        request.putQueryParams("action", "sys_upgrade");
        request.putQueryParams("version", version);
        request.putQueryParams("deliver_to_deivce", "true");
        try
        {
            client.writeRequest(request.toString());
            String responseStr = client.readLine();
            EspPureSocketResponseBaseEntity response = new EspPureSocketResponseBaseEntity(responseStr);
            if (response.isValid()&&response.getStatus()==HttpStatus.SC_OK)
            {
                return true;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * close the EspSocketClient
     * @param client the EspSocketClient to be closed
     */
    public void close(EspSocketClient client)
    {
        try
        {
            client.close();
        }
        catch (IOException ignore)
        {
        }
    }
    
    /**
     * it is used by mesh device upgrade local now,
     * if it is more useful, we should make it abstract
     * 
     * @param client the EspSocketClint
     * @param user1 the user1.bin
     * @param user2 the user2.bin
     * @param targetInetAddress the mesh device's InetAddress
     * @param router the mesh device's router
     * @param deviceBssid the mesh device's bssid
     * @param timeout the millisecond timeout
     * @return whether the device is upgrade suc
     */
    public static boolean listen(EspSocketClient client, byte[] user1, byte[] user2, InetAddress targetAddress,
        String router, String deviceBssid, long timeout)
    {
        long start = System.currentTimeMillis();
        EspPureSocketServer server =
            new EspPureSocketServer(client, user1, user2, targetAddress.getHostAddress(), router, deviceBssid);
        while (!server.isClosed() && System.currentTimeMillis() - start < timeout)
        {
            if (!server.handle())
            {
                // when the request is invalid, sleep 100ms to let other threads run
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        server.close();
        return server.isFinished();
    }
}
