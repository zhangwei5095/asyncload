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
import com.agapple.asyncload.impl.AsyncLoadEnhanceProxy;
import com.agapple.asyncload.impl.AsyncLoadProxyRepository;

/**
 * 测试对应returnClass不同类型
 * 
 * @author jianghang 2011-2-9 下午11:06:35
 */
public class AsyncLoadReturnClassTest extends BaseAsyncLoadTest {

    @Resource(name = "asyncLoadTestService")
    private AsyncLoadTestService asyncLoadTestService;
    private AsyncLoadTestService proxy;

    @Before
    public void init() {
        // 清空repository内的cache记录
        try {
            TestUtils.setField(new AsyncLoadProxyRepository(), "reponsitory", new ConcurrentHashMap<String, Class>());
        } catch (Exception e) {
            Assert.fail();
        }

        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxyFactory = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxyFactory.setService(asyncLoadTestService);
        proxyFactory.setConfig(config);
        proxyFactory.setExecutor(executor);
        // 执行测试
        proxy = proxyFactory.getProxy();
    }

    @Test
    public void testClass_ok() {
        long start, end;
        start = System.currentTimeMillis();
        Object model = proxy.getRemoteModel("first", 1000);
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // 不会阻塞
        // 检查对应的返回对象model为AsyncLoadTestModel的子类
        Assert.assertTrue(model.getClass().getSuperclass() == AsyncLoadTestModel.class);
        System.out.println(model.getClass());
    }

    @Test
    public void testClass_primitive() {
        long start, end;
        start = System.currentTimeMillis();
        int model = proxy.countRemoteModel("first", 1000);
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) > 500l); // 阻塞
        System.out.println(model);
    }

    @Test
    public void testClass_void() {
        long start, end;
        start = System.currentTimeMillis();
        proxy.updateRemoteModel("first", 1000l);
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) > 500l); // 阻塞
    }

    @Test
    public void testClass_list() {
        long start, end;
        start = System.currentTimeMillis();
        Object model = proxy.listRemoteModel("first", 1000l);
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // 不会阻塞
        // 检查对应的返回对象model为ArrayList
        Assert.assertTrue(model.getClass().getInterfaces()[0] == List.class);
        System.out.println(model.getClass());
    }

    @Test
    public void testClass_final() {
        long start, end;
        start = System.currentTimeMillis();
        Object model = proxy.getRemoteName("first", 1000l);
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) > 500l); // 阻塞
        // 检查对应的返回对象model为ArrayList
        Assert.assertTrue(model.getClass() == String.class);
        System.out.println(model.getClass());
    }

    @Test
    public void testClass_object() {
        long start, end;
        start = System.currentTimeMillis();
        Object model = proxy.getRemoteObject("first", 1000l);
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // 不阻塞
        System.out.println(model);
        // 检查对应的返回对象model为ArrayList
        Assert.assertTrue(model.getClass().getSuperclass() == Object.class);
        System.out.println(model.getClass());
    }
}
