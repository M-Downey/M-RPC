package com.downey.example.provider;

import com.downey.mrpc.server.VertxHttpServer;

public class EasyProviderExample {

    public static void main(String[] args) {
        // 启动 web 服务
        VertxHttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
