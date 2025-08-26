package com.github.rpc.config.center.impl;

import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import com.github.rpc.config.center.AbstractConfigCenter;
import com.github.rpc.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 本地文件配置中心实现
 * 支持从本地文件读取配置，并监听文件变更
 */
@Slf4j
public class LocalFileConfigCenter extends AbstractConfigCenter {

    private String configFilePath;
    private Props props;
    private WatchMonitor watchMonitor;

    public LocalFileConfigCenter() {}
    /**
     * 配置缓存
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    @Override
    public void init(Map<String, String> configProperties) {
        // 从配置属性中获取文件路径
        this.configFilePath = configProperties.getOrDefault("filePath", "application.properties");
        
        // 加载配置文件
        loadConfig();
        
        // 监听文件变更
        watchConfigFile();
        
        log.info("本地文件配置中心初始化成功，configFilePath={}", configFilePath);
    }

    /**
     * 加载配置文件
     */
    private void loadConfig() {
        try {
            File file = new File(configFilePath);
            if (!file.exists()) {
                log.warn("配置文件不存在: {}", configFilePath);
                return;
            }
            
            props = new Props(file);
            log.info("加载配置文件成功: {}", configFilePath);
        } catch (Exception e) {
            log.error("加载配置文件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 监听配置文件变更
     */
    private void watchConfigFile() {
        try {
            File file = new File(configFilePath);
            if (!file.exists()) {
                return;
            }
            
            // 创建文件监听器
            watchMonitor = WatchUtil.createAll(file.getParent(), new SimpleWatcher() {
                @Override
                public void onModify(WatchEvent<?> event, Path currentPath) {
                    String fileName = currentPath.getFileName().toString();
                    if (fileName.equals(new File(configFilePath).getName())) {
                        log.info("配置文件已修改，重新加载: {}", configFilePath);
                        
                        // 重新加载配置
                        Props newProps = new Props(configFilePath);
                        
                        // 通知所有监听器
                        notifyListeners(newProps);
                        
                        // 更新配置
                        props = newProps;
                    }
                }
            });
            
            // 启动监听
            watchMonitor.start();
            log.info("启动配置文件监听: {}", configFilePath);
        } catch (Exception e) {
            log.error("启动配置文件监听失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 通知所有监听器
     */
    private void notifyListeners(Props newProps) {
        listenerMap.forEach((key, listeners) -> {
            String oldValue = getConfig(key);
            String newValue = newProps.getStr(key);
            
            // 如果配置值发生变化，通知监听器
            if (!StrUtil.equals(oldValue, newValue)) {
                listeners.forEach((originalListener, stringConsumer) -> {
                    stringConsumer.accept(newValue);
                });
            }
        });
    }

    @Override
    public String getConfig(String key) {
        // 优先从缓存获取
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        }
        
        // 从配置文件获取
        if (props != null) {
            String value = props.getStr(key);
            if (value != null) {
                configCache.put(key, value);
            }
            return value;
        }
        
        return null;
    }

    /**
     * 添加配置监听器
     *
     * @param key      配置键
     * @param listener 配置变更监听器
     */
    @Override
    public <T> void addListener(String key, Consumer<T> listener) {
        // 添加监听器 key-对对应配置的区别键
        // 得到泛型
        Class<T> targetType = getConsumerGenericType(listener);
        //得到抽象类的map属性
        String currentValue = getConfig(key);
        if (currentValue != null) {
            //实际上调用父类的不同参数的相同方法进行处理
            // addListener(currentValue, targetType, listener);
            // //进行消费且加入缓存
            return;
        }
        // 将监听器添加到父类的监听器映射中
        throw new BizException("配置文件不存在该键值对");
    
    }

    /**
     * 获取Consumer的泛型类型
     */
    @SuppressWarnings("unchecked")
    private <T> Class<T> getConsumerGenericType(Consumer<T> consumer) {
        // 获取Consumer接口的泛型参数类型
        return (Class<T>) consumer.getClass()
                .getGenericInterfaces()[0]
                .getClass()
                .getTypeParameters()[0]
                .getBounds()[0];
    }



    @Override
    protected void doRemoveListener(String key) {
        // 本地文件配置中心不需要特殊处理
    }

    @Override
    public void shutdown() {
        // 停止文件监听
        if (watchMonitor != null) {
            watchMonitor.close();
            log.info("停止配置文件监听: {}", configFilePath);
        }
        
        // 清理缓存
        configCache.clear();
    }

   
}