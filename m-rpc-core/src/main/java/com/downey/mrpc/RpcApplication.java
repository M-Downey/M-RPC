package com.downey.mrpc;

import com.downey.mrpc.config.RpcConfig;
import com.downey.mrpc.constant.RpcConstant;
import com.downey.mrpc.util.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Rpc 全局工具类，负责初始化，存储项目全局使用变量。
 * rpcConfig 用双检锁单例模式
 */
@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc init, config = {}", rpcConfig.toString());
    }

    public static void init() {
        // 从配置文件读一个 rpcConfig
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
            init(newRpcConfig);
        } catch (Exception e) {
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    public static RpcConfig getRpcConfig() {
        // 双检索单例模式
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
