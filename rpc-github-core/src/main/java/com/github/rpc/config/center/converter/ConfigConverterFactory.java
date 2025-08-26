package com.github.rpc.config.center.converter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置转换器工厂
 * 负责管理和获取不同类型的配置转换器
 */
public class ConfigConverterFactory {

    private static final ConfigConverterFactory INSTANCE = new ConfigConverterFactory();

    /**
     * 转换器映射表
     */
    private final Map<Class<?>, ConfigConverter<?>> converterMap = new ConcurrentHashMap<>();

    private ConfigConverterFactory() {
        // 注册基本类型转换器
        registerConverter(new StringConfigConverter());
        registerConverter(new IntegerConfigConverter());
        registerConverter(new LongConfigConverter());
        registerConverter(new BooleanConfigConverter());
        registerConverter(new DoubleConfigConverter());
        // 仅注册具体类型的转换器（如Properties）
        registerConverter(new PropertiesConfigConverter());
        // 不再注册无具体目标类型的JSON/YAML转换器，避免在ConcurrentHashMap中以null为key导致NPE
    }

    /**
     * 获取工厂实例
     *
     * @return 工厂实例
     */
    public static ConfigConverterFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 注册转换器
     *
     * @param converter 转换器实例
     * @param <T>       转换目标类型
     */
    public <T> void registerConverter(ConfigConverter<T> converter) {
        if (converter == null || converter.getType() == null) {
            throw new IllegalArgumentException("Converter type must not be null: " + (converter == null ? "null" : converter.getClass().getName()));
        }
        converterMap.put(converter.getType(), converter);
    }

    /**
     * 获取指定类型的转换器
     *
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换器实例，如果不存在则返回带目标类型的JsonConfigConverter
     */
    @SuppressWarnings("unchecked")
    public <T> ConfigConverter<T> getConverter(Class<T> clazz) {
        ConfigConverter<?> converter = converterMap.get(clazz);
        if (converter == null) {
            // 对于未注册的复杂类型，默认使用JSON转换器，并传入具体目标类型以避免空指针
            return new JsonConfigConverter<>(clazz);
        }
        return (ConfigConverter<T>) converter;
    }
}