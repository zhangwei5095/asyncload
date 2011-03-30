package com.agapple.asyncload.impl.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.proxy.Callback;

/**
 * 拷贝了cglib enhancer的部分代码,为了解决能够cache对应的proxyClass问题,后续可根据对应的class动态创建对象
 * 
 * @author jianghang 2011-1-24 下午05:05:26
 */
public class EnhancerHelper {

    private static final String SET_THREAD_CALLBACKS_NAME = "CGLIB$SET_THREAD_CALLBACKS";

    public static void setThreadCallbacks(Class type, Callback[] callbacks) {
        setCallbacksHelper(type, callbacks, SET_THREAD_CALLBACKS_NAME);
    }

    public static void setCallbacksHelper(Class type, Callback[] callbacks, String methodName) {
        try {
            Method setter = getCallbacksSetter(type, methodName);
            setter.invoke(null, new Object[] { callbacks });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type + " is not an enhanced class");
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } catch (InvocationTargetException e) {
            throw new CodeGenerationException(e);
        }
    }

    public static Method getCallbacksSetter(Class type, String methodName) throws NoSuchMethodException {
        return type.getDeclaredMethod(methodName, new Class[] { Callback[].class });
    }
}
