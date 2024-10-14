package com.downey.example.provider;


import com.downey.example.common.service.UserService;
import com.downey.mrpc.RpcApplication;
import com.downey.mrpc.config.RegistryConfig;
import com.downey.mrpc.config.RpcConfig;
import com.downey.mrpc.model.ServiceMetaInfo;
import com.downey.mrpc.registry.LocalRegistry;
import com.downey.mrpc.registry.Registry;
import com.downey.mrpc.registry.RegistryFactory;
import com.downey.mrpc.server.VertxHttpServer;

/**
 * 服务提供者示例
 */
public class ProviderExample {

    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        // 本地注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(serviceName + "服务注册失败" + e);
        }

        // 启动 web 服务
        VertxHttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(rpcConfig.getServerPort());
    }
}
