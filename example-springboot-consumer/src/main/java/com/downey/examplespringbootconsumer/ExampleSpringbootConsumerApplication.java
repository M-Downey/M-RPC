package com.downey.examplespringbootconsumer;

import com.downey.example.common.model.User;
import com.downey.example.common.service.UserService;
import com.downey.mrpc.springboot.starter.annotation.EnableRpc;
import com.downey.mrpc.springboot.starter.annotation.RpcReference;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

/**
 * 示例 Spring Boot 服务消费者应用
 */
@SpringBootApplication
@EnableRpc(needServer = false)
public class ExampleSpringbootConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringbootConsumerApplication.class, args);
    }

}
