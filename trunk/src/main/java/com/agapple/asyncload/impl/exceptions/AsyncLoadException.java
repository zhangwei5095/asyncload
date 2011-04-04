/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl.exceptions;

/**
 * 并行加载自定义异常
 * 
 * @author jianghang 2011-4-1 下午05:06:37
 */
public class AsyncLoadException extends RuntimeException {

    private static final long serialVersionUID = -2128834565845654572L;

    public AsyncLoadException(){
        super();
    }

    public AsyncLoadException(String message, Throwable cause){
        super(message, cause);
    }

    public AsyncLoadException(String message){
        super(message);
    }

    public AsyncLoadException(Throwable cause){
        super(cause);
    }

}
