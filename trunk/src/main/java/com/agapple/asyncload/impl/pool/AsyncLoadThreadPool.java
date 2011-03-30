/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl.pool;

import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.util.ReflectionUtils;

/**
 * 扩展了J.U.C的ThreadPoolExecutor，主要扩展点说明：
 * 
 * <pre>
 * 1. 覆写newTaskFor函数，返回自定义的{@linkplain AsyncLoadFuture}
 * 2. 增强了Pool池中的Worker线程，会自动复制caller Thread的threadLocal信息，几点考虑：
 *   a. Worker线程为pool的内部管理对象，在操作ThreadLocal信息时安全性上不存在问题，持有的引用在task完成后也可以正常释放。ThreadLocal引用在Worker线程中的生命周期<=Caller Thread线程
 *   b. 做为并行异步加载，一个主要的设计思想就是对业务尽可能的透明，尽可能的减少使用陷井，所以这里通过非正常手段实现了ThreadLocal的支持，实属无奈
 * </pre>
 * 
 * @author jianghang 2011-3-28 下午09:56:32
 */
public class AsyncLoadThreadPool extends ThreadPoolExecutor {

    private static final Field threadLocalField            = ReflectionUtils.findField(Thread.class, "threadLocals");
    private static final Field inheritableThreadLocalField = ReflectionUtils.findField(Thread.class,
                                                                                       "inheritableThreadLocals");

    // 继承自ThreadPoolExecutor的构造函数
    public AsyncLoadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue){
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public AsyncLoadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler){
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public AsyncLoadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                               RejectedExecutionHandler handler){
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public AsyncLoadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory){
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    // ====================== 扩展点 ==========================

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new AsyncLoadFuture<T>(callable);// 使用自定义的Future
    }

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new AsyncLoadFuture<T>(runnable, value);// 使用自定义的Future
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        // 在执行之前处理下ThreadPool的属性继承
        if (r instanceof AsyncLoadFuture) {
            try {
                threadLocalField.setAccessible(true);
                inheritableThreadLocalField.setAccessible(true);
                AsyncLoadFuture afuture = (AsyncLoadFuture) r;
                // threadlocal属性复制,注意是引用复制
                ReflectionUtils.setField(threadLocalField, t, ReflectionUtils.getField(threadLocalField,
                                                                                       afuture.getCallerThread()));
                // inheritableThreadLocal属性复制，注意是引用复制
                ReflectionUtils.setField(inheritableThreadLocalField, t,
                                         ReflectionUtils.getField(inheritableThreadLocalField,
                                                                  afuture.getCallerThread()));

            } finally {
                threadLocalField.setAccessible(false);
                inheritableThreadLocalField.setAccessible(false);
            }
        }

        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        // 在执行结束后清理下ThreadPool的属性，GC处理
        if (r instanceof AsyncLoadFuture) {
            try {
                threadLocalField.setAccessible(true);
                inheritableThreadLocalField.setAccessible(true);
                AsyncLoadFuture afuture = (AsyncLoadFuture) r;
                if (afuture.getRunnerThread() != null) {// 判断下是否为null
                    // 处理这样的情况：
                    // 1. 如果caller线程没有使用ThreadLocal对象，而异步加载的runner线程执行中使用了ThreadLocal对象，则需要复制对象到caller线程上
                    // 2. 如果caller线程有使用ThreadLocal对象，这时异步加载的runner线程因为直接使用了ThreadLocal引用，不需要进行重新复制
                    if (ReflectionUtils.getField(threadLocalField, afuture.getCallerThread()) == null) {// 如果caller为null
                        Object obj = ReflectionUtils.getField(threadLocalField, afuture.getRunnerThread());
                        if (obj != null) { // 并且runner的threadLocal有值,则拷贝runner信息到caller上
                            ReflectionUtils.setField(threadLocalField, afuture.getCallerThread(), obj);
                        }
                    }

                    if (ReflectionUtils.getField(inheritableThreadLocalField, afuture.getCallerThread()) == null) {// 如果caller为null
                        Object obj = ReflectionUtils.getField(inheritableThreadLocalField, afuture.getRunnerThread());
                        if (obj != null) { // 并且runner的threadLocal有值,则拷贝runner信息到caller上
                            ReflectionUtils.setField(inheritableThreadLocalField, afuture.getCallerThread(), obj);
                        }
                    }
                    // 清理runner线程的ThreadLocal，为下一个task服务
                    ReflectionUtils.setField(threadLocalField, afuture.getRunnerThread(), null);
                    ReflectionUtils.setField(inheritableThreadLocalField, afuture.getRunnerThread(), null);
                }
            } finally {
                threadLocalField.setAccessible(false);
                inheritableThreadLocalField.setAccessible(false);
            }
        }

        super.afterExecute(r, t);
    }
}
