package com.espressif.iot.device.statemachine;

import java.util.Collection;

import com.espressif.iot.device.IEspDevice;

/*
 * @see IEspDeviceState
 * public enum Enum
 * {
 *     NEW, LOCAL, INTERNET, OFFLINE, CONFIGURING, UPGRADING_LOCAL, UPGRADING_INTERNET, ACTIVATING, DELETED, RENAMED,CLEAR
 * }
 * 
 * the device state graph1:
 * 
 *                                                                    ______________________
 *                                                                   ||                    ||
 *                                                                   ||                    || SUC
 *                                                                   \/   UPGRADE_LOCAL    ||           FAIL
 *                                                                  LOCAL------------>UPGRADE_LOCAL------------>clear LOCAL
 *                                                                   
 *                 
 *                                                  SUC
 *  ____________________                      -------------->OFFLINE
 * ||                  ||                     ||
 * ||                  || CANCEL              ||
 * \/    CONFIGURE     ||        ACTIVATE     ||          SUC            UPGRADE_INTERNET                   FAIL
 * NEW------------>CONFIGURING------------>ACTIVATING------------>INTERNET------------>UPGRADE_INTERNET------------>clear INTERNET
 *(not belong          ||                     ||                     /\                    ||
 *to user)             \/ FAIL                \/ FAIL                ||                    || SUC
 *              CONFIGURING,DELETED     ACTIVATING,DELETED           ||____________________||
 * 
 * if ACTIVATING suc, a device with OFFLINE or INTERNET will transform direction of SUC
 * clear LOCAL and clear INTERNET means clear the state bit of LOCAL or INTERNET.
 * e.g. the device state is LOCAL,INTERNET. although the UI display LOCAL, but it contains INTERNET
 *      if LOCAL is clear, the device state is INTERNET instead of OFFLINE
 * 
 * besides, OFFLINE, LOCAL and INTERNET could add the state of RENAMED or DELETED, as the follow:
 * 
 * the device state graph2:
 * 
 *                          RENAME                         SUC
 * OFFLINE,LOCAL,INTERNET------------>RENAMED (SUPPORT)------------>clear RENAMED
 * 
 *                          DELETE                          SUC
 * OFFLINE,LOCAL,INTERNET------------>DELETED (SUPPORT)------------>CLEAR
 * 
 *                               RENAME
 * "DO NOT SUPPORT" : DELETED------------>RENAMED (FORBIDDEN!!!)
 * 
 * about DELETED:
 * when user delete the device but fail to delete it on Server, the device state will be DELETED.
 * after the device is deleted on Server, the device will be removed from user's device list and local db
 * 
 * step 1. change the device(in user's device list) state to DELETED
 * step 2. update the device in local db(change its state DELETED)
 * step 3. delete the device from Server
 * step 4. delete the device in local db
 * step 5. remove the device in user's device list
 * 
 * 
 * about RENAMED:
 * when user rename the device but fail to rename it on Server, the device state will be added RENAMED.
 * after the device is renamed on Server, the device will be clear RENAMED both in user's device list and local db
 * 
 * step 1. change the device(in user's device list) state to RENAMED
 * step 2. update the device in local db(add its state RENAMED)
 * step 3. rename the device from Server
 * step 4. update the device in local db(clear its state RENAMED)
 * step 5. clear the device state RENAMED in user's device list
 * 
 * @author afunx
 * 
 */
public interface IEspDeviceStateMachine
{
    public enum Direction
    {
        SUC, FAIL, ACTIVATE, CONFIGURE, RENAME, DELETE, UPGRADE_LOCAL, UPGRADE_INTERNET
    };
    
    /**
     * transform device state
     * 
     * @param device the device
     * @param direction @see IEspDeviceStateMachine.Direction
     */
    void transformState(final IEspDevice device, final Direction direction);
    
    /**
     * transform devices state
     * 
     * @param deviceList
     * @param direction
     */
    void transformState(final Collection<IEspDevice> deviceList, final Direction direction);
}
