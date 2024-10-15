package com.downey.mrpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.downey.mrpc.config.RegistryConfig;
import com.downey.mrpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 测试 Etcd 客户端
        Client client = Client.builder().endpoints("http://localhost:2379").build();

        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from("test_key".getBytes());
        ByteSequence value = ByteSequence.from("test_value".getBytes());

        kvClient.put(key, value).get();
        CompletableFuture<GetResponse> getFuture = kvClient.get(key);
        GetResponse getResponse = getFuture.get();
        System.out.println(getResponse);
        kvClient.delete(key).get();
    }

    private Client client;
    private KV kvClient;
    /**
     * 在本服务器注册了服务的节点
     * root + serviceNodeKey
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    /**
     * 服务消费者缓存
     * Map<String(serviceKey), List<ServiceMetaInfo>>
     */
    private final RegistryServiceMultiCache registryServiceMultiCache = new RegistryServiceMultiCache();
    /**
     * 正在监听的 key 集合（服务发现是并发的，所以要用线程安全的）
     * root + serviceNodeKey
     * watch(key) 后如果 key 发生变化要删除 serviceKey 对应的缓存
     */
    private final Set<String> watchKeySet = new ConcurrentHashSet<>();

    /**
     * etcd 缓存根节点
     * @param registryConfig
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    @Override
    public void init(RegistryConfig registryConfig) {
        // 根据 registryConfig 获得 address，创建 client
        client = Client.builder().endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建 Lease 和 KV 客户端
        Lease leaseClient = client.getLeaseClient();

        // 创建一个 30 秒的租约
        long leaseId = leaseClient.grant(30).get().getID();

        // 设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // 已注册服务添加到集合中
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        try {
            kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8)).get();
        } catch (Exception e) {
            throw new RuntimeException(serviceMetaInfo.getServiceNodeKey() + "服务注销失败" + e);
        }
        // 从已注册服务集合中删除节点
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * 根据 serviceKey 进行前缀搜索，获得该服务所有节点
     * @param serviceKey 服务键名
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 读缓存
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceMultiCache.readCache(serviceKey);
        if (cachedServiceMetaInfoList != null) {
            System.out.println("查询服务 " + serviceKey + " 的节点时调用缓存");
            return cachedServiceMetaInfoList;
        }
        // 前缀搜索，结尾一定要加 '/'
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";
        System.out.println("查询服务 " + serviceKey + " 的节点时访问注册中心");
        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();
            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                        // 监听 key
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());
            // 第一次服务发现后写入缓存
            registryServiceMultiCache.writeCache(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    @Override
    public void heartBeat() {
        // 定时任务，10 秒执行一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本服务器节点所有注册服务的 nodeKey
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        // 该节点已过期（需要重启服务器节点才能重新注册）
                        if (CollUtil.isEmpty(keyValues)) {
                            continue;
                        }
                        // 节点未过期，重新注册（相当于续签）put key-value
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败", e);
                    }
                }
            }
        });

        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        // scheduler has been started 的报错
        if (!CronUtil.getScheduler().isStarted()){
            CronUtil.start();
        }
    }

    /**
     * 尝试监听 serviceNodeKey 并定义监听到后做什么
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        boolean add = watchKeySet.add(serviceNodeKey);
        if (add) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), watchResponse -> {
                for (WatchEvent event : watchResponse.getEvents()) {
                    switch (event.getEventType()) {
                        // 删除 key 时触发
                        case DELETE:
                            // 删除服务消费者缓存，要用 serviceKey
                            // serviceNodeKey: "/rpc/serviceKey/localhost:port"
                            String serviceKey = serviceNodeKey.split("/")[2];
                            System.out.println("删除缓存 serviceKey :" + serviceKey);
                            registryServiceMultiCache.clearCache(serviceKey);
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void destroy() {
        System.out.println("当前服务器节点下线");

        // 删除本服务器的所有注册服务节点
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key.getBytes())).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "服务节点下线失败" + e);
            }
        }
        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
