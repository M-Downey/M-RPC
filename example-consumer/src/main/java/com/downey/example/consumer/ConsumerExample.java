package com.downey.example.consumer;

import com.downey.example.common.model.User;
import com.downey.example.common.service.UserService;
import com.downey.mrpc.bootstrap.ConsumerBootstrap;
import com.downey.mrpc.proxy.ServiceProxyFactory;

/**
 * 服务启动类
 */
public class ConsumerExample {
    public static void main(String[] args) {
        // 服务消费者初始化，只做 RpcApplication 的初始化（ rpcConfig 初始化）
        ConsumerBootstrap.init();

        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("downey");
        User user1 = userService.getUser(user);
        System.out.println(user1.getName());
    }
}
