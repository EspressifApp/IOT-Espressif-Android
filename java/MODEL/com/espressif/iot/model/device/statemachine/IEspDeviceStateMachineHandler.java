package com.espressif.iot.model.device.statemachine;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.espressif.iot.device.IEspDeviceConfigure;

public interface IEspDeviceStateMachineHandler
{
    interface ITaskBase
    {
        /**
         * init task by callable or runnable
         */
        abstract void initCallableOrRunnable();
        
        /**
         * init suc task by callable or runnable
         */
        abstract void initSucCallableOrRunnable();
        
        /**
         * init fail task by callable or runnable
         */
        abstract void initFailCallableOrRunnable();
        
        /**
         * check whether the task is urgent
         * @return whether the task is urgent
         */
        abstract boolean isUrgent();
        
        /**
         * get the device's bssid which the task is belong to
         * @return the device's bssid which the task is belong to
         */
        String getBssid();
        
        /**
         * sleep for interval when task executed fail one time (a task will be executed many times until the task
         * timeout), the startTimestamp and endTimestamp is for task executed one time
         * 
         * @param startTimestamp the task startTimestamp one time
         * @param endTimestamp the task endTimestamp one time
         * @throws InterruptedException
         */
        void sleepForInterval(long startTimestamp, long endTimestamp)
            throws InterruptedException;
        
        /**
         * task start timestamp
         */
        void setTaskStart();

        /**
         * check whether the task is started already
         * @return whehter the task is started already
         */
        boolean isStarted();
        
        /**
         * check whether the task is expired
         * @return whether the taks is expired
         */
        boolean isExpired();
        
        /**
         * sleep a little time to check whether the task is cancelled
         * @throws InterruptedException
         */
        void checkIsTaskCancel() throws InterruptedException;
        
        /**
         * cancel the task
         */
        void cancel();
        
        /**
         * submit the task to EspDeviceStateMachineExecutor
         * @param isUrgent whether the task is urgent
         */
        void submit(boolean isUrgent);
        
        /**
         * execute the task and wait the result
         * @param task the Callable to be executed
         * @param taskSuc when the task executed suc, execute taskSuc, or null do nothing
         * @param taskFail when the task executed fail, execute taskFail, or null do nothing
         * @param taskCancel when the task is cancelled, execute taskCancel, or null do nothing
         * @param isUrgent whether the task is urgent
         * @return the task executed result or null(when the task is cancelled or fail sometimes)
         */
        <T> T execute(final Callable<T> task, final Runnable taskSuc, final Runnable taskFail,
            final Runnable taskCancel, boolean isUrgent);
        
        /**
         * do fail task runnable when task fail
         */
        void doFailRunnable();
        
        /**
         * do suc task runnable when task suc
         */
        void doSucRunnable();
        
        /**
         * check whether the task is cancelled
         * @return check whether the task is cancelled
         */
        boolean isCancelled();
        
        /**
         * check whether the task is done
         * @return whether the task is done
         */
        boolean isDone();
        
        /**
         * get the future of the task
         * @return the future of the task
         */
        Future<?> getFuture();
    }
    
    interface ITaskActivateLocal extends ITaskBase
    {
        
    }
    
    interface ITaskActivateInternet extends ITaskBase
    {
        
    }
    
    void addTask(ITaskBase task);
    
    ITaskActivateLocal createTaskActivateLocal(IEspDeviceConfigure deviceConfigure);
    
    ITaskActivateInternet createTaskActivateInternet(IEspDeviceConfigure deviceConfigure);
    
    void cancelAllTasks();
    
    boolean isAllTasksFinished();
    
    boolean isTaskFinished(String deviceBssid);
    
    boolean isTaskSuc(String bssid);
}
