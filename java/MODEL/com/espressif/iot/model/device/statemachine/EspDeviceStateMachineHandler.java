package com.espressif.iot.model.device.statemachine;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.db.IOTDeviceDBManager;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceConfigure;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine.Direction;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

/**
 * this class is used to help EspDeviceStateMachine process
 * 
 * @author afunx
 * 
 */
public class EspDeviceStateMachineHandler implements IEspDeviceStateMachineHandler
{
    /*
     * Singleton lazy initialization start
     */
    private EspDeviceStateMachineHandler()
    {
        mCancelledCount = new AtomicInteger(0);
        mTaskToken = new LinkedBlockingDeque<Object>();
        mTaskList = new Vector<ITaskBase>();
        mTaskResult = new ConcurrentHashMap<String, Boolean>();
        mMainLoop = new Runnable()
        {
            @Override
            public void run()
            {
                mainLoop();
            }
        };
        // start the main loop
        new Thread(mMainLoop).start();
    }
    
    private static class InstanceHolder
    {
        static EspDeviceStateMachineHandler instance = new EspDeviceStateMachineHandler();
    }
    
    public static EspDeviceStateMachineHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    private final static Logger log = Logger.getLogger(EspDeviceStateMachineHandler.class);
    
    private final static Object TOKEN = new Object();
    
    /**
     * it is used to monitor whether it is possible that some task has been executed
     */
    private final BlockingQueue<Object> mTaskToken;
    
    private final List<ITaskBase> mTaskList;
    
    private final Runnable mMainLoop;
    
    private final AtomicInteger mCancelledCount;
    
    private final Map<String, Boolean> mTaskResult;
    
    private void mainLoop()
    {
        while (true)
        {
            // wait semaphore for task(s) arrival,
            // sometimes, semaphore come without new task arrival, we call it fake semaphore,
            // when encountering fake semaphore, waitTaskDome() will return false
            while (true)
            {
                log.error("mainLoop():: takeTaskToken for waiting...");
                takeTaskToken();
                log.error("mainLoop():: wait task done...");
                if (waitTaskDone())
                {
                    break;
                }
            }
            
            // process all tasks
            for (int index = 0; index < mTaskList.size(); ++index)
            {
                ITaskBase currentTask = mTaskList.get(index);
                
                // check whether the mTaskList is cleared
                if (currentTask == null)
                {
                    log.info("mainLoop():: currentTask == null, break");
                    break;
                }
                
                // check whether the task is urgent
                boolean isUrgent = currentTask.isUrgent();
                
                // if the task hasn't been set started, submit() and setTaskStart()
                if (!currentTask.isStarted())
                {
                    log.info("mainLoop():: currentTask:" + currentTask.getBssid() + " start");
                    currentTask.submit(isUrgent);
                    currentTask.setTaskStart();
                }
                
                // check whether the current task is done
                boolean isDone = currentTask.isDone();
                boolean isCancelled = currentTask.isCancelled();
                
                // do fail runnable when cancelled
                if (isCancelled)
                {
                    log.info("mainLoop():: currentTask:" + currentTask.getBssid() + " is cancelled");
                    currentTask.doFailRunnable();
                    mTaskList.remove(index--);
                    log.error("mainLoop():: mTaskList.size()=" + mTaskList.size());
                }
                
                if (!isCancelled && isDone)
                {
                    log.info("mainLoop():: currentTask:" + currentTask.getBssid() + " done");
                    Future<?> future = currentTask.getFuture();
                    Object result = null;
                    try
                    {
                        result = future.get();
                        log.error("mainLoop():: get result");
                    }
                    catch (CancellationException e)
                    {
                        log.error("CancellationException:: done task can't be cancelled");
                    }
                    catch (InterruptedException e)
                    {
                        log.error("CancellationException:: done task can't be cancelled");
                    }
                    catch (ExecutionException e)
                    {
                        e.printStackTrace();
                    }
                    log.error("mainLoop():: takeTaskToken for task finished");
                    
                    // do suc runnable
                    if (result != null)
                    {
                        log.error("mainLoop():: task :" + currentTask.getBssid() + " suc");
                        // put task result
                        putTaskResult(currentTask.getBssid(), true);
                        // do suc runnable when the task is suc
                        currentTask.doSucRunnable();
                        // don't forget to remove the done task
                        mTaskList.remove(index--);
                        log.error("mainLoop():: mTaskList.size()=" + mTaskList.size());
                    }
                    else
                    {
                        // check whether the current task is expired
                        boolean isExpired = currentTask.isExpired();
                        if (isExpired)
                        {
                            log.error("mainLoop():: task :" + currentTask.getBssid() + " fail, give up");
                            // put task result
                            putTaskResult(currentTask.getBssid(), false);
                            // do fail runnable only when the task is expired
                            currentTask.doFailRunnable();
                            // don't forget to remove the done task
                            mTaskList.remove(index--);
                            log.error("mainLoop():: mTaskList.size()=" + mTaskList.size());
                        }
                        else
                        {
                            log.error("mainLoop():: task :" + currentTask.getBssid() + " fail, retry");
                            // retry
                            currentTask.submit(isUrgent);
                        }
                    }
                }
            }
        }
    }
    
    // check whether the semaphore is valid
    private boolean waitTaskDone()
    {
        log.error("waitTaskDone():: entrance");
        final long timeout = 1 * 1000;
        long startTimestamp = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTimestamp < timeout)
        {
            for (int index = 0; index < mTaskList.size(); ++index)
            {
                ITaskBase currentTask = mTaskList.get(index);
                
                // check whether the mTaskList is cleared
                if (currentTask == null)
                {
                    log.info("waitTaskDone():: currentTask == null, return");
                    return true;
                }
                
                Future<?> currentFuture = currentTask.getFuture();
                
                if (currentFuture == null)
                {
                    log.info("waitTaskDone():: the task is added just now, return");
                    return true;
                }
                
                if (currentFuture != null && currentFuture.isDone())
                {
                    log.info("waitTaskDone():: one task is done, return");
                    return true;
                }
            }
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        log.info("waitTaskDone():: tiemout, fake semaphore");
        return false;
    }
    
    private void takeTaskToken()
    {
        log.error("takeTaskToken():: mTaskList.size(): " + mTaskList.size() + ", mTaskToken.take() wait...");
        
        // wait the semaphore when it is possible that some task has been executed
        try
        {
            mTaskToken.take();
            log.error("TaskToken:: -1");
        }
        catch (InterruptedException e1)
        {
            e1.printStackTrace();
        }
        log.error("takeTaskToken():: mTaskToken.take() gotten, remained: " + mTaskToken.size());
    }
    
    private void addTaskToken()
    {
        log.error("TaskToken:: +1");
        this.mTaskToken.add(TOKEN);
        log.error("addTaskToken(), remained: " + mTaskToken.size());
    }
    
    // base class of task.
    // usually, all of the tasks will be executed more than once,
    // e.g. activate will
    abstract class TaskBase implements ITaskBase
    {
        // device's bssid
        String mBssid;
        
        final long mTimeout;
        
        final long mInterval;
        
        // when the task is started
        volatile long mStartTimestamp;
        
        volatile boolean mIsExpiredAlready;
        
        volatile Future<?> mFuture;
        
        final Object mFutureLock;
        
        Callable<?> mCallable;
        
        Runnable mRunnable;
        
        Callable<?> mSucCallable;
        
        Runnable mSucRunnable;
        
        Callable<?> mFailCallable;
        
        Runnable mFailRunnable;
        
        TaskBase(String bssid, long timeout, long interval)
        {
            this.mBssid = bssid;
            this.mTimeout = timeout;
            this.mInterval = interval;
            this.mCallable = null;
            this.mRunnable = null;
            this.mSucCallable = null;
            this.mSucRunnable = null;
            this.mFailCallable = null;
            this.mFailRunnable = null;
            this.mIsExpiredAlready = false;
            // although the default value is 0, assign the value here just to make it significant
            this.mStartTimestamp = 0;
            this.mFuture = null;
            this.mFutureLock = new Object();
            initCallableOrRunnable();
            initSucCallableOrRunnable();
            initFailCallableOrRunnable();
        }
        
        // it is determined by subclass, whether it init Callable or Runnable
        @Override
        public abstract void initCallableOrRunnable();
        
        @Override
        public void sleepForInterval(long startTimestamp, long endTimestamp)
            throws InterruptedException
        {
            long cost = endTimestamp - startTimestamp;
            long sleepTime = this.mInterval - cost;
            if (sleepTime > 0)
            {
                Thread.sleep(sleepTime);
            }
        }
        
        @Override
        public String getBssid()
        {
            return this.mBssid;
        }
        
        @Override
        public void setTaskStart()
        {
            this.mStartTimestamp = System.currentTimeMillis();
        }
        
        @Override
        public boolean isStarted()
        {
            return this.mStartTimestamp != 0;
        }
        
        @Override
        public boolean isExpired()
        {
            if (!isStarted())
            {
                throw new IllegalStateException("TaskBase:: it's not allow to call isExpired() before setTaskStart()");
            }
            // it is to guarantee that the task will be executed once more after overtime
            boolean isExpired = this.mIsExpiredAlready;
            this.mIsExpiredAlready = System.currentTimeMillis() - this.mStartTimestamp >= this.mTimeout;
            return isExpired;
        }
        
        @Override
        public void checkIsTaskCancel()
            throws InterruptedException
        {
            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
                log.info(Thread.currentThread().toString() + "##checkIsTaskCancel(): " + mBssid + " is cancelled");
                throw e;
            }
        }
        
        @Override
        public void cancel()
        {
            synchronized (this.mFutureLock)
            {
                if (this.mFuture != null)
                {
                    this.mFuture.cancel(true);
                }
            }
        }
        
        @Override
        public void submit(boolean isUrgent)
        {
            EspDeviceStateMachineExecutor executor = EspDeviceStateMachineExecutor.getInstance();
            synchronized (this.mFutureLock)
            {
                if (this.mCallable != null)
                {
                    this.mFuture = executor.submit(this.mCallable, isUrgent);
                }
                if (this.mRunnable != null)
                {
                    this.mFuture = executor.submit(mRunnable, isUrgent);
                }
            }
        }
        
        @Override
        public <T> T execute(final Callable<T> task, final Runnable taskSuc, final Runnable taskFail,
            final Runnable taskCancel, boolean isUrgent)
        {
            EspDeviceStateMachineExecutor executor = EspDeviceStateMachineExecutor.getInstance();
            return executor.execute(task, taskSuc, taskFail, taskCancel, isUrgent);
        }
        
        @Override
        public void doFailRunnable()
        {
            if (this.mFailRunnable != null)
            {
                this.mFailRunnable.run();
            }
        }
        
        @Override
        public void doSucRunnable()
        {
            if (this.mSucRunnable != null)
            {
                this.mSucRunnable.run();
            }
        }
        
        @Override
        public boolean isCancelled()
        {
            synchronized (this.mFutureLock)
            {
                if (this.mFuture == null)
                {
                    return false;
                }
                else
                {
                    return this.mFuture.isCancelled();
                }
            }
        }
        
        @Override
        public boolean isDone()
        {
            synchronized (this.mFutureLock)
            {
                if (this.mFuture == null)
                {
                    return false;
                }
                else
                {
                    return this.mFuture.isDone();
                }
            }
        }
        
        @Override
        public Future<?> getFuture()
        {
            synchronized (this.mFutureLock)
            {
                return this.mFuture;
            }
        }
    }
    
    void __transformToFail(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.FAIL);
    }
    
    class TaskActivateLocal extends TaskBase implements ITaskActivateLocal
    {
        
        private final IEspDeviceConfigure mDeviceConfigure;
        
        public TaskActivateLocal(IEspDeviceConfigure deviceConfigure)
        {
            super(deviceConfigure.getBssid(), 20 * 1000, 5 * 1000);
            this.mDeviceConfigure = deviceConfigure;
        }
        
        @Override
        public void initCallableOrRunnable()
        {
            this.mCallable = new Callable<Boolean>()
            {
                
                @Override
                public Boolean call()
                    throws Exception
                {
                    
                    long startTimestamp = System.currentTimeMillis();
                    log.debug("ActivateLocalTask:: entrance");
                    
                    boolean isSuc = false;
                    
                    // it's unnecessary to discover when first time
                    // boolean discoverRequired = !mIsFirstExecuted;
                    // mIsFirstExecuted = false;
                    
                    // discover the new device info for Esptouch result can't
                    // distinguish mesh device from normal device
                    boolean discoverRequired = true;
                    
                    // init parameters
                    String deviceBssid = mDeviceConfigure.getBssid();
                    InetAddress inetAddress = mDeviceConfigure.getInetAddress();
                    String randomToken = mDeviceConfigure.getKey();
                    String bssid = mDeviceConfigure.getBssid();
                    String parentDeviceBssid = mDeviceConfigure.getParentDeviceBssid();
                    IOTAddress iotAddress = null;
                    
                    if (discoverRequired)
                    {
                        log.debug("ActivateLocalTask:: discoverDevice: " + deviceBssid + " entrance");
                        // try to discover the device
                        iotAddress = EspBaseApiUtil.discoverDevice(deviceBssid);
                        log.debug("ActivateLocalTask:: discoverDevice finish, iotAddress:" + iotAddress);
                        inetAddress = iotAddress != null ? iotAddress.getInetAddress() : null;
                        parentDeviceBssid = iotAddress != null ? iotAddress.getParentBssid() : null;
                        // update the inetAddress and router
                        if (iotAddress != null)
                        {
                            mDeviceConfigure.setParentDeviceBssid(parentDeviceBssid);
                            mDeviceConfigure.setInetAddress(inetAddress);
                            mDeviceConfigure.setIsMeshDevice(iotAddress.isMeshDevice());
                            // for the moment, we can't see the device's name while activating,
                            // so don't mind the device's name now
//                            String prefix = "ESP_"; // : "ESP_";
//                            String ssid = BSSIDUtil.genDeviceNameByBSSID(prefix, bssid);
//                            mDeviceConfigure.setName(ssid);
                        }
                    }
                    
                    log.debug("ActivateLocalTask:: discoverDevice out");
                    // check whether the parameters is valid
                    if (inetAddress == null)
                    {
                        log.debug("ActivateLocalTask:: sleep for interval start");
                        long endTimestamp = System.currentTimeMillis();
                        sleepForInterval(startTimestamp, endTimestamp);
                        log.debug("ActivateLocalTask:: sleep for interval end");
                        log.warn("ActivateLocalTask:: inetAddress = null, return null");
                        // check whether the task is cancelled
                        checkIsTaskCancel();
                        // don't forget to add task token
                        addTaskToken();
                        return null;
                    }
                    
                    // do configure action
                    if (mDeviceConfigure.getIsMeshDevice())
                    {
                        log.debug("ActivateLocalTask:: do configure action mesh");
                        isSuc =
                            mDeviceConfigure.doActionMeshDeviceConfigureLocal(false,
                                deviceBssid,
                                inetAddress,
                                randomToken);
                    }
                    else
                    {
                        log.debug("ActivateLocalTask:: do configure action");
                        isSuc =
                            mDeviceConfigure.doActionDeviceConfigureLocal(false, inetAddress, randomToken, deviceBssid);
                    }
                    
                    log.debug(Thread.currentThread().toString() + "##ActivateLocalTask:: isSuc: " + isSuc);
                    
                    // return result
                    if (isSuc)
                    {
                        // check whether the task is cancelled
                        checkIsTaskCancel();
                        // don't forget to add task token
                        addTaskToken();
                        return isSuc;
                    }
                    else
                    {
                        log.debug("ActivateLocalTask:: sleep for interval start");
                        long endTimestamp = System.currentTimeMillis();
                        sleepForInterval(startTimestamp, endTimestamp);
                        log.debug("ActivateLocalTask:: sleep for interval end");
                        // check whether the task is cancelled
                        checkIsTaskCancel();
                        // don't forget to add task token
                        addTaskToken();
                        return null;
                    }
                    
                }
                
            };
        }
        
        @Override
        public void initSucCallableOrRunnable()
        {
            this.mSucRunnable = new Runnable()
            {
                
                @Override
                public void run()
                {
                    log.debug("ActivateLocalTask suc:: entrance");
                    EspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
                    stateMachine.transformState(mDeviceConfigure, Direction.ACTIVATE);
                }
                
            };
        }
        
        @Override
        public void initFailCallableOrRunnable()
        {
            this.mFailRunnable = new Runnable()
            {
                
                @Override
                public void run()
                {
                    log.debug("ActivateLocalTask fail:: entrance");
                    __transformToFail(mDeviceConfigure);
                }
                
            };
        }
        
        @Override
        public boolean isUrgent()
        {
            return true;
        }
        
    }
    
    class TaskActivateInternet extends TaskBase implements ITaskActivateInternet
    {
        private final IEspDeviceConfigure mDeviceConfigure;
        
        private volatile IEspDevice mDeviceResult;
        
        TaskActivateInternet(IEspDeviceConfigure deviceConfigure)
        {
            super(deviceConfigure.getBssid(), 60 * 1000, 1 * 1000);
            this.mDeviceConfigure = deviceConfigure;
            this.mDeviceResult = null;
        }
        
        @Override
        public void initCallableOrRunnable()
        {
            this.mCallable = new Callable<IEspDevice>()
            {
                
                @Override
                public IEspDevice call()
                    throws Exception
                {
                    long startTimestamp = System.currentTimeMillis();
                    log.debug("ActivateInternetTask:: entrance");
                    IEspUser user = BEspUser.getBuilder().getInstance();
                    long userId = user.getUserId();
                    String userKey = user.getUserKey();
                    String randomToken = mDeviceConfigure.getKey();
                    mDeviceResult =
                        mDeviceConfigure.doActionDeviceConfigureActivateInternet(userId, userKey, randomToken);
                    if (mDeviceResult == null)
                    {
                        log.debug("ActivateInternetTask:: sleep for interval start");
                        long endTimestamp = System.currentTimeMillis();
                        sleepForInterval(startTimestamp, endTimestamp);
                        log.debug("ActivateInternetTask:: sleep for interval end");
                    }
                    // check whether the task is cancelled
                    checkIsTaskCancel();
                    // don't forget to add task token
                    addTaskToken();
                    return mDeviceResult;
                    
                }
            };
        }
        
        @Override
        public void initSucCallableOrRunnable()
        {
            this.mSucRunnable = new Runnable()
            {
                
                @Override
                public void run()
                {
                    log.debug("ActivateInternetTask suc:: entrance");
                    // delete the exist devices whose bssid is the same
                    IOTDeviceDBManager deviceDBManager = IOTDeviceDBManager.getInstance();
                    deviceDBManager.deleteDevicesByBssid(mDeviceResult.getBssid());
                    
                    EspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
                    stateMachine.transformState(mDeviceResult, Direction.SUC);
                    
                    // save new activated device key to make new device different from others
                    IEspUser user = BEspUser.getBuilder().getInstance();
                    user.saveNewActivatedDevice(mDeviceResult.getKey());
                    
                    // send device registered broadcast
                    Context context = EspApplication.sharedInstance().getApplicationContext();
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                    Intent intent = new Intent(EspStrings.Action.ESPTOUCH_DEVICE_REGISTERED);
                    String deviceBssid = mDeviceResult.getBssid();
                    intent.putExtra(EspStrings.Key.DEVICE_BSSID, deviceBssid);
                    String deviceName = mDeviceResult.getName();
                    intent.putExtra(EspStrings.Key.DEVICE_NAME, deviceName);
                    broadcastManager.sendBroadcast(intent);
                }
                
            };
        }
        
        @Override
        public void initFailCallableOrRunnable()
        {
            // for current moment, we could configure device anytime,
            // so there's no necessary to save device's state when
            // activating fail
            this.mFailRunnable = new Runnable()
            {
                
                @Override
                public void run()
                {
                    log.debug("ActivateInternetTask fail:: entrance");
                    __transformToFail(mDeviceConfigure);
                }
                
            };
        }
        
        @Override
        public boolean isUrgent()
        {
            return false;
        }
        
    }
    
    @Override
    public void addTask(ITaskBase task)
    {
        log.error("addTask: " + task.getBssid());
        mTaskList.add(task);
        addTaskToken();
    }
    
    @Override
    public ITaskActivateLocal createTaskActivateLocal(IEspDeviceConfigure deviceConfigure)
    {
        log.error("createTaskActivateLocal(): " + deviceConfigure);
        ITaskActivateLocal task = new TaskActivateLocal(deviceConfigure);
        return task;
    }
    
    @Override
    public ITaskActivateInternet createTaskActivateInternet(IEspDeviceConfigure deviceConfigure)
    {
        log.error("createTaskActivateInternet(): " + deviceConfigure);
        ITaskActivateInternet task = new TaskActivateInternet(deviceConfigure);
        return task;
    }
    
    @Override
    public void cancelAllTasks()
    {
        log.error("cancelAllTasks()");
        // after canceling all tasks, all task result should be false, so clear mTaskResult
        mTaskResult.clear();
        List<ITaskBase> currentTasks = new ArrayList<ITaskBase>();
        currentTasks.addAll(mTaskList);
        for (ITaskBase task : currentTasks)
        {
            task.cancel();
        }
        mCancelledCount.incrementAndGet();
        addTaskToken();
    }
    
    @Override
    public boolean isAllTasksFinished()
    {
        boolean isAllTasksFinished = mTaskList.isEmpty();
        return isAllTasksFinished;
    }
    
    @Override
    public boolean isTaskFinished(String deviceBssid)
    {
        List<ITaskBase> currentTasks = new ArrayList<ITaskBase>();
        currentTasks.addAll(mTaskList);
        for (ITaskBase task : currentTasks)
        {
            if (task.getBssid().equals(deviceBssid))
            {
                return false;
            }
        }
        return true;
    }
    
    private void putTaskResult(String bssid, boolean isSuc)
    {
        mTaskResult.put(bssid, isSuc);
    }
    
    @Override
    public boolean isTaskSuc(String bssid)
    {
        Boolean result = mTaskResult.get(bssid);
        return result == null ? false : result;
    }
}
