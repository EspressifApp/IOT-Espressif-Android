package com.espressif.iot.command.device.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.sensor.IEspCommandSensorGetStatusListInternet;
import com.espressif.iot.type.device.status.IEspStatusSensor;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.TimeUtil;

public abstract class EspCommandSensorGetStatusListInternet implements IEspCommandSensorGetStatusListInternet
{
    
    private final static Logger log = Logger.getLogger(EspCommandSensorGetStatusListInternet.class);
    
    private List<IEspStatusSensor> getStatusDataList(String deviceKey, int offset, int count, String startDateStr,
        String endDateStr)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header1 = new HeaderPair(headerKey, headerValue);
        HeaderPair header2 = new HeaderPair(Time_Zone, Epoch);
        String url =
            getUrl() + "?offset=" + offset + "&row_count=" + count + "&start=" + startDateStr + "&end=" + endDateStr;
        List<IEspStatusSensor> resultList = new ArrayList<IEspStatusSensor>();
        JSONObject jsonObjectResult = EspBaseApiUtil.Get(url, header1, header2);
        JSONObject jsonObject = null;
        if (jsonObjectResult != null)
        {
            try
            {
                int status = Integer.parseInt(jsonObjectResult.getString(Status));
                if (status == HttpStatus.SC_OK)
                {
                    JSONArray jsonArray = jsonObjectResult.getJSONArray(Datapoints);
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        jsonObject = jsonArray.getJSONObject(i);
                        IEspStatusSensor statusSensor = this.parseJson(jsonObject);
                        resultList.add(statusSensor);
                    }
                }
                else
                {
                    log.warn(Thread.currentThread().toString() + "##getStatusDataList(deviceKey=[" + deviceKey
                        + "],offset=[" + offset + "],count=[" + count + "],startDateStr=[" + startDateStr
                        + "],endDateStr=[" + endDateStr + "]): null");
                    return null;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else
        {
            log.warn(Thread.currentThread().toString() + "##getStatusDataList(deviceKey=[" + deviceKey + "],offset=["
                + offset + "],count=[" + count + "],startDateStr=[" + startDateStr + "],endDateStr=[" + endDateStr
                + "]): null");
            return null;
        }
        return resultList;
    }
    
    private List<IEspStatusSensor> getStatusDataListOnePage(long startTime, long endTime, String deviceKey, int offset)
    {
        String startDateStr = TimeUtil.getDateStr(startTime, TimeUtil.ISO_8601_Pattern);
        String endDateStr = TimeUtil.getDateStr(endTime, TimeUtil.ISO_8601_Pattern);
        List<IEspStatusSensor> result = getStatusDataList(deviceKey, offset, PAGE_NUMBER, startDateStr, endDateStr);
        log.debug(Thread.currentThread().toString() + "##getStatusDataListOnePage(startTime=["
            + TimeUtil.getDateStr(startTime, null) + "],endTime=[" + TimeUtil.getDateStr(endTime, null)
            + "],deviceKey=[" + deviceKey + "],offset=[" + offset + "]): received " + result.size());
        return result;
    }
    
    private void checkValid(List<IEspStatusSensor> statusHumitureList)
    {
        if (statusHumitureList == null || statusHumitureList.size() < 2)
        {
            return;
        }
        IEspStatusSensor statusPrevious = statusHumitureList.get(0);
        IEspStatusSensor statusCurrent = statusHumitureList.get(1);
        for (int i = 2; i < statusHumitureList.size() - 1; i++)
        {
            if (statusCurrent.getAt() < statusPrevious.getAt())
            {
                throw new AssertionError();
            }
            statusPrevious = statusCurrent;
            statusCurrent = statusHumitureList.get(i);
        }
    }
    
    private List<IEspStatusSensor> getStatusesDataList(String deviceKey, long startTimestamp, long endTimestamp)
    {
        List<IEspStatusSensor> statusOriginDataAbsList = new ArrayList<IEspStatusSensor>();
        List<IEspStatusSensor> statusDataAbsOnePage = null;
        
        int offset = 0;
        
        do
        {
            statusDataAbsOnePage = getStatusDataListOnePage(startTimestamp, endTimestamp, deviceKey, offset);
            if (statusDataAbsOnePage == null)
            {
                break;
            }
            if (Thread.currentThread().isInterrupted())
            {
                log.warn("Thread.currentThread().isInterrupted()");
                return Collections.emptyList();
            }
            statusOriginDataAbsList.addAll(statusDataAbsOnePage);
            offset += PAGE_NUMBER;
            // when statusTemHumOnePage.size()<PAGE_NUMBER, it means the data is
            // gotten all
        } while (statusDataAbsOnePage.size() == PAGE_NUMBER);
        // fail to get all data, we treat it as no data is arrived
        if (statusDataAbsOnePage == null)
        {
            return Collections.emptyList();
        }
        Collections.reverse(statusOriginDataAbsList);
        checkValid(statusOriginDataAbsList);
        return statusOriginDataAbsList;
    }
    
    @Override
    public List<IEspStatusSensor> doCommandSensorGetStatusListInternet(String deviceKey, long startTimestamp,
        long endTimestamp)
    {
        List<IEspStatusSensor> result = getStatusesDataList(deviceKey, startTimestamp, endTimestamp);
        log.debug(Thread.currentThread().toString() + "##doCommandSensorGetStatusListInternet(deviceKey=[" + deviceKey
            + "],startTimestamp=[" + TimeUtil.getDateStr(startTimestamp, null) + "],endTimestamp=["
            + TimeUtil.getDateStr(endTimestamp, null) + "]): receive " + result.size());
        return result;
    }
    
}
