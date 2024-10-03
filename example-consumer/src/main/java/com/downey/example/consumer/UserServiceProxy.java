package com.downey.example.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.downey.example.common.model.User;
import com.downey.example.common.service.UserService;
import com.downey.mrpc.model.RpcRequest;
import com.downey.mrpc.model.RpcResponse;
import com.downey.mrpc.serializer.JdkSerializer;

import java.io.IOException;

public class UserServiceProxy implements UserService {
    @Override
    public User getUser(User user) {
        final JdkSerializer serializer = new JdkSerializer();

        // 构造请求
        RpcRequest rpcRequest = new RpcRequest().builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try {
            // 序列化（Java 对象 => 字节数组）
            byte[] bodyBytes = serializer.serialize(rpcRequest);

            // 发送请求
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化（字节数组 => Java 对象）
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return (User) rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
