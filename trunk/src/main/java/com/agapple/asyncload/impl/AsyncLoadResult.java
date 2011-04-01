package com.agapple.asyncload.impl;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import com.agapple.asyncload.impl.util.AsyncLoadReflectionHelper;

/**
 * 异步加载返回的proxy result
 * 
 * @author jianghang 2011-1-21 下午09:45:14
 */
public class AsyncLoadResult {

    private Class  returnClass;
    private Future future;
    private Long   timeout;

    public AsyncLoadResult(Class returnClass, Future future, Long timeout){
        this.returnClass = returnClass;
        this.future = future;
        this.timeout = timeout;
    }

    public Object getProxy() {
        Class proxyClass = AsyncLoadProxyRepository.getProxy(returnClass.getCanonicalName());
        if (proxyClass == null) { // 进行cache处理
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(returnClass);
            enhancer.setCallbackType(AsyncLoadFutureInterceptor.class);
            proxyClass = enhancer.createClass();

            AsyncLoadProxyRepository.registerProxy(returnClass.getCanonicalName(), proxyClass);
        }

        Enhancer.registerStaticCallbacks(proxyClass, new Callback[] { new AsyncLoadFutureInterceptor() });
        try {
            // 返回对象
            return AsyncLoadReflectionHelper.newInstance(proxyClass);
        } finally {
            // clear thread callbacks to allow them to be gc'd
            Enhancer.registerStaticCallbacks(proxyClass, null);
        }

    }

    class AsyncLoadFutureInterceptor implements LazyLoader {

        public Object loadObject() throws Exception {
            try {
                // 使用cglib lazyLoader，避免每次调用future
                if (timeout <= 0) {// <=0处理，不进行超时控制
                    return future.get();
                } else {
                    return future.get(timeout, TimeUnit.MILLISECONDS);
                }
            } catch (TimeoutException e) {
                future.cancel(true);
                throw e;
            }
        }

    }

}
