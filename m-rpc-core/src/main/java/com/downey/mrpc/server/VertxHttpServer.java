package com.downey.mrpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer{
    @Override
    public void doStart(int port) {
        // 创建 vertx 实例
        Vertx vertx = Vertx.vertx();

        // 创建 http 服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        // 监听端口并处理请求
//        server.requestHandler(httpServerRequest -> {
//            // 处理 http 请求
//            System.out.println("Received request: " + httpServerRequest.method() + " " + httpServerRequest.uri());
//            // 发送 http 响应
//            httpServerRequest.response()
//                    .putHeader("content-type", "text/plain")
//                    .end("Hello from Vert.x HTTP server!");
//        });
        server.requestHandler(new HttpServerHandler());

        // 启动 http 服务器并监听指定端口
        server.listen(port, httpServerAsyncResult -> {
            if (httpServerAsyncResult.succeeded()) {
                System.out.println("Server is now listening on port: " + port);
            } else {
                System.err.println("Failed to start server: " + httpServerAsyncResult.cause());;
            }
        });
    }
}
