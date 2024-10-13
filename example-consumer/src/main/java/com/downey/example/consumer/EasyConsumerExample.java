package com.downey.example.consumer;

import com.downey.example.common.model.User;
import com.downey.example.common.service.UserService;
import com.downey.mrpc.RpcApplication;
import com.downey.mrpc.config.RpcConfig;
import com.downey.mrpc.proxy.ServiceProxyFactory;
import com.downey.mrpc.util.ConfigUtils;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        // 需要获取 UserService 的代理对象
//        UserService userService = null;
        // 静态代理
//        UserService userService = new UserServiceProxy();
        // 动态代理
        // 测试 ConfigUtil 加载 rpcConfig
//        RpcConfig rpcConfig = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
//        System.out.println(rpcConfig);
        // 调用者初始化配置
        RpcApplication.init();
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("downey");
        // 调用远程服务
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
        // 调用 mock 方法
        String userInfo = userService.getUserInfo();
        System.out.println("mock user info is: " + userInfo);
    }
}
