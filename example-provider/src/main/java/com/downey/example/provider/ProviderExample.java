package com.downey.example.provider;


import com.downey.example.common.service.UserService;
import com.downey.mrpc.RpcApplication;
import com.downey.mrpc.bootstrap.ProviderBootstrap;
import com.downey.mrpc.config.RegistryConfig;
import com.downey.mrpc.config.RpcConfig;
import com.downey.mrpc.model.ServiceMetaInfo;
import com.downey.mrpc.model.ServiceRegisterInfo;
import com.downey.mrpc.registry.LocalRegistry;
import com.downey.mrpc.registry.Registry;
import com.downey.mrpc.registry.RegistryFactory;
import com.downey.mrpc.server.VertxHttpServer;
import com.downey.mrpc.server.tcp.VertxTcpServer;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务提供者示例
 */
public class ProviderExample {

    public static void main(String[] args) {
        // 要注册的服务
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        ServiceRegisterInfo<UserService> serviceRegisterInfo = new ServiceRegisterInfo<>(UserService.class.getName(), UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);

        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }
}
