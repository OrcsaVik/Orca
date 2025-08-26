package com.github.rpc.config.center.converter;

/**
 * 长整型配置转换器
 */
public class LongConfigConverter implements ConfigConverter<Long> {

    @Override
    public Long convert(String value) {
        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    @Override
    public Class<Long> getType() {
        return Long.class;
    }
}