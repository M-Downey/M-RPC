package com.downey.mrpc.loadbalancer;

import com.downey.mrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机负载均衡器
 *
 */
public class RandomLoadBalancer implements LoadBalancer {

    private final Random random = new Random();

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        // 服务列表是 0 或 1 不用选择
        int size = serviceMetaInfoList.size();
        if (size == 0) {
            return null;
        }
        // 只有 1 个服务，不用随机
        if (size == 1) {
            return serviceMetaInfoList.get(0);
        }
        return serviceMetaInfoList.get(random.nextInt(size));
    }
}