package com.github.rpc.config.center.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * YAML格式配置转换器
 * 使用Jackson库将YAML字符串转换为指定类型对象
 *
 * @param <T> 目标类型
 */
@Slf4j
public class YamlConfigConverter<T> implements ConfigConverter<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
    private Class<T> type;

    public YamlConfigConverter() {
    }

    public YamlConfigConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public T convert(String value) {
        if (value == null) {
            return null;
        }
        if (type == null) {
            throw new IllegalStateException("Target type is not set for YamlConfigConverter");
        }
        try {
            return OBJECT_MAPPER.readValue(value, type);
        } catch (IOException e) {
            log.error("Failed to convert YAML to object", e);
            throw new IllegalArgumentException("Failed to convert YAML to object", e);
        }
    }

    @Override
    public Class<T> getType() {
        return type;
    }
}