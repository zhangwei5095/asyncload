/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl.template;

/**
 * 对应AyncLoad模板的回调函数
 * 
 * @author jianghang 2011-1-24 下午07:38:10
 */
public interface AsyncLoadCallback<R> {

    public R doAsyncLoad();
}
