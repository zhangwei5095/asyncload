package com.agapple.asyncload;

import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.agapple.asyncload.impl.AsyncLoadProxyRepository;

@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class BaseAsyncLoadTest extends AbstractJUnit4SpringContextTests {

    @Before
    public void init() {
        // 清空repository内的cache记录
        try {
            TestUtils.setField(new AsyncLoadProxyRepository(), "reponsitory", new ConcurrentHashMap<String, Class>());
        } catch (Exception e) {
            Assert.fail();
        }
    }

}
