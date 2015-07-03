package com.espressif.iot.model.device.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

/**
 * this class is used to help EspDeviceStateMachine execute tasks.
 * 
 * @author afunx
 * 
 */
public class EspDeviceStateMachineExecutor
{
    private static final Logger log = Logger.getLogger(EspDeviceStateMachineExecutor.class);
    
    // this ExecutorService is used by normal tasks
    private static ExecutorService mExecutorServiceNormal;
    
    private static int THREADS_NORMAL_EXECUTOR = 1;
    
    private static List<Future<?>> mFutureNormalList;
    
    // this ExecutorService is used by urgent tasks
    private static ExecutorService mExecutorServiceUrgent;
    
    private static int THREADS_URGENT_EXECUTOR = 2;
    
    private static List<Future<?>> mFutureUrgentList;
    
    /*
     * Singleton lazy initialization start
     */
    private EspDeviceStateMachineExecutor()
    {
        mExecutorServiceNormal = Executors.newFixedThreadPool(THREADS_NORMAL_EXECUTOR);
        mFutureNormalList = new ArrayList<Future<?>>();
        mExecutorServiceUrgent = Executors.newFixedThreadPool(THREADS_URGENT_EXECUTOR);
        mFutureUrgentList = new ArrayList<Future<?>>();
    }
    
    private static class InstanceHolder
    {
        static EspDeviceStateMachineExecutor instance = new EspDeviceStateMachineExecutor();
    }
    
    public static EspDeviceStateMachineExecutor getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    private void __addNewTask(Future<?> future, boolean isUrgent)
    {
        Future<?> futureInList = null;
        if (isUrgent)
        {
            synchronized (mFutureUrgentList)
            {
                if (mFutureUrgentList.size() > THREADS_URGENT_EXECUTOR)
                {
                    for (int i = 0; i < mFutureUrgentList.size(); i++)
                    {
                        futureInList = mFutureUrgentList.get(i);
                        if (futureInList.isDone())
                        {
                            mFutureUrgentList.remove(i--);
                        }
                    }
                }
                mFutureUrgentList.add(future);
            }
        }
        else
        {
            synchronized (mFutureNormalList)
            {
                if (mFutureNormalList.size() > THREADS_NORMAL_EXECUTOR)
                {
                    for (int i = 0; i < mFutureNormalList.size(); i++)
                    {
                        futureInList = mFutureNormalList.get(i);
                        if (futureInList.isDone())
                        {
                            mFutureNormalList.remove(i--);
                        }
                    }
                }
                mFutureNormalList.add(future);
            }
        }
    }
    
    private void __cancelAllTask()
    {
        synchronized (mFutureUrgentList)
        {
            for (Future<?> futureInList : mFutureUrgentList)
            {
                futureInList.cancel(true);
            }
            mFutureUrgentList.clear();
        }
        
        synchronized (mFutureNormalList)
        {
            for (Future<?> futureInList : mFutureNormalList)
            {
                futureInList.cancel(true);
            }
            mFutureNormalList.clear();
        }
    }
    
    <T> Future<T> submit(final Callable<T> task, boolean isUrgent)
    {
        Future<T> future = null;
        if (isUrgent)
        {
            future = mExecutorServiceUrgent.submit(task);
        }
        else
        {
            future = mExecutorServiceNormal.submit(task);
        }
        __addNewTask(future, isUrgent);
        return future;
    }
    
    Future<?> submit(final Runnable task, boolean isUrgent)
    {
        Future<?> future = null;
        if (isUrgent)
        {
            future = mExecutorServiceUrgent.submit(task);
        }
        else
        {
            future = mExecutorServiceNormal.submit(task);
        }
        __addNewTask(future, isUrgent);
        return future;
    }
    
    /**
     * must run the runnable whatever happen, don't let runnable take too long
     * 
     * @param runnable
     */
    private void mustdo(Runnable runnable)
    {
        if (runnable == null)
        {
            return;
        }
        
        long start = System.currentTimeMillis();
        runnable.run();
        long consume = System.currentTimeMillis() - start;
        if (consume > 1000)
        {
            log.error(Thread.currentThread().toString() + "##mustdo() take more than 1000 ms");
        }
    }
    
    <T> Future<T> submit(final Callable<T> task, final Runnable taskSuc, final Runnable taskFail,
        final Runnable taskCancel, boolean isUrgent)
    {
        final Future<T> future = this.submit(task, isUrgent);
        Runnable runnable = new Runnable()
        {
            
            @Override
            public void run()
            {
                T result = null;
                try
                {
                    result = future.get();
                }
                catch (CancellationException e)
                {
                    log.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@CANCEL NEW@@@@@@@@@@@@@@@@@@@@@@@@@");
                    mustdo(taskCancel);
                    return;
                }
                catch (InterruptedException e)
                {
                    log.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@CANCEL@@@@@@@@@@@@@@@@@@@@@@@@@");
                    mustdo(taskCancel);
                    return;
                }
                catch (ExecutionException e)
                {
                    log.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@FAIL1@@@@@@@@@@@@@@@@@@@@@@@@@");
                    e.printStackTrace();
                    mustdo(taskFail);
                    return;
                }
                if (result != null)
                {
                    log.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@SUC@@@@@@@@@@@@@@@@@@@@@@@@@");
                    mustdo(taskSuc);
                }
                else
                {
                    log.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@FAIL2@@@@@@@@@@@@@@@@@@@@@@@@@");
                    mustdo(taskFail);
                }
            }
            
        };
        // subFuture's isUrgent should be the same as its master future
        final Future<?> subFuture = this.submit(runnable, isUrgent);
        __addNewTask(subFuture, isUrgent);
        return future;
    }
    
    <T> T execute(final Callable<T> task, final Runnable taskSuc, final Runnable taskFail, final Runnable taskCancel,
        boolean isUrgent)
    {
        final Future<T> future = this.submit(task, isUrgent);
        T result = null;
        try
        {
            result = future.get();
        }
        catch (InterruptedException e)
        {
            mustdo(taskCancel);
            return null;
        }
        catch (ExecutionException e)
        {
            mustdo(taskFail);
            return null;
        }
        if (result != null)
        {
            mustdo(taskSuc);
        }
        else
        {
            mustdo(taskFail);
        }
        return result;
    }
    
    /**
     * cancel all of the tasks
     */
    public void cancelAllTask()
    {
        __cancelAllTask();
    }
    
}
