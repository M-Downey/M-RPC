package com.downey.example.provider;

import com.downey.example.common.service.UserService;
import com.downey.mrpc.RpcApplication;
import com.downey.mrpc.registry.LocalRegistry;
import com.downey.mrpc.server.VertxHttpServer;

public class EasyProviderExample {

    public static void main(String[] args) {
        // Rpc 初始化
        RpcApplication.init();
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动 web 服务
        VertxHttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
