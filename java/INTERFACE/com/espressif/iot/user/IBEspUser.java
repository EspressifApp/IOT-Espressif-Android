package com.espressif.iot.user;

import com.espressif.iot.object.IEspSingletonBuilder;

public interface IBEspUser extends IEspSingletonBuilder
{
    /**
     * load the IEspUser from local db
     * 
     * @return the user from db
     */
    IEspUser loadUser();
}
