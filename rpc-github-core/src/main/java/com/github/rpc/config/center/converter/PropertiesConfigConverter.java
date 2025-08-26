package com.github.rpc.config.center.converter;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Properties格式配置转换器
 * 将Properties格式字符串转换为Properties对象
 */
@Slf4j
public class PropertiesConfigConverter implements ConfigConverter<Properties> {

    @Override
    public Properties convert(String value) {
        Properties properties = new Properties();
        if (value == null) {
            return properties;
        }
        try {
            // 以UTF-8编码解析，避免平台默认编码差异及BOM问题
            properties.load(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
            return properties;
        } catch (IOException e) {
            log.error("Failed to convert Properties string", e);
            throw new IllegalArgumentException("Failed to convert Properties string", e);
        }
    }

    @Override
    public Class<Properties> getType() {
        return Properties.class;
    }
}