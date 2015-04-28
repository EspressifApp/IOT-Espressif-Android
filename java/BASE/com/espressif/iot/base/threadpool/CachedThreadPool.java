package com.espressif.iot.base.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.espressif.iot.object.IEspSingletonObject;

public class CachedThreadPool implements IEspSingletonObject, ICachedThreadPool
{
    private static ExecutorService mExecutorService;
    
    private static final Logger log = Logger.getLogger(CachedThreadPool.class);
    
    // it is used to store working task to support cancel all running task
    private static List<Future<?>> mFutureList;
    
    /*
     * Singleton lazy initialization start
     */
    private CachedThreadPool()
    {
        mExecutorService =
            new ThreadPoolExecutor(THREADS_CORE_COUNT, THREADS_MAX_COUNT, THREADS_KEEP_ALIVE_TIME,
                THREADS_KEEP_ALIVE_UNIT, new LinkedBlockingQueue<Runnable>());
        
        mFutureList = new ArrayList<Future<?>>();
    }
    
    private static class InstanceHolder
    {
        static CachedThreadPool instance = new CachedThreadPool();
    }
    
    public static CachedThreadPool getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    private void __addNewTask(Future<?> future)
    {
        synchronized (mFutureList)
        {
            Future<?> futureInList = null;
            // if mFutureList.size() > THREADS_MAX_COUNT, remove the done task from mFutureList
            if (mFutureList.size() > THREADS_MAX_COUNT)
            {
                for(int i=0;i<mFutureList.size();i++)
                {
                    futureInList = mFutureList.get(i);
                    if(futureInList.isDone())
                    {
                        mFutureList.remove(i--);
                    }
                }
            }
            // add the Future
            mFutureList.add(future);
        }
    }
    
    private void __cancelAllTask()
    {
        synchronized(mFutureList)
        {
            for(Future<?> futureInList : mFutureList)
            {
                futureInList.cancel(true);
            }
            mFutureList.clear();
        }
    }
    
    @Override
    public <T> Future<T> submit(final Callable<T> task)
    {
        Future<T> future = mExecutorService.submit(task);
        __addNewTask(future);
        return future;
    }
    
    @Override
    public Future<?> submit(final Runnable task)
    {
        Future<?> future = mExecutorService.submit(task);
        __addNewTask(future);
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
    
    @Override
    public <T> Future<T> submit(final Callable<T> task, final Runnable taskSuc, final Runnable taskFail,
        final Runnable taskCancel)
    {
        final Future<T> future = mExecutorService.submit(task);
        __addNewTask(future);
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
                catch(CancellationException e)
                {
                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@CANCEL NEW@@@@@@@@@@@@@@@@@@@@@@@@@");
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
        final Future<?> subFuture = mExecutorService.submit(runnable);
        __addNewTask(subFuture);
        return future;
    }
    
    @Override
    public <T> T execute(Callable<T> task, Runnable taskSuc, Runnable taskFail, Runnable taskCancel)
    {
        final Future<T> future = mExecutorService.submit(task);
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

    @Override
    public void cancelAllTask()
    {
        __cancelAllTask();
    }
    
    @Override
    public void shutdown()
    {
        mExecutorService.shutdown();
    }
    
    @Override
    public void shutdownNow()
    {
        mExecutorService.shutdownNow();
    }
    
}
