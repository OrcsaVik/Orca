package com.github.rpc.config.center.converter;

/**
 * 配置转换器接口
 * 用于将字符串配置转换为指定类型
 *
 * @param <T> 目标类型
 */
public interface ConfigConverter<T> {

    /**
     * 将字符串配置转换为指定类型
     *
     * @param value 配置值字符串
     * @return 转换后的对象
     */
    T convert(String value);

    /**
     * 获取支持的类型
     *
     * @return 支持的类型Class对象
     */
    Class<T> getType();
}