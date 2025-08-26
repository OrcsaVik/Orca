package com.github.rpc.config.center.converter;

/**
 * 字符串配置转换器
 */
public class StringConfigConverter implements ConfigConverter<String> {

    @Override
    public String convert(String value) {
        return value;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}