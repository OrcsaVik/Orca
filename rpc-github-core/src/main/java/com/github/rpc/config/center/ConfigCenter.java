package com.github.rpc.config.center;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 配置中心接口
 * 定义配置中心的基本操作，支持不同配置中心的实现
 */
public interface ConfigCenter {

    /**
     * 初始化配置中心
     *
     * @param configProperties 配置属性
     */
    void init(Map<String, String> configProperties);

    /**
     * 获取配置
     *
     * @param key 配置键
     * @return 配置值
     */
    String getConfig(String key);

    /**
     * 获取配置并转换为指定类型
     *
     * @param key   配置键
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的配置对象
     */
    <T> T getConfig(String key, Class<T> clazz);

    /**
     * 添加配置监听器
     * 
     * 原先默认为String
     *
     * @param key      配置键
     * @param listener 配置变更监听器
     */
    <T> void addListener(String key, Consumer<T> listener);

    /**
     * 添加配置监听器并指定类型转换
     *
     * @param key      配置键
     * @param clazz    目标类型
     * @param listener 配置变更监听器
     * @param <T>      泛型类型
     */
    <T> void addListener(String key, Class<T> clazz, Consumer<T> listener);

    /**
     * 移除配置监听器
     *
     * @param key 配置键
     */
    void removeListener(String key);

    /**
     * 关闭配置中心连接
     */
    void shutdown();
}