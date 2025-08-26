package com.github.rpc.config.center;

import com.github.rpc.config.center.converter.ConfigConverter;
import com.github.rpc.config.center.converter.ConfigConverterFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 抽象配置中心实现
 * 提供配置中心的通用功能实现
 */
public abstract class AbstractConfigCenter implements ConfigCenter{

    /**
     * 配置监听器映射
     */
    protected final Map<String, Map<Consumer<?>, Consumer<String>>> listenerMap = new ConcurrentHashMap<>();

    /**
     * 配置转换器工厂
     */
    protected final ConfigConverterFactory converterFactory = ConfigConverterFactory.getInstance();

    @Override
    public <T> T getConfig(String key, Class<T> clazz) {
        String config = getConfig(key);
        if (config == null) {
            return null;
        }
        return convertValue(config, clazz);
    }

    @Override
    public <T> void addListener(String key, Class<T> clazz, Consumer<T> listener) {
        Consumer<String> stringConsumer = value -> {
            T convertedValue = convertValue(value, clazz);
            listener.accept(convertedValue);
        };

        // 存储原始监听器和转换后的监听器的映射关系 对应配置类
        listenerMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>())
                .put(listener, stringConsumer);

    }

    @Override
    public void removeListener(String key) {
        listenerMap.remove(key);
        doRemoveListener(key);
    }

    /**
     * 执行实际的监听器移除操作
     *
     * @param key 配置键
     */
    protected abstract void doRemoveListener(String key);

    /**
     * 转换配置值为指定类型
     *
     * @param value 配置值
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的配置对象
     */
    protected <T> T convertValue(String value, Class<T> clazz) {
        ConfigConverter<T> converter = converterFactory.getConverter(clazz);
        if (converter == null) {
            throw new IllegalArgumentException("No converter found for type: " + clazz.getName());
        }
        return converter.convert(value);
    }

    
}