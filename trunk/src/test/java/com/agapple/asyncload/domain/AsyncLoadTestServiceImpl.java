/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个测试AsyncLoad的默认实现
 * 
 * @author jianghang 2011-1-21 下午10:46:19
 */
public class AsyncLoadTestServiceImpl implements AsyncLoadTestService {

    private AsyncLoadTestServiceDAO asyncLoadTestServiceDAO;

    public AsyncLoadTestModel getRemoteModel(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
        AsyncLoadTestModel model = new AsyncLoadTestModel();
        model.setName(name);
        model.setId(1);
        model.setDetail(name);
        return model;
    }

    public List<AsyncLoadTestModel> listRemoteModel(String name, long sleep) {
        List<AsyncLoadTestModel> models = new ArrayList<AsyncLoadTestModel>();
        for (int i = 0; i < 2; i++) {
            if (sleep > 0) {
                asyncLoadTestServiceDAO.doSleep(sleep);
            }
            AsyncLoadTestModel model = new AsyncLoadTestModel();
            model.setName(name);
            model.setId(i);
            model.setDetail(name);
            models.add(model);
        }
        return models;
    }

    public int countRemoteModel(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
        return 0;
    }

    public void updateRemoteModel(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
    }

    public void setAsyncLoadTestServiceDAO(AsyncLoadTestServiceDAO asyncLoadTestServiceDAO) {
        this.asyncLoadTestServiceDAO = asyncLoadTestServiceDAO;
    }

    public String getRemoteName(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
        return name;
    }

    public Object getRemoteObject(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
        return name;
    }

}
