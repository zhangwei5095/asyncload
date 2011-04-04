/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.classinfo;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import net.sf.cglib.reflect.FastMethod;

import com.agapple.asyncload.BaseAsyncLoadNoRunTest;
import com.agapple.asyncload.impl.helper.AsyncLoadReflectionHelper;

/**
 * @author jianghang 2011-4-2 下午01:54:16
 */
public class JavassistClassinfoTest extends BaseAsyncLoadNoRunTest {

    public void test() throws Exception {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(ClassInfoService.class);
        Class<?> proxyClass = proxyFactory.createClass();
        ClassInfoService javassistProxy = (ClassInfoService) proxyClass.newInstance();
        ((ProxyObject) javassistProxy).setHandler(new JavaAssitInterceptor(new ClassInfoService()));

        javassistProxy.test(new Object());

        FastMethod fm = AsyncLoadReflectionHelper.getMethod(javassistProxy.getClass(), "test",
                                                            new Class[] { Object.class });
        System.out.println(fm.getJavaMethod().getAnnotations().length);
    }

    private static class JavaAssitInterceptor implements MethodHandler {

        final Object delegate;

        JavaAssitInterceptor(Object delegate){
            this.delegate = delegate;
        }

        public Object invoke(Object self, Method m, Method proceed, Object[] args) throws Throwable {
            return m.invoke(delegate, args);
        }
    }
}
