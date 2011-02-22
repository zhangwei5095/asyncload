package com.agapple.asyncload;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class BaseAsyncLoadTest extends AbstractJUnit4SpringContextTests {

}
