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
        if (RpcApplication.getRpcConfig().isMock()) {
            System.out.println("Mock user info is: " + userInfo);
        } else {
            System.out.println("Default user info is: " + userInfo);
        }
        // 第三次调用测试下线服务后，服务发现是否读缓存
        userInfo = userService.getUserInfo();
        System.out.println("第三次调用测试下线服务后，服务发现是否读缓存");
        if (RpcApplication.getRpcConfig().isMock()) {
            System.out.println("Mock user info is: " + userInfo);
        } else {
            System.out.println("Default user info is: " + userInfo);
        }
    }
}
