/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload;

import java.lang.reflect.Method;

/**
 * 异步加载机制 方法匹配对象定义
 * 
 * @author jianghang 2011-1-21 下午09:49:29
 */
public interface AsyncLoadMethodMatch {

    AsyncLoadMethodMatch TRUE = new AsyncLoadTrueMethodMatcher(); // 默认提供返回always true的实现

    boolean matches(Method method);

}

class AsyncLoadTrueMethodMatcher implements AsyncLoadMethodMatch {

    public boolean matches(Method method) {
        return true;
    }

    public String toString() {
        return "AsyncLoadTrueMethodMatcher.TURE";
    }
}
