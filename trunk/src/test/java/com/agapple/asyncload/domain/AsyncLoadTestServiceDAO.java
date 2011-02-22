/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.domain;

/**
 * @author jianghang 2011-1-21 下午10:46:19
 */
public class AsyncLoadTestServiceDAO {

    public void doSleep(long sleep) {
        try {
            Thread.sleep(sleep); // 睡一下
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
