package com.github.rpc.config.center.impl;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.github.rpc.config.center.AbstractConfigCenter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Nacos配置中心实现
 */
@Slf4j
public class NacosConfigCenter extends AbstractConfigCenter {

    private ConfigService configService;
    private String group;
    private String dataId;
    private long timeout;
    
    /**
     * 监听器映射，用于移除监听器
     */
    private final Map<String, Listener> nacosListenerMap = new ConcurrentHashMap<>();

    @Override
    public void init(Map<String, String> configProperties) {
        try {
            // 从配置属性中获取Nacos连接信息
            String serverAddr = configProperties.getOrDefault("address", "localhost:8848");
            String namespace = configProperties.getOrDefault("namespace", "");
            this.group = configProperties.getOrDefault("group", "DEFAULT_GROUP");
            this.dataId = configProperties.getOrDefault("dataId", "rpc-config");
            String username = configProperties.get("username");
            String password = configProperties.get("password");
            this.timeout = Long.parseLong(configProperties.getOrDefault("timeout", "3000"));
            
            // 创建Nacos配置服务
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            if (namespace != null && !namespace.isEmpty()) {
                properties.put("namespace", namespace);
            }
            if (username != null && !username.isEmpty()) {
                properties.put("username", username);
            }
            if (password != null && !password.isEmpty()) {
                properties.put("password", password);
            }
            
            configService = NacosFactory.createConfigService(properties);
            log.info("Nacos配置中心初始化成功，serverAddr={}, namespace={}, group={}, dataId={}", 
                    serverAddr, namespace, group, dataId);
        } catch (NacosException e) {
            log.error("Nacos配置中心初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("Nacos配置中心初始化失败", e);
        }
    }

    @Override
    public String getConfig(String key) {
        try {
            // 从Nacos获取配置
            return configService.getConfig(dataId, group, timeout);
        } catch (NacosException e) {
            log.error("从Nacos获取配置失败: {}", e.getMessage(), e);
            return null;
        }
    }

//    @Override
//    public <T> void addListener(String key, Consumer<T> listener) {
//
//    }

    @Override
    public <T> void addListener(String key, Consumer<T> listener) {
        try {
            // 创建Nacos监听器
            Listener nacosListener = new Listener() {
                @Override
                public Executor getExecutor() {
                    return null; // 使用Nacos默认的执行器
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    // 配置变更时调用监听器
                    listener.accept((T) configInfo);
                }
            };
            
            // 添加监听器
            configService.addListener(dataId, group, nacosListener);
            
            // 保存监听器引用，用于后续移除
            nacosListenerMap.put(key, nacosListener);
            
            log.info("添加Nacos配置监听器成功: dataId={}, group={}, key={}", dataId, group, key);
        } catch (NacosException e) {
            log.error("添加Nacos配置监听器失败: {}", e.getMessage(), e);
        }
    }

    @Override
    protected void doRemoveListener(String key) {
        Listener listener = nacosListenerMap.remove(key);
        if (listener != null) {
            configService.removeListener(dataId, group, listener);
            log.info("移除Nacos配置监听器成功: dataId={}, group={}, key={}", dataId, group, key);
        }
    }

    @Override
    public void shutdown() {
        // 清理所有监听器
        nacosListenerMap.forEach((key, listener) -> {
            configService.removeListener(dataId, group, listener);
        });
        nacosListenerMap.clear();
        log.info("Nacos配置中心已关闭");
    }
}