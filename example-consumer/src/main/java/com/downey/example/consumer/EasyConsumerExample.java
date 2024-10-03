package com.downey.example.consumer;

import com.downey.example.common.model.User;
import com.downey.example.common.service.UserService;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        // todo 需要获取 UserService 的代理对象
        UserService userService = null;
        User user = new User();
        user.setName("downey");
        // 调用远程服务
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
