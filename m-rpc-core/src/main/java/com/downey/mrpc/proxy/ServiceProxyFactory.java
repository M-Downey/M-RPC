package com.downey.mrpc.proxy;

import com.downey.mrpc.RpcApplication;
import com.downey.mrpc.config.RpcConfig;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂（用于创建代理对象）
 *
 */
public class ServiceProxyFactory {

    /**
     * 根据服务类获取代理对象
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        if (RpcApplication.getRpcConfig().isMock()) {
            return getMockProxy(serviceClass);
        }
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());
    }

    /**
     * 获取 mock 代理对象
     * @param serviceClass
     * @return
     * @param <T>
     */
    private static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy()
        );
    }
}
