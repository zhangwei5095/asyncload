package com.agapple.asyncload.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.util.Assert;

import com.agapple.asyncload.AsyncLoadConfig;
import com.agapple.asyncload.AsyncLoadExecutor;
import com.agapple.asyncload.AsyncLoadMethodMatch;
import com.agapple.asyncload.AsyncLoadProxy;
import com.agapple.asyncload.impl.util.AsyncLoadReflectionHelper;
import com.agapple.asyncload.impl.util.EnhancerHelper;

/**
 * 基于cglib enhance proxy的实现
 * 
 * @author jianghang 2011-1-21 下午10:56:39
 */
public class AsyncLoadEnhanceProxy<T> implements AsyncLoadProxy<T> {

    private T                 service;
    private AsyncLoadConfig   config;
    private AsyncLoadExecutor executor;

    public AsyncLoadEnhanceProxy(){
    }

    public AsyncLoadEnhanceProxy(T service, AsyncLoadExecutor executor){
        this(service, new AsyncLoadConfig(), executor);
    }

    public AsyncLoadEnhanceProxy(T service, AsyncLoadConfig config, AsyncLoadExecutor executor){
        this.service = service;
        this.config = config;
        this.executor = executor;
    }

    public T getProxy() {
        validate();
        return getProxyInternal();
    }

    /**
     * 相应的检查方法
     */
    private void validate() {
        Assert.notNull(service, "service should not be null");
        Assert.notNull(config, "config should not be null");
        Assert.notNull(executor, "executor should not be null");

        if (Modifier.isFinal(service.getClass().getModifiers())) { // 目前暂不支持final类型的处理，以后可以考虑使用jdk proxy
            throw new IllegalArgumentException("Enhance proxy not support final class :" + service.getClass());
        }
    }

    class AsyncLoadCallbackFilter implements CallbackFilter {

        public int accept(Method method) {
            // 预先进行匹配，直接计算好需要处理的method，避免动态匹配浪费性能
            Map<AsyncLoadMethodMatch, Long> matches = config.getMatches();
            Set<AsyncLoadMethodMatch> methodMatchs = matches.keySet();
            if (methodMatchs != null && !methodMatchs.isEmpty()) {
                for (Iterator<AsyncLoadMethodMatch> methodMatch = methodMatchs.iterator(); methodMatch.hasNext();) {
                    if (methodMatch.next().matches(method)) {
                        return 1;
                    }
                }
            }

            return 0;
        }
    }

    class AsyncLoadDirect implements Dispatcher {

        public Object loadObject() throws Exception {
            return service;
        }

    }

    class AsyncLoadInterceptor implements MethodInterceptor {

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Long timeout = getMatchTimeout(method);
            final Object finObj = service;
            final Object[] finArgs = args;
            final Method finMethod = method;

            Class returnClass = method.getReturnType();
            if (Void.TYPE.isAssignableFrom(returnClass)) {// 判断返回值是否为void
                // 不处理void的函数调用
                return finMethod.invoke(finObj, finArgs);
            } else if (Modifier.isFinal(returnClass.getModifiers())) {
                // 处理特殊的final类型，目前暂不支持，后续可采用jdk proxy
                return finMethod.invoke(finObj, finArgs);
            } else if (returnClass.isPrimitive() || returnClass.isArray()) {
                // 不处理特殊类型，因为无法使用cglib代理
                return finMethod.invoke(finObj, finArgs);
            } else {
                Future future = executor.submit(new Callable() {

                    public Object call() throws Exception {
                        try {
                            return finMethod.invoke(finObj, finArgs);// 需要直接委托对应的finObj(service)进行处理
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                // 够造一个返回的AsyncLoadResult
                AsyncLoadResult result = new AsyncLoadResult(returnClass, future, timeout);
                // 继续返回一个代理对象
                return result.getProxy();
            }

        }

        /**
         * 返回对应的匹配的timeout时间，一定能找到对应的匹配点
         * 
         * @param method
         * @return
         */
        private Long getMatchTimeout(Method method) {
            Map<AsyncLoadMethodMatch, Long> matches = config.getMatches();
            Set<Map.Entry<AsyncLoadMethodMatch, Long>> entrys = matches.entrySet();
            if (entrys != null && !entrys.isEmpty()) {
                for (Iterator<Map.Entry<AsyncLoadMethodMatch, Long>> iter = entrys.iterator(); iter.hasNext();) {
                    Map.Entry<AsyncLoadMethodMatch, Long> entry = iter.next();
                    if (entry.getKey().matches(method)) {
                        return entry.getValue();
                    }
                }
            }

            return config.getDefaultTimeout();
        }
    }

    // =========================== help mehotd =================================

    /**
     * 优先从Repository进行获取ProxyClass,创建对应的object
     * 
     * @return
     */
    private T getProxyInternal() {
        Class proxyClass = AsyncLoadProxyRepository.getProxy(service.getClass().getCanonicalName());
        if (proxyClass == null) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(this.service.getClass());
            enhancer.setCallbackTypes(new Class[] { AsyncLoadDirect.class, AsyncLoadInterceptor.class });
            enhancer.setCallbackFilter(new AsyncLoadCallbackFilter());
            proxyClass = enhancer.createClass();
            // 注册proxyClass
            AsyncLoadProxyRepository.registerProxy(service.getClass().getCanonicalName(), proxyClass);
        }

        EnhancerHelper.setThreadCallbacks(proxyClass, new Callback[] { new AsyncLoadDirect(),
                new AsyncLoadInterceptor() });
        try {
            return (T) AsyncLoadReflectionHelper.newInstance(proxyClass);
        } finally {
            // clear thread callbacks to allow them to be gc'd
            EnhancerHelper.setThreadCallbacks(proxyClass, null);
        }
    }

    // ====================== setter / getter ===========================

    public void setService(T service) {
        this.service = service;
    }

    public void setConfig(AsyncLoadConfig config) {
        this.config = config;
    }

    public void setExecutor(AsyncLoadExecutor executor) {
        this.executor = executor;
    }

}
