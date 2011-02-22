/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 针对executor的单元测试
 * 
 * @author jianghang 2011-1-24 下午09:17:02
 */
public class AsyncLoadExecutorTest extends BaseAsyncLoadTest {

    private static final String POOL_NAME = "pool";

    @Test
    public void testLifeCycle() {
        AsyncLoadExecutor executor = new AsyncLoadExecutor();
        // 启动
        executor.initital();
        // 关闭
        executor.destory();
    }

    @Test
    public void testPoolConfig() {
        ThreadPoolExecutor executor = null;
        // 创建初始参数
        AsyncLoadExecutor def = new AsyncLoadExecutor();
        def.initital();

        executor = (ThreadPoolExecutor) TestUtils.getField(def, POOL_NAME);
        // 检查pool size
        Assert.assertEquals(executor.getCorePoolSize(), AsyncLoadExecutor.DEFAULT_POOL_SIZE);
        // 检查handler处理模式
        boolean result1 = executor.getRejectedExecutionHandler().getClass().isAssignableFrom(
                                                                                             ThreadPoolExecutor.AbortPolicy.class);

        Assert.assertTrue(result1);
        // 检查block queue
        boolean result2 = executor.getQueue().getClass().isAssignableFrom(ArrayBlockingQueue.class);
        Assert.assertTrue(result2);
    }
}