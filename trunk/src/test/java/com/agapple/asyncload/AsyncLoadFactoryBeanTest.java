/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.agapple.asyncload.domain.AsyncLoadTestModel;
import com.agapple.asyncload.domain.AsyncLoadTestService;
import com.agapple.asyncload.impl.AsyncLoadProxyRepository;

/**
 * @author jianghang 2011-1-29 下午06:06:29
 */
public class AsyncLoadFactoryBeanTest extends BaseAsyncLoadTest {

    @Resource(name = "asyncLoadTestFactoryBean")
    private AsyncLoadTestService asyncLoadTestFactoryBean;

    @Before
    public void init() {
        // 清空repository内的cache记录
        try {
            TestUtils.setField(new AsyncLoadProxyRepository(), "reponsitory", new ConcurrentHashMap<String, Class>());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testFactoryBean() {
        AsyncLoadTestModel model1 = asyncLoadTestFactoryBean.getRemoteModel("first", 1000);
        AsyncLoadTestModel model2 = asyncLoadTestFactoryBean.getRemoteModel("two", 1000);
        long start = 0, end = 0;
        start = System.currentTimeMillis();
        System.out.println(model1.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) > 500l); // 第一次会阻塞, 响应时间会在1000ms左右

        start = System.currentTimeMillis();
        System.out.println(model2.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // 第二次不会阻塞，第一个已经阻塞了1000ms

        long model3_start = System.currentTimeMillis();
        AsyncLoadTestModel model3 = asyncLoadTestFactoryBean.getRemoteModel("three", 1000);
        List<AsyncLoadTestModel> model4 = asyncLoadTestFactoryBean.listRemoteModel("three", 1000);

        start = System.currentTimeMillis();
        System.out.println(model3.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 1500l); // 不会阻塞，因为list已经阻塞了1000ms

        System.out.println(model4.get(0));
        Assert.assertTrue((System.currentTimeMillis() - model3_start) > 1500l); // 因为被排除list不走asyncLoad，所以时间是近2000ms
    }
}
