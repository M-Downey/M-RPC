package com.downey.mrpc.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock 服务代理（JDK动态代理）
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        // 根据调用方法的返回值类型，返回该类型的默认值
        Class<?> methodReturnType = method.getReturnType();
        log.info("return type is: {}", methodReturnType.toString());
        log.info("mock invoke: {}", method.getName());
        return getDefualtObject(methodReturnType);
    }

    private Object getDefualtObject(Class<?> methodReturnType) {
        // 基本数据类型
        if (methodReturnType.isPrimitive()) {
            if (methodReturnType.equals(boolean.class)) {
                return false;
            } else if (methodReturnType.equals(byte.class)){
                return (byte) 0;
            } else if (methodReturnType.equals(short.class)) {
                return (short) 0;
            } else if (methodReturnType.equals(int.class)) {
                return 0;
            } else if (methodReturnType.equals(long.class)) {
                return 0L;
            } else if (methodReturnType.equals(float.class)) {
                return (float) 0;
            } else if (methodReturnType.equals(double.class)) {
                return (double) 0;
            }
        } else if (methodReturnType.equals(String.class)) {
            return "This is String mock result";
        }
        // 对象类型
        return null;
    }
}
