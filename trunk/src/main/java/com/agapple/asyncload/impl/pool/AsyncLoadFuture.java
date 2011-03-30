/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl.pool;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 继承J.U.C下的FutureTask,主要的变化点：
 * 
 * <pre>
 * 1. 持有提交task的thread引用，用于threadLocal处理.新的pool处理线程可以继承/共享callerThread线程的threadLocal信息
 * </pre>
 * 
 * @author jianghang 2011-3-28 下午10:15:04
 */
public class AsyncLoadFuture<V> extends FutureTask<V> {

    private Thread callerThread; // 记录提交runnable的thread，在ThreadPool中用于提取ThreadLocal
    private Thread runnerThread;

    public AsyncLoadFuture(Callable<V> callable){
        super(callable);
        callerThread = Thread.currentThread();
    }

    public AsyncLoadFuture(Runnable runnable, V result){
        super(runnable, result);
        callerThread = Thread.currentThread();
    }

    @Override
    public void run() {
        runnerThread = Thread.currentThread(); // 记录的下具体pool中的runnerThread
        super.run();
    }

    // =============== setter / getter ===============

    public Thread getCallerThread() {
        return callerThread;
    }

    public Thread getRunnerThread() {
        return runnerThread;
    }

}
