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
    private long   startTime = 0; // 记录下future开始执行的时间
    private long   endTime   = 0; // 记录下future执行结束时间

    public AsyncLoadFuture(Callable<V> callable){
        super(callable);
        callerThread = Thread.currentThread();
    }

    public AsyncLoadFuture(Runnable runnable, V result){
        super(runnable, result);
        callerThread = Thread.currentThread();
    }

    @Override
    protected void done() {
        endTime = System.currentTimeMillis(); // 记录一下时间点，Future在cancel调用，正常完成，或者运行出异常都会回调该方法
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        runnerThread = Thread.currentThread(); // 记录的下具体pool中的runnerThread，可能是caller自己
        super.run();
    }

    // =============== setter / getter ===============

    public Thread getCallerThread() {
        return callerThread;
    }

    public Thread getRunnerThread() {
        return runnerThread;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

}
