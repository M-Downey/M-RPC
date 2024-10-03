package com.downey.example.common.model;

import java.io.Serializable;

/**
 * 用户实体类，作为测试服务方法的参数和返回值
 * 实现 Serializable 接口，后续网络传输
 */
public class User implements Serializable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
