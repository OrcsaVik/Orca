package com.github.rpc.common.registry.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.json.JSONUtil;
import com.github.rpc.common.registry.Registry;
import com.github.rpc.config.RegistryConfig;
import com.github.rpc.model.RegistryServiceCache;
import com.github.rpc.model.RegistryServiceMultiCache;
import com.github.rpc.model.dto.ServiceMetaInfoDTO;
import io.etcd.jetcd.*;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.support.CloseableClient;
import io.etcd.jetcd.watch.WatchEvent;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class EtcdRegistry implements Registry {

    private Client client;

    private KV kvClient;

    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务缓存（只支持单个服务缓存，已废弃，请使用下方的 RegistryServiceMultiCache）
     */
    @Deprecated
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 正在监听的 Watch 实例集合（用于后续取消监听）
     */
    private final Map<String, Watch.Watcher> watchMap = new ConcurrentHashMap<>();
    /**
     * 注册中心服务缓存（支持多个服务键）
     */
    private final RegistryServiceMultiCache registryServiceMultiCache = new RegistryServiceMultiCache();

    /**
     * 续期对应的服务MAP
     */
    private Map<String, Long> serviceLeaseMap = new ConcurrentHashMap<>();
    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();
    // 1. 定义一个容器，保存每个 Lease 的 KeepAlive 连接
    private final Map<String, CloseableClient> keepAliveMap = new ConcurrentHashMap<>();

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
    }



    @Override
    public void register(ServiceMetaInfoDTO serviceMetaInfo) throws Exception {
        // 创建 Lease 和 KV 客户端
        Lease leaseClient = client.getLeaseClient();


        // 创建租约，TTL 设置为 10 秒
        LeaseGrantResponse leaseGrantResponse = client.getLeaseClient().grant(10).get();
        long leaseId = leaseGrantResponse.getID();

        // 设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
        //本地映射 方便keepAlive对应租期
        serviceLeaseMap.put(registerKey, leaseId);
        // 将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();
        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registerKey);

        // 启动 KeepAlive
        // 启动 KeepAlive
        StreamObserver<LeaseKeepAliveResponse> responseObserver = new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse response) {
                System.out.println("Lease " + leaseId + " 续期成功，剩余 TTL: " + response.getTTL());
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Lease " + leaseId + " 续期失败: " + throwable.getMessage());
                // 在这里可以考虑重新注册服务
            }

            @Override
            public void onCompleted() {
                System.out.println("Lease " + leaseId + " KeepAlive 完成");
            }
        };
        //加入对应
        CloseableClient keepAliveClient = client.getLeaseClient().keepAlive(leaseId, responseObserver);

        // ✅ 保存引用，防止被 GC 回收导致连接断开
        keepAliveMap.put(registerKey, keepAliveClient);
        log.info("启动keepAlive服务~ 成功");

    }


    //取消注册 如果一个节点发生下线 rafte协议 存在续期 等到续期自动过期
    @Override
    public void unRegister(ServiceMetaInfoDTO serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);

        try {
            // 1. 先关闭 KeepAlive
            CloseableClient keepAliveClient = keepAliveMap.remove(registerKey);
            if (keepAliveClient != null) {
                keepAliveClient.close();
            }

            // 2. 撤销 Lease（立即删除 key） 删除缓存id
            Long leaseId = serviceLeaseMap.remove(registerKey);
            if (leaseId != null) {
                client.getLeaseClient().revoke(leaseId).get();
            }

            // 3. 删除 key
            kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8)).get();

            // 4. 清理监听（如果是本机注册的服务）
            if (watchMap.containsKey(registerKey)) {
                cleanupWatchResources(registerKey);
            }


            // 4. 从本地缓存移除
            localRegisterNodeKeySet.remove(registerKey);

            log.info("服务已注销: {}", registerKey);

        } catch (Exception e) {
            log.error("注销服务失败: {}", registerKey, e);
            // 不抛异常，避免影响业务
        }
    }

    //服务发现
    @Override
    public List<ServiceMetaInfoDTO> serviceDiscovery(String serviceKey) {
        if (serviceKey == null || serviceKey.trim().isEmpty()) {
            throw new IllegalArgumentException("serviceKey 不能为空");
        }
        serviceKey = serviceKey.trim();
        // 优先从缓存获取服务
        // 原教程代码，不支持多个服务同时缓存
        // List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        // 优化后的代码，支持多个服务同时缓存 根据请求键值
        List<ServiceMetaInfoDTO> cachedServiceMetaInfoList = registryServiceMultiCache.readCache(serviceKey);
        if (cachedServiceMetaInfoList != null) {
            return cachedServiceMetaInfoList;
        }

        // 前缀搜索，结尾一定要加 '/'
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();
            // 解析服务信息 得到注册中心服务实例 存在异常 导致无法监听的可能
//            List<ServiceMetaInfoDTO> serviceMetaInfoList = keyValues.stream()
//                    .map(keyValue -> {
//                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
//                        // 监听 key 的变化 如果节点发生变化 进行删除对应缓存
//                        watch(key);
//                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
//                        return JSONUtil.toBean(value, ServiceMetaInfoDTO.class);
//                    })
//                    .collect(Collectors.toList());
            // 分开化处理逻辑
            List<ServiceMetaInfoDTO> serviceMetaInfoList = new ArrayList<>();
            List<String> nodeKeysToWatch = new ArrayList<>();

            for (KeyValue kv : keyValues) {
                try {
                    String key = kv.getKey().toString(StandardCharsets.UTF_8);
                    String value = kv.getValue().toString(StandardCharsets.UTF_8);
                    ServiceMetaInfoDTO service = JSONUtil.toBean(value, ServiceMetaInfoDTO.class);
                    serviceMetaInfoList.add(service);
                    nodeKeysToWatch.add(key);
                } catch (Exception e) {
                    log.warn("解析服务实例失败，跳过: {}", kv.getKey(), e);
                }
            }

            // 第二步：统一启动监听（即使某个失败，不影响其他）
            for (String nodeKey : nodeKeysToWatch) {
                try {
                    watch(nodeKey);
                } catch (Exception e) {
                    log.error("监听服务节点失败: {}", nodeKey, e);
                    // 可考虑：后续定时重试监听
                }
            }

            registryServiceMultiCache.writeCache(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }
    //这里空实现 使用etcd的keepAlive的类心跳
    @Override
    public void heartBeat() {

    }


    /**
     * 监听（消费端）
     * 根据监听到各个逻辑 对应各自的处理策略
     *
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        // 避免重复监听
        if (watchingKeySet.contains(serviceNodeKey)) {
            return;
        }

        Watch watchClient = client.getWatchClient();
        ByteSequence key = ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8);

        Watch.Watcher watcher = watchClient.watch(key, response -> {
            for (WatchEvent event : response.getEvents()) {
                switch (event.getEventType()) {
                    case DELETE:
                        handleServiceDelete(serviceNodeKey);
                        break;
                    case PUT:
                        handleServiceUpdate(serviceNodeKey, event.getKeyValue());
                        break;
                    default:
                        break;
                }
            }
        }, error -> {
            log.error("监听发生错误，服务节点: {}", serviceNodeKey, error);
            // 错误后也应清理资源
            cleanupWatchResources(serviceNodeKey);
        });

        // 保存监听状态
        try {
            watchingKeySet.add(serviceNodeKey);
            watchMap.put(serviceNodeKey, watcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.debug("已启动监听: {}", serviceNodeKey);
    }

    @Override
    public void destroy() {
        log.warn("ETCD客户端连接下线");

        // 1. 注销所有本地服务
        for (String key : new HashSet<>(localRegisterNodeKeySet)) {
            try {
                ByteSequence keyBs = ByteSequence.from(key, StandardCharsets.UTF_8);
                kvClient.delete(keyBs).get();
            } catch (Exception e) {
                log.error("节点下线失败: {}", key, e);
            }
        }

        // 2. 关闭所有 KeepAlive
        keepAliveMap.values().forEach(CloseableClient::close);
        keepAliveMap.clear();

        // 3. 关闭所有 Watch
        watchMap.values().forEach(Watch.Watcher::close);
        watchMap.clear();
        watchingKeySet.clear();

        // 4. 关闭客户端
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }

        log.info("ETCD 客户端已销毁");
    }

    /**
     * 从注册路径中提取 serviceKey
     * 例如：/rpc/userService/127.0.0.1:8080 -> userService
     */
    private String extractServiceKeyFromNodeKey(String serviceNodeKey) {
        // 去除前缀 /rpc/
        if (serviceNodeKey.startsWith(ETCD_ROOT_PATH)) {
            String subPath = serviceNodeKey.substring(ETCD_ROOT_PATH.length());
            int firstSlash = subPath.indexOf('/');
            if (firstSlash > 0) {
                return subPath.substring(0, firstSlash);
            }
            return subPath;
        }
        return serviceNodeKey;
    }

    /**
     * 监听删除 处理逻辑
     * @param serviceNodeKey
     */
    private void handleServiceDelete(String serviceNodeKey) {
        log.info("服务节点被删除: {}", serviceNodeKey);
        String serviceKey = extractServiceKeyFromNodeKey(serviceNodeKey);

        // 1. 清除本地服务缓存
        registryServiceMultiCache.clearCache(serviceKey);

        //2.同时清理监听数据
        cleanupWatchResources(serviceNodeKey);

        // 3. 如果是本机注册的服务，也从租期 map 移除
        if (localRegisterNodeKeySet.contains(serviceNodeKey)) {
            Long leaseId = serviceLeaseMap.remove(serviceNodeKey);
            if (leaseId != null) {
                log.debug("已移除租期信息: {}, LeaseId={}", serviceNodeKey, leaseId);
            }
        }
    }


    private void handleServiceUpdate(String serviceNodeKey, KeyValue updatedKv) {
        log.debug("服务节点更新: {}", serviceNodeKey);

        try {
            String value = updatedKv.getValue().toString(StandardCharsets.UTF_8);
            //更新服务实例元信息
            ServiceMetaInfoDTO updatedService = JSONUtil.toBean(value, ServiceMetaInfoDTO.class);

            // 例如：更新了权重、版本、标签等
            String serviceKey = extractServiceKeyFromNodeKey(serviceNodeKey);

            // 刷新缓存：先读取当前缓存，再替换对应实例
            List<ServiceMetaInfoDTO> cachedList = registryServiceMultiCache.readCache(serviceKey);
            if (cachedList != null) {
                List<ServiceMetaInfoDTO> updatedList = cachedList.stream()
                        .map(cached -> {
                            if (cached.getServiceNodeKey().equals(updatedService.getServiceNodeKey())) {
                                return updatedService; // 替换为新数据
                            }
                            return cached;
                        })
                        .collect(Collectors.toList());

                registryServiceMultiCache.writeCache(serviceKey, updatedList);
                log.debug("服务缓存已更新: {}", serviceKey);
            }

        } catch (Exception e) {
            log.warn("解析服务更新数据失败: {}", serviceNodeKey, e);
            // 失败后可选择清空缓存，触发下次重新拉取 对应实例缓存
            String serviceKey = extractServiceKeyFromNodeKey(serviceNodeKey);
            registryServiceMultiCache.clearCache(serviceKey);
        }
    }

    /**
     * 清理监听相关资源
     */
    private void cleanupWatchResources(String serviceNodeKey) {
        // 1. 关闭 Watcher
        Watch.Watcher watcher = watchMap.remove(serviceNodeKey);
        if (watcher != null) {
            try {
                watcher.close();
            } catch (Exception e) {
                log.warn("关闭 Watcher 失败: {}", serviceNodeKey, e);
            }
        }

        // 2. 从监听集合移除
        watchingKeySet.remove(serviceNodeKey);

        log.debug("已清理监听资源: {}", serviceNodeKey);
    }
}

