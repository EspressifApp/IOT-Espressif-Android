package com.espressif.iot.action.device.esptouch;

import java.util.List;

import com.espressif.iot.action.device.IEspActionUnactivated;
import com.espressif.iot.type.device.esptouch.IEsptouchResult;

public interface IEspActionDeviceEsptouch extends IEspActionUnactivated
{
    /**
     * Note: !!!Don't call the task at UI Main Thread or RuntimeException will be thrown Execute the Esptouch Task and
     * return the result
     * 
     * Smart Config v2.2 support the API
     * 
     * It will be blocked until the client receive result count >= expectTaskResultCount. If it fail, it will return one
     * fail result will be returned in the list. If it is cancelled while executing, if it has received some results,
     * all of them will be returned in the list. if it hasn't received any results, one cancel result will be returned
     * in the list.
     * 
     * @param expectTaskResultCount the expect result count(if expectTaskResultCount <= 0, expectTaskResultCount =
     *            Integer.MAX_VALUE)
     * @param apSsid the Ap's ssid
     * @param apBssid the Ap's bssid
     * @param apPassword the Ap's password
     * @param isSsidHidden whether the Ap's ssid is hidden
     * @param timeoutMillisecond(it should be >= 10000+8000) millisecond of total timeout
     * 
     * @return the List of IEsptouchResult
     */
    List<IEsptouchResult> doActionDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden, int timeoutMillisecond);
    
    /**
     * the same as this{@link #doActionDeviceEsptouch(int, String, String, String, boolean, int)},
     * except timeoutMillisecond = 58000
     * 
     * @param expectTaskResultCount the expect result count(if expectTaskResultCount <= 0, expectTaskResultCount =
     *            Integer.MAX_VALUE)
     * @param apSsid the Ap's ssid
     * @param apBssid the Ap's bssid
     * @param apPassword the Ap's password
     * @param isSsidHidden whether the Ap's ssid is hidden
     * 
     * @return the List of IEsptouchResult
     */
    List<IEsptouchResult> doActionDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden);
    
    /**
     * check whether the Action Device Esptouch is cancelled
     * 
     * @return whether the Action Device Esptouch is cancelled
     */
    boolean isCancelled();
    
    /**
     * cancel the action
     */
    void cancel();
    
    /**
     * check whether exist Action Device Esptouch running
     * @return whether exist Action Device Esptouch running
     */
    boolean isExecuted();
}
