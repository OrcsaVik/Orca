package com.github.rpc.config.center.listener;

/**
 * 配置变更监听器接口
 * 用于处理配置变更事件
 *
 * @param <T> 配置类型
 */
public interface ConfigChangeListener<T> {

    /**
     * 处理配置变更事件
     *
     * @param key      配置键
     * @param oldValue 旧配置值
     * @param newValue 新配置值
     */
    void onChange(String key, T oldValue, T newValue);
}