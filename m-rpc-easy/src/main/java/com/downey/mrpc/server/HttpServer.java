package com.downey.mrpc.server;

/**
 * http 服务器接口
 */
public interface HttpServer {

    /**
     * 启动服务器
     * @param port
     */
    void doStart(int port);
}
