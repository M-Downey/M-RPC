package com.downey.example.common.service;

import com.downey.example.common.model.User;

/**
 * 用户服务
 */
public interface UserService {

    User getUser(User user);

    /**
     * 要 mock 的方法（未实现）实际应返回 short 的 mock 值
     * @return
     */
    default String getUserInfo() {
        return "This is User Info default implementation";
    }
}
