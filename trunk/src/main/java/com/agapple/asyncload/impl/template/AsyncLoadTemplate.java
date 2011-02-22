/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl.template;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.springframework.util.Assert;

import com.agapple.asyncload.AsyncLoadConfig;
import com.agapple.asyncload.AsyncLoadExecutor;
import com.agapple.asyncload.impl.AsyncLoadResult;

/**
 * 基于template模式提供的一套AsyncLoad机制，编程式
 * 
 * @author jianghang 2011-1-24 下午07:01:07
 */
public class AsyncLoadTemplate {

    private AsyncLoadExecutor executor;
    private Long              defaultTimeout = AsyncLoadConfig.DEFAULT_TIME_OUT; // 3秒

    /**
     * 异步执行callback模板,设置默认的超时时间，同时返回对应的proxy model,执行AsyncLoad
     * 
     * @param <R>
     * @param callback
     * @return
     */
    public <R> R execute(AsyncLoadCallback<R> callback) {
        return execute(callback, defaultTimeout);
    }

    /**
     * 异步执行callback模板,同时返回对应的proxy model,执行AsyncLoad
     * 
     * @param <R>
     * @param callback
     * @param timeout
     * @return
     */
    public <R> R execute(final AsyncLoadCallback<R> callback, long timeout) {
        Assert.notNull(callback, "");

        Future<R> future = executor.submit(new Callable<R>() {

            public R call() throws Exception {
                return callback.doAsyncLoad();
            }
        });

        Type type = callback.getClass().getGenericInterfaces()[0];
        if (!(type instanceof ParameterizedType)) {
            // 用户不指定AsyncLoadCallBack的泛型信息
            // TODO: 可以考虑，如果不指定返回对象,默认不做lazyLoad
            throw new RuntimeException(
                                       "you should specify AsyncLoadCallBack<R> for R type, ie: AsyncLoadCallBack<OfferModel>");
        }
        Class returnClass = (Class) getGenericClass((ParameterizedType) type, 0);
        // 够造一个返回的AsyncLoadResult
        AsyncLoadResult result = new AsyncLoadResult(returnClass, future, timeout);
        // 继续返回一个代理对象
        return (R) result.getProxy();
    }

    /**
     * 异步执行callback模板,设置默认的超时时间，同时返回对应的proxy model,执行AsyncLoad
     * 
     * @param <R>
     * @param callback
     * @param returnClass 期望的返回对象class
     * @return
     */
    public <R> R execute(AsyncLoadCallback<R> callback, Class<?> returnClass) {
        return execute(callback, returnClass, defaultTimeout);
    }

    /**
     * 异步执行callback模板,同时返回对应的proxy model,执行AsyncLoad
     * 
     * @param <R>
     * @param callback
     * @param returnClass 期望的返回对象class
     * @param timeout
     * @return
     */
    public <R> R execute(final AsyncLoadCallback<R> callback, Class<?> returnClass, long timeout) {
        Assert.notNull(callback, "");

        Future<R> future = executor.submit(new Callable<R>() {

            public R call() throws Exception {
                return callback.doAsyncLoad();
            }
        });
        // 够造一个返回的AsyncLoadResult
        AsyncLoadResult result = new AsyncLoadResult(returnClass, future, timeout);
        // 继续返回一个代理对象
        return (R) result.getProxy();
    }

    /**
     * 取得范性信息
     * 
     * @param cls
     * @param i
     * @return
     */
    private Class<?> getGenericClass(ParameterizedType parameterizedType, int i) {
        Object genericClass = parameterizedType.getActualTypeArguments()[i];
        if (genericClass instanceof ParameterizedType) { // 处理多级泛型
            return (Class<?>) ((ParameterizedType) genericClass).getRawType();
        } else if (genericClass instanceof GenericArrayType) { // 处理数组泛型
            return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
        } else {
            return (Class<?>) genericClass;
        }
    }

    // ===================== setter / getter =============================

    public void setExecutor(AsyncLoadExecutor executor) {
        this.executor = executor;
    }

    public void setDefaultTimeout(Long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

}
