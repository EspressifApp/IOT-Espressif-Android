package com.espressif.iot.command.user;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.user.EspRegisterResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspCommandUserRegisterInternet implements IEspCommandUserRegisterInternet
{
    private final static Logger log = Logger.getLogger(EspCommandUserRegisterInternet.class);
    
    @Override
    public EspRegisterResult doCommandUserRegisterInternet(String userName, String userEmail, String userPassword)
    {
        JSONObject jsonObject = new JSONObject();
        
        try
        {
            jsonObject.put(User_Name, userName);
            jsonObject.put(User_Email, userEmail);
            jsonObject.put(User_Password, userPassword);
            
            JSONObject jsonObjectResult = EspBaseApiUtil.Post(URL, jsonObject);
            if (jsonObjectResult == null)
            {
                log.debug(Thread.currentThread().toString() + "##doCommandUserRegisterInternet(userName=[" + userName
                    + "],userEmail=[" + userEmail + "],userPassword=[" + userPassword + "]): "
                    + EspRegisterResult.NETWORK_UNACCESSIBLE);
                return EspRegisterResult.NETWORK_UNACCESSIBLE;
            }
            int status = jsonObjectResult.getInt(Status);
            if (status == HttpStatus.SC_OK)
            {
                IEspUser user = BEspUser.getBuilder().getInstance();
                user.setUserEmail(userEmail);
            }
            EspRegisterResult result = EspRegisterResult.getEspLoginResult(status);
            log.debug(Thread.currentThread().toString() + "##doCommandUserRegisterInternet(userName=[" + userName
                + "],userEmail=[" + userEmail + "],userPassword=[" + userPassword + "]): " + result);
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        log.debug(Thread.currentThread().toString() + "##doCommandUserRegisterInternet(userName=[" + userName
            + "],userEmail=[" + userEmail + "],userPassword=[" + userPassword + "]): "
            + EspRegisterResult.NETWORK_UNACCESSIBLE);
        return EspRegisterResult.NETWORK_UNACCESSIBLE;
    }
    
}
