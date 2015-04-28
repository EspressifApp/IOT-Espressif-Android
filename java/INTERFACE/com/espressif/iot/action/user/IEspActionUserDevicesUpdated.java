package com.espressif.iot.action.user;

import com.espressif.iot.command.IEspCommandUser;


public interface IEspActionUserDevicesUpdated extends IEspCommandUser
{
    /**
     * when device's updated, the broadcast of DEVICES_ARRIVE (@see EspStrings) will sended.
     * when IUser receive the broadcast, he should doActionUserDevicesUpdated()
     * using Void instead of void just to indicate the method is blocked until it finished
     * 
     * @param isStateMachine whether it is device stateMachine notify the user to update
     * @return null
     */
    Void doActionDevicesUpdated(boolean isStateMachine);
}
