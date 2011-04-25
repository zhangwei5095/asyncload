/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author jianghang 2011-4-25 下午03:14:42
 */
public class LogInteceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            System.out.println("start invoke:" + invocation.getMethod().getName());
            return invocation.proceed();
        } finally {
            System.out.println("end invoke:" + invocation.getMethod().getName());
        }
    }

}
