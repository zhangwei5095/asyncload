/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload;

/**
 * 异步加载proxy工厂
 * 
 * @author jianghang 2011-1-21 下午08:26:32
 */
public interface AsyncLoadProxy<T> {

    public T getProxy();
}
