package com.espressif.iot.base.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface ICachedThreadPool
{
    static final int THREADS_CORE_COUNT = 16;
    static final int THREADS_MAX_COUNT = Integer.MAX_VALUE;
    static final long THREADS_KEEP_ALIVE_TIME = 60L;
    static final TimeUnit THREADS_KEEP_ALIVE_UNIT = TimeUnit.SECONDS;
    /**
     * @see ExecutorService
     * @param task
     * @return
     */
    <T> Future<T> submit(final Callable<T> task);
    
    /**
     * @see ExecutorService
     * @param task
     * @return
     */
    Future<?> submit(final Runnable task);
    
    /**
     * @param task the task to be computed
     * @param taskSuc to do task if task suc
     * @param taskFail to do task if task fail
     * @param taskCancel to do task if task is cancelled
     * @return @see Future
     */
    <T> Future<T> submit(final Callable<T> task, final Runnable taskSuc, final Runnable taskFail,
        final Runnable taskCancel);
    
    /**
     * 
     * @param task the task to be executed
     * @param taskSuc to do task if task suc
     * @param taskFail to do task if task fail
     * @param taskCancel to do task if task is cancelled
     * @return T executed result
     */
    <T> T execute(final Callable<T> task, final Runnable taskSuc, final Runnable taskFail, final Runnable taskCancel);
    
    /**
     * cancel all of the task in the threadpool
     */
    void cancelAllTask();
    
    /**
     * @see ExecutorService
     */
    void shutdown();
    
    /**
     * @see ExecutorService
     */
    void shutdownNow();
}