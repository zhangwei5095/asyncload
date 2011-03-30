/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl.util;

import java.lang.reflect.Constructor;

import net.sf.cglib.core.ReflectUtils;

/**
 * @author jianghang 2011-3-29 下午09:55:12
 */
public abstract class AsyncLoadReflectionHelper {

    /**
     * 特殊处理，允许通过带参数的constructor创建对象
     * 
     * @param type
     * @return
     */
    public static Object newInstance(Class type) {
        Constructor[] constructors = type.getConstructors();
        if (constructors.length == 0) {
            throw new UnsupportedOperationException("Class[" + type.getName() + "] has no public constructors");
        }
        Constructor _constructor = constructors[0];// 默认取第一个参数
        Object[] _constructorArgs = new Object[0];
        if (_constructor != null) {
            Class[] params = _constructor.getParameterTypes();
            _constructorArgs = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                _constructorArgs[i] = getDefaultValue(params[i]);
            }
        }

        return ReflectUtils.newInstance(_constructor, _constructorArgs);
    }

    /**
     * 根据class类型返回默认值值
     * 
     * @param cl
     * @return
     */
    public static Object getDefaultValue(Class cl) {
        if (!cl.isPrimitive()) {
            return null;
        } else if (boolean.class.equals(cl)) {
            return Boolean.FALSE;
        } else if (byte.class.equals(cl)) {
            return new Byte((byte) 0);
        } else if (short.class.equals(cl)) {
            return new Short((short) 0);
        } else if (char.class.equals(cl)) {
            return new Character((char) 0);
        } else if (int.class.equals(cl)) {
            return Integer.valueOf(0);
        } else if (long.class.equals(cl)) {
            return Long.valueOf(0);
        } else if (float.class.equals(cl)) {
            return Float.valueOf(0);
        } else if (double.class.equals(cl)) {
            return Double.valueOf(0);
        } else {
            throw null;
        }
    }
}
