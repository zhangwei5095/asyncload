package com.agapple.asyncload;

/**
 * 异步加载proxy工厂
 * 
 * @author jianghang 2011-1-21 下午08:26:32
 */
public interface AsyncLoadProxy<T> {

    public T getProxy();
}
