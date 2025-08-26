package com.github.rpc.config.center.converter;

/**
 * 布尔类型配置转换器
 */
public class BooleanConfigConverter implements ConfigConverter<Boolean> {

    @Override
    public Boolean convert(String value) {
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }
}