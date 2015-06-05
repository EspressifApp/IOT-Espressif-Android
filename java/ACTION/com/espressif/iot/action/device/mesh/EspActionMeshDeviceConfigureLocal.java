package com.espressif.iot.action.device.mesh;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

import org.apache.log4j.Logger;

import android.text.TextUtils;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.command.device.mesh.EspCommandMeshConfigureLocal;
import com.espressif.iot.command.device.mesh.IEspCommandMeshConfigureLocal;
import com.espressif.iot.command.device.mesh.IEspCommandMeshConfigureLocal.MeshMode;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.type.net.WifiCipherType;

public class EspActionMeshDeviceConfigureLocal implements IEspActionMeshDeviceConfigureLocal
{

    private final static Logger log = Logger.getLogger(EspActionMeshDeviceConfigureLocal.class);
    
    @Override
    public MeshDeviceConfigureLocalResult doActionMeshDeviceConfigureLocal(IEspDeviceNew deviceNew, MeshMode meshMode,
        String apSsid, String apPassword, String randomToken)
    {
        boolean isSuc = false;
        IOTAddress iotAddress = EspBaseApiUtil.discoverDevice(deviceNew.getBssid());
        String bssid = deviceNew.getBssid();
        InetAddress inetAddress = iotAddress == null ? null : iotAddress.getInetAddress();
        String router = inetAddress == null ? null : iotAddress.getRouter();
        MeshDeviceConfigureLocalResult result = null;
        IEspCommandMeshConfigureLocal command = new EspCommandMeshConfigureLocal();
        // if local, send command directly
        if (inetAddress != null)
        {
            log.debug("mesh device is local, send command directly");
            isSuc =
                command.doCommandMeshConfigureLocal(router,
                    bssid,
                    meshMode,
                    inetAddress,
                    apSsid,
                    apPassword,
                    randomToken);
            if (isSuc)
            {
                result = MeshDeviceConfigureLocalResult.SUC;
            }
            else
            {
                result = MeshDeviceConfigureLocalResult.FAIL;
            }
        }
        // if not local, connect to device, send command, try its best to connect to previous Ap
        else
        {
            log.debug("mesh device is new, connect to its softap directly");
            String currentApSsid = EspBaseApiUtil.getWifiConnectedSsid();
            String deviceSoftapSsid = deviceNew.getSsid();
            log.debug("deviceSoftapSsid:" + deviceSoftapSsid);
            boolean isConnectSuc = false;
            try
            {
                isConnectSuc = EspBaseApiUtil.connect(deviceSoftapSsid, WifiCipherType.WIFICIPHER_OPEN);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            if (isConnectSuc)
            {
                log.debug("mesh device is new, connect to its softap suc");
                // get gateway
                String gateway = EspApplication.sharedInstance().getGateway();
                // hard coding to compose router by gateway
                String[] gateways = gateway.split("\\.");
                int routerHeader = Integer.parseInt(gateways[3]);
                router = routerHeader <= 0x0f ? "0" + Integer.toHexString(routerHeader).toUpperCase(Locale.US) + "FFFFFF"
                    : Integer.toHexString(routerHeader).toUpperCase(Locale.US) + "FFFFFF";
                log.debug("router = " + router);
                inetAddress = null;
                try
                {
                    inetAddress = InetAddress.getByName(gateway);
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                if(inetAddress!=null) {
                    isSuc =
                        command.doCommandMeshConfigureLocal(router,
                            bssid,
                            meshMode,
                            inetAddress,
                            apSsid,
                            apPassword,
                            randomToken);
                }
                if (isSuc)
                {
                    result = MeshDeviceConfigureLocalResult.SUC;
                }
                else
                {
                    result = MeshDeviceConfigureLocalResult.FAIL;
                }
            }
            else
            {
                log.debug("mesh device is new, connect to its softap fail");
                result = MeshDeviceConfigureLocalResult.FAIL;
            }
            if (!TextUtils.isEmpty(currentApSsid))
            {
                log.debug("mesh device is new, connect to previous ap: " + currentApSsid);
                EspBaseApiUtil.enableConnected(currentApSsid);
            }
        }
        log.debug(Thread.currentThread().toString() + "##doActionMeshDeviceConfigureLocal(deviceNew=[" + deviceNew
            + "],meshMode=[" + meshMode + "],apSsid=[" + apSsid + "],apPassword=[" + apPassword + "],randomToken=["
            + randomToken + "]): " + result);
        return result;
    }
    
}
